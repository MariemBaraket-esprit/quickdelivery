package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import utils.NotificationManager.VehicleNotification;
import java.util.List;

public class NotificationPopupController {
    @FXML private ListView<VehicleNotification> notificationList;
    @FXML private Button clearButton;
    
    public void setNotifications(List<VehicleNotification> notifications) {
        notificationList.getItems().setAll(notifications);
    }
    
    @FXML
    private void handleClearAll() {
        notificationList.getItems().clear();
    }
    
    public int getNotificationCount() {
        return notificationList.getItems().size();
    }
} 