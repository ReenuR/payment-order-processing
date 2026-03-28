package com.payments.payment_order_processing.dto;

import com.payments.payment_order_processing.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequestDTO {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID customerId;

    @NotNull
    private PaymentType paymentType;

    @NotBlank
    private String currency;

    @Positive
    private BigDecimal paymentAmount;

    @NotBlank
    private String idempotencyKey;

    @NotBlank
    private String paymentMethodId;
}
