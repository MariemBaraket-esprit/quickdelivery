package controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.prefs.Preferences;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import models.Utilisateur;
import services.UtilisateurService;
import utils.Session;
import javafx.scene.control.TextArea;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private Label emailError;
    @FXML
    private Label passwordError;
    @FXML
    private StackPane rootPane;
    @FXML
    private ImageView loginImageView;
    @FXML
    private VBox loginCard;
    @FXML
    private ToggleButton themeToggle;

    private UtilisateurService utilisateurService;
    private TextField passwordTextField;
    private static final String PREF_THEME = "theme";
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();

            // Create passwordTextField programmatically and add it to the HBox
            passwordTextField = new TextField();
            passwordTextField.getStyleClass().addAll(passwordField.getStyleClass());
            passwordTextField.promptTextProperty().bind(passwordField.promptTextProperty());
            passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
            passwordTextField.setManaged(false);
            passwordTextField.setVisible(false);
            HBox passwordBox = (HBox) passwordField.getParent();
            passwordBox.getChildren().add(1, passwordTextField);

            // Now call setupPasswordToggle() (passwordTextField is NOT null)
            setupPasswordToggle();

            // Add listeners for real-time validation
            setupValidationListeners();

            // Set rounded corners for the image
            if (loginImageView != null) {
                Rectangle clip = new Rectangle(400, 650);
                clip.setArcWidth(60);
                clip.setArcHeight(60);
                loginImageView.setClip(clip);
            }

            // Animate the login card (fade-in and scale)
            if (loginCard != null) {
                loginCard.setOpacity(0);
                loginCard.setScaleX(0.95);
                loginCard.setScaleY(0.95);
                FadeTransition fade = new FadeTransition(Duration.millis(900), loginCard);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                ScaleTransition scale = new ScaleTransition(Duration.millis(900), loginCard);
                scale.setFromX(0.95);
                scale.setFromY(0.95);
                scale.setToX(1);
                scale.setToY(1);
                scale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                fade.play();
                scale.play();

                // Card hover animation
                DropShadow normalShadow = new DropShadow(32, Color.rgb(74,144,226,0.18));
                DropShadow hoverShadow = new DropShadow(64, Color.rgb(74,144,226,0.32));
                loginCard.setEffect(normalShadow);
                loginCard.setOnMouseEntered(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(250), loginCard);
                    st.setToX(1.035);
                    st.setToY(1.035);
                    st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                    st.play();
                    loginCard.setEffect(hoverShadow);
                });
                loginCard.setOnMouseExited(e -> {
                    ScaleTransition st = new ScaleTransition(Duration.millis(250), loginCard);
                    st.setToX(1);
                    st.setToY(1);
                    st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                    st.play();
                    loginCard.setEffect(normalShadow);
                });
            }

            // Theme toggle logic
            if (themeToggle != null) {
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
            }

            // Configuration de la fenêtre pour le plein écran
            Platform.runLater(() -> {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                if (stage != null) {
                    // Obtenir les dimensions de l'écran
                    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                    
                    // Configurer la fenêtre
                    stage.setMaximized(true);
                    stage.setMinWidth(screenBounds.getWidth() * 0.8);
                    stage.setMinHeight(screenBounds.getHeight() * 0.8);
                    
                    // Centrer la fenêtre
                    stage.centerOnScreen();
                }
            });
        } catch (SQLException e) {
            showError("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    private void setupPasswordToggle() {
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

    private boolean validatePassword() {
        String password = passwordField.getText().trim();
        boolean isValid = true;

        if (password.isEmpty()) {
            passwordError.setText("Ce champ doit être rempli");
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

    @FXML
    private void handleLogin() {
        clearErrorMessages();
        
        boolean isValid = true;
        
        // Validate email
        if (emailField.getText().trim().isEmpty()) {
            emailError.setText("L'email est requis");
            emailError.setVisible(true);
            emailField.getStyleClass().add("field-error");
            isValid = false;
        } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailError.setText("Format d'email invalide");
            emailError.setVisible(true);
            emailField.getStyleClass().add("field-error");
            isValid = false;
        }
        
        // Validate password
        if (passwordField.getText().trim().isEmpty()) {
            passwordError.setText("Le mot de passe est requis");
            passwordError.setVisible(true);
            passwordField.getStyleClass().add("field-error");
            isValid = false;
        }
        
        if (!isValid) {
            return;
        }

        try {
            Utilisateur utilisateur = utilisateurService.authenticate(
                emailField.getText().trim(),
                passwordField.getText()
            );

            if (utilisateur != null) {
                // Store the logged-in user
                Session.getInstance().setCurrentUser(utilisateur);
                
                // Load the main dashboard
                loadDashboard(utilisateur);
            } else {
                emailError.setText("Email ou mot de passe incorrect");
                emailError.setVisible(true);
                emailField.getStyleClass().add("field-error");
                passwordField.getStyleClass().add("field-error");
            }
        } catch (SQLException e) {
            emailError.setText("Erreur lors de la connexion: " + e.getMessage());
            emailError.setVisible(true);
        }
    }

    private void clearErrorMessages() {
        // Hide all error labels
        emailError.setVisible(false);
        passwordError.setVisible(false);
        
        // Remove error styles
        emailField.getStyleClass().remove("field-error");
        passwordField.getStyleClass().remove("field-error");
        passwordTextField.getStyleClass().remove("field-error");
    }

    @FXML
    private void handleSignupLink() {
        try {
            System.out.println("Début du chargement de la page d'inscription...");
            
            // 1. Charger le FXML depuis le classpath
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LoginController.class.getResource("/fxml/Signup.fxml"));
            
            if (loader.getLocation() == null) {
                // Essayer des chemins alternatifs
                String[] paths = {
                    "fxml/Signup.fxml",
                    "/Signup.fxml",
                    "../fxml/Signup.fxml"
                };
                
                for (String path : paths) {
                    URL url = LoginController.class.getResource(path);
                    if (url != null) {
                        System.out.println("FXML trouvé à: " + url);
                        loader.setLocation(url);
                        break;
                    }
                }
                
                if (loader.getLocation() == null) {
                    throw new IOException("Impossible de trouver Signup.fxml dans le classpath");
                }
            }
            
            System.out.println("FXML location: " + loader.getLocation());
            
            // 2. Charger la racine
            Parent root = loader.load();
            
            // 3. Charger le CSS
            URL cssUrl = LoginController.class.getResource("/styles/styles.css");
            if (cssUrl == null) {
                System.err.println("Attention: styles.css non trouvé");
            }
            
            // 4. Créer et configurer la scène
            Scene scene = new Scene(root);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // 5. Configurer la fenêtre
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Inscription");
            
            // Obtenir les dimensions de l'écran
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setMaximized(true);
            stage.setMinWidth(screenBounds.getWidth() * 0.8);
            stage.setMinHeight(screenBounds.getHeight() * 0.8);
            
            // 6. Afficher la nouvelle fenêtre
            stage.show();
            
            // 7. Fermer la fenêtre de connexion
            ((Stage) rootPane.getScene().getWindow()).close();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la page d'inscription:");
            e.printStackTrace();
            
            // Afficher une alerte avec les détails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de chargement");
            alert.setHeaderText("Impossible de charger la page d'inscription");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            
            // Ajouter les détails de la stack trace
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            
            TextArea textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            alert.getDialogPane().setExpandableContent(new VBox(textArea));
            alert.getDialogPane().setExpanded(true);
            
            alert.showAndWait();
        }
    }

    private void loadDashboard(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainDashboard.fxml"));
            Parent root = loader.load();
            
            // Obtenir les dimensions de l'écran
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            // Créer une nouvelle scène avec les dimensions de l'écran
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            
            // Configurer la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Dashboard");
            stage.setMaximized(true);
            stage.setResizable(true);
            
            // Initialiser le contrôleur du dashboard
            MainDashboardController dashboardController = loader.getController();
            dashboardController.initData(user);
            
            // Centrer et afficher la fenêtre
            stage.centerOnScreen();
            stage.show();
            
            // Fermer la fenêtre de connexion
            ((Stage) rootPane.getScene().getWindow()).close();
            
        } catch (IOException e) {
            showError("Erreur lors du chargement du dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        showError("Erreur", message);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void applyTheme(String theme) {
        Scene scene = emailField.getScene();
        if (scene == null) return;

        // Supprimer tous les styles existants
        scene.getStylesheets().clear();
        
        // Ajouter le style de base
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        
        // Ajouter le thème sombre si nécessaire
        if (THEME_DARK.equals(theme)) {
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        }
    }
} 