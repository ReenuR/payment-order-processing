package dto;

import enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponseDTO {
    private UUID orderId;
    private UUID customerId;
    private PaymentStatus paymentStatus;
    private UUID transactionId;
    private LocalDateTime createdAt;
    private String failureReason;
}
