package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import services.DatabaseConnection;
import models.Utilisateur;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ModifierReclamationController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField dateCreationField;
    @FXML private TextField dateModificationField;
    @FXML private TextArea descriptionField;
    @FXML private TextField responseField;
    @FXML private ComboBox<String> typeComboBox;

    @FXML private Button chooseImageButton;
    @FXML private Button enregistrerButton;
    @FXML private Button retourButton;
    @FXML private Button annulerButton;

    @FXML private Label imageNameLabel;
    @FXML private Label feedbackLabel;
    @FXML private ImageView screenshotView;

    private File chosenImageFile;
    private StackPane contentArea;
    private int reclamationId;
    private boolean imageUpdated = false;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void loadReclamationData(int reclamationId) {
        this.reclamationId = reclamationId;

        // Récupérer le type d'utilisateur courant
        Utilisateur currentUser = MainDashboardController.getInstance().getCurrentUser();
        String typeUtilisateur = currentUser != null ? currentUser.getTypeUtilisateur() : null;
        int userId = currentUser != null ? currentUser.getIdUser() : -1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement recStmt = conn.prepareStatement(
                    "SELECT r.*, u.nom, u.prenom " +
                            "FROM reclamations r " +
                            "JOIN utilisateur u ON r.id_utilisateur = u.id_user " +
                            "WHERE r.id_reclamation = ?"
            );
            recStmt.setInt(1, reclamationId);
            ResultSet rs = recStmt.executeQuery();

            // Populate ComboBox options FIRST
            typeComboBox.getItems().setAll(
                    "Application Bug", "Application Problem", "Payment Issue", "Delivery Delay", "Other"
            );

            if (rs.next()) {
                nomField.setText(rs.getString("nom"));
                prenomField.setText(rs.getString("prenom"));
                descriptionField.setText(rs.getString("description"));
                responseField.setText("");
                typeComboBox.setValue(rs.getString("type"));
                dateCreationField.setText(rs.getTimestamp("date_creation").toString());
                dateModificationField.setText(java.time.LocalDateTime.now().toString());
                String imagePath = rs.getString("image_reclamation_path");
                if (imagePath != null && !imagePath.isBlank()) {
                    chosenImageFile = new File(imagePath);
                    if (chosenImageFile.exists()) {
                        screenshotView.setImage(new Image(chosenImageFile.toURI().toString()));
                        imageNameLabel.setText(chosenImageFile.getName());
                    }
                }
            }

            // Champ réponse : editable seulement pour ADMIN/RESPONSABLE
            if (typeUtilisateur != null && (typeUtilisateur.equalsIgnoreCase("ADMIN") || typeUtilisateur.equalsIgnoreCase("RESPONSABLE"))) {
                responseField.setEditable(true);
                enregistrerButton.setDisable(false);
            } else {
                responseField.setEditable(false);
                enregistrerButton.setDisable(true);
            }

            feedbackLabel.setVisible(false);
            imageUpdated = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        Window w = chooseImageButton.getScene().getWindow();
        File f = chooser.showOpenDialog(w);
        if (f != null) {
            chosenImageFile = f;
            imageNameLabel.setText(f.getName());
            screenshotView.setImage(new Image(f.toURI().toString(),
                    screenshotView.getFitWidth(),
                    screenshotView.getFitHeight(),
                    true, true));
            imageUpdated = true;
        }
    }

    @FXML
    private void handleSaveButtonClick() {
        feedbackLabel.setVisible(false);

        String desc = descriptionField.getText().trim();
        String type = typeComboBox.getValue();
        String response = responseField.getText();

        if (desc.isEmpty() || type == null) {
            feedbackLabel.setText("La description et le type sont requis.");
            feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-error");
            feedbackLabel.setVisible(true);
            return;
        }

        Utilisateur currentUser = MainDashboardController.getInstance().getCurrentUser();
        String typeUtilisateur = currentUser != null ? currentUser.getTypeUtilisateur() : null;
        int userId = currentUser != null ? currentUser.getIdUser() : -1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String imagePathParam = null;

            // Save new image if updated
            if (imageUpdated && chosenImageFile != null) {
                Path uploadsDir = Paths.get("uploads");
                if (!Files.exists(uploadsDir)) {
                    Files.createDirectories(uploadsDir);
                }

                Path targetPath = uploadsDir.resolve(chosenImageFile.getName());
                Files.copy(chosenImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                imagePathParam = targetPath.toString().replace("\\", "/");
            }

            // First, update the reclamation
            String sql = """
                UPDATE reclamations
                SET description = ?, 
                    type = ?, 
                    date_update = NOW(), 
                    priority = ?, 
                    image_reclamation_path = COALESCE(?, image_reclamation_path)
                WHERE id_reclamation = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, desc);
                stmt.setString(2, type);
                stmt.setString(3, "MEDIUM"); // Can be made dynamic later
                stmt.setString(4, imagePathParam);
                stmt.setInt(5, reclamationId);

                int updated = stmt.executeUpdate();

                // Si ADMIN/RESPONSABLE et réponse non vide, enregistrer dans reclamations_response
                if (typeUtilisateur != null && (typeUtilisateur.equalsIgnoreCase("ADMIN") || typeUtilisateur.equalsIgnoreCase("RESPONSABLE"))
                        && response != null && !response.trim().isEmpty()) {
                    // Vérifier si la table existe
                    boolean tableExists = false;
                    try (PreparedStatement checkStmt = conn.prepareStatement("SHOW TABLES LIKE 'reclamations_response'")) {
                        ResultSet rs = checkStmt.executeQuery();
                        tableExists = rs.next();
                    }

                    if (tableExists) {
                        // Insérer ou mettre à jour la réponse
                        String responseSql = """
                            INSERT INTO reclamations_response 
                            (id_reclamation, responder_id, response, response_date) 
                            VALUES (?, ?, ?, NOW())
                            ON DUPLICATE KEY UPDATE response = VALUES(response), response_date = NOW()
                        """;
                        try (PreparedStatement responseStmt = conn.prepareStatement(responseSql)) {
                            responseStmt.setInt(1, reclamationId);
                            responseStmt.setInt(2, userId);
                            responseStmt.setString(3, response);
                            responseStmt.executeUpdate();
                        }
                    }
                }

                if (updated > 0) {
                    feedbackLabel.setText("Réclamation modifiée avec succès.");
                    feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-success");
                    feedbackLabel.setVisible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            feedbackLabel.setText("Erreur lors de la modification : " + e.getMessage());
            feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-error");
            feedbackLabel.setVisible(true);
        }
    }

    @FXML
    private void handleAnnulerButtonClick() {
        SupportController.returnToMainPage(contentArea);
    }

    @FXML
    private void handleBackToSupport() {
        SupportController.returnToMainPage(contentArea);
    }
}