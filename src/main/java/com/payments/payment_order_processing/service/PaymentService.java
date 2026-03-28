package com.payments.payment_order_processing.service;

import com.payments.payment_order_processing.dto.PaymentRequestDTO;
import com.payments.payment_order_processing.dto.PaymentResponseDTO;
import com.payments.payment_order_processing.entity.Payment;
import com.payments.payment_order_processing.enums.PaymentStatus;
import com.payments.payment_order_processing.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProcessor paymentProcessor;

    public PaymentService(PaymentRepository paymentRepository, PaymentProcessor paymentProcessor) {
        this.paymentRepository = paymentRepository;
        this.paymentProcessor = paymentProcessor;
    }

    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) {

        log.info("Processing payment request for orderId: {}", paymentRequestDTO.getOrderId());
        String idempotencyKey = paymentRequestDTO.getIdempotencyKey();
        Optional<Payment> existingOrder = paymentRepository.findByIdempotencyKey(idempotencyKey);

        if (existingOrder.isPresent()) {
            log.info("Duplicate request detected for idempotencyKey: {}", idempotencyKey);
            return mapToResponseDTO(existingOrder.get());
        }
        Payment payment = buildPaymentOrder(paymentRequestDTO);
        Payment savedOrder = paymentRepository.save(payment);
        paymentProcessor.initiatePayment(savedOrder);
        return mapToResponseDTO(savedOrder);

    }

    private Payment buildPaymentOrder(PaymentRequestDTO paymentRequestDTO) {
        Payment payment = new Payment();
        payment.setPaymentAmount(paymentRequestDTO.getPaymentAmount());
        payment.setPaymentType(paymentRequestDTO.getPaymentType());
        payment.setPaymentStatus(PaymentStatus.ORDER_CREATED);
        payment.setCurrency(paymentRequestDTO.getCurrency());
        payment.setIdempotencyKey(paymentRequestDTO.getIdempotencyKey());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setOrderId(paymentRequestDTO.getOrderId());
        payment.setRetryCount(0);
        payment.setCustomerId(paymentRequestDTO.getCustomerId());
        payment.setPaymentMethodId(paymentRequestDTO.getPaymentMethodId());
        return payment;
    }

    private PaymentResponseDTO mapToResponseDTO(Payment order) {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO();
        responseDTO.setPaymentStatus(order.getPaymentStatus());
        responseDTO.setOrderId(order.getOrderId());
        responseDTO.setCreatedAt(order.getCreatedAt());
        responseDTO.setCustomerId(order.getCustomerId());
        responseDTO.setTransactionId(order.getTransactionId());
        responseDTO.setPaymentId(order.getPaymentId());
        return responseDTO;
    }
}
