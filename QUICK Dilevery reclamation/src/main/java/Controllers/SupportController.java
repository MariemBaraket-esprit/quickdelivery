package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.DataBaseConnection;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import models.ReclamationStatus;

public class SupportController {

    @FXML private FlowPane cardContainer;

    // injected by MainDashboardController
    private StackPane contentArea;

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
        System.out.println("SupportController: contentArea set: " + (contentArea != null));
        // Load cards after contentArea is set
        loadReclamationCards();
    }

    @FXML
    public void initialize() {
        System.out.println("SupportController: initialize called");
        // initialize is called before setContentArea, so we'll load cards in setContentArea
    }

    private void loadReclamationCards() {
        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null in SupportController.loadReclamationCards()");
            return;
        }

        // First check if the reclamations_reponse table exists
        boolean responseTableExists = false;

        try (Connection conn = DataBaseConnection.getConnection();
             Statement checkStmt = conn.createStatement();
             ResultSet tables = checkStmt.executeQuery("SHOW TABLES LIKE 'reclamations_reponse'")) {

            responseTableExists = tables.next(); // Will be true if table exists

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Get current user ID
        int currentUserId = MainDashboardController.getInstance().currentUser.getIdUser();

        // Use different SQL based on whether the table exists
        String sql;
        if (responseTableExists) {
            sql = """
                SELECT r.id_reclamation,
                       r.type,
                       r.description,
                       r.date_creation,
                       r.status,
                       r.image_reclamation_path,
                       rr.response_text,
                       u.nom,
                       u.prenom
                  FROM reclamations r
                  LEFT JOIN (
                       SELECT id_reclamation, response_text, responder_id
                         FROM reclamations_reponse
                        ORDER BY response_date DESC
                        LIMIT 1
                  ) rr ON r.id_reclamation = rr.id_reclamation
                  LEFT JOIN utilisateur u
                    ON rr.responder_id = u.id_user
                  WHERE r.id_utilisateur = ?
            """;
        } else {
            // Simplified query without the reclamations_reponse table
            sql = """
                SELECT r.id_reclamation,
                       r.type,
                       r.description,
                       r.date_creation,
                       r.status,
                       r.image_reclamation_path,
                       NULL as response_text,
                       NULL as nom,
                       NULL as prenom
                  FROM reclamations r
                  WHERE r.id_utilisateur = ?
            """;
        }

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            cardContainer.getChildren().clear(); // Clear existing cards

            List<VBox> cardList = new ArrayList<>();
            List<String> statusList = new ArrayList<>();
            int idx=1;
            while (rs.next()) {
                int    id     = rs.getInt("id_reclamation");
                String type   = rs.getString("type");
                String desc   = rs.getString("description");
                String date   = rs.getTimestamp("date_creation").toString();
                String status = rs.getString("status");
                String img    = rs.getString("image_reclamation_path");
                String respText = rs.getString("response_text");
                String responder = null;
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                if (nom != null && prenom != null) {
                    responder = nom + " " + prenom;
                }

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/CardTemplate.fxml")
                );
                VBox card = loader.load();

                ReclamationCardController cc = loader.getController();
                // Pass the reclamation ID and contentArea to the controller
                cc.setData(idx++, id, type, desc, date, status, img, responder, respText, contentArea);

                // open ModifierReclamation.fxml on edit
                cc.setOnEdit(() -> {
                    try {
                        FXMLLoader m = new FXMLLoader(
                                getClass().getResource("/fxml/ModifierReclamation.fxml")
                        );
                        Parent root = m.load();
                        ModifierReclamationController mc = m.getController();
                        mc.setContentArea(contentArea);
                        mc.loadReclamationData(id);
                        contentArea.getChildren().setAll(root);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                cardList.add(card);
                statusList.add(status);
            }

            // Sort cards by status using the enum order
            List<ReclamationStatus> statusOrder = Arrays.asList(
                ReclamationStatus.NEW,
                ReclamationStatus.RECEIVED,
                ReclamationStatus.IN_PROGRESS,
                ReclamationStatus.PENDING_RESPONSE,
                ReclamationStatus.RESOLVED,
                ReclamationStatus.REJECTED,
                ReclamationStatus.CLOSED
            );
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < statusList.size(); i++) indices.add(i);
            indices.sort(Comparator.comparingInt(i -> statusOrder.indexOf(
                ReclamationStatus.valueOf(statusList.get(i))
            )));
            for (int i : indices) {
                cardContainer.getChildren().add(cardList.get(i));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSupportButton() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AjouterReclamation.fxml")
            );
            Parent ajouter = loader.load();
            AjouterReclamationController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            contentArea.getChildren().setAll(ajouter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleRateUsButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Rateus.fxml"));
            Parent rateUs = loader.load();
            // Optionally, pass contentArea to RateUsController if you want a back button
            // RateUsController ctrl = loader.getController();
            // ctrl.setContentArea(contentArea);
            contentArea.getChildren().setAll(rateUs);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}