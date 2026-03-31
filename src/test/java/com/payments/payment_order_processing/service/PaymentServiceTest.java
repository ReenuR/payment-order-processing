package com.payments.payment_order_processing.service;

import com.payments.payment_order_processing.dto.PaymentRequestDTO;
import com.payments.payment_order_processing.dto.PaymentResponseDTO;
import com.payments.payment_order_processing.entity.Payment;
import com.payments.payment_order_processing.enums.PaymentStatus;
import com.payments.payment_order_processing.enums.PaymentType;
import com.payments.payment_order_processing.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProcessor paymentProcessor;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_newRequest_returnsOrderCreatedStatus(){

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId(UUID.randomUUID());
        request.setCustomerId(UUID.randomUUID());
        request.setPaymentAmount(BigDecimal.valueOf(99.99));
        request.setCurrency("usd");
        request.setPaymentType(PaymentType.CREDIT_CARD);
        request.setIdempotencyKey("test-key-001");
        request.setPaymentMethodId("pm_card_visa");

        when(paymentRepository.findByIdempotencyKey("test-key-001"))
                .thenReturn(Optional.empty());

        Payment savedPayment = new Payment();
        savedPayment.setPaymentStatus(PaymentStatus.ORDER_CREATED);
        savedPayment.setOrderId(request.getOrderId());
        savedPayment.setCustomerId(request.getCustomerId());
        savedPayment.setCreatedAt(LocalDateTime.now());
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        PaymentResponseDTO response = paymentService.processPayment(request);
        assertNotNull(response);
        assertEquals(PaymentStatus.ORDER_CREATED, response.getPaymentStatus());
        verify(paymentProcessor, times(1)).initiatePayment(any(Payment.class));
    }
}