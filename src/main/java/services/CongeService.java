package services;

import models.DemandeConge;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CongeService {
    private final DatabaseConnection dbConnection;

    public CongeService() {
        this.dbConnection = new DatabaseConnection();
    }

    public List<DemandeConge> getCongesUtilisateur(Long userId) {
        List<DemandeConge> conges = new ArrayList<>();
        String query = "SELECT * FROM demande_conge WHERE id_utilisateur = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                conges.add(new DemandeConge(
                    rs.getLong("id"),
                    rs.getLong("id_utilisateur"),
                    rs.getDate("date_debut").toLocalDate(),
                    rs.getDate("date_fin").toLocalDate(),
                    rs.getString("type_conge"),
                    rs.getString("statut"),
                    rs.getString("description_conge")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des congés", e);
        }
        return conges;
    }

    public void demanderConge(Long userId, LocalDate dateDebut, LocalDate dateFin, String type, String description) {
        // Vérification des chevauchements
        if (hasOverlappingConge(userId, dateDebut, dateFin)) {
            throw new IllegalStateException("La période demandée chevauche un congé existant");
        }

        String query = "INSERT INTO demande_conge (id_utilisateur, date_debut, date_fin, type_conge, statut, description_conge) " +
                      "VALUES (?, ?, ?, ?, 'EN_ATTENTE', ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(dateDebut));
            pstmt.setDate(3, Date.valueOf(dateFin));
            pstmt.setString(4, type);
            pstmt.setString(5, description);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la demande de congé", e);
        }
    }

    private boolean hasOverlappingConge(Long userId, LocalDate dateDebut, LocalDate dateFin) {
        String query = "SELECT COUNT(*) FROM demande_conge " +
                      "WHERE id_utilisateur = ? " +
                      "AND statut NOT IN ('REFUSE', 'ANNULE') " +
                      "AND ((date_debut <= ? AND date_fin >= ?) " +
                      "OR (date_debut <= ? AND date_fin >= ?))";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(dateFin));
            pstmt.setDate(3, Date.valueOf(dateDebut));
            pstmt.setDate(4, Date.valueOf(dateDebut));
            pstmt.setDate(5, Date.valueOf(dateFin));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification des chevauchements", e);
        }
        return false;
    }

    public void annulerDemandeConge(Long congeId) {
        updateStatutDemandeConge(congeId, "ANNULE");
    }

    public void updateStatutDemandeConge(Long congeId, String nouveauStatut) {
        String query = "UPDATE demande_conge SET statut = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, nouveauStatut);
            pstmt.setLong(2, congeId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Demande de congé non trouvée");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du statut", e);
        }
    }

    public List<DemandeConge> getAllConges() {
        List<DemandeConge> conges = new ArrayList<>();
        String query = "SELECT dc.*, u.nom, u.prenom, u.type_utilisateur " +
                      "FROM demande_conge dc " +
                      "JOIN utilisateur u ON dc.id_utilisateur = u.id_user " +
                      "ORDER BY dc.date_debut DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                DemandeConge conge = new DemandeConge(
                    rs.getLong("id"),
                    rs.getLong("id_utilisateur"),
                    rs.getDate("date_debut").toLocalDate(),
                    rs.getDate("date_fin").toLocalDate(),
                    rs.getString("type_conge"),
                    rs.getString("statut"),
                    rs.getString("description_conge")
                );
                conge.setNomComplet(rs.getString("prenom") + " " + rs.getString("nom"));
                conge.setTypeUtilisateur(rs.getString("type_utilisateur"));
                conges.add(conge);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des congés", e);
        }
        return conges;
    }
} 