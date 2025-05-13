package controllers;

import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import models.Vehicule;
import java.time.LocalDate;

public class VehiculeFormController {
    @FXML private TextField immatriculationField;
    @FXML private TextField marqueField;
    @FXML private TextField modeleField;
    @FXML private ComboBox<Vehicule.Statut> statutCombo;
    @FXML private ComboBox<Vehicule.Type> typeCombo;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private TextField longueurField;
    @FXML private TextField hauteurField;
    @FXML private TextField largeurField;
    @FXML private TextField poidsField;
    @FXML private Button backButton;
    @FXML private DatePicker dateEntretienPicker;
    @FXML private DatePicker dateVisiteTechniquePicker;
    @FXML private DatePicker dateVidangePicker;
    @FXML private DatePicker dateAssurancePicker;
    @FXML private DatePicker dateVignettePicker;

    private Consumer<Vehicule> onSave;
    private Vehicule vehiculeToEdit;
    private Runnable onBack;

    @FXML
    public void initialize() {
        try {
            statutCombo.getItems().setAll(Vehicule.Statut.values());
            typeCombo.getItems().setAll(Vehicule.Type.values());
        } catch (Exception e) {
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    public void setVehicule(Vehicule vehicule) {
        try {
            this.vehiculeToEdit = vehicule;
            if (vehicule != null) {
                immatriculationField.setText(vehicule.getImmatriculation());
                immatriculationField.setDisable(true);
                marqueField.setText(vehicule.getMarque());
                modeleField.setText(vehicule.getModele());
                statutCombo.setValue(vehicule.getStatut());
                typeCombo.setValue(vehicule.getType());
                if (vehicule.getLongueur() != null) longueurField.setText(String.format("%.2f", vehicule.getLongueur()));
                if (vehicule.getHauteur() != null) hauteurField.setText(String.format("%.2f", vehicule.getHauteur()));
                if (vehicule.getLargeur() != null) largeurField.setText(String.format("%.2f", vehicule.getLargeur()));
                if (vehicule.getPoids() != null) poidsField.setText(String.format("%.2f", vehicule.getPoids()));
                if (vehicule.getDateEntretien() != null) dateEntretienPicker.setValue(vehicule.getDateEntretien());
                if (vehicule.getDateVisiteTechnique() != null) dateVisiteTechniquePicker.setValue(vehicule.getDateVisiteTechnique());
                if (vehicule.getDateVidange() != null) dateVidangePicker.setValue(vehicule.getDateVidange());
                if (vehicule.getDateAssurance() != null) dateAssurancePicker.setValue(vehicule.getDateAssurance());
                if (vehicule.getDateVignette() != null) dateVignettePicker.setValue(vehicule.getDateVignette());
            }
        } catch (Exception e) {
            showError("Erreur lors du chargement du véhicule", e.getMessage());
        }
    }

    public void setOnSave(Consumer<Vehicule> onSave) {
        this.onSave = onSave;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    public void handleSave() {
        try {
            if (!validateForm()) return;
            Double longueur = Double.valueOf(longueurField.getText().replace(',', '.'));
            Double hauteur = Double.valueOf(hauteurField.getText().replace(',', '.'));
            Double largeur = Double.valueOf(largeurField.getText().replace(',', '.'));
            Double poids = Double.valueOf(poidsField.getText().replace(',', '.'));
            if (longueur <= 0 || hauteur <= 0 || largeur <= 0 || poids <= 0) {
                showError("Dimensions invalides", "Les dimensions et le poids doivent être supérieurs à 0.");
                return;
            }
            Vehicule v = new Vehicule(
                immatriculationField.getText(),
                marqueField.getText(),
                modeleField.getText(),
                statutCombo.getValue(),
                typeCombo.getValue(),
                longueur,
                hauteur,
                largeur,
                poids,
                dateEntretienPicker.getValue(),
                dateVisiteTechniquePicker.getValue(),
                dateVidangePicker.getValue(),
                dateAssurancePicker.getValue(),
                dateVignettePicker.getValue()
            );
            if (onSave != null) {
                onSave.accept(v);
                closeWindow();
            }
        } catch (Exception e) {
            showError("Erreur lors de la sauvegarde", e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        if (onBack != null) {
            onBack.run();
        } else {
            closeWindow();
        }
    }

    @FXML
    public void handleBack() {
        if (onBack != null) {
            onBack.run();
        } else {
            closeWindow();
        }
    }

    private boolean validateForm() {
        if (immatriculationField.getText().isEmpty() || marqueField.getText().isEmpty() || modeleField.getText().isEmpty() ||
            statutCombo.getValue() == null || typeCombo.getValue() == null ||
            longueurField.getText().isEmpty() || hauteurField.getText().isEmpty() || largeurField.getText().isEmpty() || poidsField.getText().isEmpty()) {
            showError("Champs requis", "Tous les champs sont obligatoires.");
            return false;
        }

        if (!immatriculationField.getText().matches("^\\d{3}TUN\\d{1,4}$")) {
            showError("Format immatriculation invalide", 
                "L'immatriculation doit être sous la forme 123TUN4, 123TUN45, 123TUN456 ou 123TUN4567 (3 chiffres, TUN, 1 à 4 chiffres).");
            return false;
        }

        try {
            Double.valueOf(longueurField.getText().replace(',', '.'));
            Double.valueOf(hauteurField.getText().replace(',', '.'));
            Double.valueOf(largeurField.getText().replace(',', '.'));
            Double.valueOf(poidsField.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            showError("Format invalide", "Longueur, hauteur, largeur et poids doivent être des nombres valides.");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        try {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 