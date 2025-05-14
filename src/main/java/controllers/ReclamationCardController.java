package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import models.ReclamationStatus;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;

public class ReclamationCardController {
    @FXML private Text typeText;
    @FXML private Text descText;
    @FXML private Text dateText;
    @FXML private Text statusText;
    @FXML private Text responderText;
    @FXML private Text responseText;
    @FXML private ImageView imageView;
    @FXML private Button editButton;
    @FXML private Button viewButton;
    @FXML private Label errorLabel;

    private int reclamationId;
    private StackPane contentArea;
    private Runnable onEditCallback;

    public void setData(int idx, int id, String type, String desc, String date, String status,
                       String imgPath, String responder, String response, StackPane contentArea) {
        this.reclamationId = id;
        this.contentArea = contentArea;

        // Set the data
        typeText.setText(idx + ". " + type);
        descText.setText(desc);
        dateText.setText(date);
        statusText.setText(status);

        // Set responder and response if available
        if (responder != null) {
            responderText.setText("Répondu par : " + responder);
        } else {
            responderText.setText("—");
        }

        if (response != null) {
            responseText.setText(response);
        } else {
            responseText.setText("—");
        }

        // Load image if available
        if (imgPath != null && !imgPath.isBlank()) {
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                imageView.setImage(new Image(imgFile.toURI().toString(),
                        imageView.getFitWidth(),
                        imageView.getFitHeight(),
                        true, true));
            }
        }

        // Set status color
        try {
            ReclamationStatus reclamationStatus = ReclamationStatus.valueOf(status.toUpperCase());
            statusText.setFill(reclamationStatus.getColor());
        } catch (IllegalArgumentException e) {
            statusText.setFill(Color.GRAY);
        }
    }

    public void setOnEdit(Runnable callback) {
        this.onEditCallback = callback;
    }

    @FXML
    private void handleEdit() {
        if (onEditCallback != null) {
            onEditCallback.run();
        }
    }

    @FXML
    private void handleView() {
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