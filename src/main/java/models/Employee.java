package models;

import java.util.Date;

public class Employee {
    private int id_employe;
    private int id_offre;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Date date_embauche;
    private String statut_emploi;
    private double salaire_actuel;
    private String cv_path;

    // Constructeurs
    public Employee() {}

    public Employee(int id_employe, int id_offre, String nom, String prenom, String email, String telephone,
                    Date date_embauche, String statut_emploi, double salaire_actuel, String cv_path) {
        this.id_employe = id_employe;
        this.id_offre = id_offre;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.date_embauche = date_embauche;
        this.statut_emploi = statut_emploi;
        this.salaire_actuel = salaire_actuel;
        this.cv_path = cv_path;
    }

    // Getters & Setters
    public int getId_Employe() { return id_employe; }
    public void setId_Employe(int id_employe) { this.id_employe = id_employe; }

    public int getId_offre() { return id_offre; }
    public void setId_offre(int id_offre) { this.id_offre = id_offre; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public Date getDate_embauche() { return date_embauche; }
    public void setDate_embauche(Date date_embauche) { this.date_embauche = date_embauche; }

    public String getStatut_emploi() { return statut_emploi; }
    public void setStatut_emploi(String statut_emploi) { this.statut_emploi = statut_emploi; }

    public double getSalaire_actuel() { return salaire_actuel; }
    public void setSalaire_actuel(double salaire_actuel) { this.salaire_actuel = salaire_actuel; }

    public String getCv_path() { return cv_path; }
    public void setCv_path(String cv_path) { this.cv_path = cv_path; }
}
