package models;

import java.util.Date;

public class Offre {

    private int id_offre;
    private String poste;
    private String type_contrat;
    private String description;
    private double salaire;
    private Date date_publication;
    private String statut;
    private String formulaire_candidature;


    public  Offre() {
    }


    public Offre(int id_offre, String poste, String type_contrat, String description, double salaire, Date date_publication, String statut, String formulaire_candidature) {
        this.id_offre= id_offre;
        this.poste = poste;
        this.type_contrat = type_contrat;
        this.description = description;
        this.salaire = salaire;
        this.date_publication = date_publication;
        this.statut = statut;
        this.formulaire_candidature = formulaire_candidature;
    }


    public int getId_offre() {
        return id_offre;
    }

    public void setId_offre(int id_offre) {
        this.id_offre = id_offre;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getType_contrat() {
        return type_contrat;
    }

    public void setType_contrat(String type_contrat) {
        this.type_contrat = type_contrat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }

    public Date getDate_publication() {
        return date_publication;
    }

    public void setDate_publication(Date date_publication) {
        this.date_publication = date_publication;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getFormulaire_candidature() {
        return formulaire_candidature;
    }

    public void setFormulaire_candidature(String formulaire_candidature) {
        this.formulaire_candidature = formulaire_candidature;
    }

    @Override
    public String toString() {
        return "Offre{" +
                "id_offre=" + id_offre +
                ", poste='" + poste + '\'' +
                ", type_contrat='" + type_contrat + '\'' +
                ", description='" + description + '\'' +
                ", salaire=" + salaire +
                ", date_publication=" + date_publication +
                ", statut='" + statut + '\'' +
                ", formulaire_candidature='" + formulaire_candidature + '\'' +
                '}';
    }
}
