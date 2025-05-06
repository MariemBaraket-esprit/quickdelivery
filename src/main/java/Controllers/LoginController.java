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
import java.net.URL;
import javafx.scene.layout.Region;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private Label emailError;
    @FXML
    private Label passwordError;

    private UtilisateurService utilisateurService;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();

            // Setup password toggle functionality
            setupPasswordToggle();

            // Add listeners for real-time validation
            setupValidationListeners();
        } catch (SQLException e) {
            showError("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    private void setupPasswordToggle() {
        // Bind text properties bidirectionally
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());

        // Set initial state
        passwordField.setManaged(true);
        passwordField.setVisible(true);
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);
        togglePasswordButton.setText("👁");

        // Make sure the PasswordField is the one showing initially
        if (!passwordField.isVisible()) {
            togglePasswordVisibility();
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            // Switch to TextField (show password)
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            togglePasswordButton.setText("🔒");
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            // Switch to PasswordField (hide password)
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordButton.setText("👁");
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    private void setupValidationListeners() {
        // Email field listener
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateEmail();
            updateFieldStyle(emailField, emailError.isVisible());
        });

        // Password field listener
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword();
            updateFieldStyle(passwordField, passwordError.isVisible());
        });

        // Add listener for passwordTextField as well
        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword();
            updateFieldStyle(passwordTextField, passwordError.isVisible());
        });
    }

    private void updateFieldStyle(TextField field, boolean hasError) {
        if (hasError) {
            field.getStyleClass().add("field-error");
        } else {
            field.getStyleClass().remove("field-error");
        }
    }

    @FXML
    private void handleLogin() {
        // Clear previous error states
        clearErrorMessages();

        // Collect all validation errors
        StringBuilder errorMessage = new StringBuilder("Veuillez corriger les erreurs suivantes:\n\n");
        boolean hasErrors = false;

        // Validate email
        if (!validateEmail()) {
            errorMessage.append("- ").append(emailError.getText()).append("\n");
            hasErrors = true;
        }

        // Validate password
        if (!validatePassword()) {
            errorMessage.append("- ").append(passwordError.getText()).append("\n");
            hasErrors = true;
        }

        if (hasErrors) {
            // Show all validation errors in a single alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Des erreurs ont été détectées");
            alert.setContentText(errorMessage.toString());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
            return;
        }

        // If validation passes, attempt login
        try {
            String email = emailField.getText().trim();
            String password = passwordField.isVisible() ? passwordField.getText().trim() : passwordTextField.getText().trim();

            Utilisateur user = utilisateurService.authenticate(email, password);
            if (user != null) {
                // Show success message before redirecting
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Connexion réussie");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Bienvenue " + user.getEmail() + " !");
                successAlert.showAndWait();

                openMainDashboard(user);
            } else {
                showError("Erreur d'authentification", "Email ou mot de passe incorrect.\nVeuillez vérifier vos informations et réessayer.");
                // Clear password field for security
                passwordField.clear();
                passwordTextField.clear();
            }
        } catch (SQLException e) {
            showError("Erreur de connexion", "Une erreur est survenue lors de la tentative de connexion:\n" + e.getMessage());
        }
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

    private boolean validatePassword() {
        String password = passwordField.getText().trim();
        boolean isValid = true;

        if (password.isEmpty()) {
            passwordError.setText("Le mot de passe est requis");
            passwordError.setVisible(true);
            isValid = false;
        } else {
            passwordError.setVisible(false);
        }

        return isValid;
    }

    private void clearErrorMessages() {
        emailError.setVisible(false);
        passwordError.setVisible(false);

        emailField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().remove("field-error");
        passwordTextField.getStyleClass().remove("field-error");
    }

    @FXML
    private void handleSignupLink() {
        try {
            // Get the URL of the FXML file
            URL signupFxml = getClass().getResource("/fxml/Signup.fxml");
            if (signupFxml == null) {
                throw new IOException("Cannot find /fxml/Signup.fxml");
            }

            // Create and configure the loader
            FXMLLoader loader = new FXMLLoader(signupFxml);

            // Load the FXML
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Create and set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Update the window title
            stage.setTitle("Inscription");

        } catch (IOException e) {
            System.err.println("Error loading Signup.fxml: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Erreur lors de l'ouverture de la page d'inscription: " + e.getMessage());
        }
    }
    // Dans LoginController.java, modifiez la méthode openMainDashboard
    private void openMainDashboard(Utilisateur user) {
        try {
            System.out.println("Type d'utilisateur: " + user.getTypeUtilisateur());

            String fxmlPath;
            if ("CLIENT".equalsIgnoreCase(user.getTypeUtilisateur())) {
                fxmlPath = "/fxml/ClientDashboard.fxml";
                System.out.println("Redirection vers le dashboard client: " + fxmlPath);

                URL dashboardFxml = getClass().getResource(fxmlPath);
                if (dashboardFxml == null) {
                    System.out.println("ERREUR: Impossible de trouver le fichier FXML: " + fxmlPath);
                    throw new IOException("Cannot find " + fxmlPath);
                }

                FXMLLoader loader = new FXMLLoader(dashboardFxml);
                Parent root = loader.load();

                ClientDashboardController controller = loader.getController();
                controller.initData(user);

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
            } else {
                fxmlPath = "/fxml/MainDashboard.fxml";
                System.out.println("Redirection vers le dashboard principal: " + fxmlPath);

                URL dashboardFxml = getClass().getResource(fxmlPath);
                if (dashboardFxml == null) {
                    System.out.println("ERREUR: Impossible de trouver le fichier FXML: " + fxmlPath);
                    throw new IOException("Cannot find " + fxmlPath);
                }

                FXMLLoader loader = new FXMLLoader(dashboardFxml);
                Parent root = loader.load();

                MainDashboardController controller = loader.getController();
                controller.initData(user);

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
            }
        } catch (Exception e) {
            System.err.println("Erreur détaillée: " + e);
            e.printStackTrace();
            showError("Erreur", "Erreur lors de l'ouverture du tableau de bord: " + e.getMessage());
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
}