package models;

import java.time.LocalDate;

public class DemandeConge {
    private Long id;
    private Long userId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String type;
    private String statut;
    private String motif;
    private String nomComplet;
    private String typeUtilisateur;

    public DemandeConge() {}

    public DemandeConge(Long id, Long userId, LocalDate dateDebut, LocalDate dateFin, String type, String statut, String motif) {
        this.id = id;
        this.userId = userId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.type = type;
        this.statut = statut;
        this.motif = motif;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getTypeUtilisateur() { return typeUtilisateur; }
    public void setTypeUtilisateur(String typeUtilisateur) { this.typeUtilisateur = typeUtilisateur; }

    // Utility methods
    public int getNombreJours() {
        if (dateDebut == null || dateFin == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
    }
} 