package services;

import models.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {
    private Connection connection;

    public UtilisateurService() throws SQLException {
        this.connection = DataBaseConnection.getConnection();
    }

    public void ajouterUtilisateur(Utilisateur utilisateur) throws SQLException {
        String query = "INSERT INTO utilisateur (nom, prenom, email, telephone, adresse, date_naissance, date_debut_contrat, " +
                "date_fin_contrat, password, salaire, type_utilisateur, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, utilisateur.getNom());
            statement.setString(2, utilisateur.getPrenom());
            statement.setString(3, utilisateur.getEmail());
            statement.setString(4, utilisateur.getTelephone());
            statement.setString(5, utilisateur.getAdresse());
            statement.setDate(6, utilisateur.getDateNaissance() != null ? Date.valueOf(utilisateur.getDateNaissance()) : null);
            statement.setDate(7, utilisateur.getDateDebutContrat() != null ? Date.valueOf(utilisateur.getDateDebutContrat()) : null);
            statement.setDate(8, utilisateur.getDateFinContrat() != null ? Date.valueOf(utilisateur.getDateFinContrat()) : null);
            statement.setString(9, utilisateur.getPassword());

            if (utilisateur.getSalaire() != null) {
                statement.setDouble(10, utilisateur.getSalaire());
            } else {
                statement.setNull(10, Types.DOUBLE);
            }

            statement.setString(11, utilisateur.getTypeUtilisateur());
            statement.setString(12, utilisateur.getStatut());

            statement.executeUpdate();
        }
    }

    public void modifierUtilisateur(Utilisateur utilisateur) throws SQLException {
        String query = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, telephone = ?, adresse = ?, date_naissance = ?, " +
                "date_debut_contrat = ?, date_fin_contrat = ?, password = ?, salaire = ?, " +
                "type_utilisateur = ?, statut = ? WHERE id_user = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, utilisateur.getNom());
            statement.setString(2, utilisateur.getPrenom());
            statement.setString(3, utilisateur.getEmail());
            statement.setString(4, utilisateur.getTelephone());
            statement.setString(5, utilisateur.getAdresse());
            statement.setDate(6, utilisateur.getDateNaissance() != null ? Date.valueOf(utilisateur.getDateNaissance()) : null);
            statement.setDate(7, utilisateur.getDateDebutContrat() != null ? Date.valueOf(utilisateur.getDateDebutContrat()) : null);
            statement.setDate(8, utilisateur.getDateFinContrat() != null ? Date.valueOf(utilisateur.getDateFinContrat()) : null);
            statement.setString(9, utilisateur.getPassword());

            if (utilisateur.getSalaire() != null) {
                statement.setDouble(10, utilisateur.getSalaire());
            } else {
                statement.setNull(10, Types.DOUBLE);
            }

            statement.setString(11, utilisateur.getTypeUtilisateur());
            statement.setString(12, utilisateur.getStatut());
            statement.setInt(13, utilisateur.getIdUser());

            statement.executeUpdate();
        }
    }

    public List<Utilisateur> getAllUtilisateurs() {
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

                System.out.println("Utilisateur trouvé: " + utilisateur.getPrenom() + " " + utilisateur.getNom());
                utilisateurs.add(utilisateur);
            }

            System.out.println("Nombre total d'utilisateurs trouvés: " + utilisateurs.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
            e.printStackTrace();
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
                return user;
            }
            return null;
        }
    }
}