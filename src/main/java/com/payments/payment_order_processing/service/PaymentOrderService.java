package com.payments.payment_order_processing.service;

import com.payments.payment_order_processing.dto.PaymentRequestDTO;
import com.payments.payment_order_processing.dto.PaymentResponseDTO;
import com.payments.payment_order_processing.entity.PaymentOrder;
import com.payments.payment_order_processing.enums.PaymentStatus;
import com.payments.payment_order_processing.repository.PaymentOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class PaymentOrderService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentProcessor paymentProcessor;

    public PaymentOrderService(PaymentOrderRepository paymentOrderRepository, PaymentProcessor paymentProcessor) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentProcessor = paymentProcessor;
    }

    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO) {

        log.info("Processing payment request for orderId: {}", paymentRequestDTO.getOrderId());
        String idempotencyKey = paymentRequestDTO.getIdempotencyKey();
        Optional<PaymentOrder> existingOrder = paymentOrderRepository.findByIdempotencyKey(idempotencyKey);

        if (existingOrder.isPresent()) {
            log.info("Duplicate request detected for idempotencyKey: {}", idempotencyKey);
            return mapToResponseDTO(existingOrder.get());
        }
        PaymentOrder paymentOrder = buildPaymentOrder(paymentRequestDTO);
        PaymentOrder savedOrder = paymentOrderRepository.save(paymentOrder);
        paymentProcessor.initiatePayment(savedOrder);
        return mapToResponseDTO(savedOrder);

    }

    private PaymentOrder buildPaymentOrder(PaymentRequestDTO paymentRequestDTO) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setPaymentAmount(paymentRequestDTO.getPaymentAmount());
        paymentOrder.setPaymentType(paymentRequestDTO.getPaymentType());
        paymentOrder.setPaymentStatus(PaymentStatus.ORDER_CREATED);
        paymentOrder.setCurrency(paymentRequestDTO.getCurrency());
        paymentOrder.setIdempotencyKey(paymentRequestDTO.getIdempotencyKey());
        paymentOrder.setCreatedAt(LocalDateTime.now());
        paymentOrder.setUpdatedAt(LocalDateTime.now());
        paymentOrder.setOrderId(paymentRequestDTO.getOrderId());
        paymentOrder.setRetryCount(0);
        paymentOrder.setCustomerId(paymentRequestDTO.getCustomerId());
        return paymentOrder;
    }

    private PaymentResponseDTO mapToResponseDTO(PaymentOrder order) {
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
