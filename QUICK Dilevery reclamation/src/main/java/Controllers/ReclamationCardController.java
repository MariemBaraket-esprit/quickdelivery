package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;

public class ReclamationCardController {
    @FXML private VBox rootVBox;
    @FXML private Label numberLabel;
    @FXML private Label statusLabel;
    @FXML private Label typeLabel;
    @FXML private Label descriptionLabel;
    @FXML private ImageView thumbnailView;
    @FXML private Label dateLabel;
    @FXML private Label responderLabel;
    @FXML private Label responseLabel;
    @FXML private Button editButton;

    private Runnable onEdit;
    private int reclamationId;
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Add click event to the entire card
        rootVBox.setOnMouseClicked(this::handleCardClick);

        // Consume mouse events on the edit button to prevent them from bubbling up to the VBox
        editButton.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            event.consume();
        });

        // Add cursor style to indicate the card is clickable
        rootVBox.setStyle("-fx-cursor: hand;");
    }

    /**
     * Called by SupportController to populate this card.
     */
    public void setData(int number,
                        int reclamationId,
                        String type,
                        String desc,
                        String date,
                        String status,
                        String imgPath,
                        String responder,
                        String response,
                        StackPane contentArea) {

        this.reclamationId = reclamationId;
        this.contentArea = contentArea;

        // Debug output
        System.out.println("ReclamationCardController: setData called with contentArea: " + (contentArea != null));

        numberLabel.setText("Réclamation #" + number);
        statusLabel.setText(status);

        typeLabel.setText(type);
        // Truncate description if too long
        int maxDescLength = 120;
        if (desc != null && desc.length() > maxDescLength) {
            descriptionLabel.setText(desc.substring(0, maxDescLength) + "...");
        } else {
            descriptionLabel.setText(desc);
        }

        // thumbnail
        if (imgPath != null && !imgPath.isBlank()) {
            File f = new File(imgPath);
            if (f.exists()) {
                thumbnailView.setImage(new Image(
                        f.toURI().toString(),
                        thumbnailView.getFitWidth(),
                        thumbnailView.getFitHeight(),
                        true, true
                ));
                thumbnailView.setVisible(true);
            }
        }

        dateLabel.setText(date);

        if (responder != null) {
            responderLabel.setText("Répondu par : " + responder);
            responderLabel.setVisible(true);
        }
        if (response != null) {
            responseLabel.setText(response);
            responseLabel.setVisible(true);
        }

        // Set the edit button action
        editButton.setOnAction(event -> {
            if (onEdit != null) onEdit.run();
        });
    }

    /**
     * The parent (SupportController) must call this to handle "Modifier" clicks.
     */
    public void setOnEdit(Runnable onEdit) {
        this.onEdit = onEdit;
    }

    /**
     * Handle click on the card to open AfficherReclamation.fxml
     */
    private void handleCardClick(MouseEvent event) {
        // Check if contentArea is null
        if (contentArea == null) {
            System.err.println("Error: contentArea is null in ReclamationCardController");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AfficherReclamation.fxml")
            );
            Parent root = loader.load();

            AfficherReclamationController controller = loader.getController();
            controller.setContentArea(contentArea);
            controller.loadData(reclamationId);

            contentArea.getChildren().setAll(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}