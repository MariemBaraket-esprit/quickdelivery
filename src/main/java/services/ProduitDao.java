package services;

import com.sun.javafx.sg.prism.NGNode;
import models.Produit;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDao {
    private Connection connection;

    public ProduitDao() {
        this.connection = MyDataBase.getConnection();
    }

    /**
     * Insère un nouveau produit dans la base de données
     * @param produit Le produit à insérer
     * @return true si l'insertion a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean insert(Produit produit) throws SQLException {
        String query = "INSERT INTO produits (nom, description, categorie, prix, stock, date_ajout, taille) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produit.getNom());
            ps.setString(2, produit.getDescription());
            ps.setString(3, produit.getCategorie());
            ps.setDouble(4, produit.getPrix());
            ps.setInt(5, produit.getStock());
            ps.setDate(6, Date.valueOf(produit.getDateAjout()));
            ps.setInt(7, produit.getTaille());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    produit.setId(generatedKeys.getInt(1));
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * Met à jour un produit existant dans la base de données
     * @param produit Le produit à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean update(Produit produit) throws SQLException {
        String query = "UPDATE produits SET nom = ?, description = ?, categorie = ?, prix = ?, stock = ?, taille = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, produit.getNom());
            ps.setString(2, produit.getDescription());
            ps.setString(3, produit.getCategorie());
            ps.setDouble(4, produit.getPrix());
            ps.setInt(5, produit.getStock());
            ps.setInt(6, produit.getTaille());
            ps.setInt(7, produit.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Met à jour uniquement le stock d'un produit
     * @param produitId L'identifiant du produit
     * @param newStock La nouvelle valeur du stock
     * @return true si la mise à jour a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean updateStock(int produitId, int newStock) throws SQLException {
        String query = "UPDATE produits SET stock = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, newStock);
            ps.setInt(2, produitId);

            return ps.executeUpdate() > 0;
        }
    }
    public List<Produit> getAllProduits() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits";


        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setDescription(rs.getString("description"));
                produit.setCategorie(rs.getString("categorie"));
                produit.setPrix(rs.getDouble("prix"));
                produit.setStock(rs.getInt("stock"));

                if (rs.getDate("date_ajout") != null) {
                    produit.setDateAjout(rs.getDate("date_ajout").toLocalDate());
                }

                produit.setTaille(rs.getInt("taille"));
                produit.setImage(rs.getString("image"));

                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    /**
     * Supprime un produit de la base de données
     * @param id L'identifiant du produit à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean delete(int id) throws SQLException {
        // Vérifier si le produit est utilisé dans des commandes
        String checkQuery = "SELECT COUNT(*) FROM commandes WHERE produit_id = ?";
        try (PreparedStatement checkPs = connection.prepareStatement(checkQuery)) {
            checkPs.setInt(1, id);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Impossible de supprimer le produit car il est utilisé dans des commandes");
                }
            }
        }

        String query = "DELETE FROM produits WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Récupère un produit par son identifiant
     * @param id L'identifiant du produit à récupérer
     * @return Le produit correspondant à l'identifiant, ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public Produit getById(int id) throws SQLException {
        String query = "SELECT * FROM produits WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProduitFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Récupère tous les produits de la base de données
     * @return Une liste de tous les produits
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Produit> getAll() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                produits.add(extractProduitFromResultSet(rs));
            }
        }
        return produits;
    }

    /**
     * Recherche des produits par nom
     * @param name Le nom à rechercher
     * @return Une liste des produits correspondant au nom
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Produit> searchByName(String name) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE nom LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    produits.add(extractProduitFromResultSet(rs));
                }
            }
        }
        return produits;
    }

    /**
     * Recherche des produits par catégorie
     * @param categorie La catégorie à rechercher
     * @return Une liste des produits correspondant à la catégorie
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Produit> getByCategorie(String categorie) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE categorie = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, categorie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    produits.add(extractProduitFromResultSet(rs));
                }
            }
        }
        return produits;
    }

    /**
     * Récupère les produits dont le stock est inférieur à une valeur donnée
     * @param minStock La valeur minimale du stock
     * @return Une liste des produits dont le stock est inférieur à minStock
     * @throws SQLException En cas d'erreur SQL
     */
    public List<Produit> getLowStock(int minStock) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE stock < ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, minStock);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    produits.add(extractProduitFromResultSet(rs));
                }
            }
        }
        return produits;
    }

    /**
     * Extrait un objet Produit d'un ResultSet
     * @param rs Le ResultSet contenant les données du produit
     * @return Un objet Produit
     * @throws SQLException En cas d'erreur SQL
     */
    private Produit extractProduitFromResultSet(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setId(rs.getInt("id"));
        produit.setNom(rs.getString("nom"));
        produit.setDescription(rs.getString("description"));
        produit.setCategorie(rs.getString("categorie"));
        produit.setPrix(rs.getDouble("prix"));
        produit.setStock(rs.getInt("stock"));
        produit.setDateAjout(rs.getDate("date_ajout").toLocalDate());

        // Récupérer la taille comme un entier
        try {
            produit.setTaille(rs.getInt("taille"));
        } catch (SQLException e) {
            // Si la colonne taille n'est pas un entier ou est NULL
            produit.setTaille(0);
        }

        return produit;
    }
}