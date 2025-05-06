package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import java.io.IOException;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class SignupController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField adresseField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> typeUtilisateurCombo;
    @FXML
    private Label nomError;
    @FXML
    private Label prenomError;
    @FXML
    private Label emailError;
    @FXML
    private Label telephoneError;
    @FXML
    private Label adresseError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confirmPasswordError;
    @FXML
    private Label typeError;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private Button toggleConfirmPasswordButton;

    private TextField passwordTextField;
    private TextField confirmPasswordTextField;
    private UtilisateurService utilisateurService;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();

            // Initialize type utilisateur combo box
            typeUtilisateurCombo.setItems(FXCollections.observableArrayList(
                    "CLIENT",
                    "RESPONSABLE",
                    "MAGASINIER",
                    "LIVREUR"
            ));

            // Initialize tooltips
            nomField.setTooltip(new Tooltip("Votre nom de famille"));
            prenomField.setTooltip(new Tooltip("Votre prénom"));
            emailField.setTooltip(new Tooltip("Format: exemple@domaine.com"));
            telephoneField.setTooltip(new Tooltip("Exactement 8 chiffres"));
            adresseField.setTooltip(new Tooltip("Entrez votre adresse complète"));

            // Initialize password text fields
            setupPasswordFields();

            // Add real-time validation listeners
            setupValidationListeners();
        } catch (SQLException e) {
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    private void setupPasswordFields() {
        // Create text fields for showing passwords
        passwordTextField = new TextField();
        confirmPasswordTextField = new TextField();

        // Copy styles and properties
        passwordTextField.getStyleClass().addAll(passwordField.getStyleClass());
        confirmPasswordTextField.getStyleClass().addAll(confirmPasswordField.getStyleClass());

        passwordTextField.promptTextProperty().bind(passwordField.promptTextProperty());
        confirmPasswordTextField.promptTextProperty().bind(confirmPasswordField.promptTextProperty());

        // Bind text properties bidirectionally
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(confirmPasswordTextField.textProperty());

        // Hide text fields initially
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
        confirmPasswordTextField.setManaged(false);
        confirmPasswordTextField.setVisible(false);

        // Add text fields to the parent HBox
        ((HBox) passwordField.getParent()).getChildren().add(1, passwordTextField);
        ((HBox) confirmPasswordField.getParent()).getChildren().add(1, confirmPasswordTextField);
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordField.setManaged(!passwordField.isManaged());
        passwordField.setVisible(!passwordField.isVisible());
        passwordTextField.setManaged(!passwordTextField.isManaged());
        passwordTextField.setVisible(!passwordTextField.isVisible());
        togglePasswordButton.setText(passwordField.isVisible() ? "👁" : "🔒");
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordField.setManaged(!confirmPasswordField.isManaged());
        confirmPasswordField.setVisible(!confirmPasswordField.isVisible());
        confirmPasswordTextField.setManaged(!confirmPasswordTextField.isManaged());
        confirmPasswordTextField.setVisible(!confirmPasswordTextField.isVisible());
        toggleConfirmPasswordButton.setText(confirmPasswordField.isVisible() ? "👁" : "🔒");
    }

    private void setupValidationListeners() {
        // Nom validation listener
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNom();
            updateFieldStyle(nomField, nomError.isVisible());
        });

        // Prenom validation listener
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePrenom();
            updateFieldStyle(prenomField, prenomError.isVisible());
        });

        // Email validation listener
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateEmail();
            updateFieldStyle(emailField, emailError.isVisible());
        });

        // Phone validation listener
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                telephoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 8) {
                telephoneField.setText(oldValue);
            }
            validatePhone();
            updateFieldStyle(telephoneField, telephoneError.isVisible());
        });

        // Address validation listener
        adresseField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateAddress();
            updateFieldStyle(adresseField, adresseError.isVisible());
        });

        // Password validation listener
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isPasswordValid = validatePassword();
            boolean isConfirmValid = validateConfirmPassword();
            updateFieldStyle(passwordField, !isPasswordValid);
            updateFieldStyle(passwordTextField, !isPasswordValid);
            updateFieldStyle(confirmPasswordField, !isConfirmValid);
            updateFieldStyle(confirmPasswordTextField, !isConfirmValid);
        });

        // Confirm password validation listener
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isConfirmValid = validateConfirmPassword();
            updateFieldStyle(confirmPasswordField, !isConfirmValid);
            updateFieldStyle(confirmPasswordTextField, !isConfirmValid);
        });

        // Type utilisateur validation listener
        typeUtilisateurCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateType();
            updateComboBoxStyle(typeUtilisateurCombo, typeError.isVisible());
        });
    }

    private boolean validateNom() {
        String nom = nomField.getText().trim();
        boolean isValid = true;

        if (nom.isEmpty()) {
            nomError.setText("Le nom est requis");
            nomError.setVisible(true);
            isValid = false;
        } else if (nom.length() < 2) {
            nomError.setText("Le nom doit contenir au moins 2 caractères");
            nomError.setVisible(true);
            isValid = false;
        } else if (!nom.matches("[\\p{L} -]+")) {
            nomError.setText("Le nom ne doit contenir que des lettres, espaces et tirets");
            nomError.setVisible(true);
            isValid = false;
        } else {
            nomError.setVisible(false);
        }

        return isValid;
    }

    private boolean validatePrenom() {
        String prenom = prenomField.getText().trim();
        boolean isValid = true;

        if (prenom.isEmpty()) {
            prenomError.setText("Le prénom est requis");
            prenomError.setVisible(true);
            isValid = false;
        } else if (prenom.length() < 2) {
            prenomError.setText("Le prénom doit contenir au moins 2 caractères");
            prenomError.setVisible(true);
            isValid = false;
        } else if (!prenom.matches("[\\p{L} -]+")) {
            prenomError.setText("Le prénom ne doit contenir que des lettres, espaces et tirets");
            prenomError.setVisible(true);
            isValid = false;
        } else {
            prenomError.setVisible(false);
        }

        return isValid;
    }

    private boolean validateAddress() {
        String address = adresseField.getText().trim();
        boolean isValid = true;

        if (address.isEmpty()) {
            adresseError.setText("L'adresse est requise");
            adresseError.setVisible(true);
            isValid = false;
        } else if (address.length() < 5) {
            adresseError.setText("L'adresse doit contenir au moins 5 caractères");
            adresseError.setVisible(true);
            isValid = false;
        } else {
            adresseError.setVisible(false);
        }

        return isValid;
    }

    private void updateFieldStyle(TextField field, boolean hasError) {
        if (hasError) {
            field.getStyleClass().add("field-error");
        } else {
            field.getStyleClass().remove("field-error");
        }
    }

    private void updateComboBoxStyle(ComboBox<?> comboBox, boolean hasError) {
        if (hasError) {
            comboBox.getStyleClass().add("field-error");
        } else {
            comboBox.getStyleClass().remove("field-error");
        }
    }

    @FXML
    private void handleSignup() {
        clearErrorMessages();

        boolean isValid = validateNom() &
                validatePrenom() &
                validateEmail() &
                validatePhone() &
                validateAddress() &
                validatePassword() &
                validateConfirmPassword() &
                validateType();

        if (!isValid) {
            return;
        }

        try {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(nomField.getText().trim());
            utilisateur.setPrenom(prenomField.getText().trim());
            utilisateur.setEmail(emailField.getText().trim());
            utilisateur.setTelephone(telephoneField.getText().trim());
            utilisateur.setAdresse(adresseField.getText().trim());
            utilisateur.setPassword(passwordField.getText());
            utilisateur.setTypeUtilisateur(typeUtilisateurCombo.getValue());
            utilisateur.setStatut("ACTIF");

            utilisateurService.ajouterUtilisateur(utilisateur);
            showSuccess("Inscription réussie", "Votre compte a été créé avec succès.");
            redirectToLogin();
        } catch (SQLException e) {
            showError("Erreur d'inscription", e.getMessage());
        }
    }

    private void clearErrorMessages() {
        nomError.setVisible(false);
        prenomError.setVisible(false);
        emailError.setVisible(false);
        telephoneError.setVisible(false);
        adresseError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
        typeError.setVisible(false);

        nomField.getStyleClass().remove("field-error");
        prenomField.getStyleClass().remove("field-error");
        emailField.getStyleClass().remove("field-error");
        telephoneField.getStyleClass().remove("field-error");
        adresseField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().remove("field-error");
        confirmPasswordField.getStyleClass().remove("field-error");
        typeUtilisateurCombo.getStyleClass().remove("field-error");
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        boolean isValid = true;

        if (email.isEmpty()) {
            emailError.setText("L'email est requis");
            emailError.setVisible(true);
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailError.setText("Format d'email invalide");
            emailError.setVisible(true);
            isValid = false;
        } else {
            emailError.setVisible(false);
        }

        return isValid;
    }

    private boolean validatePhone() {
        String phone = telephoneField.getText().trim();
        boolean isValid = true;

        if (phone.isEmpty()) {
            telephoneError.setText("Le numéro de téléphone est requis");
            telephoneError.setVisible(true);
            isValid = false;
        } else if (phone.length() != 8) {
            telephoneError.setText("Le numéro doit contenir exactement 8 chiffres");
            telephoneError.setVisible(true);
            isValid = false;
        } else if (!phone.matches("\\d{8}")) {
            telephoneError.setText("Le numéro doit contenir uniquement des chiffres");
            telephoneError.setVisible(true);
            isValid = false;
        } else {
            telephoneError.setVisible(false);
            telephoneField.getStyleClass().remove("field-error");
        }

        return isValid;
    }

    private boolean validatePassword() {
        String password = passwordField.getText().trim();
        boolean isValid = true;

        if (password.isEmpty()) {
            passwordError.setText("Le mot de passe est requis");
            passwordError.setVisible(true);
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setText("Le mot de passe doit contenir au moins 6 caractères");
            passwordError.setVisible(true);
            isValid = false;
        } else if (!password.matches(".*[A-Z].*")) {
            passwordError.setText("Le mot de passe doit contenir au moins une majuscule");
            passwordError.setVisible(true);
            isValid = false;
        } else if (!password.matches(".*[a-z].*")) {
            passwordError.setText("Le mot de passe doit contenir au moins une minuscule");
            passwordError.setVisible(true);
            isValid = false;
        } else if (!password.matches(".*\\d.*")) {
            passwordError.setText("Le mot de passe doit contenir au moins un chiffre");
            passwordError.setVisible(true);
            isValid = false;
        } else {
            passwordError.setVisible(false);
            updateFieldStyle(passwordField, false);
            updateFieldStyle(passwordTextField, false);
        }

        return isValid;
    }

    private boolean validateConfirmPassword() {
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        boolean isValid = true;

        if (confirmPassword.isEmpty()) {
            confirmPasswordError.setText("La confirmation du mot de passe est requise");
            confirmPasswordError.setVisible(true);
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
            confirmPasswordError.setVisible(true);
            isValid = false;
        } else {
            confirmPasswordError.setVisible(false);
            isValid = true;
        }

        return isValid;
    }

    private boolean validateType() {
        boolean isValid = true;

        if (typeUtilisateurCombo.getValue() == null) {
            typeError.setText("Le type d'utilisateur est requis");
            typeError.setVisible(true);
            isValid = false;
        } else {
            typeError.setVisible(false);
        }

        return isValid;
    }

    @FXML
    private void handleLoginLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du retour à la page de connexion: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void redirectToLogin() {
        handleLoginLink();
    }
}