package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import java.io.IOException;
import java.sql.SQLException;
import javafx.scene.layout.Region;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button togglePasswordButton;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private ImageView loginImageView;
    @FXML private ToggleButton themeToggle;
    @FXML private StackPane rootPane;

    private UtilisateurService utilisateurService;

    @FXML
    public void initialize() {
        try {
            // Initialize fields first
            if (emailField == null || passwordField == null) {
                System.err.println("Warning: Some FXML fields failed to inject");
                return;
            }

            // Try to initialize service
            try {
                utilisateurService = new UtilisateurService();
            } catch (Exception e) {
                System.err.println("Service initialization failed: " + e.getMessage());
                // Continue anyway
            }

            // Setup UI components
            setupPasswordToggle();
            setupValidationListeners();
            setupThemeToggle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPasswordToggle() {
        togglePasswordButton.setOnAction(event -> {
            if (passwordField.isVisible()) {
                // Show password
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                togglePasswordButton.setText("🔒");
            } else {
                // Hide password
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                togglePasswordButton.setText("👁");
            }
        });
    }

    private void setupThemeToggle() {
        themeToggle.setOnAction(event -> {
            if (themeToggle.isSelected()) {
                rootPane.getStyleClass().add("dark-theme");
                themeToggle.setText("☀️ Mode Clair");
            } else {
                rootPane.getStyleClass().remove("dark-theme");
                themeToggle.setText("🌙 Mode Sombre");
            }
        });
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
        clearErrorMessages();
        
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Validation Error", "Please fill in all fields");
            return;
        }

        // Try to login
        Utilisateur user = utilisateurService.login(email, password);
        if (user != null) {
            // Login successful
            MainDashboardController.getInstance().setCurrentUser(user);
            loadMainDashboard();
        } else {
            showError("Login Failed", "Invalid email or password");
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
    }

    @FXML
    private void handleSignupLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Signup.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Error", "Error loading signup page: " + e.getMessage());
        }
    }

    private void loadMainDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
        } catch (IOException e) {
            showError("Error", "Error loading dashboard: " + e.getMessage());
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