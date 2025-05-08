package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import services.DataBaseConnection;

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
    private Pane contentArea;
    private int reclamationId;
    private boolean imageUpdated = false;

    public void setContentArea(Pane contentArea) {
        this.contentArea = contentArea;
    }

    public void loadReclamationData(int reclamationId) {
        this.reclamationId = reclamationId;

        try (Connection conn = DataBaseConnection.getConnection()) {
            PreparedStatement recStmt = conn.prepareStatement(
                    "SELECT r.*, u.nom, u.prenom " +
                            "FROM reclamations r " +
                            "JOIN utilisateur u ON r.id_utilisateur = u.id_user " +
                            "WHERE r.id_reclamation = ?"
            );
            recStmt.setInt(1, reclamationId);
            ResultSet rs = recStmt.executeQuery();

            if (rs.next()) {
                nomField.setText(rs.getString("nom"));
                prenomField.setText(rs.getString("prenom"));
                descriptionField.setText(rs.getString("description"));
                responseField.setText(rs.getString("response"));
                typeComboBox.setValue(rs.getString("type"));
                dateCreationField.setText(rs.getTimestamp("date_creation").toString());

                if (rs.getTimestamp("date_update") != null) {
                    dateModificationField.setText(rs.getTimestamp("date_update").toString());
                }

                String imagePath = rs.getString("image_reclamation_path");
                if (imagePath != null && !imagePath.isBlank()) {
                    chosenImageFile = new File(imagePath);
                    if (chosenImageFile.exists()) {
                        screenshotView.setImage(new Image(chosenImageFile.toURI().toString()));
                        imageNameLabel.setText(chosenImageFile.getName());
                    }
                }
            }

            // Populate ComboBox options
            typeComboBox.getItems().setAll(
                    "Application Bug", "Application Problem", "Payment Issue", "Delivery Delay", "Other"
            );

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

        try (Connection conn = DataBaseConnection.getConnection()) {
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

            String sql = """
                UPDATE reclamations
                SET description = ?, 
                    type = ?, 
                    date_update = NOW(), 
                    response = ?, 
                    priority = ?, 
                    image_reclamation_path = COALESCE(?, image_reclamation_path)
                WHERE id_reclamation = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, desc);
                stmt.setString(2, type);
                stmt.setString(3, response);
                stmt.setString(4, "MEDIUM"); // Can be made dynamic later
                stmt.setString(5, imagePathParam);
                stmt.setInt(6, reclamationId);

                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    feedbackLabel.setText("Modifié avec succès !");
                    feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-success");
                    imageUpdated = false;
                } else {
                    feedbackLabel.setText("Aucune modification effectuée.");
                    feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-error");
                }
                feedbackLabel.setVisible(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            feedbackLabel.setText("Erreur lors de la sauvegarde de l'image.");
            feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-error");
            feedbackLabel.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            feedbackLabel.setText("Erreur : " + e.getMessage());
            feedbackLabel.getStyleClass().setAll("feedback-label", "feedback-error");
            feedbackLabel.setVisible(true);
        }
    }

    @FXML
    private void handleAnnulerButtonClick() {
        MainDashboardController.getInstance().handleSupport();
    }

    @FXML
    private void handleBackToSupport() {
        MainDashboardController.getInstance().handleSupport();
    }
}
