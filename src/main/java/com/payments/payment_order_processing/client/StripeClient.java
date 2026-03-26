package com.payments.payment_order_processing.client;

import com.payments.payment_order_processing.enums.PaymentType;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StripeClient {

    @Value("${stripe.api.key}")
    private String stripeAPIKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeAPIKey;
    }
    public PaymentIntent chargePayment(BigDecimal amount, String currency, PaymentType paymentMethod) throws StripeException {

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // Stripe uses cents
                .setCurrency(currency.toLowerCase())
                .setPaymentMethod(paymentMethod.name())
                .setConfirm(true) // charge immediately
                .build();

        return PaymentIntent.create(params);

    }
}
