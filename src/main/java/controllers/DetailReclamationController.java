package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import services.DatabaseConnection;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DetailReclamationController {
    @FXML private Label typeLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView screenshotView;
    @FXML private Label dateLabel;
    @FXML private Label responderLabel;
    @FXML private Label responseLabel;

    private int reclamationId;
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Initialiser les labels avec des valeurs par défaut
        if (typeLabel != null) typeLabel.setText("");
        if (descriptionLabel != null) descriptionLabel.setText("");
        if (dateLabel != null) dateLabel.setText("");
        if (responderLabel != null) responderLabel.setText("—");
        if (responseLabel != null) responseLabel.setText("—");
    }

    public void setReclamationId(int id) {
        this.reclamationId = id;
        loadReclamationDetails();
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    private void loadReclamationDetails() {
        // On lit la dernière réponse depuis reclamations_response
        String sql = """
            SELECT r.*, u.nom, u.prenom, u.email, rr.response, rr.responder_id
            FROM reclamations r 
            LEFT JOIN utilisateur u ON r.id_utilisateur = u.id_user 
            LEFT JOIN (
                SELECT id_reclamation, response, responder_id, response_date
                FROM reclamations_response
                WHERE id_reclamation = ?
                ORDER BY response_date DESC
                LIMIT 1
            ) rr ON r.id_reclamation = rr.id_reclamation
            WHERE r.id_reclamation = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reclamationId);
            stmt.setInt(2, reclamationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean dataLoaded = false;
                // Type de réclamation
                if (typeLabel != null) {
                    String type = rs.getString("type");
                    if (type != null) {
                        typeLabel.setText(type);
                        dataLoaded = true;
                    }
                }
                // Description
                if (descriptionLabel != null) {
                    String description = rs.getString("description");
                    if (description != null) {
                        descriptionLabel.setText(description);
                        dataLoaded = true;
                    }
                }
                // Image
                String imagePath = rs.getString("image_reclamation_path");
                if (imagePath != null && !imagePath.isEmpty() && screenshotView != null) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        screenshotView.setImage(new Image(imageFile.toURI().toString()));
                        dataLoaded = true;
                    }
                }
                // Date de création
                if (dateLabel != null) {
                    String date = rs.getString("date_creation");
                    if (date != null) {
                        dateLabel.setText(date);
                        dataLoaded = true;
                    }
                }
                // Répondeur (si disponible)
                if (responderLabel != null) {
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    if (nom != null && prenom != null) {
                        responderLabel.setText(nom + " " + prenom);
                        dataLoaded = true;
                    } else {
                        responderLabel.setText("—");
                    }
                }
                // Réponse (si disponible)
                if (responseLabel != null) {
                    String response = rs.getString("response");
                    if (response != null && !response.isEmpty()) {
                        responseLabel.setText(response);
                        dataLoaded = true;
                    } else {
                        responseLabel.setText("—");
                    }
                }
                if (!dataLoaded) {
                    showError("Aucune donnée trouvée pour cette réclamation.");
                }
            } else {
                showError("Réclamation non trouvée.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des détails de la réclamation : " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlGuezguez/Support.fxml"));
            Parent supportView = loader.load();
            
            SupportController controller = loader.getController();
            controller.setContentArea(contentArea);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(supportView);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du retour à la page support.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 