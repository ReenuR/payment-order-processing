package com.payments.payment_order_processing.dto;

import com.payments.payment_order_processing.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponseDTO {
    private UUID orderId;
    private UUID customerId;
    private UUID paymentID;
    private PaymentStatus paymentStatus;
    private UUID transactionId;
    private LocalDateTime createdAt;
    private String failureReason;
}
