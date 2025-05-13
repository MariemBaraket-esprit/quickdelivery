package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import models.Utilisateur;
import services.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class AjouterReclamationController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> typeReclamationComboBox;
    @FXML private TextArea descriptionField;
    @FXML private TextField creationrecField;

    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label typeError;
    @FXML private Label descriptionError;

    private Pane contentArea;

    public void setContentArea(Pane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        typeReclamationComboBox.getItems().setAll(
                "Delivery Issue", "Damaged Product", "Wrong Address", "Late Delivery"
        );

        Utilisateur u = MainDashboardController.getInstance().currentUser;
        if (u != null) {
            nomField.setText(u.getNom());
            prenomField.setText(u.getPrenom());
            emailField.setText(u.getEmail());
        }
        creationrecField.setText(LocalDateTime.now().toString());
    }

    @FXML
    private void handSaveReclamation() {
        clearErrors();

        String type = typeReclamationComboBox.getValue();
        String description = descriptionField.getText().trim();

        boolean valid = true;
        if (type == null)          { typeError.setText("Sélectionnez un type.");      valid = false; }
        if (description.isEmpty()) { descriptionError.setText("Description requise."); valid = false; }
        if (!valid) return;

        try (Connection conn = DataBaseConnection.getConnection()) {
            String sql = """
            INSERT INTO reclamations
              (id_utilisateur, type, status, description, date_creation, priority)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, MainDashboardController.getInstance().currentUser.getIdUser());
                stmt.setString(2, type);
                stmt.setString(3, "PENDING");
                stmt.setString(4, description);
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(6, "MEDIUM");

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    showSuccess("Réclamation ajoutée avec succès!");
                    clearFields();
                } else {
                    showError("Erreur lors de l'ajout de la réclamation.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur : " + e.getMessage());
        }
    }

    private void clearFields() {
        typeReclamationComboBox.getSelectionModel().clearSelection();
        descriptionField.clear();
    }

    private void clearErrors() {
        nomError.setText("");
        prenomError.setText("");
        emailError.setText("");
        typeError.setText("");
        descriptionError.setText("");
    }

    private void showSuccess(String msg) { 
        descriptionError.setTextFill(Color.GREEN); 
        descriptionError.setText(msg); 
    }
    
    private void showError(String msg) { 
        descriptionError.setTextFill(Color.RED);   
        descriptionError.setText(msg); 
    }
} 