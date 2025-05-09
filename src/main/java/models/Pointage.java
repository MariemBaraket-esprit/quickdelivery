package models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Pointage {
    private Long id;
    private Long userId;
    private LocalDate datePointage;
    private LocalTime heureEntree;
    private LocalTime heureSortie;
    private LocalTime dureePause;
    private String statut;
    private String commentaire;
    private String nomComplet;  // Pour l'affichage
    private String typeUtilisateur;  // Pour l'affichage

    // Constructeurs
    public Pointage() {}

    public Pointage(Long id, Long userId, LocalDate datePointage, LocalTime heureEntree, 
                   LocalTime heureSortie, LocalTime dureePause, String statut) {
        this.id = id;
        this.userId = userId;
        this.datePointage = datePointage;
        this.heureEntree = heureEntree;
        this.heureSortie = heureSortie;
        this.dureePause = dureePause;
        this.statut = statut;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDatePointage() { return datePointage; }
    public void setDatePointage(LocalDate datePointage) { this.datePointage = datePointage; }

    public LocalTime getHeureEntree() { return heureEntree; }
    public void setHeureEntree(LocalTime heureEntree) { this.heureEntree = heureEntree; }

    public LocalTime getHeureSortie() { return heureSortie; }
    public void setHeureSortie(LocalTime heureSortie) { this.heureSortie = heureSortie; }

    public LocalTime getDureePause() { return dureePause; }
    public void setDureePause(LocalTime dureePause) { this.dureePause = dureePause; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getTypeUtilisateur() { return typeUtilisateur; }
    public void setTypeUtilisateur(String typeUtilisateur) { this.typeUtilisateur = typeUtilisateur; }

    // Méthodes utilitaires
    public String getDureeTravailFormatted() {
        if (heureEntree == null || heureSortie == null) {
            return "--:--";
        }

        try {
            // Utiliser Duration pour un calcul plus précis
            Duration duree = Duration.between(heureEntree, heureSortie);
            
            // Si la durée est négative
            if (duree.isNegative()) {
                return "Erreur";
            }

            // Convertir en minutes
            long minutesTotales = duree.toMinutes();

            // Soustraire la durée de pause si elle existe
            if (dureePause != null) {
                long minutesPause = dureePause.getHour() * 60L + dureePause.getMinute();
                minutesTotales = Math.max(0, minutesTotales - minutesPause);
            }

            // Calculer les heures et minutes
            long heures = minutesTotales / 60;
            long minutes = minutesTotales % 60;

            // Debug: afficher les valeurs calculées
            System.out.println(String.format(
                "Calcul temps de travail - Entrée: %s, Sortie: %s, Pause: %s, Total: %dh %dmin",
                heureEntree, heureSortie, dureePause, heures, minutes
            ));

            // Formater le résultat
            if (heures > 0) {
                if (minutes > 0) {
                    return String.format("%dh %dmin", heures, minutes);
                } else {
                    return String.format("%dh", heures);
                }
            } else {
                return String.format("%dmin", minutes);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de la durée de travail: " + e.getMessage());
            e.printStackTrace(); // Pour avoir plus de détails sur l'erreur
            return "Erreur";
        }
    }

    // Méthode pour obtenir la durée en minutes
    public long getDureeTravailMinutes() {
        if (heureEntree == null || heureSortie == null) {
            return 0;
        }

        try {
            Duration duree = Duration.between(heureEntree, heureSortie);
            
            if (duree.isNegative()) {
                return 0;
            }

            long minutesTotales = duree.toMinutes();

            if (dureePause != null) {
                long minutesPause = dureePause.getHour() * 60L + dureePause.getMinute();
                return Math.max(0, minutesTotales - minutesPause);
            }

            return minutesTotales;
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des minutes de travail: " + e.getMessage());
            return 0;
        }
    }

    // Méthode pour obtenir la durée en heures (avec décimales)
    public double getDureeTravailHeures() {
        return getDureeTravailMinutes() / 60.0;
    }
} 