package com.payments.payment_order_processing.kafka.consumer;

import com.payments.payment_order_processing.kafka.event.PaymentFailedEvent;
import com.payments.payment_order_processing.kafka.event.PaymentSucceededEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventConsumer {

    @KafkaListener(topics = "payments.success", groupId = "payment-group")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Sending success email to customerId: {}", event.getCustomerId());

    }

    @KafkaListener(topics = "payments.failed", groupId = "payment-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Sending failure email to customerId: {}", event.getCustomerId());
    }
}
