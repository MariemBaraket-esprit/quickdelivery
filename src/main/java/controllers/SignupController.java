package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.prefs.Preferences;
import java.time.LocalDate;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import models.Utilisateur;
import services.UtilisateurService;

public class SignupController {
    @FXML
    private StackPane rootPane;
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
    private Button togglePasswordButton;
    @FXML
    private Button toggleConfirmPasswordButton;
    @FXML
    private StackPane imageCard;
    @FXML
    private VBox signupForm;
    @FXML
    private ImageView signupImage;
    @FXML
    private ToggleButton themeToggle;

    private TextField passwordTextField;
    private TextField confirmPasswordTextField;
    private UtilisateurService utilisateurService;
    private static final String PREF_THEME = "theme";
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private Preferences prefs = Preferences.userNodeForPackage(SignupController.class);

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            
            // Initialize tooltips
            setupTooltips();
            
            // Initialize password text fields
            setupPasswordFields();

            // Add real-time validation listeners
            setupValidationListeners();

            // Setup image and card height binding
            setupImageAndCard();

            // Theme toggle logic
            setupThemeToggle();

            // Configure window for fullscreen
            setupWindow();
            
        } catch (SQLException e) {
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    private void setupTooltips() {
        nomField.setTooltip(new Tooltip("Votre nom de famille"));
        prenomField.setTooltip(new Tooltip("Votre prénom"));
        emailField.setTooltip(new Tooltip("Format: exemple@domaine.com"));
        telephoneField.setTooltip(new Tooltip("Exactement 8 chiffres"));
        adresseField.setTooltip(new Tooltip("Entrez votre adresse complète"));
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
    }

    private boolean validateNom() {
        String nom = nomField.getText().trim();
        boolean isValid = true;

        if (nom.isEmpty()) {
            nomError.setText("Ce champ doit être rempli");
            nomError.setVisible(true);
            nomField.getStyleClass().add("field-error");
            isValid = false;
        } else if (nom.length() < 2) {
            nomError.setText("Le nom doit contenir au moins 2 caractères");
            nomError.setVisible(true);
            nomField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!nom.matches("[\\p{L} -]+")) {
            nomError.setText("Le nom ne doit contenir que des lettres, espaces et tirets");
            nomError.setVisible(true);
            nomField.getStyleClass().add("field-error");
            isValid = false;
        } else {
            nomError.setVisible(false);
            nomField.getStyleClass().remove("field-error");
        }

        return isValid;
    }

    private boolean validatePrenom() {
        String prenom = prenomField.getText().trim();
        boolean isValid = true;

        if (prenom.isEmpty()) {
            prenomError.setText("Ce champ doit être rempli");
            prenomError.setVisible(true);
            prenomField.getStyleClass().add("field-error");
            isValid = false;
        } else if (prenom.length() < 2) {
            prenomError.setText("Le prénom doit contenir au moins 2 caractères");
            prenomError.setVisible(true);
            prenomField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!prenom.matches("[\\p{L} -]+")) {
            prenomError.setText("Le prénom ne doit contenir que des lettres, espaces et tirets");
            prenomError.setVisible(true);
            prenomField.getStyleClass().add("field-error");
            isValid = false;
        } else {
            prenomError.setVisible(false);
            prenomField.getStyleClass().remove("field-error");
        }

        return isValid;
    }

    private boolean validateAddress() {
        String address = adresseField.getText().trim();
        boolean isValid = true;

        if (address.isEmpty()) {
            adresseError.setText("Ce champ doit être rempli");
            adresseError.setVisible(true);
            adresseField.getStyleClass().add("field-error");
            isValid = false;
        } else if (address.length() < 5) {
            adresseError.setText("L'adresse doit contenir au moins 5 caractères");
            adresseError.setVisible(true);
            adresseField.getStyleClass().add("field-error");
            isValid = false;
        } else {
            adresseError.setVisible(false);
            adresseField.getStyleClass().remove("field-error");
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

    @FXML
    private void handleSignup() {
        clearErrorMessages();
        
        if (!validateInput()) {
            return;
        }
        
        try {
            // Vérifier si l'email existe déjà
            if (utilisateurService.emailExists(emailField.getText().trim())) {
                emailError.setText("Cet email est déjà utilisé");
                return;
            }
            
            // Vérifier si le téléphone existe déjà
            if (utilisateurService.telephoneExists(telephoneField.getText().trim())) {
                telephoneError.setText("Ce numéro de téléphone est déjà utilisé");
                return;
            }
            
            // Créer l'utilisateur
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(nomField.getText().trim());
            utilisateur.setPrenom(prenomField.getText().trim());
            utilisateur.setEmail(emailField.getText().trim());
            utilisateur.setTelephone(telephoneField.getText().trim());
            utilisateur.setAdresse(adresseField.getText().trim());
            utilisateur.setPassword(passwordField.getText());
            utilisateur.setTypeUtilisateur("CLIENT");
            utilisateur.setStatut("ACTIF");
            utilisateur.setDateDebutContrat(LocalDate.now());
            
            // Enregistrer l'utilisateur
            utilisateurService.ajouterUtilisateur(utilisateur);
            
            // Afficher un message de succès
            showSuccess("Inscription réussie", "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");
            
            // Rediriger vers la page de connexion
            handleLoginLink();
            
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean isValid = true;
        
        // Validation du nom
        if (nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est requis");
            isValid = false;
        }
        
        // Validation du prénom
        if (prenomField.getText().trim().isEmpty()) {
            prenomError.setText("Le prénom est requis");
            isValid = false;
        }
        
        // Validation de l'email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("L'email est requis");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailError.setText("Format d'email invalide");
            isValid = false;
        }
        
        // Validation du téléphone
        String telephone = telephoneField.getText().trim();
        if (telephone.isEmpty()) {
            telephoneError.setText("Le téléphone est requis");
            isValid = false;
        } else if (!telephone.matches("^[0-9]{8}$")) {
            telephoneError.setText("Le numéro doit contenir 8 chiffres");
            isValid = false;
        }
        
        // Validation de l'adresse
        if (adresseField.getText().trim().isEmpty()) {
            adresseError.setText("L'adresse est requise");
            isValid = false;
        }
        
        // Validation du mot de passe
        String password = passwordField.getText();
        if (password.isEmpty()) {
            passwordError.setText("Le mot de passe est requis");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setText("Le mot de passe doit contenir au moins 6 caractères");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*[0-9].*")) {
            passwordError.setText("Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre");
            isValid = false;
        }
        
        // Validation de la confirmation du mot de passe
        if (!confirmPasswordField.getText().equals(password)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
            isValid = false;
        }
        
        return isValid;
    }

    private void clearErrorMessages() {
        nomError.setVisible(false);
        prenomError.setVisible(false);
        emailError.setVisible(false);
        telephoneError.setVisible(false);
        adresseError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
        
        nomField.getStyleClass().remove("field-error");
        prenomField.getStyleClass().remove("field-error");
        emailField.getStyleClass().remove("field-error");
        telephoneField.getStyleClass().remove("field-error");
        adresseField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().remove("field-error");
        confirmPasswordField.getStyleClass().remove("field-error");
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        boolean isValid = true;

        if (email.isEmpty()) {
            emailError.setText("Ce champ doit être rempli");
            emailError.setVisible(true);
            emailField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailError.setText("Format d'email invalide (exemple: user@domain.com)");
            emailError.setVisible(true);
            emailField.getStyleClass().add("field-error");
            isValid = false;
        } else {
            emailError.setVisible(false);
            emailField.getStyleClass().remove("field-error");
        }

        return isValid;
    }

    private boolean validatePhone() {
        String phone = telephoneField.getText().trim();
        boolean isValid = true;

        if (phone.isEmpty()) {
            telephoneError.setText("Ce champ doit être rempli");
            telephoneError.setVisible(true);
            telephoneField.getStyleClass().add("field-error");
            isValid = false;
        } else if (phone.length() != 8) {
            telephoneError.setText("Le numéro doit contenir exactement 8 chiffres");
            telephoneError.setVisible(true);
            telephoneField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!phone.matches("\\d{8}")) {
            telephoneError.setText("Le numéro doit contenir uniquement des chiffres");
            telephoneError.setVisible(true);
            telephoneField.getStyleClass().add("field-error");
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
            passwordError.setText("Ce champ doit être rempli");
            passwordError.setVisible(true);
            passwordField.getStyleClass().add("field-error");
            passwordTextField.getStyleClass().add("field-error");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setText("Le mot de passe doit contenir au moins 6 caractères");
            passwordError.setVisible(true);
            passwordField.getStyleClass().add("field-error");
            passwordTextField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*")) {
            passwordError.setText("Le mot de passe doit contenir au moins une majuscule");
            passwordError.setVisible(true);
            passwordField.getStyleClass().add("field-error");
            passwordTextField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!password.matches(".*[a-z].*")) {
            passwordError.setText("Le mot de passe doit contenir au moins une minuscule");
            passwordError.setVisible(true);
            passwordField.getStyleClass().add("field-error");
            passwordTextField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!password.matches(".*\\d.*")) {
            passwordError.setText("Le mot de passe doit contenir au moins un chiffre");
            passwordError.setVisible(true);
            passwordField.getStyleClass().add("field-error");
            passwordTextField.getStyleClass().add("field-error");
            isValid = false;
        } else {
            passwordError.setVisible(false);
            passwordField.getStyleClass().remove("field-error");
            passwordTextField.getStyleClass().remove("field-error");
        }

        return isValid;
    }

    private boolean validateConfirmPassword() {
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        boolean isValid = true;

        if (confirmPassword.isEmpty()) {
            confirmPasswordError.setText("Ce champ doit être rempli");
            confirmPasswordError.setVisible(true);
            confirmPasswordField.getStyleClass().add("field-error");
            confirmPasswordTextField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
            confirmPasswordError.setVisible(true);
            confirmPasswordField.getStyleClass().add("field-error");
            confirmPasswordTextField.getStyleClass().add("field-error");
            isValid = false;
        } else {
            confirmPasswordError.setVisible(false);
            confirmPasswordField.getStyleClass().remove("field-error");
            confirmPasswordTextField.getStyleClass().remove("field-error");
            isValid = true;
        }

        return isValid;
    }

    @FXML
    private void handleLoginLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            // Obtenir les dimensions de l'écran
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            // Créer une nouvelle scène avec les dimensions de l'écran
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            
            // Configurer la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.setMaximized(true);
            stage.setResizable(true);
            
            // Centrer et afficher la fenêtre
            stage.centerOnScreen();
            stage.show();
            
            // Fermer la fenêtre d'inscription
            ((Stage) rootPane.getScene().getWindow()).close();
            
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement de la page de connexion: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void applyTheme(String theme) {
        Scene scene = emailField.getScene();
        System.out.println("[ThemeToggle] applyTheme called. Scene is " + (scene == null ? "null" : "not null") + ", theme=" + theme);
        if (scene == null) {
            // Retry after a short delay
            javafx.application.Platform.runLater(() -> applyTheme(theme));
            return;
        }
        
        // Supprimer tous les styles existants
        scene.getStylesheets().clear();
        
        // Ajouter le style de base
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        
        // Ajouter le thème sombre si nécessaire
        if (THEME_DARK.equals(theme)) {
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        }
        
        System.out.println("[ThemeToggle] Theme applied: " + theme);
    }

    private void setupImageAndCard() {
        if (signupForm != null && imageCard != null && signupImage != null) {
            try {
                // Bind card height
                imageCard.minHeightProperty().bind(signupForm.heightProperty());
                imageCard.maxHeightProperty().bind(signupForm.heightProperty());
                signupImage.fitHeightProperty().bind(signupForm.heightProperty());

                // Load image
                String imagePath = "/images/signup-image.png";
                var imageStream = getClass().getResourceAsStream(imagePath);
                if (imageStream == null) {
                    throw new IOException("Cannot find image: " + imagePath);
                }
                Image image = new Image(imageStream);
                
                if (image.isError()) {
                    throw new IOException("Failed to load image: " + image.getException().getMessage());
                }
                
                signupImage.setImage(image);
                
                // Set rounded corners
                Rectangle clip = new Rectangle();
                clip.widthProperty().bind(signupImage.fitWidthProperty());
                clip.heightProperty().bind(signupImage.fitHeightProperty());
                clip.setArcWidth(32);
                clip.setArcHeight(32);
                signupImage.setClip(clip);
                
            } catch (Exception e) {
                System.err.println("Error setting up image: " + e.getMessage());
                // Continue without the image
                imageCard.setVisible(false);
                imageCard.setManaged(false);
            }
        } else {
            System.err.println("One or more components are null:");
            System.err.println("signupForm: " + (signupForm == null ? "null" : "ok"));
            System.err.println("imageCard: " + (imageCard == null ? "null" : "ok"));
            System.err.println("signupImage: " + (signupImage == null ? "null" : "ok"));
        }
    }

    private void setupWindow() {
        Platform.runLater(() -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            if (stage != null) {
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setMaximized(true);
                stage.setMinWidth(screenBounds.getWidth() * 0.8);
                stage.setMinHeight(screenBounds.getHeight() * 0.8);
                stage.centerOnScreen();
            }
        });
    }

    private void setupThemeToggle() {
        if (themeToggle != null) {
            Platform.runLater(() -> {
                String savedTheme = prefs.get(PREF_THEME, THEME_LIGHT);
                applyTheme(savedTheme);
                themeToggle.setSelected(THEME_DARK.equals(savedTheme));
                themeToggle.setText(themeToggle.isSelected() ? "☀️ Mode Clair" : "🌙 Mode Sombre");
                themeToggle.selectedProperty().addListener((obs, oldVal, isDark) -> {
                    String theme = isDark ? THEME_DARK : THEME_LIGHT;
                    applyTheme(theme);
                    prefs.put(PREF_THEME, theme);
                    themeToggle.setText(isDark ? "☀️ Mode Clair" : "🌙 Mode Sombre");
                });
            });
        }
    }
} 