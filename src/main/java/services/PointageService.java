package services;

import models.Pointage;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PointageService {
    
    public List<Pointage> getPointagesUtilisateur(Long userId) throws SQLException {
        List<Pointage> pointages = new ArrayList<>();
        String sql = """
            SELECT p.*, u.nom, u.prenom, u.type_utilisateur 
            FROM pointage p 
            JOIN utilisateur u ON p.id_user = u.id_user 
            WHERE p.id_user = ? 
            ORDER BY p.date_pointage DESC, p.heure_entree DESC
            """;
            
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Pointage pointage = new Pointage();
                pointage.setId(rs.getLong("id_pointage"));
                pointage.setUserId(rs.getLong("id_user"));
                pointage.setDatePointage(rs.getDate("date_pointage").toLocalDate());
                pointage.setHeureEntree(rs.getTime("heure_entree").toLocalTime());
                
                Time heureSortie = rs.getTime("heure_sortie");
                if (heureSortie != null) {
                    pointage.setHeureSortie(heureSortie.toLocalTime());
                }
                
                Time dureePause = rs.getTime("duree_pause");
                if (dureePause != null) {
                    pointage.setDureePause(dureePause.toLocalTime());
                }
                
                pointage.setStatut(rs.getString("statut"));
                pointage.setCommentaire(rs.getString("commentaire"));
                pointage.setNomComplet(rs.getString("prenom") + " " + rs.getString("nom"));
                pointage.setTypeUtilisateur(rs.getString("type_utilisateur"));
                
                pointages.add(pointage);
            }
        }
        return pointages;
    }

    public List<Pointage> getAllPointages() throws SQLException {
        List<Pointage> pointages = new ArrayList<>();
        String sql = """
            SELECT p.*, u.nom, u.prenom, u.type_utilisateur 
            FROM pointage p 
            JOIN utilisateur u ON p.id_user = u.id_user 
            WHERE u.type_utilisateur IN ('LIVREUR', 'MAGASINIER')
            ORDER BY p.date_pointage DESC, p.heure_entree DESC
            """;
            
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Pointage pointage = new Pointage();
                pointage.setId(rs.getLong("id_pointage"));
                pointage.setUserId(rs.getLong("id_user"));
                pointage.setDatePointage(rs.getDate("date_pointage").toLocalDate());
                pointage.setHeureEntree(rs.getTime("heure_entree").toLocalTime());
                
                Time heureSortie = rs.getTime("heure_sortie");
                if (heureSortie != null) {
                    pointage.setHeureSortie(heureSortie.toLocalTime());
                }
                
                Time dureePause = rs.getTime("duree_pause");
                if (dureePause != null) {
                    pointage.setDureePause(dureePause.toLocalTime());
                }
                
                pointage.setStatut(rs.getString("statut"));
                pointage.setCommentaire(rs.getString("commentaire"));
                pointage.setNomComplet(rs.getString("prenom") + " " + rs.getString("nom"));
                pointage.setTypeUtilisateur(rs.getString("type_utilisateur"));
                
                pointages.add(pointage);
            }
        }
        return pointages;
    }

    public void enregistrerEntree(Long userId) throws SQLException {
        String sql = "INSERT INTO pointage (id_user, date_pointage, heure_entree, statut) VALUES (?, ?, ?, 'PRESENT')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            pstmt.setTime(3, Time.valueOf(LocalTime.now()));
            
            pstmt.executeUpdate();
        }
    }

    public void enregistrerSortie(Long userId) throws SQLException {
        String sql = """
            UPDATE pointage 
            SET heure_sortie = ?, 
                duree_pause = '01:00:00'  -- Pause par défaut d'une heure
            WHERE id_user = ? 
            AND date_pointage = ? 
            AND heure_sortie IS NULL
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTime(1, Time.valueOf(LocalTime.now()));
            pstmt.setLong(2, userId);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            
            pstmt.executeUpdate();
        }
    }

    public boolean isPointageEntree(Long userId) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM pointage 
            WHERE id_user = ? 
            AND date_pointage = ? 
            AND heure_sortie IS NULL
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    public int getJoursPresents(Long userId) throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT date_pointage) 
            FROM pointage 
            WHERE id_user = ? 
            AND statut = 'PRESENT'
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getHeuresTravaillees(Long userId) throws SQLException {
        String sql = """
            SELECT SUM(
                TIME_TO_SEC(TIMEDIFF(heure_sortie, heure_entree)) - 
                TIME_TO_SEC(IFNULL(duree_pause, '00:00:00'))
            ) / 3600 as total_heures
            FROM pointage 
            WHERE id_user = ? 
            AND statut = 'PRESENT'
            AND heure_sortie IS NOT NULL
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total_heures");
            }
        }
        return 0.0;
    }

    public void enregistrerEntreeManuelle(Long userId, LocalDateTime dateTime) throws SQLException {
        String sql = "INSERT INTO pointage (id_user, date_pointage, heure_entree, statut) VALUES (?, ?, ?, 'PRESENT')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setDate(2, Date.valueOf(dateTime.toLocalDate()));
            pstmt.setTime(3, Time.valueOf(dateTime.toLocalTime()));
            
            pstmt.executeUpdate();
        }
    }

    public void enregistrerSortieManuelle(Long userId, LocalDateTime dateTime) throws SQLException {
        String sql = """
            UPDATE pointage 
            SET heure_sortie = ?, 
                duree_pause = '01:00:00'  -- Pause par défaut d'une heure
            WHERE id_user = ? 
            AND date_pointage = ? 
            AND heure_sortie IS NULL
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTime(1, Time.valueOf(dateTime.toLocalTime()));
            pstmt.setLong(2, userId);
            pstmt.setDate(3, Date.valueOf(dateTime.toLocalDate()));
            
            pstmt.executeUpdate();
        }
    }
} 