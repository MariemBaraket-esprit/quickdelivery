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

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ClientProduitDetailsController {

    @FXML
    private Label lblNomProduit;

    // Utilisez le nom qui existe réellement dans votre FXML
    @FXML
    private Button btnAddToCart;  // Gardez le nom original qui est dans le FXML

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
        System.out.println("Initialisation de ClientProduitDetailsController");
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

        // Désactiver le bouton si le stock est épuisé
        btnAddToCart.setDisable(produit.getStock() <= 0);
        btnAddToCart.setText("Commander"); // Changer le texte du bouton à "Commander"
    }

    @FXML
    private void handleAddToCart() {  // Gardez le nom de la méthode qui est dans le FXML
        // Cette méthode sera appelée quand le bouton est cliqué
        handleCommander();
    }

    @FXML
    private void handleCommander() {
        int quantite = spinnerQuantite.getValue();

        if (quantite <= 0) {
            clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité invalide",
                    "La quantité doit être supérieure à 0.");
            return;
        }

        if (quantite > produit.getStock()) {
            clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Stock insuffisant",
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
            System.err.println("Erreur lors de l'ouverture du formulaire de commande: " + e.getMessage());
            e.printStackTrace();
            clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir le formulaire de commande: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblNomProduit.getScene().getWindow();
        stage.close();
    }
}