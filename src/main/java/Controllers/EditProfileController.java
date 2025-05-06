package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import java.sql.SQLException;

public class EditProfileController {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextArea adresseField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorMessage;

    private Utilisateur currentUser;
    private UtilisateurService utilisateurService;
    private Runnable onProfileUpdated;

    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    public void initData(Utilisateur user) {
        this.currentUser = user;
        populateFields();
    }

    public void setOnProfileUpdated(Runnable callback) {
        this.onProfileUpdated = callback;
    }

    private void populateFields() {
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            telephoneField.setText(currentUser.getTelephone());
            adresseField.setText(currentUser.getAdresse());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try {
            updateUser();
            if (onProfileUpdated != null) {
                onProfileUpdated.run();
            }
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour du profil: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty() ||
                prenomField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                telephoneField.getText().trim().isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires");
            return false;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Format d'email invalide");
            return false;
        }

        if (!telephoneField.getText().matches("^[0-9+]+$")) {
            showError("Format de téléphone invalide");
            return false;
        }

        if (!passwordField.getText().isEmpty()) {
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showError("Les mots de passe ne correspondent pas");
                return false;
            }
            if (passwordField.getText().length() < 6) {
                showError("Le mot de passe doit contenir au moins 6 caractères");
                return false;
            }
        }

        return true;
    }

    private void updateUser() throws SQLException {
        currentUser.setNom(nomField.getText().trim());
        currentUser.setPrenom(prenomField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setTelephone(telephoneField.getText().trim());
        currentUser.setAdresse(adresseField.getText().trim());

        if (!passwordField.getText().isEmpty()) {
            currentUser.setPassword(passwordField.getText());
        }

        utilisateurService.modifierUtilisateur(currentUser);
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    @FXML
    private void handleCancel() {
        if (onProfileUpdated != null) {
            onProfileUpdated.run();
        }
    }

    @FXML
    private void handleBack() {
        if (onProfileUpdated != null) {
            onProfileUpdated.run();
        }
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}