package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import services.DataBaseConnection;

import java.io.File;
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

    /**
     * Call this after FXMLLoader.load() to populate the view.
     * @param reclamationId the id_reclamation to display
     */
    public void loadData(int reclamationId) {
        String sql = """
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

        try (Connection conn = DataBaseConnection.getConnection();
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

                // 5) Responder name
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                textnom.setText((nom != null && prenom != null)
                        ? nom + " " + prenom
                        : "—");

                // 6) Response text
                textreponse.setText(rs.getString("response_text"));
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
    private void handleBack() {
        // your logic to go back, e.g.
        MainDashboardController.getInstance().handleSupport();
    }

    @FXML
    private void handleCancel() {
        handleBack();
    }

    @FXML
    private void handleDelete() {
        // implement delete if needed
    }
}
