package com.payments.payment_order_processing.service;

import com.payments.payment_order_processing.client.StripeClient;
import com.payments.payment_order_processing.entity.PaymentOrder;
import com.payments.payment_order_processing.enums.PaymentStatus;
import com.payments.payment_order_processing.exception.PaymentDeclinedException;
import com.payments.payment_order_processing.exception.PaymentProcessingException;
import com.payments.payment_order_processing.repository.PaymentOrderRepository;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentProcessor {

    private final PaymentOrderRepository paymentOrderRepository;
    private final StripeClient stripeClient;

    public PaymentProcessor(PaymentOrderRepository paymentOrderRepository, StripeClient stripeClient){
        this.paymentOrderRepository = paymentOrderRepository;
        this.stripeClient = stripeClient;
    }


    @Retryable(
            retryFor = PaymentProcessingException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void initiatePayment(PaymentOrder paymentOrder) {
        log.info("Initiating payment for orderId: {}", paymentOrder.getOrderId());
        paymentOrder.setPaymentStatus(PaymentStatus.PAYMENT_INITIATED);
        paymentOrder.setUpdatedAt(LocalDateTime.now());
        paymentOrderRepository.save(paymentOrder);
        PaymentIntent paymentIntent;
        try {
            paymentIntent = stripeClient.chargePayment(paymentOrder.getPaymentAmount(), paymentOrder.getCurrency(), paymentOrder.getPaymentType());
        } catch (CardException e) {
            // permanent failure - card declined
            throw new PaymentDeclinedException("Card declined: " + e.getMessage());
        } catch (StripeException e) {
            // transient failure - retryable
            throw new PaymentProcessingException("Payment processing failed", e);
        }

        if ("succeeded".equals(paymentIntent.getStatus())) {
            log.info("Payment succeeded for orderId: {}", paymentOrder.getOrderId());
            paymentOrder.setPaymentStatus(PaymentStatus.PAYMENT_SUCCESS);
            paymentOrder.setTransactionId(paymentIntent.getId());
            paymentOrder.setUpdatedAt(LocalDateTime.now());
            paymentOrderRepository.save(paymentOrder);
        } else {
            log.error("Payment failed for orderId: {}", paymentOrder.getOrderId());
            paymentOrder.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
            paymentOrder.setUpdatedAt(LocalDateTime.now());
            paymentOrderRepository.save(paymentOrder);
        }

    }

    @Recover
    public void recover(PaymentProcessingException e, PaymentOrder paymentOrder) {
        log.error("All retries exhausted for orderId: {}. Reason: {}",
                paymentOrder.getOrderId(), e.getMessage());
        paymentOrder.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
        paymentOrder.setUpdatedAt(LocalDateTime.now());
        paymentOrderRepository.save(paymentOrder);

    }

}
