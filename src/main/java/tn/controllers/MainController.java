package tn.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tn.services.ProduitDao;
import tn.entities.Produit;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private ListView<Produit> produitListView;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button btnProduits;

    @FXML
    private Button btnCommandes;

    @FXML
    private Button btnAddProduit;

    private ProduitDao produitDao;
    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        produitDao = new ProduitDao();

        // Configuration de la ListView
        produitListView.setItems(produitsList);

        // Utiliser notre cellule personnalisée pour afficher les produits sous forme de cartes
        produitListView.setCellFactory(lv -> new ProduitListCell(this));

        // Définir une hauteur fixe pour chaque cellule
        produitListView.setFixedCellSize(200);

        // Charger les produits
        loadProduits();
    }

    // Cette méthode est maintenant publique pour être accessible depuis ProduitListCell
    public void showProduitDetails(Produit produit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du produit");
        alert.setHeaderText(produit.getNom());

        String details = "ID: " + produit.getId() + "\n" +
                "Description: " + produit.getDescription() + "\n" +
                "Catégorie: " + produit.getCategorie() + "\n" +
                "Prix: " + produit.getPrix() + " DT\n" +
                "Taille: " + produit.getTaille() + "\n" +
                "Stock: " + produit.getStock() + "\n" +
                "Date d'ajout: " + produit.getDateAjout();

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void loadProduits() {
        try {
            List<Produit> produits = produitDao.getAll();
            produitsList.setAll(produits);
            statusLabel.setText("Total produits: " + produits.size());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger les produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProduits() {
        // Cette méthode est déjà active car nous sommes dans la vue des produits
    }

    @FXML
    private void handleCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/commande-view.fxml"));
            Parent commandeView = loader.load();
            mainContainer.setCenter(commandeView);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger la vue des commandes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/produit-add-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un produit");
            stage.setScene(new Scene(root));

            // Après la fermeture de la fenêtre d'ajout, actualiser la liste
            stage.setOnHidden(event -> loadProduits());

            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cette méthode est maintenant publique pour être accessible depuis ProduitListCell
    public void handleEditProduit(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/produit-edit-view.fxml"));
            Parent root = loader.load();

            ProduitEditController controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Modifier un produit");
            stage.setScene(new Scene(root));

            // Après la fermeture de la fenêtre de modification, actualiser la liste
            stage.setOnHidden(event -> loadProduits());

            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cette méthode est maintenant publique pour être accessible depuis ProduitListCell
    public void handleDeleteProduit(Produit produit) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le produit");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le produit: " + produit.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = produitDao.delete(produit.getId());
                if (success) {
                    loadProduits();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Suppression réussie",
                            "Le produit a été supprimé avec succès.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de suppression",
                            "Impossible de supprimer le produit.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur de suppression",
                        e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadProduits();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();

        try {
            List<Produit> produits;
            if (searchTerm.isEmpty()) {
                produits = produitDao.getAll();
            } else {
                produits = produitDao.searchByName(searchTerm);
            }

            produitsList.setAll(produits);
            statusLabel.setText("Résultats: " + produits.size() + " produit(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de recherche",
                    "Impossible d'effectuer la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}