package com.payments.payment_order_processing.kafka.producer;

import com.payments.payment_order_processing.kafka.event.PaymentFailedEvent;
import com.payments.payment_order_processing.kafka.event.PaymentSucceededEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object>  kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }
    public void publishPaymentSucceeded(PaymentSucceededEvent event){
        log.info("Publishing PaymentSucceededEvent for orderId: {}", event.getOrderId());
        kafkaTemplate.send("payments.success", event.getOrderId().toString(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event){
        log.info("Publishing PaymentFailedEvent for orderId: {}", event.getOrderId());
        kafkaTemplate.send("payments.failed", event.getOrderId().toString(), event );
    }
}
