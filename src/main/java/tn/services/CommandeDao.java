package tn.services;

import tn.entities.Commande;
import tn.entities.Produit;
import tn.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeDao {
    private Connection connection;
    private ProduitDao produitDao;

    public CommandeDao() {
        this.connection = MyDataBase.getConnection();
        this.produitDao = new ProduitDao();
    }

    /**
     * Insère une nouvelle commande dans la base de données
     * @param commande La commande à insérer
     * @return true si l'insertion a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean insert(Commande commande) throws SQLException {
        // Vérifier si le produit existe et s'il y a assez de stock
        Produit produit = produitDao.getById(commande.getProduitId());
        if (produit == null) {
            throw new SQLException("Le produit avec l'ID " + commande.getProduitId() + " n'existe pas");
        }

        if (produit.getStock() < commande.getQuantite()) {
            throw new SQLException("Stock insuffisant pour le produit " + produit.getNom());
        }

        connection.setAutoCommit(false);
        try {
            // Insérer la commande
            String query = "INSERT INTO commandes (date_commande, produit_id, quantite, total) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, Date.valueOf(commande.getDateCommande()));
                ps.setInt(2, commande.getProduitId());
                ps.setInt(3, commande.getQuantite());
                ps.setDouble(4, commande.getTotal());

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        commande.setId(generatedKeys.getInt(1));
                    }
                }
            }

            // Mettre à jour le stock du produit
            int newStock = produit.getStock() - commande.getQuantite();
            if (!produitDao.updateStock(produit.getId(), newStock)) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Met à jour une commande existante dans la base de données
     * @param commande La commande à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean update(Commande commande) throws SQLException {
        // Récupérer l'ancienne commande pour connaître l'ancienne quantité
        Commande oldCommande = getById(commande.getId());
        if (oldCommande == null) {
            return false;
        }

        // Vérifier si le produit existe
        Produit produit = produitDao.getById(commande.getProduitId());
        if (produit == null) {
            throw new SQLException("Le produit avec l'ID " + commande.getProduitId() + " n'existe pas");
        }

        // Calculer la différence de quantité
        int quantityDifference = commande.getQuantite() - oldCommande.getQuantite();

        // Vérifier s'il y a assez de stock si la nouvelle quantité est supérieure
        if (quantityDifference > 0 && produit.getStock() < quantityDifference) {
            throw new SQLException("Stock insuffisant pour le produit " + produit.getNom());
        }

        connection.setAutoCommit(false);
        try {
            // Mettre à jour la commande
            String query = "UPDATE commandes SET date_commande = ?, produit_id = ?, quantite = ?, total = ? WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setDate(1, Date.valueOf(commande.getDateCommande()));
                ps.setInt(2, commande.getProduitId());
                ps.setInt(3, commande.getQuantite());
                ps.setDouble(4, commande.getTotal());
                ps.setInt(5, commande.getId());

                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // Mettre à jour le stock du produit
            int newStock = produit.getStock() - quantityDifference;
            if (!produitDao.updateStock(produit.getId(), newStock)) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Supprime une commande de la base de données
     * @param id L'identifiant de la commande à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean delete(int id) throws SQLException {
        // Récupérer la commande pour connaître la quantité et le produit
        Commande commande = getById(id);
        if (commande == null) {
            return false;
        }

        // Récupérer le produit
        Produit produit = produitDao.getById(commande.getProduitId());
        if (produit == null) {
            throw new SQLException("Le produit associé à la commande n'existe pas");
        }

        connection.setAutoCommit(false);
        try {
            // Supprimer la commande
            String query = "DELETE FROM commandes WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
            }

            // Remettre la quantité dans le stock
            int newStock = produit.getStock() + commande.getQuantite();
            if (!produitDao.updateStock(produit.getId(), newStock)) {
                connection.rollback();
                return false;
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Récupère une commande par son identifiant
     * @param id L'identifiant de la commande à récupérer
     * @return La commande correspondant à l'identifiant, ou null si elle n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public Commande getById(int id) throws SQLException {
        String query = "SELECT * FROM commandes WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCommandeFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Récupère toutes les commandes de la base de données
     * @return Une liste de toutes les commandes
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Commande> getAll() throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commandes";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                commandes.add(extractCommandeFromResultSet(rs));
            }
        }
        return commandes;
    }

    /**
     * Récupère les commandes par produit
     * @param produitId L'identifiant du produit
     * @return Une liste des commandes pour le produit spécifié
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Commande> getByProduitId(int produitId) throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commandes WHERE produit_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, produitId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    commandes.add(extractCommandeFromResultSet(rs));
                }
            }
        }
        return commandes;
    }

    /**
     * Récupère les commandes par plage de dates
     * @param startDate La date de début
     * @param endDate La date de fin
     * @return Une liste des commandes dans la plage de dates spécifiée
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Commande> getByDateRange(Date startDate, Date endDate) throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commandes WHERE date_commande BETWEEN ? AND ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setDate(1, startDate);
            ps.setDate(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    commandes.add(extractCommandeFromResultSet(rs));
                }
            }
        }
        return commandes;
    }

    /**
     * Calcule le total des ventes par produit
     * @param produitId L'identifiant du produit
     * @return Le total des ventes pour le produit spécifié
     * @throws SQLException En cas d'erreur SQL
     */
    public double getTotalSalesByProduit(int produitId) throws SQLException {
        String query = "SELECT SUM(total) as total_sales FROM commandes WHERE produit_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, produitId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_sales");
                }
            }
        }
        return 0;
    }

    /**
     * Extrait un objet Commande d'un ResultSet
     * @param rs Le ResultSet contenant les données de la commande
     * @return Un objet Commande
     * @throws SQLException En cas d'erreur SQL
     */
    private Commande extractCommandeFromResultSet(ResultSet rs) throws SQLException {
        Commande commande = new Commande();
        commande.setId(rs.getInt("id"));
        commande.setDateCommande(rs.getDate("date_commande").toLocalDate());
        commande.setProduitId(rs.getInt("produit_id"));
        commande.setQuantite(rs.getInt("quantite"));
        commande.setTotal(rs.getDouble("total"));
        return commande;
    }
}