package Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ProduitDao;
import models.Produit;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProduitViewController {

    @FXML
    private TableView<Produit> tableProduits;

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
    private TableColumn<Produit, LocalDate> colDateAjout;

    @FXML
    private TextField txtRecherche;

    @FXML
    private Button btnRechercher;

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    @FXML
    private Button btnRafraichir;

    private ProduitDao produitDao;
    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        produitDao = new ProduitDao();

        // Initialiser les colonnes de la table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Formater la colonne de date
        colDateAjout.setCellValueFactory(new PropertyValueFactory<>("dateAjout"));
        colDateAjout.setCellFactory(column -> {
            TableCell<Produit, LocalDate> cell = new TableCell<Produit, LocalDate>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
            return cell;
        });

        // Charger les données
        loadProduits();

        // Désactiver les boutons modifier et supprimer au démarrage
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);

        // Ajouter un listener pour la sélection d'un produit
        tableProduits.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnModifier.setDisable(newSelection == null);
            btnSupprimer.setDisable(newSelection == null);
        });
    }

    private void loadProduits() {
        try {
            List<Produit> produits = produitDao.getAll();
            produitsList.clear();
            produitsList.addAll(produits);
            tableProduits.setItems(produitsList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des produits", e.getMessage());
        }
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtRecherche.getText().trim();

        if (keyword.isEmpty()) {
            loadProduits();
            return;
        }

        try {
            List<Produit> produits = produitDao.searchByName(keyword);
            produitsList.clear();
            produitsList.addAll(produits);
            tableProduits.setItems(produitsList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de recherche", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/views/produit-add-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir la liste après l'ajout
            loadProduits();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'ouverture de fenêtre", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        Produit selectedProduit = tableProduits.getSelectionModel().getSelectedItem();
        if (selectedProduit != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/views/produit-edit-view.fxml"));
                Parent root = loader.load();

                ProduitEditController controller = loader.getController();
                controller.setProduit(selectedProduit);

                Stage stage = new Stage();
                stage.setTitle("Modifier un Produit");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                // Rafraîchir la liste après la modification
                loadProduits();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'ouverture de fenêtre", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimer() {
        Produit selectedProduit = tableProduits.getSelectionModel().getSelectedItem();
        if (selectedProduit != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation de suppression");
            confirmAlert.setHeaderText("Supprimer le produit");
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    boolean success = produitDao.delete(selectedProduit.getId());

                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé", "Le produit a été supprimé avec succès.");
                        loadProduits();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de suppression", "Impossible de supprimer le produit.");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur de suppression", e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleRafraichir() {
        loadProduits();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}