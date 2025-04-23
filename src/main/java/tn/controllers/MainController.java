package tn.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.entities.Produit;
import tn.services.ProduitDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MainController {

    @FXML
    private TableView<Produit> produitTable;

    @FXML
    private TableColumn<Produit, Integer> colId;

    @FXML
    private TableColumn<Produit, String> colNom;

    @FXML
    private TableColumn<Produit, String> colDescription;

    @FXML
    private TableColumn<Produit, String> colCategorie;

    @FXML
    private TableColumn<Produit, Double> colPrix;

    @FXML
    private TableColumn<Produit, Integer> colStock;

    @FXML
    private TableColumn<Produit, Void> colActions;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    private ProduitDao produitDao;
    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        produitDao = new ProduitDao();

        // Configurer les colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Configurer la colonne d'actions
        setupActionsColumn();

        // Charger les produits
        loadProduits();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Produit, Void>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    handleEditProduit(produit);
                });

                btnDelete.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    handleDeleteProduit(produit);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadProduits() {
        try {
            // Utiliser la méthode getAll() de ProduitDao
            List<Produit> produits = produitDao.getAll();
            produitsList.setAll(produits);
            produitTable.setItems(produitsList);
            statusLabel.setText("Total produits: " + produits.size());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger les produits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduit() {
        try {
            // Ajustez le chemin selon votre structure de projet
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

    private void handleEditProduit(Produit produit) {
        try {
            // Ajustez le chemin selon votre structure de projet
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

    private void handleDeleteProduit(Produit produit) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le produit");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le produit: " + produit.getNom() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
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
        });
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

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}