package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import services.DatabaseConnection;
import models.Utilisateur;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;

public class RateUsController {
    @FXML private Polygon star1;
    @FXML private Polygon star2;
    @FXML private Polygon star3;
    @FXML private Polygon star4;
    @FXML private Polygon star5;
    @FXML private Button retourBtn;
    @FXML private Text noThanksText;

    private int currentRating = 0;
    private Polygon[] stars;
    private StackPane contentArea;

    @FXML
    public void initialize() {
        stars = new Polygon[]{star1, star2, star3, star4, star5};
        updateStars(0);
        for (int i = 0; i < stars.length; i++) {
            final int idx = i;
            stars[i].setOnMouseClicked(e -> handleStarClick(idx + 1));
            stars[i].setOnMouseEntered(e -> updateStars(idx + 1));
            stars[i].setOnMouseExited(e -> updateStars(currentRating));
            stars[i].setCursor(Cursor.HAND);
        }

        // Set cursor for noThanksText
        noThanksText.setCursor(Cursor.HAND);
    }

    private void updateStars(int rating) {
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars[i].setFill(Color.GOLD);
                stars[i].setStroke(Color.GOLDENROD);
            } else {
                stars[i].setFill(Color.LIGHTGRAY);
                stars[i].setStroke(Color.GRAY);
            }
        }
    }

    private void handleStarClick(int rating) {
        currentRating = rating;
        updateStars(rating);
        boolean success = saveRatingToDatabase(rating);
        if (success) {
            showConfirmation();
            if (contentArea != null) {
                goBackToSupport();
            }
        } else {
            showError();
        }
    }

    private boolean saveRatingToDatabase(int rating) {
        Integer userId = null;
        try {
            MainDashboardController dashboard = MainDashboardController.getInstance();
            if (dashboard != null) {
                Utilisateur currentUser = dashboard.getCurrentUser();
                if (currentUser != null) {
                    userId = currentUser.getIdUser();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
        }

        String sql = "INSERT INTO app_ratings (user_id, rating) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setInt(2, rating);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rating saved successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving rating to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void showConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Merci!");
        alert.setHeaderText(null);
        alert.setContentText("Merci d'avoir noté notre application!");
        alert.showAndWait();
    }

    private void showError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText("Impossible de sauvegarder votre note. Veuillez réessayer plus tard.");
        alert.showAndWait();
    }

    @FXML
    private void handleRetour() {
        SupportController.returnToMainPage(contentArea);
    }

    private void goBackToSupport() {
        SupportController.returnToMainPage(contentArea);
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }
}
