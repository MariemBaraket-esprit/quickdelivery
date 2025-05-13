package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import models.Utilisateur;
import java.io.IOException;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

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
    public Label pageTitle;
    @FXML
    public StackPane contentArea;

    public Utilisateur currentUser;
    private static MainDashboardController instance;

    public void initialize() {
        instance = this;
    }

    public static MainDashboardController getInstance() {
        return instance;
    }

    public void initData(Utilisateur user) {
        this.currentUser = user;
        updateUserInfo();
        // Load users list by default
        handleUsers();
    }

    public void updateCurrentUser(Utilisateur updatedUser) {
        if (updatedUser.getIdUser() == currentUser.getIdUser()) {
            this.currentUser = updatedUser;
            updateUserInfo();
        }
    }

    private void updateUserInfo() {
        // Set user's full name
        String firstName = currentUser.getPrenom() != null ? currentUser.getPrenom() : "";
        String lastName = currentUser.getNom() != null ? currentUser.getNom() : "";

        // Format the full name properly
        String fullName = "";
        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            fullName = String.format("%s %s",
                    capitalizeFirstLetter(firstName),
                    lastName.toUpperCase());
        } else {
            // Fallback to email if name is not available
            String email = currentUser.getEmail();
            fullName = email.substring(0, email.indexOf('@'));
        }
        userFullName.setText(fullName);

        // Set user type with icon and proper formatting
        String typeIcon = getTypeIcon(currentUser.getTypeUtilisateur());
        String formattedType = capitalizeFirstLetter(currentUser.getTypeUtilisateur().toLowerCase());
        userType.setText(typeIcon + " " + formattedType);

        // Set user status with appropriate styling and proper formatting
        String status = currentUser.getStatut().toLowerCase();
        String formattedStatus = capitalizeFirstLetter(status);
        userStatus.setText(formattedStatus);
        userStatus.getStyleClass().removeAll("actif", "inactif", "conge", "absent");
        userStatus.getStyleClass().add(status);

        // Update status indicator
        updateStatusIndicator(currentUser.getStatut());

        // Set profile photo color and initials
        setProfilePhotoColor();
    }

    private void updateStatusIndicator(String status) {
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

    private void setProfilePhotoColor() {
        // Generate a unique color based on user's name
        String firstName = currentUser.getPrenom() != null ? currentUser.getPrenom() : "";
        String lastName = currentUser.getNom() != null ? currentUser.getNom() : "";

        String initials;
        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            initials = getInitials(firstName, lastName);
        } else {
            // Fallback to email initials if name is not available
            String email = currentUser.getEmail();
            String username = email.substring(0, email.indexOf('@'));
            initials = username.length() > 0 ? username.substring(0, Math.min(2, username.length())).toUpperCase() : "U";
        }

        // Use the initials to generate a color
        int hash = initials.hashCode();
        double hue = Math.abs(hash % 360);
        Color color = Color.hsb(hue, 0.7, 0.8);
        profilePhoto.setFill(color);
    }

    private String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    @FXML
    private void handleDashboard() {
        pageTitle.setText("Tableau de bord");
        // Load dashboard content
    }

    @FXML
    private void handleUsers() {
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
        pageTitle.setText("Gestion des Véhicules");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListeVehicules.fxml"));
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement de la liste des véhicules: " + e.getMessage());
        }
    }

    @FXML
    private void handleOrders() {
        pageTitle.setText("Gestion des Commandes");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListeCommandes.fxml"));
            Parent content = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du chargement de la liste des commandes: " + e.getMessage());
        }
    }

    @FXML
    private void handleRecruitment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatBot.fxml"));
            Parent chatbotContent = loader.load();

            ChatbotController chatbotController = loader.getController();
            chatbotController.setContentArea(contentArea); // Pass contentArea reference

            contentArea.getChildren().clear();
            contentArea.getChildren().add(chatbotContent);

            // Set the page title
            pageTitle.setText("ChatBot");

        } catch (IOException e) {
            // Handle any errors that occur during loading
            showError("Error", "Failed to load ChatBot page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSupport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Support.fxml"));
            Parent supportContent = loader.load();

            SupportController supportController = loader.getController();
            supportController.setContentArea(contentArea); // 🔁 Pass contentArea reference

            contentArea.getChildren().clear();
            contentArea.getChildren().add(supportContent);


            // Set the page title
            pageTitle.setText("Support");

        } catch (IOException e) {
            // Handle any errors that occur during loading
            showError("Error", "Failed to load support page: " + e.getMessage());
            e.printStackTrace();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
        } catch (IOException e) {
            showError("Erreur", "Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    @FXML
    public void handleAjouterUtilisateur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterUtilisateur.fxml"));
            Parent content = loader.load();

            Controllers.AjouterUtilisateurController controller = loader.getController();

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

            Controllers.EditProfileController controller = loader.getController();
            controller.initData(currentUser);

            // Update the page title
            pageTitle.setText("Modifier Profil");

            // Clear and set the new content
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);

            // Set up a listener to refresh user info when profile is updated
            controller.setOnProfileUpdated(() -> {
                updateUserInfo();
                handleUsers(); // Return to users list after update
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

            Controllers.ModifierUtilisateurController controller = loader.getController();
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

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}