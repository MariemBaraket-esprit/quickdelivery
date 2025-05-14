package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Utilisateur;
import models.Produit;
import services.CommandeDao;
import Controllers.QRCodeGeneratorController;
import Controllers.QRCodeScannerController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ClientProduitDetailsController {

    @FXML
    private Label lblNomProduit;
    @FXML
    private Button btnCommander;
    @FXML
    private Button btnAddToCart;
    @FXML
    private Button btnQRCode; // Nouveau bouton pour le QR code
    @FXML
    private Button btnScanQRCode; // Nouveau bouton pour scanner un QR code

    @FXML
    private Label lblCategorie;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblPrix;

    @FXML
    private Label lblTaille;

    @FXML
    private Label lblStock;

    @FXML
    private Label lblDateAjout;

    @FXML
    private Spinner<Integer> spinnerQuantite;

    private Produit produit;
    private Utilisateur utilisateur;
    private ClientController clientController;

    @FXML
    private void initialize() {
        // Initialiser les boutons QR code
        if (btnQRCode != null) {
            btnQRCode.setOnAction(event -> handleGenerateQRCode());
        }

        if (btnScanQRCode != null) {
            btnScanQRCode.setOnAction(event -> handleScanQRCode());
        }
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        populateFields();
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    private void populateFields() {
        lblNomProduit.setText(produit.getNom());
        lblCategorie.setText(produit.getCategorie());
        lblDescription.setText(produit.getDescription());
        lblPrix.setText(produit.getPrix() + " DT");
        lblTaille.setText(String.valueOf(produit.getTaille()));
        lblStock.setText(String.valueOf(produit.getStock()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDateAjout.setText(produit.getDateAjout().format(formatter));

        // Configurer le spinner de quantité
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, produit.getStock(), 1);
        spinnerQuantite.setValueFactory(valueFactory);
    }

    @FXML
    private void handleGenerateQRCode() {
        try {
            // Charger le FXML du générateur de QR code
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/generer-qr-code.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            QRCodeGeneratorController controller = loader.getController();
            controller.setProduit(produit);

            // Créer et configurer la fenêtre
            Stage stage = new Stage();
            stage.setTitle("Générer QR Code");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir le générateur de QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleScanQRCode() {
        try {
            // Charger le FXML du scanner de QR code
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/scanner-qr-code.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            QRCodeScannerController controller = loader.getController();
            controller.setClientController(clientController);

            // Créer et configurer la fenêtre
            Stage stage = new Stage();
            stage.setTitle("Scanner QR Code");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir le scanner de QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCommander() {
        int quantite = spinnerQuantite.getValue();

        if (quantite <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité invalide",
                    "La quantité doit être supérieure à 0.");
            return;
        }

        if (quantite > produit.getStock()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Stock insuffisant",
                    "Il n'y a que " + produit.getStock() + " unités en stock.");
            return;
        }

        try {
            // Fermer la fenêtre de détails du produit
            Stage currentStage = (Stage) lblNomProduit.getScene().getWindow();
            currentStage.close();

            // Ouvrir le formulaire de commande
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-commande-form-view.fxml"));
            Parent root = loader.load();

            ClientCommandeFormController controller = loader.getController();
            controller.setClientController(clientController);
            controller.setUtilisateur(utilisateur);

            // Passer le produit et la quantité au formulaire de commande
            controller.setProduitCommande(produit, quantite);

            Stage stage = new Stage();
            stage.setTitle("Formulaire de commande");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir le formulaire de commande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblNomProduit.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        if (clientController != null) {
            clientController.showAlert(type, title, header, content);
        } else {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
}