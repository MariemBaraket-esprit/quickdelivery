package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import models.Utilisateur;
import models.Produit;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ClientProduitDetailsIntegratedController {

    @FXML
    private Label lblNomProduit;

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

    @FXML
    private Button btnCommander;

    @FXML
    private Button btnRetour;

    private Produit produit;
    private Utilisateur utilisateur;
    private ClientController clientController;
    private ClientProduitsController produitsController;

    @FXML
    private void initialize() {
        System.out.println("Initialisation de ClientProduitDetailsIntegratedController");
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

    public void setProduitsController(ClientProduitsController produitsController) {
        this.produitsController = produitsController;
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
        btnCommander.setDisable(produit.getStock() <= 0);
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
            // Charger le formulaire de commande intégré
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-commande-form-integrated-view.fxml"));
            Parent commandeForm = loader.load();

            ClientCommandeFormIntegratedController controller = loader.getController();
            controller.setClientController(clientController);
            controller.setUtilisateur(utilisateur);
            controller.setProduitCommande(produit, quantite);
            controller.setProduitsController(produitsController);

            // Remplacer le contenu principal par le formulaire de commande
            BorderPane mainContainer = (BorderPane) btnCommander.getScene().getRoot();
            mainContainer.setCenter(commandeForm);

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire de commande: " + e.getMessage());
            e.printStackTrace();
            clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                    "Impossible d'ouvrir le formulaire de commande: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        // Revenir à la liste des produits
        if (produitsController != null) {
            produitsController.retourListeProduits();
        }
    }
}
