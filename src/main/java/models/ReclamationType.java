package models;

public enum ReclamationType {
    DELIVERY_DELAY("Delivery Delay"),
    DAMAGED_PACKAGE("Damaged Package"),
    WRONG_ITEM("Wrong Item"),
    MISSING_ITEM("Missing Item"),
    BILLING_ISSUE("Billing Issue"),
    CUSTOMER_SERVICE("Customer Service"),
    APP_PROBLEM("App Problem"),
    APP_BUG("App Bug"),
    OTHER("Other");

    private final String displayName;

    ReclamationType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}