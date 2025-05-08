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
import javafx.scene.layout.HBox;
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
    private ListView<Commande> commandesListView;

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
        try {
            commandeDao = new CommandeDao();
            System.out.println("Initialisation du contrôleur des commandes");

            // Configuration de la ListView
            commandesListView.setItems(commandesList);
            commandesListView.setCellFactory(lv -> new CommandeListCell(this));

            // Définir une hauteur fixe pour chaque cellule
            commandesListView.setFixedCellSize(180);

            // Charger les commandes
            loadCommandes();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du contrôleur des commandes: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation",
                    "Erreur lors de l'initialisation de la vue des commandes",
                    "Détails: " + e.getMessage());
        }
    }

    private void loadCommandes() {
        try {
            List<Commande> commandes = commandeDao.getAllCommandes();
            commandesList.setAll(commandes);
            statusLabel.setText("Total commandes: " + commandes.size());
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des commandes: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger les commandes: " + e.getMessage());
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

    public void handleEditCommande(Commande commande) {
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
            System.err.println("Erreur lors de l'ouverture de la fenêtre de modification: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    public void handleDeleteCommande(Commande commande) {
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
                System.err.println("Erreur SQL lors de la suppression: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur de suppression",
                        e.getMessage());
            }
        }
    }

    public void handleCommandeDetails(Commande commande) {
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