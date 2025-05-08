package models;

import java.time.LocalDate;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private int taille;
    private int stock;
    private String categorie;
    private LocalDate dateAjout;
    private String imagePath; // New field for image path

    public Produit() {
        this.dateAjout = LocalDate.now();
    }

    public Produit(int id, String nom, String description, double prix, int taille, int stock, String categorie, LocalDate dateAjout, String imagePath) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.taille = taille;
        this.stock = stock;
        this.categorie = categorie;
        this.dateAjout = dateAjout;
        this.imagePath = imagePath;
    }

    // Getters and setters
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

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getTaille() {
        return taille;
    }

    public void setTaille(int taille) {
        this.taille = taille;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public LocalDate getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDate dateAjout) {
        this.dateAjout = dateAjout;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", taille=" + taille +
                ", stock=" + stock +
                ", categorie='" + categorie + '\'' +
                ", dateAjout=" + dateAjout +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
