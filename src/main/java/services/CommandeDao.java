package services;

import models.Commande;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommandeDao {
    private Connection connection;

    public CommandeDao() {
        this.connection = DataBaseConnection.getConnection();
        System.out.println("CommandeDao: Connexion à la base de données établie");
    }

    /**
     * Vérifie et rétablit la connexion si nécessaire
     */
    private void checkConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DataBaseConnection.getConnection();
                System.out.println("CommandeDao: Connexion à la base de données rétablie");
            }
        } catch (SQLException e) {
            System.err.println("CommandeDao: Erreur lors de la vérification de la connexion: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Crée une commande directe pour un produit
     */
    public boolean createDirectOrder(int idUser, int produitId, int quantite, String adresse, String instructions) throws SQLException {
        checkConnection();

        // Vérifier d'abord si le produit a suffisamment de stock
        int stockDisponible = getProductStock(produitId);
        if (stockDisponible < quantite) {
            System.err.println("Stock insuffisant pour le produit ID " + produitId +
                    ": demandé " + quantite + ", disponible " + stockDisponible);
            throw new SQLException("Stock insuffisant. Seulement " + stockDisponible + " unités disponibles.");
        }

        String sql = "INSERT INTO commandes (id_user, produit_id, quantite, adresse, description_cmd, date_commande, total) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Récupérer le prix du produit pour calculer le total
        double prix = getProduitPrice(produitId);
        double total = prix * quantite;

        logSQLQuery(sql, idUser, produitId, quantite, adresse, instructions, LocalDate.now(), total);

        PreparedStatement stmt = null;
        boolean success = false;

        try {
            // Désactiver l'auto-commit pour permettre une transaction
            connection.setAutoCommit(false);

            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idUser);
            stmt.setInt(2, produitId);
            stmt.setInt(3, quantite);
            stmt.setString(4, adresse);
            stmt.setString(5, instructions);
            stmt.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setDouble(7, total);

            int rowsAffected = stmt.executeUpdate();

            // Mettre à jour le stock du produit seulement si la commande a été créée
            if (rowsAffected > 0) {
                updateProductStock(produitId, quantite);
                System.out.println("Commande créée avec succès");
                success = true;

                // Valider la transaction
                connection.commit();
            } else {
                System.out.println("Échec de création de la commande");
                // Annuler la transaction
                connection.rollback();
            }
        } catch (SQLException e) {
            // En cas d'erreur, annuler la transaction
            try {
                if (connection != null) {
                    connection.rollback();
                    System.err.println("Transaction annulée suite à une erreur");
                }
            } catch (SQLException ex) {
                System.err.println("Erreur lors de l'annulation de la transaction: " + ex.getMessage());
            }

            System.err.println("Erreur SQL dans createDirectOrder: " + e.getMessage());
            throw e;
        } finally {
            // Rétablir l'auto-commit
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors du rétablissement de l'auto-commit: " + e.getMessage());
            }

            // Fermer le PreparedStatement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }

        return success;
    }

    /**
     * Supprime une commande
     */
    public boolean deleteCommande(int commandeId) throws SQLException {
        checkConnection();

        // D'abord, récupérer les informations de la commande pour pouvoir restaurer le stock
        Commande commande = getCommandeById(commandeId);
        if (commande == null) {
            throw new SQLException("Commande non trouvée avec l'ID: " + commandeId);
        }

        String sql = "DELETE FROM commandes WHERE id = ?";
        logSQLQuery(sql, commandeId);

        PreparedStatement stmt = null;
        boolean success = false;

        try {
            // Désactiver l'auto-commit pour permettre une transaction
            connection.setAutoCommit(false);

            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, commandeId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Restaurer le stock du produit
                restoreProductStock(commande.getProduitId(), commande.getQuantite());
                System.out.println("Commande supprimée avec succès, stock restauré");
                success = true;

                // Valider la transaction
                connection.commit();
            } else {
                System.out.println("Échec de suppression de la commande");
                // Annuler la transaction
                connection.rollback();
            }
        } catch (SQLException e) {
            // En cas d'erreur, annuler la transaction
            try {
                if (connection != null) {
                    connection.rollback();
                    System.err.println("Transaction annulée suite à une erreur");
                }
            } catch (SQLException ex) {
                System.err.println("Erreur lors de l'annulation de la transaction: " + ex.getMessage());
            }

            System.err.println("Erreur SQL dans deleteCommande: " + e.getMessage());
            throw e;
        } finally {
            // Rétablir l'auto-commit
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors du rétablissement de l'auto-commit: " + e.getMessage());
            }

            // Fermer le PreparedStatement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }

        return success;
    }

    /**
     * Restaure le stock d'un produit après suppression d'une commande
     */
    private void restoreProductStock(int produitId, int quantite) throws SQLException {
        String sql = "UPDATE produits SET stock = stock + ? WHERE id = ?";
        logSQLQuery(sql, quantite, produitId);

        PreparedStatement stmt = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, quantite);
            stmt.setInt(2, produitId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Restauration du stock échouée. Produit non trouvé: " + produitId);
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Modifie une commande existante
     */
    public boolean updateCommande(Commande commande) throws SQLException {
        checkConnection();

        // Récupérer la commande originale pour comparer les quantités
        Commande originalCommande = getCommandeById(commande.getId());
        if (originalCommande == null) {
            throw new SQLException("Commande non trouvée avec l'ID: " + commande.getId());
        }

        // Calculer la différence de quantité
        int quantiteDifference = commande.getQuantite() - originalCommande.getQuantite();

        // Si la quantité augmente, vérifier le stock disponible
        if (quantiteDifference > 0) {
            int stockDisponible = getProductStock(commande.getProduitId());
            if (stockDisponible < quantiteDifference) {
                throw new SQLException("Stock insuffisant. Seulement " + stockDisponible + " unités disponibles.");
            }
        }

        // Recalculer le total
        double prix = getProduitPrice(commande.getProduitId());
        double total = prix * commande.getQuantite();

        String sql = "UPDATE commandes SET produit_id = ?, quantite = ?, adresse = ?, description_cmd = ?, total = ? WHERE id = ?";
        logSQLQuery(sql, commande.getProduitId(), commande.getQuantite(), commande.getAdresse(),
                commande.getDescription(), total, commande.getId());

        PreparedStatement stmt = null;
        boolean success = false;

        try {
            // Désactiver l'auto-commit pour permettre une transaction
            connection.setAutoCommit(false);

            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, commande.getProduitId());
            stmt.setInt(2, commande.getQuantite());
            stmt.setString(3, commande.getAdresse());
            stmt.setString(4, commande.getDescription());
            stmt.setDouble(5, total);
            stmt.setInt(6, commande.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Mettre à jour le stock du produit en fonction de la différence de quantité
                if (quantiteDifference != 0) {
                    if (quantiteDifference > 0) {
                        // Si la quantité a augmenté, réduire le stock
                        updateProductStock(commande.getProduitId(), quantiteDifference);
                    } else {
                        // Si la quantité a diminué, augmenter le stock
                        restoreProductStock(commande.getProduitId(), -quantiteDifference);
                    }
                }

                System.out.println("Commande mise à jour avec succès");
                success = true;

                // Valider la transaction
                connection.commit();
            } else {
                System.out.println("Échec de mise à jour de la commande");
                // Annuler la transaction
                connection.rollback();
            }
        } catch (SQLException e) {
            // En cas d'erreur, annuler la transaction
            try {
                if (connection != null) {
                    connection.rollback();
                    System.err.println("Transaction annulée suite à une erreur");
                }
            } catch (SQLException ex) {
                System.err.println("Erreur lors de l'annulation de la transaction: " + ex.getMessage());
            }

            System.err.println("Erreur SQL dans updateCommande: " + e.getMessage());
            throw e;
        } finally {
            // Rétablir l'auto-commit
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors du rétablissement de l'auto-commit: " + e.getMessage());
            }

            // Fermer le PreparedStatement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }

        return success;
    }

    /**
     * Récupère une commande par son ID
     */
    public Commande getCommandeById(int commandeId) throws SQLException {
        checkConnection();

        String sql = "SELECT c.*, p.nom as produit_nom, p.prix FROM commandes c " +
                "JOIN produits p ON c.produit_id = p.id " +
                "WHERE c.id = ?";
        logSQLQuery(sql, commandeId);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, commandeId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setIdUser(rs.getInt("id_user"));
                commande.setProduitId(rs.getInt("produit_id"));
                commande.setProduitNom(rs.getString("produit_nom"));
                commande.setQuantite(rs.getInt("quantite"));
                commande.setAdresse(rs.getString("adresse"));
                commande.setDescription(rs.getString("description_cmd"));

                Date dateCommande = rs.getDate("date_commande");
                if (dateCommande != null) {
                    commande.setDateCommande(dateCommande.toLocalDate());
                } else {
                    commande.setDateCommande(LocalDate.now());
                }

                // Récupérer le total depuis la base de données ou le calculer
                if (rs.getObject("total") != null) {
                    commande.setTotal(rs.getDouble("total"));
                } else {
                    double prix = rs.getDouble("prix");
                    commande.setTotal(prix * commande.getQuantite());
                }

                return commande;
            }

            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du ResultSet: " + e.getMessage());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Récupère le stock actuel d'un produit
     */
    private int getProductStock(int produitId) throws SQLException {
        checkConnection();

        String sql = "SELECT stock FROM produits WHERE id = ?";
        logSQLQuery(sql, produitId);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, produitId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("stock");
            }

            throw new SQLException("Produit non trouvé avec l'ID: " + produitId);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du ResultSet: " + e.getMessage());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Journalise une requête SQL et ses paramètres pour le débogage
     */
    private void logSQLQuery(String query, Object... params) {
        StringBuilder sb = new StringBuilder("Exécution de la requête SQL: ");
        sb.append(query).append(" avec les paramètres: [");

        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i < params.length - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        System.out.println(sb.toString());
    }

    /**
     * Récupère toutes les commandes
     */
    public List<Commande> getAllCommandes() throws SQLException {
        checkConnection();

        String sql = "SELECT c.*, u.nom as client_nom, u.prenom as client_prenom, p.nom as produit_nom, p.prix " +
                "FROM commandes c " +
                "JOIN utilisateur u ON c.id_user = u.id_user " +
                "JOIN produits p ON c.produit_id = p.id " +
                "ORDER BY c.date_commande DESC";

        logSQLQuery(sql);
        List<Commande> commandes = new ArrayList<>();

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setIdUser(rs.getInt("id_user"));
                commande.setProduitId(rs.getInt("produit_id"));
                commande.setQuantite(rs.getInt("quantite"));
                commande.setAdresse(rs.getString("adresse"));
                commande.setDescription(rs.getString("description_cmd"));

                Date dateCommande = rs.getDate("date_commande");
                if (dateCommande != null) {
                    commande.setDateCommande(dateCommande.toLocalDate());
                } else {
                    commande.setDateCommande(LocalDate.now());
                }

                // Définir le nom du client
                String clientPrenom = rs.getString("client_prenom");
                String clientNom = rs.getString("client_nom");
                commande.setClientNom((clientPrenom != null ? clientPrenom : "") + " " +
                        (clientNom != null ? clientNom : ""));

                // Définir le nom du produit
                commande.setProduitNom(rs.getString("produit_nom"));

                // Calculer le total
                double prix = rs.getDouble("prix");
                commande.setTotal(prix * commande.getQuantite());

                commandes.add(commande);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du ResultSet: " + e.getMessage());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du Statement: " + e.getMessage());
                }
            }
        }

        return commandes;
    }

    /**
     * Met à jour le stock d'un produit après une commande
     */
    private void updateProductStock(int produitId, int quantite) throws SQLException {
        checkConnection();

        String sql = "UPDATE produits SET stock = stock - ? WHERE id = ?";
        logSQLQuery(sql, quantite, produitId);

        PreparedStatement stmt = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, quantite);
            stmt.setInt(2, produitId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Mise à jour du stock échouée. Produit non trouvé: " + produitId);
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Récupère les commandes d'un client spécifique
     */
    public List<Commande> getCommandesByClient(int idUser) throws SQLException {
        checkConnection();

        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT c.*, p.nom as produit_nom, p.prix FROM commandes c " +
                "JOIN produits p ON c.produit_id = p.id " +
                "WHERE c.id_user = ? ORDER BY c.date_commande DESC";

        logSQLQuery(query, idUser);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, idUser);
            rs = ps.executeQuery();

            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));

                Date dateCommande = rs.getDate("date_commande");
                if (dateCommande != null) {
                    commande.setDateCommande(dateCommande.toLocalDate());
                } else {
                    commande.setDateCommande(LocalDate.now());
                }

                commande.setProduitId(rs.getInt("produit_id"));
                commande.setProduitNom(rs.getString("produit_nom"));
                commande.setQuantite(rs.getInt("quantite"));

                // Récupérer le total depuis la base de données ou le calculer
                if (rs.getObject("total") != null) {
                    commande.setTotal(rs.getDouble("total"));
                } else {
                    double prix = rs.getDouble("prix");
                    commande.setTotal(prix * commande.getQuantite());
                }

                commande.setIdUser(rs.getInt("id_user"));
                commande.setAdresse(rs.getString("adresse"));
                commande.setDescription(rs.getString("description_cmd"));

                commandes.add(commande);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du ResultSet: " + e.getMessage());
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }

        return commandes;
    }

    /**
     * Récupère le prix d'un produit
     */
    private double getProduitPrice(int produitId) throws SQLException {
        checkConnection();

        String sql = "SELECT prix FROM produits WHERE id = ?";
        logSQLQuery(sql, produitId);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, produitId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("prix");
            }

            throw new SQLException("Produit non trouvé avec l'ID: " + produitId);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du ResultSet: " + e.getMessage());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Ferme la connexion à la base de données
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("CommandeDao: Connexion à la base de données fermée");
            } catch (SQLException e) {
                System.err.println("CommandeDao: Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
}