package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
    private Label userNameLabel;
    @FXML
    private Circle userAvatar;
    @FXML
    private Circle userStatusIndicator;
    @FXML
    private Label welcomeMessage;
    @FXML
    private BorderPane contentArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Label dateTimeLabel;
    @FXML
    private VBox welcomeBanner;

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

        if (userNameLabel == null || welcomeMessage == null) {
            System.out.println("ERREUR: Éléments UI non initialisés dans updateUserInfo");
            return;
        }

        // Mettre à jour le nom d'utilisateur
        String firstName = currentUser.getPrenom() != null ? currentUser.getPrenom() : "";
        String lastName = currentUser.getNom() != null ? currentUser.getNom() : "";

        System.out.println("Mise à jour des informations utilisateur: " + firstName + " " + lastName);

        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            userNameLabel.setText(capitalizeFirstLetter(firstName) + " " + capitalizeFirstLetter(lastName));
            welcomeMessage.setText("Bienvenue, " + firstName + "!");
        } else {
            // Utiliser l'email si le nom n'est pas disponible
            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                String username = email.substring(0, email.indexOf('@'));
                userNameLabel.setText(username);
                welcomeMessage.setText("Bienvenue, " + username + "!");
            } else {
                userNameLabel.setText("Utilisateur");
                welcomeMessage.setText("Bienvenue!");
            }
        }

        // Définir la couleur de l'avatar et l'indicateur de statut
        setAvatarColor();
        updateStatusIndicator(currentUser.getStatut());
    }

    private void setAvatarColor() {
        if (userAvatar == null) {
            System.out.println("ERREUR: userAvatar est null dans setAvatarColor");
            return;
        }

        // Générer une couleur unique basée sur le nom de l'utilisateur
        String firstName = currentUser.getPrenom() != null ? currentUser.getPrenom() : "";
        String lastName = currentUser.getNom() != null ? currentUser.getNom() : "";

        String initials;
        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            initials = getInitials(firstName, lastName);
        } else {
            // Utiliser les initiales de l'email si le nom n'est pas disponible
            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                String username = email.substring(0, email.indexOf('@'));
                initials = username.length() > 0 ? username.substring(0, Math.min(2, username.length())).toUpperCase() : "U";
            } else {
                initials = "U";
            }
        }

        // Utiliser les initiales pour générer une couleur
        int hash = initials.hashCode();
        double hue = Math.abs(hash % 360);
        javafx.scene.paint.Color color = javafx.scene.paint.Color.hsb(hue, 0.7, 0.8);
        userAvatar.setFill(color);
    }

    private void updateStatusIndicator(String status) {
        if (userStatusIndicator == null) {
            System.out.println("ERREUR: userStatusIndicator est null dans updateStatusIndicator");
            return;
        }

        if (status == null) {
            userStatusIndicator.setFill(javafx.scene.paint.Color.valueOf("#3498db")); // Bleu par défaut
            return;
        }

        switch (status.toUpperCase()) {
            case "ACTIF":
                userStatusIndicator.setFill(javafx.scene.paint.Color.valueOf("#2ecc71")); // Vert
                break;
            case "INACTIF":
                userStatusIndicator.setFill(javafx.scene.paint.Color.valueOf("#e74c3c")); // Rouge
                break;
            case "CONGE":
                userStatusIndicator.setFill(javafx.scene.paint.Color.valueOf("#f1c40f")); // Jaune
                break;
            case "ABSENT":
                userStatusIndicator.setFill(javafx.scene.paint.Color.valueOf("#95a5a6")); // Gris
                break;
            default:
                userStatusIndicator.setFill(javafx.scene.paint.Color.valueOf("#3498db")); // Bleu
        }
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

    @FXML
    private void handleProducts() {
        System.out.println("Chargement des produits");

        if (welcomeBanner != null) {
            // Cacher la bannière de bienvenue
            welcomeBanner.setVisible(false);
        }

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

            contentArea.setCenter(content);

            if (statusLabel != null) {
                statusLabel.setText("Catalogue de produits chargé");
            }
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

        if (welcomeBanner != null) {
            // Cacher la bannière de bienvenue
            welcomeBanner.setVisible(false);
        }

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

            contentArea.setCenter(content);

            if (statusLabel != null) {
                statusLabel.setText("Liste des commandes chargée");
            }
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