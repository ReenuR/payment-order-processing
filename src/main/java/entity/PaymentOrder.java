package entity;

import enums.PaymentStatus;
import enums.PaymentType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;
    private UUID orderId;

    private UUID customerId;
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private String currency;
    private String idempotencyKey;
    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
