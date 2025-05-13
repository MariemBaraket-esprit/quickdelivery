package services;

import models.Utilisateur;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class UtilisateurService {
    private Connection connection;

    public UtilisateurService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public void ajouterUtilisateur(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, telephone, adresse, " +
                    "date_naissance, date_debut_contrat, date_fin_contrat, password, " +
                    "salaire, type_utilisateur, statut, photo_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getPrenom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setString(4, utilisateur.getTelephone());
            pstmt.setString(5, utilisateur.getAdresse());
            
            if (utilisateur.getDateNaissance() != null) {
                pstmt.setDate(6, java.sql.Date.valueOf(utilisateur.getDateNaissance()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            
            if (utilisateur.getDateDebutContrat() != null) {
                pstmt.setDate(7, java.sql.Date.valueOf(utilisateur.getDateDebutContrat()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            
            if (utilisateur.getDateFinContrat() != null) {
                pstmt.setDate(8, java.sql.Date.valueOf(utilisateur.getDateFinContrat()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            
            pstmt.setString(9, utilisateur.getPassword());
            
            if (utilisateur.getSalaire() != null) {
                pstmt.setDouble(10, utilisateur.getSalaire());
            } else {
                pstmt.setNull(10, Types.DOUBLE);
            }
            
            pstmt.setString(11, utilisateur.getTypeUtilisateur());
            pstmt.setString(12, utilisateur.getStatut());
            pstmt.setString(13, utilisateur.getPhotoUrl());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    utilisateur.setIdUser(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void modifierUtilisateur(Utilisateur utilisateur) throws SQLException {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, telephone = ?, " +
                    "adresse = ?, date_naissance = ?, date_debut_contrat = ?, date_fin_contrat = ?, " +
                    "password = ?, salaire = ?, type_utilisateur = ?, statut = ?, photo_url = ? " +
                    "WHERE id_user = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, utilisateur.getNom());
            pstmt.setString(2, utilisateur.getPrenom());
            pstmt.setString(3, utilisateur.getEmail());
            pstmt.setString(4, utilisateur.getTelephone());
            pstmt.setString(5, utilisateur.getAdresse());
            
            if (utilisateur.getDateNaissance() != null) {
                pstmt.setDate(6, java.sql.Date.valueOf(utilisateur.getDateNaissance()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            
            if (utilisateur.getDateDebutContrat() != null) {
                pstmt.setDate(7, java.sql.Date.valueOf(utilisateur.getDateDebutContrat()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            
            if (utilisateur.getDateFinContrat() != null) {
                pstmt.setDate(8, java.sql.Date.valueOf(utilisateur.getDateFinContrat()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            
            pstmt.setString(9, utilisateur.getPassword());
            
            if (utilisateur.getSalaire() != null) {
                pstmt.setDouble(10, utilisateur.getSalaire());
            } else {
                pstmt.setNull(10, Types.DOUBLE);
            }
            
            pstmt.setString(11, utilisateur.getTypeUtilisateur());
            pstmt.setString(12, utilisateur.getStatut());
            pstmt.setString(13, utilisateur.getPhotoUrl());
            pstmt.setInt(14, utilisateur.getIdUser());
            
            pstmt.executeUpdate();
        }
    }

    public List<Utilisateur> getAllUtilisateurs() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("Exécution de la requête SQL: " + sql);
            
            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setIdUser(rs.getInt("id_user"));
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setPrenom(rs.getString("prenom"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setTelephone(rs.getString("telephone"));
                utilisateur.setAdresse(rs.getString("adresse"));
                
                // Handle potentially null dates
                Date dateNaissance = rs.getDate("date_naissance");
                if (dateNaissance != null) {
                    utilisateur.setDateNaissance(dateNaissance.toLocalDate());
                }
                
                Date dateDebutContrat = rs.getDate("date_debut_contrat");
                if (dateDebutContrat != null) {
                    utilisateur.setDateDebutContrat(dateDebutContrat.toLocalDate());
                }
                
                Date dateFinContrat = rs.getDate("date_fin_contrat");
                if (dateFinContrat != null) {
                    utilisateur.setDateFinContrat(dateFinContrat.toLocalDate());
                }
                
                utilisateur.setPassword(rs.getString("password"));
                
                // Handle potentially null salary
                double salaire = rs.getDouble("salaire");
                if (!rs.wasNull()) {
                    utilisateur.setSalaire(salaire);
                }
                
                utilisateur.setTypeUtilisateur(rs.getString("type_utilisateur"));
                utilisateur.setStatut(rs.getString("statut"));
                utilisateur.setPhotoUrl(rs.getString("photo_url"));
                
                System.out.println("Utilisateur trouvé: " + utilisateur.getPrenom() + " " + utilisateur.getNom());
                utilisateurs.add(utilisateur);
            }
            
            System.out.println("Nombre total d'utilisateurs trouvés: " + utilisateurs.size());
        }
        return utilisateurs;
    }

    public void deleteUtilisateur(int idUser) throws SQLException {
        String query = "DELETE FROM utilisateur WHERE id_user = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, idUser);
            statement.executeUpdate();
        }
    }

    public boolean verifyPassword(int userId, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE id_user = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    public Utilisateur authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setIdUser(rs.getInt("id_user"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setTelephone(rs.getString("telephone"));
                user.setAdresse(rs.getString("adresse"));
                
                // Handle dates
                Date dateNaissance = rs.getDate("date_naissance");
                if (dateNaissance != null) {
                    user.setDateNaissance(dateNaissance.toLocalDate());
                }
                
                Date dateDebutContrat = rs.getDate("date_debut_contrat");
                if (dateDebutContrat != null) {
                    user.setDateDebutContrat(dateDebutContrat.toLocalDate());
                }
                
                Date dateFinContrat = rs.getDate("date_fin_contrat");
                if (dateFinContrat != null) {
                    user.setDateFinContrat(dateFinContrat.toLocalDate());
                }
                
                user.setPassword(rs.getString("password"));
                
                // Handle salary
                double salaire = rs.getDouble("salaire");
                if (!rs.wasNull()) {
                    user.setSalaire(salaire);
                }
                
                user.setTypeUtilisateur(rs.getString("type_utilisateur"));
                user.setStatut(rs.getString("statut"));
                user.setPhotoUrl(rs.getString("photo_url"));
                return user;
            }
            return null;
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    public boolean telephoneExists(String telephone) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE telephone = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, telephone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdUser(rs.getInt("id_user"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setEmail(rs.getString("email"));
        utilisateur.setTelephone(rs.getString("telephone"));
        utilisateur.setAdresse(rs.getString("adresse"));
        
        java.sql.Date dateNaissance = rs.getDate("date_naissance");
        if (dateNaissance != null) {
            utilisateur.setDateNaissance(dateNaissance.toLocalDate());
        }
        
        java.sql.Date dateDebutContrat = rs.getDate("date_debut_contrat");
        if (dateDebutContrat != null) {
            utilisateur.setDateDebutContrat(dateDebutContrat.toLocalDate());
        }
        
        java.sql.Date dateFinContrat = rs.getDate("date_fin_contrat");
        if (dateFinContrat != null) {
            utilisateur.setDateFinContrat(dateFinContrat.toLocalDate());
        }
        
        utilisateur.setPassword(rs.getString("password"));
        
        double salaire = rs.getDouble("salaire");
        if (!rs.wasNull()) {
            utilisateur.setSalaire(salaire);
        }
        
        utilisateur.setTypeUtilisateur(rs.getString("type_utilisateur"));
        utilisateur.setStatut(rs.getString("statut"));
        utilisateur.setPhotoUrl(rs.getString("photo_url"));
        
        return utilisateur;
    }

    public Utilisateur getUtilisateurById(Long userId) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id_user = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
            return null;
        }
    }
} 