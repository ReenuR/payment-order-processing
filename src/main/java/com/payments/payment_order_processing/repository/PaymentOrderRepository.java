package com.payments.payment_order_processing.repository;

import com.payments.payment_order_processing.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {

    Optional<PaymentOrder> findByIdempotencyKey(String idempotencyKey);
}
