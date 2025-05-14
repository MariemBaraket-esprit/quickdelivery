package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import services.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AfficherReclamationController {

    // Reclamation info
    @FXML private Text texttype;
    @FXML private Text textdesc;
    @FXML private ImageView imgrec;
    @FXML private Text textdate;

    // Responder info
    @FXML private Text textnom;
    @FXML private Text textreponse;

    // Buttons
    @FXML private Button backButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteButton;

    @FXML private Label errorMessage;

    private StackPane contentArea;
    private int reclamationId;

    /**
     * Set the content area for navigation
     */
    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
        System.out.println("AfficherReclamationController: contentArea set: " + (contentArea != null));
    }

    /**
     * Call this after FXMLLoader.load() to populate the view.
     * @param reclamationId the id_reclamation to display
     */
    public void loadData(int reclamationId) {
        this.reclamationId = reclamationId;

        // Check if reclamations_reponse table exists
        boolean responseTableExists = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SHOW TABLES LIKE 'reclamations_reponse'")) {

            ResultSet tables = checkStmt.executeQuery();
            responseTableExists = tables.next(); // Will be true if table exists

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Use different SQL based on whether the table exists
        String sql;
        if (responseTableExists) {
            sql = """
                SELECT 
                  r.type,
                  r.description,
                  r.image_reclamation_path,
                  r.date_creation,
                  rr.response_text,
                  rr.responder_id,
                  u.nom,
                  u.prenom
                FROM reclamations r
                LEFT JOIN reclamations_reponse rr
                  ON r.id_reclamation = rr.id_reclamation
                LEFT JOIN utilisateur u
                  ON rr.responder_id = u.id_user
                WHERE r.id_reclamation = ?
                ORDER BY rr.response_date DESC
                LIMIT 1
            """;
        } else {
            // Simplified query without the reclamations_reponse table
            sql = """
                SELECT 
                  r.type,
                  r.description,
                  r.image_reclamation_path,
                  r.date_creation
                FROM reclamations r
                WHERE r.id_reclamation = ?
            """;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reclamationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // 1) Type
                texttype.setText(rs.getString("type"));

                // 2) Description
                textdesc.setText(rs.getString("description"));

                // 3) Image
                String imgPath = rs.getString("image_reclamation_path");
                if (imgPath != null && !imgPath.isBlank()) {
                    File imgFile = new File(imgPath);
                    if (imgFile.exists()) {
                        imgrec.setImage(new Image(imgFile.toURI().toString(),
                                imgrec.getFitWidth(),
                                imgrec.getFitHeight(),
                                true, true));
                    }
                }

                // 4) Creation date
                textdate.setText(rs.getTimestamp("date_creation").toString());

                if (responseTableExists) {
                    // 5) Responder name
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    textnom.setText((nom != null && prenom != null)
                            ? nom + " " + prenom
                            : "—");

                    // 6) Response text
                    String responseText = rs.getString("response_text");
                    textreponse.setText(responseText != null ? responseText : "—");
                } else {
                    textnom.setText("—");
                    textreponse.setText("—");
                }
            } else {
                errorMessage.setText("Aucune réclamation trouvée.");
                errorMessage.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.setText("Erreur lors du chargement : " + e.getMessage());
            errorMessage.setVisible(true);
        }
    }

    @FXML
    public void handleBack() {
        // Go back to the support view
        if (contentArea != null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxmlGuezguez/Support.fxml")
                );
                Parent root = loader.load();
                SupportController controller = loader.getController();
                controller.setContentArea(contentArea);
                contentArea.getChildren().setAll(root);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            System.err.println("Error: contentArea is null in AfficherReclamationController.handleBack()");
        }
    }

    @FXML
    public void handleCancel() {
        handleBack();
    }

    @FXML
    public void handleDelete() {
        // Implement delete functionality if needed
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM reclamations WHERE id_reclamation = ?")) {

            stmt.setInt(1, reclamationId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Successfully deleted, go back to support view
                handleBack();
            } else {
                errorMessage.setText("Erreur lors de la suppression.");
                errorMessage.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.setText("Erreur lors de la suppression : " + e.getMessage());
            errorMessage.setVisible(true);
        }
    }
}