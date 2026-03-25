package enums;

public enum PaymentType {
    CREDIT_CARD("Credit card"),
    DEBIT_CARD("Debit card"),
    GPAY("G-pay"),
    PAYPAL("Paypal");
    private final String description;

    PaymentType(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

}
