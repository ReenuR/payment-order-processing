package com.payments.payment_order_processing.kafka.event;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentFailedEvent {

    private UUID orderId;
    private UUID customerId;
    private BigDecimal paymentAmount;
    private String currency;
    private String reason;
    private LocalDateTime timestamp;
}
