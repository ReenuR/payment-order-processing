package com.payments.payment_order_processing.client;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
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
    public PaymentIntent chargePayment(BigDecimal amount, String currency, String paymentMethodId,
                                       String idempotencyKey) throws StripeException {

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // Stripe uses cents
                .setCurrency(currency.toLowerCase())
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                .build();

        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .build();
        return PaymentIntent.create(params, requestOptions);

    }

}
