package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Offre;
import services.ServiceOffre;

import java.net.URL;
import java.util.ResourceBundle;

public class ModifierOffre implements Initializable {
    @FXML private TextField tfPoste;
    @FXML private TextField tfTypeContrat;
    @FXML private TextField tfDescription;
    @FXML private TextField tfSalaire;
    @FXML private TextField tfStatut;
    @FXML private TextField tfFormulaire;

    private Offre selectedOffer;
    private final ServiceOffre service = new ServiceOffre();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation si nécessaire
    }

    public void setOffer(Offre offer) {
        this.selectedOffer = offer;
        tfPoste.setText(offer.getPoste());
        tfTypeContrat.setText(offer.getType_contrat());
        tfDescription.setText(offer.getDescription());
        tfSalaire.setText(String.valueOf(offer.getSalaire()));
        tfStatut.setText(offer.getStatut());
        tfFormulaire.setText(offer.getFormulaire_candidature());
    }

    @FXML
    private void onModifierClicked() {
        if (selectedOffer == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune offre sélectionnée");
            return;
        }

        try {
            selectedOffer.setPoste(tfPoste.getText());
            selectedOffer.setType_contrat(tfTypeContrat.getText());
            selectedOffer.setDescription(tfDescription.getText());
            selectedOffer.setSalaire(Double.parseDouble(tfSalaire.getText()));
            selectedOffer.setStatut(tfStatut.getText());
            selectedOffer.setFormulaire_candidature(tfFormulaire.getText());

            service.update(selectedOffer);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Offre modifiée avec succès !");

            // Close popup
            Stage stage = (Stage) tfPoste.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification : " + e.getMessage());
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
