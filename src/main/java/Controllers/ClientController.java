package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import models.Utilisateur;

import java.io.IOException;

public class ClientController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Label lblClientInfo;

    @FXML
    private Label lblClientStatus;

    @FXML
    private Button btnProduits;

    @FXML
    private Button btnCommandes;

    private Utilisateur utilisateur;

    @FXML
    private void initialize() {
        System.out.println("Initialisation du ClientController");
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        System.out.println("Utilisateur défini dans ClientController: " +
                (utilisateur != null ? "ID=" + utilisateur.getIdUser() : "null"));

        // Vérifier si nous sommes dans un contexte UI avant de mettre à jour l'interface
        if (isUIInitialized()) {
            // Mettre à jour les informations du client dans l'interface
            updateClientInfo();

            // Charger la vue des produits par défaut
            handleProduits();
        }
    }

    private boolean isUIInitialized() {
        // Vérifier si les éléments UI sont initialisés
        boolean initialized = mainContainer != null;
        System.out.println("UI initialisée: " + initialized);
        return initialized;
    }

    private void updateClientInfo() {
        try {
            if (utilisateur != null && isUIInitialized()) {
                if (lblClientInfo != null) {
                    String prenom = utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "";
                    String nom = utilisateur.getNom() != null ? utilisateur.getNom() : "";
                    lblClientInfo.setText(prenom + " " + nom + " - Client");
                }

                if (lblClientStatus != null) {
                    String statut = utilisateur.getStatut() != null ? utilisateur.getStatut() : "Actif";
                    lblClientStatus.setText(statut);
                    lblClientStatus.getStyleClass().clear();
                    lblClientStatus.getStyleClass().add("Actif".equals(statut) ? "client-status-active" : "client-status-inactive");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des informations client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProduits() {
        if (!isUIInitialized()) {
            System.out.println("UI non initialisée, impossible de charger les produits");
            return;
        }

        try {
            System.out.println("Chargement de la vue des produits");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-produits-view.fxml"));
            Parent produitsView = loader.load();

            ClientProduitsController controller = loader.getController();
            controller.setClientController(this);
            controller.setUtilisateur(utilisateur);

            mainContainer.setCenter(produitsView);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue des produits: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger la vue des produits: " + e.getMessage());
        }
    }
    @FXML
    public void handleCommandes() {
        try {
            System.out.println("Chargement de la vue des commandes client");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-commandes-view.fxml"));
            Parent commandesView = loader.load();

            ClientCommandesController controller = loader.getController();
            controller.setClientController(this);
            controller.setUtilisateur(utilisateur);

            mainContainer.setCenter(commandesView);
            System.out.println("Vue des commandes client chargée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue des commandes client: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger la vue des commandes: " + e.getMessage());
        }
    }

    public void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        try {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage de l'alerte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
}