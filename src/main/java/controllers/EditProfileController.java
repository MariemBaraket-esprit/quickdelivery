package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;

public class EditProfileController {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextArea adresseField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorMessage;
    @FXML private Circle photoCircle;
    @FXML private ImageView photoImageView;

    private Utilisateur currentUser;
    private UtilisateurService utilisateurService;
    private Runnable onProfileUpdated;
    private String newPhotoPath;
    private static final String UPLOAD_DIR = "uploads/profiles/";
    private File uploadDir;

    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            // Create uploads directory if it doesn't exist
            uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) photoCircle.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Ensure upload directory exists
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = Paths.get(UPLOAD_DIR, fileName);
                
                // Copy file to uploads directory
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update UI with new photo
                Image image = new Image(targetPath.toUri().toString());
                photoCircle.setFill(new ImagePattern(image));
                
                // Save path for later update
                newPhotoPath = fileName;
                
            } catch (IOException e) {
                showError("Erreur lors du chargement de l'image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void initData(Utilisateur user) {
        this.currentUser = user;
        populateFields();
        // Load profile photo if exists
        if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
            try {
                File photoFile = new File(System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "profiles" + File.separator + currentUser.getPhotoUrl());
                System.out.println("[EditProfileController] Looking for photo at: " + photoFile.getAbsolutePath());
                if (photoFile.exists()) {
                    String uri = photoFile.toURI().toString();
                    Image image = new Image(uri);
                    if (!image.isError()) {
                        photoCircle.setFill(new ImagePattern(image));
                    } else {
                        System.err.println("[EditProfileController] Image error: " + image.getException());
                        setDefaultPhoto();
                    }
                } else {
                    System.err.println("[EditProfileController] Photo file does not exist: " + photoFile.getAbsolutePath());
                    setDefaultPhoto();
                }
            } catch (Exception e) {
                System.err.println("[EditProfileController] Erreur lors du chargement de la photo: " + e.getMessage());
                setDefaultPhoto();
            }
        } else {
            setDefaultPhoto();
        }
    }

    private void setDefaultPhoto() {
        photoCircle.setFill(javafx.scene.paint.Color.valueOf("#4CAF50"));
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
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Profil mis à jour avec succès!");
            alert.showAndWait();
            
            // Run the callback if it exists
            if (onProfileUpdated != null) {
                onProfileUpdated.run();
            }
            
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour du profil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        if (stage != null) {
            stage.close();
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

        // Vérification du mot de passe uniquement si l'utilisateur souhaite le changer
        if (!passwordField.getText().isEmpty()) {
            // Vérifier si l'ancien mot de passe est fourni
            if (oldPasswordField.getText().isEmpty()) {
                showError("Veuillez saisir votre ancien mot de passe");
                return false;
            }

            // Vérifier si l'ancien mot de passe est correct
            try {
                if (!utilisateurService.verifyPassword(currentUser.getIdUser(), oldPasswordField.getText())) {
                    showError("L'ancien mot de passe est incorrect");
                    return false;
                }
            } catch (SQLException e) {
                showError("Erreur lors de la vérification du mot de passe: " + e.getMessage());
                return false;
            }

            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showError("Les nouveaux mots de passe ne correspondent pas");
                return false;
            }

            // Vérification du format du nouveau mot de passe
            String newPassword = passwordField.getText();
            if (newPassword.length() < 6) {
                showError("Le nouveau mot de passe doit contenir au moins 6 caractères");
                return false;
            }
            
            // Vérifier la présence d'au moins une majuscule
            if (!newPassword.matches(".*[A-Z].*")) {
                showError("Le nouveau mot de passe doit contenir au moins une lettre majuscule");
                return false;
            }
            
            // Vérifier la présence d'au moins un chiffre
            if (!newPassword.matches(".*[0-9].*")) {
                showError("Le nouveau mot de passe doit contenir au moins un chiffre");
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
        
        // Update photo if changed
        if (newPhotoPath != null) {
            try {
                // Delete old photo if exists
                if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().isEmpty()) {
                    try {
                        Path oldPhotoPath = Paths.get(UPLOAD_DIR, currentUser.getPhotoUrl());
                        Files.deleteIfExists(oldPhotoPath);
                    } catch (IOException e) {
                        System.err.println("Erreur lors de la suppression de l'ancienne photo: " + e.getMessage());
                    }
                }
                currentUser.setPhotoUrl(newPhotoPath);
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour de la photo: " + e.getMessage());
                e.printStackTrace();
            }
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
            onProfileUpdated.run(); // This will navigate back in the main app
        }
        // Do NOT close the stage here
    }

    @FXML
    private void handleBack() {
        if (onProfileUpdated != null) {
            onProfileUpdated.run(); // This will navigate back in the main app
        }
    }
} 