package com.payments.payment_order_processing.dto;

import com.payments.payment_order_processing.enums.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequestDTO {
    private UUID orderId;
    private UUID customerId;
    private PaymentType paymentType;
    private String currency;
    private BigDecimal paymentAmount;
    private String idempotencyKey;
}
