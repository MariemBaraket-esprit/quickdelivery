package Controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Commande;
import services.CommandeDao;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CommandeController {

    @FXML
    private TableView<Commande> commandesTable;

    @FXML
    private TableColumn<Commande, Integer> idColumn;

    @FXML
    private TableColumn<Commande, LocalDate> dateColumn;

    @FXML
    private TableColumn<Commande, String> clientColumn;

    @FXML
    private TableColumn<Commande, String> produitColumn;

    @FXML
    private TableColumn<Commande, Integer> quantiteColumn;

    @FXML
    private TableColumn<Commande, Double> totalColumn;

    @FXML
    private TableColumn<Commande, String> adresseColumn;

    @FXML
    private TableColumn<Commande, Void> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button btnRefresh;

    private CommandeDao commandeDao;
    private ObservableList<Commande> commandesList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        commandeDao = new CommandeDao();

        // Configuration des colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        dateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDateCommande()));
        dateColumn.setCellFactory(column -> new TableCell<Commande, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });

        clientColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClientNom()));

        produitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduitNom()));

        quantiteColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());

        totalColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
        totalColumn.setCellFactory(column -> new TableCell<Commande, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", total));
                }
            }
        });

        adresseColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAdresse()));

        // Configuration de la colonne d'actions
        actionsColumn.setCellFactory(param -> new TableCell<Commande, Void>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final Button btnDetails = new Button("Détails");

            {
                btnEdit.getStyleClass().add("button-edit");
                btnDelete.getStyleClass().add("button-delete");
                btnDetails.getStyleClass().add("button-details");

                btnEdit.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleEditCommande(commande);
                });

                btnDelete.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleDeleteCommande(commande);
                });

                btnDetails.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleCommandeDetails(commande);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Créer un conteneur pour les boutons
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                    buttons.getChildren().addAll(btnDetails, btnEdit, btnDelete);
                    setGraphic(buttons);
                }
            }
        });

        // Configuration de la TableView
        commandesTable.setItems(commandesList);

        // Charger les commandes
        loadCommandes();
    }

    private void loadCommandes() {
        try {
            List<Commande> commandes = commandeDao.getAllCommandes();
            commandesList.setAll(commandes);
            statusLabel.setText("Total commandes: " + commandes.size());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger les commandes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadCommandes();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadCommandes();
            return;
        }

        // Filtrer les commandes localement
        try {
            List<Commande> allCommandes = commandeDao.getAllCommandes();
            List<Commande> filteredCommandes = allCommandes.stream()
                    .filter(c -> c.getProduitNom().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            c.getClientNom().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            c.getAdresse().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();

            commandesList.setAll(filteredCommandes);
            statusLabel.setText("Résultats: " + filteredCommandes.size() + " commande(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de recherche",
                    "Impossible d'effectuer la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditCommande(Commande commande) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/commande-edit-view.fxml"));
            Parent root = loader.load();

            CommandeEditController controller = loader.getController();
            controller.setCommande(commande);

            Stage stage = new Stage();
            stage.setTitle("Modifier une commande");
            stage.setScene(new Scene(root));

            // Après la fermeture de la fenêtre de modification, actualiser la liste
            stage.setOnHidden(event -> loadCommandes());

            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteCommande(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la commande");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la commande #" + commande.getId() +
                " pour le produit " + commande.getProduitNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = commandeDao.deleteCommande(commande.getId());
                if (success) {
                    loadCommandes();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Suppression réussie",
                            "La commande a été supprimée avec succès.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de suppression",
                            "Impossible de supprimer la commande.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur de suppression",
                        e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleCommandeDetails(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la commande");
        alert.setHeaderText("Commande #" + commande.getId());

        String details = "Client: " + commande.getClientNom() + "\n" +
                "Produit: " + commande.getProduitNom() + "\n" +
                "Quantité: " + commande.getQuantite() + "\n" +
                "Total: " + String.format("%.2f DT", commande.getTotal()) + "\n" +
                "Date: " + commande.getDateCommande().format(dateFormatter) + "\n" +
                "Adresse de livraison: " + commande.getAdresse() + "\n" +
                "Instructions: " + (commande.getDescription() != null ? commande.getDescription() : "Aucune");

        alert.setContentText(details);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}