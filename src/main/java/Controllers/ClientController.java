package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Produit;
import models.Utilisateur;

import java.io.IOException;

public class ClientController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Label lblClientInfo;

    @FXML
    private Circle userAvatar;

    @FXML
    private Circle userStatusIndicator;

    @FXML
    private MenuButton userMenuButton;

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
                    lblClientInfo.setText(prenom + " " + nom);
                }

                if (userStatusIndicator != null) {
                    String statut = utilisateur.getStatut() != null ? utilisateur.getStatut() : "Actif";
                    userStatusIndicator.setStyle("-fx-fill: " + ("Actif".equals(statut) ? "#2ecc71" : "#e74c3c"));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des informations client: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Ajouter cette méthode à votre classe ClientController existante
    public void showProductDetails(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-produit-details-integrated-view.fxml"));
            Parent root = loader.load();

            ClientProduitDetailsController controller = loader.getController();
            controller.setClientController(this);
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Détails du produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir les détails du produit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleProduits() {
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

            // Assurez-vous que le contrôleur charge les commandes
            controller.loadCommandes();

            mainContainer.setCenter(commandesView);
            System.out.println("Vue des commandes client chargée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue des commandes client: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger la vue des commandes: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        // Implémenter la gestion du profil utilisateur
        showAlert(Alert.AlertType.INFORMATION, "Profil", "Profil utilisateur",
                "Fonctionnalité de profil à implémenter.");
    }

    @FXML
    private void handleLogout() {
        // Implémenter la déconnexion
        showAlert(Alert.AlertType.INFORMATION, "Déconnexion", "Déconnexion",
                "Fonctionnalité de déconnexion à implémenter.");
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
