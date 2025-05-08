package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import services.DataBaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SupportController {

    @FXML private Button needsuppbtn;
    @FXML private HBox notificationPane;
    @FXML private Label notificationLabel;
    @FXML private FlowPane cardContainer;

    // This is set by MainDashboardController
    private Pane contentArea;

    /**
     * This method is called by the MainDashboardController to inject the content area.
     */
    public void setContentArea(Pane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        loadReclamationCards();
    }

    private void loadReclamationCards() {
        String sql = "SELECT id_reclamation, description, date_creation, status FROM reclamations";
        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int cardNumber = 1;
            while (rs.next()) {
                int id      = rs.getInt("id_reclamation");
                String desc = rs.getString("description");
                String date = rs.getString("date_creation");
                String stat = rs.getString("status");

                // 1) Load the FXML template
                FXMLLoader loader = new FXMLLoader(getClass()
                        .getResource("/fxml/CardTemplate.fxml"));
                VBox card = loader.load();

                // 2) Populate it
                ReclamationCardController cardCtrl = loader.getController();
                cardCtrl.setData(cardNumber, id, desc, date, stat);

                // 3) Attach exactly the same click handler you had before
                card.setOnMouseClicked(evt -> {
                    try {
                        FXMLLoader modLoader = new FXMLLoader(getClass()
                                .getResource("/fxml/ModifierReclamation.fxml"));
                        Parent modifierRoot = modLoader.load();

                        ModifierReclamationController modCtrl = modLoader.getController();
                        modCtrl.setContentArea(contentArea);    // inject the area
                        modCtrl.loadReclamationData(id);

                        contentArea.getChildren().setAll(modifierRoot);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                // 4) Add to the FlowPane
                cardContainer.getChildren().add(card);
                cardNumber++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @FXML
    private void handleSupportButton() {
        System.out.println("Need Support button clicked");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterReclamation.fxml"));
            Parent ajouterRoot = loader.load();

            AjouterReclamationController ajouterCtrl = loader.getController();
            ajouterCtrl.setContentArea(contentArea);  // if you want to inject the container

            contentArea.getChildren().setAll(ajouterRoot);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    /**
     * Shows an alert with the given title and message.
     */
    private void showAlert(String title, String message) {
        // You can implement an actual alert dialog here
        System.out.println(title + ": " + message);
    }
}
