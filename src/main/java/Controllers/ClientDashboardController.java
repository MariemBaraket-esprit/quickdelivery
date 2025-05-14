package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import models.Utilisateur;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class ClientDashboardController {
    @FXML
    private Label userFullName;
    @FXML
    private Label userType;
    @FXML
    private Label userTypeIcon;
    @FXML
    private Label userStatus;
    @FXML
    private Circle profilePhoto;
    @FXML
    private Circle statusIndicator;
    @FXML
    private Label pageTitle;
    @FXML
    private Label dateTimeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnProducts;
    @FXML
    private Button btnOrders;

    private Utilisateur currentUser;
    private Timer timer;

    @FXML
    public void initialize() {
        System.out.println("Initialisation de ClientDashboardController");

        // Initialiser l'horloge
        startClock();
    }

    public void initData(Utilisateur user) {
        System.out.println("Initialisation des données utilisateur dans ClientDashboardController");
        this.currentUser = user;

        updateUserInfo();

        // Charger la liste des produits par défaut
        handleProducts();
    }

    private void updateUserInfo() {
        if (currentUser == null) {
            System.out.println("ERREUR: currentUser est null dans updateUserInfo");
            return;
        }

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
            if (email != null && email.contains("@")) {
                fullName = email.substring(0, email.indexOf('@'));
            } else {
                fullName = "Utilisateur";
            }
        }
        userFullName.setText(fullName);

        // Set user type with icon and proper formatting
        String typeIcon = getTypeIcon(currentUser.getTypeUtilisateur());
        userTypeIcon.setText(typeIcon);
        String formattedType = capitalizeFirstLetter(currentUser.getTypeUtilisateur().toLowerCase());
        userType.setText(formattedType);

        // Set user status with appropriate styling
        String status = currentUser.getStatut().toLowerCase();
        String formattedStatus = capitalizeFirstLetter(status);
        userStatus.setText(formattedStatus);
        userStatus.getStyleClass().removeAll("actif", "inactif", "conge", "absent");
        userStatus.getStyleClass().add(status);

        // Update status indicator
        updateStatusIndicator(currentUser.getStatut());

        // Set profile photo color
        setProfilePhotoColor();
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

    private void updateStatusIndicator(String status) {
        if (statusIndicator == null) {
            System.out.println("ERREUR: statusIndicator est null dans updateStatusIndicator");
            return;
        }

        switch (status.toUpperCase()) {
            case "ACTIF":
                statusIndicator.setFill(javafx.scene.paint.Color.valueOf("#2ecc71")); // Green
                break;
            case "INACTIF":
                statusIndicator.setFill(javafx.scene.paint.Color.valueOf("#e74c3c")); // Red
                break;
            case "CONGE":
                statusIndicator.setFill(javafx.scene.paint.Color.valueOf("#f1c40f")); // Yellow
                break;
            case "ABSENT":
                statusIndicator.setFill(javafx.scene.paint.Color.valueOf("#95a5a6")); // Gray
                break;
            default:
                statusIndicator.setFill(javafx.scene.paint.Color.valueOf("#3498db")); // Blue
        }
    }

    private void setProfilePhotoColor() {
        if (profilePhoto == null) {
            System.out.println("ERREUR: profilePhoto est null dans setProfilePhotoColor");
            return;
        }

        // Generate a unique color based on user's name
        String firstName = currentUser.getPrenom() != null ? currentUser.getPrenom() : "";
        String lastName = currentUser.getNom() != null ? currentUser.getNom() : "";

        String initials;
        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            initials = getInitials(firstName, lastName);
        } else {
            // Fallback to email initials if name is not available
            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                String username = email.substring(0, email.indexOf('@'));
                initials = username.length() > 0 ? username.substring(0, Math.min(2, username.length())).toUpperCase() : "U";
            } else {
                initials = "U";
            }
        }

        // Use the initials to generate a color
        int hash = initials.hashCode();
        double hue = Math.abs(hash % 360);
        javafx.scene.paint.Color color = javafx.scene.paint.Color.hsb(hue, 0.7, 0.8);
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

    private void startClock() {
        if (dateTimeLabel == null) {
            System.out.println("ERREUR: dateTimeLabel est null dans startClock");
            return;
        }

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    dateTimeLabel.setText(formatter.format(now));
                });
            }
        }, 0, 1000);
    }

    private void setActiveButton(Button activeButton) {
        // Remove selected class from all buttons
        btnProducts.getStyleClass().remove("selected");
        btnOrders.getStyleClass().remove("selected");

        // Add selected class to active button
        if (activeButton != null) {
            activeButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void handleProducts() {
        System.out.println("Chargement des produits");
        pageTitle.setText("Catalogue de Produits");
        setActiveButton(btnProducts);

        if (contentArea == null) {
            System.out.println("ERREUR: contentArea est null dans handleProducts");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-produits-view.fxml"));
            Parent content = loader.load();

            ClientProduitsController controller = loader.getController();
            controller.setUtilisateur(currentUser);

            // Créer un nouveau ClientController si nécessaire
            ClientController tempController = new ClientController();
            tempController.setUtilisateur(currentUser);
            controller.setClientController(tempController);

            // Replace the content in the StackPane
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            System.out.println("ERREUR lors du chargement des produits: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger le catalogue de produits: " + e.getMessage());
        }
    }

    @FXML
    private void handleMyOrders() {
        System.out.println("Chargement des commandes");
        pageTitle.setText("Mes Commandes");
        setActiveButton(btnOrders);

        if (contentArea == null) {
            System.out.println("ERREUR: contentArea est null dans handleMyOrders");
            return;
        }

        try {
            // Vérifiez que ce chemin est correct
            String fxmlPath = "/views/client-commande-view.fxml";
            System.out.println("Tentative de chargement du fichier FXML: " + fxmlPath);

            // Vérifiez si le fichier existe
            if (getClass().getResource(fxmlPath) == null) {
                System.out.println("ERREUR: Le fichier FXML n'existe pas: " + fxmlPath);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Fichier introuvable",
                        "Le fichier FXML n'a pas été trouvé: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            ClientCommandesController controller = loader.getController();
            controller.setUtilisateur(currentUser);

            // Créer un nouveau ClientController si nécessaire
            ClientController tempController = new ClientController();
            tempController.setUtilisateur(currentUser);
            controller.setClientController(tempController);

            controller.loadCommandes();

            // Replace the content in the StackPane
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            System.out.println("ERREUR lors du chargement des commandes: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger la liste des commandes: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        System.out.println("Affichage du profil");
        pageTitle.setText("Mon Profil");

        if (currentUser == null) {
            System.out.println("ERREUR: currentUser est null dans handleProfile");
            return;
        }

        // Afficher les informations du profil
        String nom = currentUser.getNom() != null ? currentUser.getNom() : "Non renseigné";
        String prenom = currentUser.getPrenom() != null ? currentUser.getPrenom() : "Non renseigné";
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "Non renseigné";
        String telephone = currentUser.getTelephone() != null ? currentUser.getTelephone() : "Non renseigné";
        String adresse = currentUser.getAdresse() != null ? currentUser.getAdresse() : "Non renseignée";

        showAlert(Alert.AlertType.INFORMATION, "Profil", "Informations du profil",
                "Nom: " + nom + "\n" +
                        "Prénom: " + prenom + "\n" +
                        "Email: " + email + "\n" +
                        "Téléphone: " + telephone + "\n" +
                        "Adresse: " + adresse);
    }

    @FXML
    private void handleLogout() {
        System.out.println("Déconnexion");

        // Arrêter le timer
        if (timer != null) {
            timer.cancel();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            if (contentArea == null) {
                System.out.println("ERREUR: contentArea est null dans handleLogout");
                return;
            }

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
        } catch (IOException e) {
            System.out.println("ERREUR lors de la déconnexion: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de déconnexion",
                    "Impossible de se déconnecter: " + e.getMessage());
        }
    }

    public void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}