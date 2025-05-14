package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Utilisateur;
import javafx.stage.Screen;
import utils.Session;
import javafx.scene.control.Tooltip;
import utils.ThemeManager;
import javafx.stage.Window;
import javafx.stage.Popup;
import utils.NotificationManager;
import utils.NotificationManager.VehicleNotification;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class MainDashboardController {
    @FXML
    private Label userFullName;
    @FXML
    private Label userType;
    @FXML
    private Label userStatus;
    @FXML
    private Circle profilePhoto;
    @FXML
    private Circle statusIndicator;
    @FXML
    private Label pageTitle;
    @FXML
    private StackPane contentArea;
    @FXML
    private HBox root;
    @FXML
    private ToggleButton themeToggle;
    @FXML private Button dashboardButton;
    @FXML private Button usersButton;
    @FXML private Button vehiclesButton;
    @FXML private Button ordersButton;
    @FXML private Button recruitmentButton;
    @FXML private Button supportButton;
    @FXML private Button performancesButton;
    @FXML private Button notificationsButton;
    @FXML private Button chatbotButton;
    @FXML
    private VBox statActifCard, statInactifCard, statCongeCard, statAbsentCard;
    @FXML
    private Label statActifCount, statInactifCount, statCongeCount, statAbsentCount;
    @FXML
    private PieChart statPieChart;
    @FXML private Label statTotalVehiculesCount;
    @FXML private PieChart vehiculeStatPieChart;
    @FXML private Label statTotalPersonnelCount;
    @FXML private PieChart clientStatPieChart;
    @FXML private Label statTotalClientsCount;

    private Utilisateur currentUser;
    private static MainDashboardController instance;
    private static final String UPLOAD_DIR = "uploads/profiles/";

    public void initialize() {
        instance = this;
        
        // Initialize dark mode toggle
        themeToggle.setSelected(false);
        themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                root.getStyleClass().add("dark-mode");
            } else {
                root.getStyleClass().remove("dark-mode");
            }
        });
    }

    public static MainDashboardController getInstance() {
        return instance;
    }

    public void initData(Utilisateur user) {
        this.currentUser = user;
        updateUserInfo();
        // Disable usersButton and dashboardButton for non-admin and non-responsable
        if (currentUser != null) {
            String type = currentUser.getTypeUtilisateur();
            boolean notAdminOrResponsable = type == null || (!type.equalsIgnoreCase("admin") && !type.equalsIgnoreCase("responsable"));
            boolean isClient = type != null && type.equalsIgnoreCase("client");
            
            if (notAdminOrResponsable) {
                usersButton.setVisible(false);
                dashboardButton.setVisible(false);
                // For non-admin/non-responsable, load edit profile page by default
                handleEditProfile();
            } else {
                // For admin/responsable, load users list by default
                handleUsers();
            }
            
            // Hide performances button for clients
            if (isClient) {
                performancesButton.setVisible(false);
                performancesButton.setManaged(false); // This will remove the space taken by the button
            }
        }
    }

    public void updateCurrentUser(Utilisateur updatedUser) {
        if (updatedUser.getIdUser() == currentUser.getIdUser()) {
            this.currentUser = updatedUser;
            updateUserInfo();
        }
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            userFullName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            userType.setText(currentUser.getTypeUtilisateur());
            userStatus.setText(currentUser.getStatut());
            userStatus.getStyleClass().removeAll("status-actif", "status-inactif", "status-conge", "status-absent");
            userStatus.getStyleClass().add("status-" + currentUser.getStatut().toLowerCase());
            
            updateStatusIndicator(currentUser.getStatut());
            loadProfilePhoto();
        }
    }

    private void loadProfilePhoto() {
        try {
            if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
                File photoFile = new File(System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "profiles" + File.separator + currentUser.getPhotoUrl());
                System.out.println("Looking for photo at: " + photoFile.getAbsolutePath());
                if (photoFile.exists()) {
                    try {
                        // For local files, do NOT append cache-busting query string
                        String uri = photoFile.toURI().toString();
                        Image image = new Image(uri);
                        if (!image.isError()) {
                            profilePhoto.setFill(new ImagePattern(image));
                            return;
                        } else {
                            System.err.println("Image error: " + image.getException());
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading profile image: " + e.getMessage());
                    }
                } else {
                    System.err.println("Photo file does not exist: " + photoFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in loadProfilePhoto: " + e.getMessage());
        }
        setDefaultProfilePhoto();
    }

    private void setDefaultProfilePhoto() {
        try {
            // Set a default color with user initials
            String initials = getInitials();
            profilePhoto.setFill(Color.valueOf("#4CAF50"));
            
            // You could also set a default image:
            /*
            String defaultImagePath = "/images/default-avatar.png";
            Image defaultImage = new Image(getClass().getResourceAsStream(defaultImagePath));
            if (!defaultImage.isError()) {
                profilePhoto.setFill(new ImagePattern(defaultImage));
            }
            */
        } catch (Exception e) {
            System.err.println("Error setting default photo: " + e.getMessage());
            profilePhoto.setFill(Color.valueOf("#4CAF50")); // Fallback color
        }
    }

    private String getInitials() {
        if (currentUser == null) return "";
        
        StringBuilder initials = new StringBuilder();
        if (currentUser.getPrenom() != null && !currentUser.getPrenom().isEmpty()) {
            initials.append(currentUser.getPrenom().charAt(0));
        }
        if (currentUser.getNom() != null && !currentUser.getNom().isEmpty()) {
            initials.append(currentUser.getNom().charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    private void updateStatusIndicator(String status) {
        if (statusIndicator != null) {
            statusIndicator.getStyleClass().clear();
            statusIndicator.getStyleClass().add("status-indicator");
            
            switch (status.toUpperCase()) {
                case "ACTIF":
                    statusIndicator.setFill(Color.valueOf("#2ecc71")); // Green
                    break;
                case "INACTIF":
                    statusIndicator.setFill(Color.valueOf("#e74c3c")); // Red
                    break;
                case "CONGE":
                    statusIndicator.setFill(Color.valueOf("#f1c40f")); // Yellow
                    break;
                case "ABSENT":
                    statusIndicator.setFill(Color.valueOf("#95a5a6")); // Gray
                    break;
                default:
                    statusIndicator.setFill(Color.valueOf("#95a5a6")); // Default gray
            }
        }
    }

    private String getTypeIcon(String type) {
        switch (type.toUpperCase()) {
            case "ADMIN":
                return "👑";
            case "RESPONSABLE":
                return "👔";
            case "MAGASINIER":
                return "📦";
            case "LIVREUR":
                return "🚚";
            case "CLIENT":
                return "👤";
            default:
                return "👤";
        }
    }

    private void setSelectedMenuButton(Button selectedButton) {
        Button[] buttons = {
            dashboardButton, usersButton, vehiclesButton, ordersButton, recruitmentButton, supportButton, performancesButton, notificationsButton, chatbotButton
        };
        for (Button btn : buttons) {
            btn.getStyleClass().remove("selected");
        }
        if (!selectedButton.getStyleClass().contains("selected")) {
            selectedButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void handleDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardStats.fxml"));
            Parent dashboardContent = loader.load();
            DashboardStatsController controller = loader.getController();
            // Le contrôleur est déjà initialisé par JavaFX grâce à l'interface Initializable
            changeContent(dashboardContent, "Tableau de bord");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le tableau de bord");
        }
    }

    private void animatePieChart(javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieChartData, double total) {
        // Initial scale
            for (javafx.scene.chart.PieChart.Data data : pieChartData) {
                data.getNode().setScaleX(0);
                data.getNode().setScaleY(0);
            }

        // Animation sequence
            javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition();
            for (javafx.scene.chart.PieChart.Data data : pieChartData) {
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(400), data.getNode());
                st.setToX(1);
                st.setToY(1);
                st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                seq.getChildren().add(st);
            }
            seq.play();

        // Hover effects
            for (javafx.scene.chart.PieChart.Data data : pieChartData) {
                data.getNode().setOnMouseEntered(e -> {
                    double percent = (total > 0) ? (data.getPieValue() / total * 100) : 0;
                    String originalLabel = data.getName();
                    data.setName(String.format("%s (%.1f%%)", originalLabel.split(" ")[0], percent));
                    javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), data.getNode());
                    st.setToX(1.08);
                    st.setToY(1.08);
                    st.play();
                });
                data.getNode().setOnMouseExited(e -> {
                    String originalLabel = data.getName().split(" ")[0];
                    data.setName(originalLabel);
                    javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), data.getNode());
                    st.setToX(1);
                    st.setToY(1);
                    st.play();
                });
        }
    }

    @FXML
    public void handleUsers() {
        setSelectedMenuButton(usersButton);
        pageTitle.setText("Gestion des Utilisateurs");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListeUtilisateurs.fxml"));
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement de la liste des utilisateurs: " + e.getMessage());
        }
    }

    @FXML
    private void handleVehicles() {
        setSelectedMenuButton(vehiclesButton);
        pageTitle.setText("Gestion des Véhicules");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlZayen/Vehicules.fxml"));
            Parent content = loader.load();
        contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement de la gestion des véhicules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOrders() {
        setSelectedMenuButton(ordersButton);
        pageTitle.setText("Gestion des Commandes");
        // TODO: Implémenter la gestion des commandes
        contentArea.getChildren().clear();
    }

    @FXML
    private void handleRecruitment() {
        setSelectedMenuButton(recruitmentButton);
        pageTitle.setText("Gestion des Recrutements");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/Offre.fxml"));
            Parent content = loader.load();
        contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement de la gestion des recrutements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupport() {
        setSelectedMenuButton(supportButton);
        pageTitle.setText("Support");
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlGuezguez/Support.fxml"));
            Parent root = loader.load();
            
            SupportController controller = loader.getController();
            if (controller != null) {
                controller.setContentArea(contentArea);
            }
            
        contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page de support: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePerformances() {
        setSelectedMenuButton(performancesButton);
        pageTitle.setText("Gestion des Performances");
        
        if (currentUser == null) {
            showError("Erreur", "Aucun utilisateur connecté");
            return;
        }

        try {
            // Vérifier que le fichier FXML existe
            var fxmlUrl = getClass().getResource("/fxml/GestionPerformances.fxml");
            if (fxmlUrl == null) {
                showError("Erreur", "Le fichier FXML de gestion des performances est introuvable");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            GestionPerformancesController controller = loader.getController();
            if (controller == null) {
                showError("Erreur", "Impossible d'initialiser le contrôleur de performances");
                return;
            }

            // Conversion sécurisée de l'ID utilisateur
            Long userId;
            try {
                userId = Long.valueOf(currentUser.getIdUser());
            } catch (NumberFormatException e) {
                showError("Erreur", "ID utilisateur invalide: " + currentUser.getIdUser());
                return;
            }

            // Initialisation des données
            try {
                controller.initData(userId);
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de l'initialisation des données: " + e.getMessage());
                e.printStackTrace(); // Pour le debugging
                return;
            }
            
            // Mise à jour de l'interface
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page de gestion des performances: " + e.getMessage());
            e.printStackTrace(); // Pour le debugging
        } catch (Exception e) {
            showError("Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
            e.printStackTrace(); // Pour le debugging
        }
    }

    @FXML
    private void handleSettings() {
        pageTitle.setText("Paramètres");
        // Load settings content
    }

    @FXML
    private void handleLogout() {
        try {
            // Clear the session
            Session.getInstance().clearSession();
            
            // Get the current stage
            Stage currentStage = (Stage) contentArea.getScene().getWindow();
            
            // Create a new stage for login
            Stage loginStage = new Stage();
            
            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            // Get the screen dimensions
            javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            // Create new scene with the screen dimensions
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            
            // Configure the new stage
            loginStage.setScene(scene);
            loginStage.setTitle("Connexion");
            
            // Set the window properties for full screen
            loginStage.setMaximized(true);
            loginStage.setResizable(true);
            
            // Center on screen
            loginStage.centerOnScreen();
            
            // Initialize the login controller
            LoginController loginController = loader.getController();
            loginController.initialize();
            
            // Show the login window
            loginStage.show();
            
            // Close the current window
            currentStage.close();
            
        } catch (IOException e) {
            showError("Erreur", "Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    @FXML
    public void handleAjouterUtilisateur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterUtilisateur.fxml"));
            Parent content = loader.load();
            
            AjouterUtilisateurController controller = loader.getController();
            
            // Update the page title
            pageTitle.setText("Ajouter Utilisateur");
            
            // Clear and set the new content
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            
            // Set up a listener to refresh users list when a user is added
            controller.setOnUserAdded(() -> {
                handleUsers(); // Return to users list after adding
            });
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement du formulaire d'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProfile.fxml"));
            Parent content = loader.load();
            
            EditProfileController controller = loader.getController();
            controller.initData(currentUser);
            
            // Update the page title
            pageTitle.setText("Modifier Profil");
            
            // Clear and set the new content
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            
            // Set up a listener to refresh user info when profile is updated
            controller.setOnProfileUpdated(() -> {
                updateUserInfo();
                String type = currentUser.getTypeUtilisateur();
                if (type != null && (type.equalsIgnoreCase("admin") || type.equalsIgnoreCase("responsable"))) {
                    handleUsers(); // Only for admin/responsable
                } else {
                    handleEditProfile(); // Stay on edit profile for others
                }
            });
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement du formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    public void handleModifierUtilisateur(Utilisateur utilisateur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierUtilisateur.fxml"));
            Parent content = loader.load();
            
            ModifierUtilisateurController controller = loader.getController();
            controller.initData(utilisateur);
            
            // Update the page title
            pageTitle.setText("Modifier Utilisateur");
            
            // Clear and set the new content
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            
            // Set up a listener to refresh users list when a user is modified
            controller.setOnUserModified(() -> {
                handleUsers(); // Return to users list after modification
            });
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement du formulaire de modification: " + e.getMessage());
        }
    }

    @FXML
    private void handleNotifications() {
        setSelectedMenuButton(notificationsButton);
        setPageTitle("Notifications");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlZayen/NotificationPage.fxml"));
            Parent content = loader.load();
            changeContent(content, "Notifications");
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page des notifications.");
        }
    }

    @FXML
    private void handleChatbot() {
        setSelectedMenuButton(chatbotButton);
        setPageTitle("Assistant IA");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlGuezguez/ChatBot.fxml"));
            Parent content = loader.load();
            ChatbotController chatbotController = loader.getController();
            chatbotController.setContentArea(contentArea);
            changeContent(content, "Assistant IA");
        } catch (IOException e) {
            e.printStackTrace(); // Ajouter cette ligne pour voir l'erreur complète
            showError("Erreur", "Impossible de charger l'assistant IA : " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    public void setPageTitle(String title) {
            pageTitle.setText(title);
        }

    public void changeContent(Parent content, String title) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
        setPageTitle(title);
    }

    public Utilisateur getCurrentUser() {
        return currentUser;
    }
} 