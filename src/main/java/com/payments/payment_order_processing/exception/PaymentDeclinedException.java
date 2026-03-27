package com.payments.payment_order_processing.exception;

public class PaymentDeclinedException extends RuntimeException{
    public PaymentDeclinedException(String message) {
        super(message);
    }
}
