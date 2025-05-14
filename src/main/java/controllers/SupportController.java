package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import models.ReclamationStatus;
import javafx.scene.control.Alert;
import models.Utilisateur;

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

    // Method to return to the main support page
    public static void returnToMainPage(StackPane contentArea) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SupportController.class.getResource("/fxmlGuezguez/Support.fxml")
            );
            Parent support = loader.load();
            SupportController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            contentArea.getChildren().setAll(support);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private ReclamationStatus convertStatus(String dbStatus) {
        if (dbStatus == null) return ReclamationStatus.NEW;
        
        return switch (dbStatus.toLowerCase()) {
            case "nouveau" -> ReclamationStatus.NEW;
            case "reçu" -> ReclamationStatus.RECEIVED;
            case "en cours" -> ReclamationStatus.IN_PROGRESS;
            case "en attente de réponse" -> ReclamationStatus.PENDING_RESPONSE;
            case "résolu" -> ReclamationStatus.RESOLVED;
            case "rejeté" -> ReclamationStatus.REJECTED;
            case "fermé" -> ReclamationStatus.CLOSED;
            default -> ReclamationStatus.NEW;
        };
    }

    private void loadReclamationCards() {
        if (contentArea == null) {
            System.err.println("ERROR: contentArea is null in SupportController.loadReclamationCards()");
            return;
        }

        System.out.println("Loading reclamation cards...");

        // First check if the reclamations_response table exists
        boolean responseTableExists = false;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement checkStmt = conn.createStatement();
             ResultSet tables = checkStmt.executeQuery("SHOW TABLES LIKE 'reclamations_response'")) {

            responseTableExists = tables.next(); // Will be true if table exists
            System.out.println("reclamations_response table exists: " + responseTableExists);

            // Check reclamations table structure
            ResultSet columns = checkStmt.executeQuery("SHOW COLUMNS FROM reclamations");
            System.out.println("\nReclamations table structure:");
            while (columns.next()) {
                System.out.println(columns.getString("Field") + " - " + columns.getString("Type"));
            }

        } catch (Exception ex) {
            System.err.println("Error checking tables: " + ex.getMessage());
            ex.printStackTrace();
        }

        // Récupérer l'utilisateur courant
        Utilisateur currentUser = MainDashboardController.getInstance().getCurrentUser();
        String typeUtilisateur = currentUser != null ? currentUser.getTypeUtilisateur() : null;
        int userId = currentUser != null ? currentUser.getIdUser() : -1;

        // Use different SQL based on whether the table exists
        String sql;
        if (responseTableExists) {
            if (typeUtilisateur != null && typeUtilisateur.equalsIgnoreCase("CLIENT")) {
            sql = """
                SELECT r.id_reclamation,
                       r.type,
                       r.description,
                       r.date_creation,
                       r.status,
                       r.image_reclamation_path,
                           rr.response,
                       u.nom,
                       u.prenom
                  FROM reclamations r
                  LEFT JOIN (
                           SELECT id_reclamation, response, responder_id
                             FROM reclamations_response
                        ORDER BY response_date DESC
                        LIMIT 1
                  ) rr ON r.id_reclamation = rr.id_reclamation
                  LEFT JOIN utilisateur u
                    ON rr.responder_id = u.id_user
                  WHERE r.id_utilisateur = ?
                      ORDER BY r.date_creation DESC
                """;
            } else {
                sql = """
                    SELECT r.id_reclamation,
                           r.type,
                           r.description,
                           r.date_creation,
                           r.status,
                           r.image_reclamation_path,
                           rr.response,
                           u.nom,
                           u.prenom
                      FROM reclamations r
                      LEFT JOIN (
                           SELECT id_reclamation, response, responder_id
                             FROM reclamations_response
                            ORDER BY response_date DESC
                            LIMIT 1
                      ) rr ON r.id_reclamation = rr.id_reclamation
                      LEFT JOIN utilisateur u
                        ON rr.responder_id = u.id_user
                      ORDER BY r.date_creation DESC
            """;
            }
        } else {
            if (typeUtilisateur != null && typeUtilisateur.equalsIgnoreCase("CLIENT")) {
            sql = """
                SELECT r.id_reclamation,
                       r.type,
                       r.description,
                       r.date_creation,
                       r.status,
                       r.image_reclamation_path,
                           NULL as response,
                       NULL as nom,
                       NULL as prenom
                  FROM reclamations r
                  WHERE r.id_utilisateur = ?
                      ORDER BY r.date_creation DESC
                """;
            } else {
                sql = """
                    SELECT r.id_reclamation,
                           r.type,
                           r.description,
                           r.date_creation,
                           r.status,
                           r.image_reclamation_path,
                           NULL as response,
                           NULL as nom,
                           NULL as prenom
                      FROM reclamations r
                      ORDER BY r.date_creation DESC
            """;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (typeUtilisateur != null && typeUtilisateur.equalsIgnoreCase("CLIENT")) {
                stmt.setInt(1, userId);
            }
            ResultSet rs = stmt.executeQuery();

            cardContainer.getChildren().clear(); // Clear existing cards
            System.out.println("Cleared card container");

            List<VBox> cardList = new ArrayList<>();
            List<ReclamationStatus> statusList = new ArrayList<>();
            int idx=1;
            int cardCount = 0;
            while (rs.next()) {
                cardCount++;
                int    id     = rs.getInt("id_reclamation");
                String type   = rs.getString("type");
                String desc   = rs.getString("description");
                String date   = rs.getTimestamp("date_creation").toString();
                String status = rs.getString("status");
                String img    = rs.getString("image_reclamation_path");
                String respText = rs.getString("response");
                String responder = null;
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                if (nom != null && prenom != null) {
                    responder = nom + " " + prenom;
                }

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxmlGuezguez/CardTemplate.fxml")
                );
                VBox card = loader.load();

                ReclamationCardController cc = loader.getController();
                // Pass the reclamation ID and contentArea to the controller
                cc.setData(idx++, id, type, desc, date, status, img, responder, respText, contentArea);

                // open ModifierReclamation.fxml on edit
                cc.setOnEdit(() -> {
                    try {
                        FXMLLoader m = new FXMLLoader(
                                getClass().getResource("/fxmlGuezguez/ModifierReclamation.fxml")
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
                statusList.add(convertStatus(status));
            }
            System.out.println("Found " + cardCount + " reclamations");

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
            indices.sort(Comparator.comparingInt(i -> statusOrder.indexOf(statusList.get(i))));
            for (int i : indices) {
                cardContainer.getChildren().add(cardList.get(i));
            }
            System.out.println("Added " + cardList.size() + " cards to container");
        } catch (Exception ex) {
            System.err.println("Error loading reclamation cards: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSupportButton() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxmlGuezguez/AjouterReclamation.fxml")
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlGuezguez/Rateus.fxml"));
            Parent rateUs = loader.load();
            RateUsController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            contentArea.getChildren().setAll(rateUs);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleViewReclamation(int reclamationId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlGuezguez/DetailReclamation.fxml"));
            Parent detailView = loader.load();
            
            DetailReclamationController controller = loader.getController();
            controller.setReclamationId(reclamationId);
            controller.setContentArea(contentArea);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(detailView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des détails de la réclamation.");
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