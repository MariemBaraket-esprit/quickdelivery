package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ReclamationCardController {
    @FXML private Label numberLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;
    @FXML private Button editButton;

    /**
     * Called by SupportController after loading the FXML.
     */
    public void setData(int number, int id, String desc, String date, String status) {
        numberLabel.setText("Réclamation #" + number);
        descriptionLabel.setText(desc);
        dateLabel.setText(date);
        statusLabel.setText(status);

        // forward the click on the editButton to the same handler as clicking the entire card
        editButton.setOnAction(e -> cardClicked(id));
    }

    /**
     * Helper to notify the parent container that this card wants to open the modifier screen.
     * We'll look up the parent SupportController via lookup, or you can inject a callback.
     */
    private void cardClicked(int id) {
        // delegate to the outer support container
        // 👇 this lookup trick assumes the root of the card is a VBox
        ((VBox) numberLabel.getScene().lookup(".reclamation-card"))
                .fireEvent(new MouseEvent(
                        MouseEvent.MOUSE_CLICKED, 0,0,0,0,
                        MouseButton.PRIMARY, 1,
                        false,false,false,false,
                        false,false,false,false,false,false,null
                ));
        // you could also inject a Consumer<Integer> callback into this controller
    }
}
