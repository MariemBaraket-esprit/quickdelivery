package tn.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.services.ProduitDao;
import tn.entities.Produit;

import java.sql.SQLException;
import java.time.LocalDate;

public class ProduitAddController {

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
    private Button btnSauvegarder;

    @FXML
    private Button btnAnnuler;

    private ProduitDao produitDao;

    @FXML
    private void initialize() {
        produitDao = new ProduitDao();
    }

    @FXML
    private void handleSauvegarder() {
        if (validateInputs()) {
            try {
                String nom = txtNom.getText();
                String description = txtDescription.getText();
                String categorie = txtCategorie.getText();
                double prix = Double.parseDouble(txtPrix.getText());
                int stock = Integer.parseInt(txtStock.getText());

                Produit nouveauProduit = new Produit();
                nouveauProduit.setNom(nom);
                nouveauProduit.setDescription(description);
                nouveauProduit.setCategorie(categorie);
                nouveauProduit.setPrix(prix);
                nouveauProduit.setStock(stock);
                nouveauProduit.setDateAjout(LocalDate.now());

                boolean success = produitDao.insert(nouveauProduit);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté", "Le produit a été ajouté avec succès.");
                    closeStage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'ajout", "Impossible d'ajouter le produit.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Format invalide", "Veuillez entrer des valeurs numériques valides pour le prix et le stock.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur d'ajout", e.getMessage());
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