package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import utils.NotificationManager;
import utils.NotificationManager.VehicleNotification;
import models.Vehicule;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.sql.*;
import java.time.LocalDate;

public class NotificationPageController {
    @FXML private ListView<VehicleNotification> notificationListView;

    public void initialize() {
        List<Vehicule> vehicules = getAllVehiculesFromDB();
        List<VehicleNotification> allNotifications = NotificationManager.checkVehicleDates(vehicules);

        // Grouper par immatriculation
        Map<String, List<VehicleNotification>> byVehicle = allNotifications.stream()
            .collect(Collectors.groupingBy(VehicleNotification::getImmatriculation));

        List<VehicleNotification> toDisplay = new java.util.ArrayList<>();
        for (Map.Entry<String, List<VehicleNotification>> entry : byVehicle.entrySet()) {
            List<VehicleNotification> notifs = entry.getValue();
            // Trouver la date la plus proche
            LocalDate minDate = notifs.stream()
                .map(VehicleNotification::getDate)
                .min(LocalDate::compareTo)
                .orElse(null);
            if (minDate != null) {
                // Ajouter toutes les notifications de cette date
                notifs.stream()
                    .filter(n -> n.getDate().equals(minDate))
                    .forEach(toDisplay::add);
            }
        }
        // Trier par date croissante
        toDisplay.sort(Comparator.comparing(VehicleNotification::getDate));

        notificationListView.getItems().setAll(toDisplay);
        notificationListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(VehicleNotification notif, boolean empty) {
                super.updateItem(notif, empty);
                if (empty || notif == null) {
                    setText(null);
                } else {
                    setText(String.format("[%s] %s - %s\n%s",
                        notif.getImmatriculation(),
                        notif.getType(),
                        notif.getDate(),
                        notif.getMessage()
                    ));
                }
            }
        });
    }

    private List<Vehicule> getAllVehiculesFromDB() {
        List<Vehicule> vehicules = new java.util.ArrayList<>();
        String sql = "SELECT * FROM vehicule";
        try (Connection conn = services.DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vehicule v = new Vehicule(
                    rs.getString("immatriculation"),
                    rs.getString("marque"),
                    rs.getString("modele"),
                    Vehicule.Statut.valueOf(rs.getString("statut")),
                    Vehicule.Type.valueOf(rs.getString("type")),
                    rs.getObject("longueur") != null ? rs.getDouble("longueur") : null,
                    rs.getObject("hauteur") != null ? rs.getDouble("hauteur") : null,
                    rs.getObject("largeur") != null ? rs.getDouble("largeur") : null,
                    rs.getObject("poids") != null ? rs.getDouble("poids") : null,
                    rs.getObject("dateEntretien") != null ? rs.getDate("dateEntretien").toLocalDate() : null,
                    rs.getObject("dateVisiteTechnique") != null ? rs.getDate("dateVisiteTechnique").toLocalDate() : null,
                    rs.getObject("dateVidange") != null ? rs.getDate("dateVidange").toLocalDate() : null,
                    rs.getObject("dateAssurance") != null ? rs.getDate("dateAssurance").toLocalDate() : null,
                    rs.getObject("dateVignette") != null ? rs.getDate("dateVignette").toLocalDate() : null
                );
                vehicules.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vehicules;
    }
} 