package models;

public enum ReclamationType {
    DELIVERY_DELAY("Retard de livraison"),
    DAMAGED_PACKAGE("Colis endommagé"),
    WRONG_ITEM("Mauvais article"),
    MISSING_ITEM("Article manquant"),
    BILLING_ISSUE("Problème de facturation"),
    OTHER("Autre");

    private final String displayName;

    ReclamationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 