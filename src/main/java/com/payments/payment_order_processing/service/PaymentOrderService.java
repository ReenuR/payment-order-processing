package com.payments.payment_order_processing.service;

import com.payments.payment_order_processing.dto.PaymentRequestDTO;
import com.payments.payment_order_processing.dto.PaymentResponseDTO;
import com.payments.payment_order_processing.entity.PaymentOrder;
import com.payments.payment_order_processing.enums.PaymentStatus;
import org.springframework.stereotype.Service;
import com.payments.payment_order_processing.repository.PaymentOrderRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentOrderService {

    private final PaymentOrderRepository paymentOrderRepository;

    public PaymentOrderService(PaymentOrderRepository paymentOrderRepository){
        this.paymentOrderRepository = paymentOrderRepository;
    }

    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequestDTO){

        String idempotencyKey = paymentRequestDTO.getIdempotencyKey();

        Optional<PaymentOrder> existingOrder = paymentOrderRepository.findByIdempotencyKey(idempotencyKey);

        if(existingOrder.isPresent()) {
            return mapToResponseDTO(existingOrder.get());
        }
        PaymentOrder paymentOrder = buildPaymentOrder(paymentRequestDTO);
        PaymentOrder savedOrder = paymentOrderRepository.save(paymentOrder);
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
        paymentOrder.setOrderId(paymentRequestDTO.getOrderId());
        paymentOrder.setRetryCount(0);
        paymentOrder.setCustomerId(paymentRequestDTO.getCustomerId());
        return paymentOrder;
    }

    private PaymentResponseDTO mapToResponseDTO( PaymentOrder savedOrder) {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO();
        responseDTO.setPaymentStatus(savedOrder.getPaymentStatus());
        responseDTO.setOrderId(savedOrder.getOrderId());
        responseDTO.setCreatedAt(savedOrder.getCreatedAt());
        responseDTO.setCustomerId(savedOrder.getCustomerId());
        responseDTO.setTransactionId(savedOrder.getTransactionId());
        responseDTO.setPaymentID(savedOrder.getPaymentId());
        return responseDTO;
    }
}
