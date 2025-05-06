package models;

import java.time.LocalDate;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private String categorie;
    private double prix;
    private int stock;
    private LocalDate dateAjout;
    private int taille; // Taille comme entier

    public Produit() {}

    public Produit(int id, String nom, String description, String categorie, double prix, int stock, LocalDate dateAjout, int taille) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.categorie = categorie;
        this.prix = prix;
        this.stock = stock;
        this.dateAjout = dateAjout;
        this.taille = taille;
    }

    // Getters et Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public LocalDate getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDate dateAjout) {
        this.dateAjout = dateAjout;
    }

    public int getTaille() {
        return taille;
    }

    public void setTaille(int taille) {
        this.taille = taille;
    }

    @Override
    public String toString() {
        return nom + " - " + prix + " DT - Taille: " + taille;
    }
}