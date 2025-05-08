package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.Utilisateur;
import services.DataBaseConnection;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AjouterReclamationController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> typeReclamationComboBox;
    @FXML private TextArea descriptionField;
    @FXML private TextField creationrecField;

    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label typeError;
    @FXML private Label descriptionError;
    @FXML private Label imageNameLabel;

    @FXML private Button chooseImageBtn;
    @FXML private Button enregistrerButton;
    @FXML private ImageView screenshotView;

    // hold the chosen file
    private File chosenImageFile;

    // injected by MainDashboardController when you do loader.getController().setContentArea(...)
    private Pane contentArea;

    /** Called by MainDashboardController right after loading FXML. */
    public void setContentArea(Pane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        // fill type combo
        typeReclamationComboBox.getItems().setAll(
                "Delivery Issue", "Damaged Product", "Wrong Address", "Late Delivery"
        );

        // pre-fill user info & date
        Utilisateur u = MainDashboardController.getInstance().currentUser;
        if (u != null) {
            nomField.setText(u.getNom());
            prenomField.setText(u.getPrenom());
            emailField.setText(u.getEmail());
        }
        creationrecField.setText(LocalDateTime.now().toString());
    }

    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg")
        );
        Window w = chooseImageBtn.getScene().getWindow();
        File f = chooser.showOpenDialog(w);
        if (f != null) {
            chosenImageFile = f;
            imageNameLabel.setText(f.getName());
            screenshotView.setImage(new Image(f.toURI().toString(),
                    screenshotView.getFitWidth(),
                    screenshotView.getFitHeight(),
                    true,true));
        }
    }

    @FXML
    private void handSaveReclamation() {
        clearErrors();

        String nom         = nomField.getText().trim();
        String prenom      = prenomField.getText().trim();
        String email       = emailField.getText().trim();
        String type        = typeReclamationComboBox.getValue();
        String description = descriptionField.getText().trim();

        boolean valid = true;
        if (nom.isEmpty())         { nomError.setText("Champ requis."); valid = false; }
        if (prenom.isEmpty())      { prenomError.setText("Champ requis."); valid = false; }
        if (email.isEmpty() || !email.contains("@")) { emailError.setText("Email invalide."); valid = false; }
        if (type == null)          { typeError.setText("Sélectionnez un type."); valid = false; }
        if (description.isEmpty()) { descriptionError.setText("Description requise."); valid = false; }
        if (!valid) return;

        try (Connection conn = DataBaseConnection.getConnection()) {
            // 1) Copy chosen image (if any) into uploads/
            String imagePath = null;
            if (chosenImageFile != null) {
                Path target = Paths.get("uploads", chosenImageFile.getName());
                Files.createDirectories(target.getParent());
                Files.copy(chosenImageFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                imagePath = target.toString();
            }

            // 2) Insert using the correct column name:
            String sql = """
            INSERT INTO reclamations
              (id_utilisateur, type, status, description, date_creation, response, priority, image_reclamation_path)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int uid = MainDashboardController.getInstance().currentUser.getIdUser();
                stmt.setInt   (1, uid);
                stmt.setString(2, type);
                stmt.setString(3, "NEW");
                stmt.setString(4, description);
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(6, "No response yet");
                stmt.setString(7, "MEDIUM");
                stmt.setString(8, imagePath);
                stmt.executeUpdate();
            }

            showSuccess("Réclamation ajoutée.");
            clearFields();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur lors de l'ajout : " + ex.getMessage());
        }
    }


    @FXML
    private void handleBackToSupport() {
        MainDashboardController.getInstance().handleSupport();
    }

    private void clearFields() {
        // keep user & date, clear others
        typeReclamationComboBox.getSelectionModel().clearSelection();
        descriptionField.clear();
        chosenImageFile = null;
        imageNameLabel.setText("(aucune image)");
        screenshotView.setImage(null);
    }
    private void clearErrors() {
        nomError.setText("");
        prenomError.setText("");
        emailError.setText("");
        typeError.setText("");
        descriptionError.setText("");
    }
    private void showSuccess(String msg)   { descriptionError.setTextFill(Color.GREEN); descriptionError.setText(msg); }
    private void showError(String msg)     { descriptionError.setTextFill(Color.RED);   descriptionError.setText(msg); }
}
