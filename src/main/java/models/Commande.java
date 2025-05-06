package models;

import java.time.LocalDate;

public class Commande {
    private int id;
    private LocalDate dateCommande;
    private int produitId;
    private String produitNom;
    private int quantite;
    private double total;
    private int idUser;  // Renommé de clientId à idUser
    private int vehicleId;
    private String adresse;
    private String description;
    private String clientNom;

    public Commande() {}

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

    public String getProduitNom() {
        return produitNom;
    }

    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
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

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    // Pour la compatibilité avec le code existant
    // Ces méthodes peuvent être supprimées si vous mettez à jour tout le code
    public int getClientId() {
        return idUser;
    }

    public void setClientId(int clientId) {
        this.idUser = clientId;
    }

    public int getIdClient() {
        return idUser;
    }

    public void setIdClient(int idClient) {
        this.idUser = idClient;
    }

    public int getIdProduit() {
        return produitId;
    }

    public void setIdProduit(int idProduit) {
        this.produitId = idProduit;
    }
}