package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ReclamationCardController {
    @FXML private VBox rootVBox;
    @FXML private Label numberLabel;
    @FXML private Label statusLabel;
    @FXML private Label typeLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label responderLabel;
    @FXML private Label responseLabel;
    @FXML private Button editButton;

    private Runnable onEdit;
    private int reclamationId;
    private StackPane contentArea;

    @FXML
    public void initialize() {
        rootVBox.setOnMouseClicked(this::handleCardClick);
        editButton.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            event.consume();
        });
        rootVBox.setStyle("-fx-cursor: hand;");
    }

    public void setData(int number,
                       int reclamationId,
                       String type,
                       String desc,
                       String date,
                       String status,
                       String responder,
                       String response,
                       StackPane contentArea) {

        this.reclamationId = reclamationId;
        this.contentArea = contentArea;

        numberLabel.setText("Réclamation #" + number);
        statusLabel.setText(status);
        typeLabel.setText(type);
        descriptionLabel.setText(desc);
        dateLabel.setText(date);

        if (responder != null) {
            responderLabel.setText("Répondu par : " + responder);
            responderLabel.setVisible(true);
        }
        if (response != null) {
            responseLabel.setText(response);
            responseLabel.setVisible(true);
        }

        editButton.setOnAction(event -> {
            if (onEdit != null) onEdit.run();
        });
    }

    public void setOnEdit(Runnable onEdit) {
        this.onEdit = onEdit;
    }
} 