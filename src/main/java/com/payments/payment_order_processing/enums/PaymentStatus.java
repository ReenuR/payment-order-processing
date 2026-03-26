package com.payments.payment_order_processing.enums;

public enum PaymentStatus {
    ORDER_CREATED("Order has been created"),
    INVENTORY_RESERVED("Inventory reserved"),
    PAYMENT_INITIATED("Payment attempt started"),
    PAYMENT_SUCCESS("Payment completed successfully"),
    PAYMENT_FAILED("Payment failed after retries"),
    INVENTORY_RELEASED("Inventory has been released"),
    CUSTOMER_NOTIFIED("Customer has been notified");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
