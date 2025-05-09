package models;

public enum TypeUtilisateur {
    ADMIN("Admin"),
    RESPONSABLE("Responsable"),
    CLIENT("Client"),
    LIVREUR("Livreur"),
    MAGASINIER("Magasinier");

    private final String label;

    TypeUtilisateur(String label) {
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