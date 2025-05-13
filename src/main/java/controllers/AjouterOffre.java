package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Offre;
import services.ServiceOffre;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class AjouterOffre implements Initializable {
    @FXML private TextField tfPoste;
    @FXML private TextField tfTypeContrat;
    @FXML private TextField tfDescription;
    @FXML private TextField tfSalaire;
    @FXML private TextField tfStatut;
    @FXML private TextField tfFormulaire;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation si nécessaire
    }

    @FXML
    void onValiderClicked() {
        if (tfPoste.getText().isEmpty() ||
                tfTypeContrat.getText().isEmpty() ||
                tfDescription.getText().isEmpty() ||
                tfSalaire.getText().isEmpty() ||
                tfStatut.getText().isEmpty() ||
                tfFormulaire.getText().isEmpty()) {

            Alert requiredAlert = new Alert(Alert.AlertType.WARNING);
            requiredAlert.setTitle("Champs manquants");
            requiredAlert.setHeaderText(null);
            requiredAlert.setContentText("Veuillez remplir tous les champs.");
            requiredAlert.showAndWait();
            return;
        }

        try {
            String poste = tfPoste.getText();
            String typeContrat = tfTypeContrat.getText();
            String description = tfDescription.getText();
            String salaire = tfSalaire.getText();
            String statut = tfStatut.getText();
            String formulaire = tfFormulaire.getText();

            Offre newOffer = new Offre(1, poste, typeContrat, description, Double.parseDouble(salaire), new Date(), statut, formulaire);
            ServiceOffre serviceOffre = new ServiceOffre();
            serviceOffre.add(newOffer);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre ajoutée avec succès !");
            
            // Fermer la fenêtre actuelle
            Stage stage = (Stage) tfPoste.getScene().getWindow();
            stage.close();
            
            // Recharger la page des offres
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/Offre.fxml"));
            Parent content = loader.load();
            
            MainDashboardController mainController = MainDashboardController.getInstance();
            if (mainController != null) {
                mainController.changeContent(content, "Gestion des Offres");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout de l'offre : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
