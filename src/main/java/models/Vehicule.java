package models;

import java.time.LocalDate;

public class Vehicule {
    public enum Statut {
        EN_MARCHE, EN_PANNE, VIDANGE;
    }
    public enum Type {
        VOITURE, CAMION, CAMIONETTE
    }

    private String immatriculation;
    private String marque;
    private String modele;
    private Statut statut;
    private Type type;
    private Double longueur;
    private Double hauteur;
    private Double largeur;
    private Double poids;
    private int id_livreur;
    private String livreurNom;
    private LocalDate dateEntretien;
    private LocalDate dateVisiteTechnique;
    private LocalDate dateVidange;
    private LocalDate dateAssurance;
    private LocalDate dateVignette;

    public Vehicule(String immatriculation, String marque, String modele, Statut statut, Type type) {
        this(immatriculation, marque, modele, statut, type, null, null, null, null);
    }

    public Vehicule(String immatriculation, String marque, String modele, Statut statut, Type type, Double longueur, Double hauteur, Double largeur) {
        this(immatriculation, marque, modele, statut, type, longueur, hauteur, largeur, null);
    }

    public Vehicule(String immatriculation, String marque, String modele, Statut statut, Type type, Double longueur, Double hauteur, Double largeur, Double poids) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.statut = statut;
        this.type = type;
        this.longueur = longueur;
        this.hauteur = hauteur;
        this.largeur = largeur;
        this.poids = poids;
    }

    public Vehicule(String immatriculation, String marque, String modele, Statut statut, Type type, Double longueur, Double hauteur, Double largeur, Double poids, LocalDate dateEntretien, LocalDate dateVisiteTechnique, LocalDate dateVidange, LocalDate dateAssurance, LocalDate dateVignette) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.statut = statut;
        this.type = type;
        this.longueur = longueur;
        this.hauteur = hauteur;
        this.largeur = largeur;
        this.poids = poids;
        this.dateEntretien = dateEntretien;
        this.dateVisiteTechnique = dateVisiteTechnique;
        this.dateVidange = dateVidange;
        this.dateAssurance = dateAssurance;
        this.dateVignette = dateVignette;
    }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Double getLongueur() { return longueur; }
    public void setLongueur(Double longueur) { this.longueur = longueur; }

    public Double getHauteur() { return hauteur; }
    public void setHauteur(Double hauteur) { this.hauteur = hauteur; }

    public Double getLargeur() { return largeur; }
    public void setLargeur(Double largeur) { this.largeur = largeur; }

    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }

    public int getIdLivreur() { return id_livreur; }
    public void setIdLivreur(int id_livreur) { this.id_livreur = id_livreur; }

    public String getLivreurNom() { return livreurNom; }
    public void setLivreurNom(String livreurNom) { this.livreurNom = livreurNom; }

    public LocalDate getDateEntretien() { return dateEntretien; }
    public void setDateEntretien(LocalDate dateEntretien) { this.dateEntretien = dateEntretien; }

    public LocalDate getDateVisiteTechnique() { return dateVisiteTechnique; }
    public void setDateVisiteTechnique(LocalDate dateVisiteTechnique) { this.dateVisiteTechnique = dateVisiteTechnique; }

    public LocalDate getDateVidange() { return dateVidange; }
    public void setDateVidange(LocalDate dateVidange) { this.dateVidange = dateVidange; }

    public LocalDate getDateAssurance() { return dateAssurance; }
    public void setDateAssurance(LocalDate dateAssurance) { this.dateAssurance = dateAssurance; }

    public LocalDate getDateVignette() { return dateVignette; }
    public void setDateVignette(LocalDate dateVignette) { this.dateVignette = dateVignette; }

    @Override
    public String toString() {
        return immatriculation + " - " + marque + " " + modele + " (" + statut + ", " + type + ")";
    }
} 