package tn.entities;

import java.time.LocalDate;

public class Commande {
    private int id;
    private LocalDate dateCommande;
    private int produitId;
    private int quantite;
    private double total;

    public Commande() {}

    public Commande(int id, LocalDate dateCommande, int produitId, int quantite, double total) {
        this.id = id;
        this.dateCommande = dateCommande;
        this.produitId = produitId;
        this.quantite = quantite;
        this.total = total;
    }

    // Getters et Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", dateCommande=" + dateCommande +
                ", produitId=" + produitId +
                ", quantite=" + quantite +
                ", total=" + total +
                '}';
    }
}
