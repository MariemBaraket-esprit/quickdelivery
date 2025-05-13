package Controllers;

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
import services.DataBaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            Controllers.MainDashboardController dashboard = Controllers.MainDashboardController.getInstance();
            if (dashboard != null && dashboard.currentUser != null) {
                userId = dashboard.currentUser.getIdUser();
            }
        } catch (Exception ignored) {}
        String sql = "INSERT INTO app_ratings (user_id, rating) VALUES (?, ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setInt(2, rating);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thank you!");
        alert.setHeaderText(null);
        alert.setContentText("Thank you for rating our app!");
        alert.showAndWait();
    }

    private void showError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Failed to save your rating. Please try again later.");
        alert.showAndWait();
    }

    @FXML
    private void handleRetour() {
        // Use the same logic as handleBackToSupport
        MainDashboardController.getInstance().handleSupport();
    }

    private void goBackToSupport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Support.fxml"));
            Parent support = loader.load();
            Controllers.SupportController ctrl = loader.getController();
            ctrl.setContentArea(contentArea);
            contentArea.getChildren().setAll(support);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }
}
