package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.ProduitDao;
import models.Produit;

import java.sql.SQLException;

public class ProduitEditController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtCategorie;

    @FXML
    private TextField txtPrix;

    @FXML
    private TextField txtStock;

    @FXML
    private TextField txtTaille;

    @FXML
    private Button btnSauvegarder;

    @FXML
    private Button btnAnnuler;

    private ProduitDao produitDao;
    private Produit produit;

    @FXML
    private void initialize() {
        produitDao = new ProduitDao();
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        populateFields();
    }

    private void populateFields() {
        if (produit != null) {
            txtNom.setText(produit.getNom());
            txtDescription.setText(produit.getDescription());
            txtCategorie.setText(produit.getCategorie());
            txtPrix.setText(String.valueOf(produit.getPrix()));
            txtStock.setText(String.valueOf(produit.getStock()));
            txtTaille.setText(String.valueOf(produit.getTaille()));
        }
    }

    @FXML
    private void handleSauvegarder() {
        if (validateInputs()) {
            try {
                produit.setNom(txtNom.getText());
                produit.setDescription(txtDescription.getText());
                produit.setCategorie(txtCategorie.getText());
                produit.setPrix(Double.parseDouble(txtPrix.getText()));
                produit.setStock(Integer.parseInt(txtStock.getText()));
                produit.setTaille(Integer.parseInt(txtTaille.getText()));

                boolean success = produitDao.update(produit);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit modifié", "Le produit a été modifié avec succès.");
                    closeStage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de modification", "Impossible de modifier le produit.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Format invalide", "Veuillez entrer des valeurs numériques valides pour le prix, le stock et la taille.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur de modification", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        closeStage();
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (txtNom.getText().trim().isEmpty()) {
            errorMessage.append("Le nom du produit ne peut pas être vide.\n");
        }

        if (txtCategorie.getText().trim().isEmpty()) {
            errorMessage.append("La catégorie ne peut pas être vide.\n");
        }

        try {
            double prix = Double.parseDouble(txtPrix.getText());
            if (prix <= 0) {
                errorMessage.append("Le prix doit être supérieur à 0.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Le prix doit être un nombre valide.\n");
        }

        try {
            int stock = Integer.parseInt(txtStock.getText());
            if (stock < 0) {
                errorMessage.append("Le stock ne peut pas être négatif.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Le stock doit être un nombre entier valide.\n");
        }

        try {
            int taille = Integer.parseInt(txtTaille.getText());
            if (taille <= 0) {
                errorMessage.append("La taille doit être supérieure à 0.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("La taille doit être un nombre entier valide.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Veuillez corriger les erreurs suivantes:", errorMessage.toString());
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeStage() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }
}