package models;

public enum StatutUtilisateur {
    CONGE("Congé"),
    ACTIF("Actif"),
    INACTIF("Inactif"),
    ABSENT("Absent");

    private final String label;

    StatutUtilisateur(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
} 