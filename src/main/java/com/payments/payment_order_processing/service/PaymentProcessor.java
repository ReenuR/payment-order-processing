    package com.payments.payment_order_processing.service;

    import com.payments.payment_order_processing.client.StripeClient;
    import com.payments.payment_order_processing.entity.Payment;
    import com.payments.payment_order_processing.enums.PaymentStatus;
    import com.payments.payment_order_processing.exception.PaymentDeclinedException;
    import com.payments.payment_order_processing.exception.PaymentProcessingException;
    import com.payments.payment_order_processing.kafka.event.PaymentFailedEvent;
    import com.payments.payment_order_processing.kafka.event.PaymentSucceededEvent;
    import com.payments.payment_order_processing.kafka.producer.PaymentEventProducer;
    import com.payments.payment_order_processing.repository.PaymentRepository;
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

        private final PaymentRepository paymentRepository;
        private final StripeClient stripeClient;
        private final PaymentEventProducer paymentEventProducer;

        public PaymentProcessor(PaymentRepository paymentRepository, StripeClient stripeClient, PaymentEventProducer paymentEventProducer){
            this.paymentRepository = paymentRepository;
            this.stripeClient = stripeClient;
            this.paymentEventProducer = paymentEventProducer;
        }


        @Retryable(
                retryFor = PaymentProcessingException.class,
                maxAttempts = 3,
                backoff = @Backoff(delay = 2000, multiplier = 2)
        )
        public void initiatePayment(Payment payment) {
            log.info("Initiating payment for orderId: {}", payment.getOrderId());
            payment.setPaymentStatus(PaymentStatus.PAYMENT_INITIATED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            PaymentIntent paymentIntent;
            try {
                paymentIntent = stripeClient.chargePayment(payment.getPaymentAmount(), payment.getCurrency(), payment.getPaymentMethodId());
            } catch (CardException e) {
                throw new PaymentDeclinedException("Card declined: " + e.getMessage());
            } catch (StripeException e) {
                log.error("Stripe call failed for orderId: {}. Error: {}",
                        payment.getOrderId(), e.getMessage(), e);
                throw new PaymentProcessingException("Payment processing failed", e);
            }

            if ("succeeded".equals(paymentIntent.getStatus())) {
                log.info("Payment succeeded for orderId: {}", payment.getOrderId());
                payment.setPaymentStatus(PaymentStatus.PAYMENT_SUCCESS);
                payment.setTransactionId(paymentIntent.getId());
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                publishPaymentSucceededEvent(payment, paymentIntent);
            } else {
                log.error("Payment failed for orderId: {}", payment.getOrderId());
                payment.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                publishPaymentFailedEvent(payment);

            }

        }

        @Recover
        public void recover(PaymentProcessingException e, Payment payment) {
            log.error("All retries exhausted for orderId: {}. Reason: {}",
                    payment.getOrderId(), e.getMessage(), e);
            payment.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            publishPaymentFailedEvent(payment);

        }

        private void publishPaymentFailedEvent(Payment payment) {
            PaymentFailedEvent event = new PaymentFailedEvent();
            event.setOrderId(payment.getOrderId());
            event.setCustomerId(payment.getCustomerId());
            event.setPaymentAmount(payment.getPaymentAmount());
            event.setCurrency(payment.getCurrency());
            event.setReason("Payment declined by payment gateway");
            event.setTimestamp(LocalDateTime.now());
            paymentEventProducer.publishPaymentFailed(event);
        }


        private void publishPaymentSucceededEvent(Payment payment, PaymentIntent paymentIntent) {
            PaymentSucceededEvent event = new PaymentSucceededEvent();
            event.setOrderId(payment.getOrderId());
            event.setCustomerId(payment.getCustomerId());
            event.setPaymentAmount(payment.getPaymentAmount());
            event.setCurrency(payment.getCurrency());
            event.setTransactionId(paymentIntent.getId());
            event.setTimestamp(LocalDateTime.now());
            paymentEventProducer.publishPaymentSucceeded(event);
        }

    }
