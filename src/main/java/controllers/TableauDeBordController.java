package controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Reservation;
import utils.DatabaseConnection;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TableauDeBordController implements Initializable {

    @FXML
    private PieChart pieChart;

    @FXML
    private Label totalLabel;

    private Map<String, List<Reservation>> reservationsParType = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new Thread(() -> {
            Map<String, Integer> counts = new HashMap<>();
            int total = 0;

            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT v.type, COUNT(*) AS total " +
                                 "FROM reservation r " +
                                 "JOIN vehicule v ON r.vehicule = v.immatriculation " +
                                 "GROUP BY v.type")) {

                while (rs.next()) {
                    String type = rs.getString("type");
                    int count = rs.getInt("total");
                    counts.put(type, count);
                    total += count;
                }

                // Charger les réservations de tous les types
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT r.*, v.type, u.nom as client_nom " +
                                "FROM reservation r " +
                                "JOIN vehicule v ON r.vehicule = v.immatriculation " +
                                "LEFT JOIN utilisateur u ON r.id_client = u.id " +
                                "WHERE v.type = ? " +
                                "ORDER BY r.date_debut DESC")) {

                    for (String type : counts.keySet()) {
                        ps.setString(1, type);
                        try (ResultSet rrs = ps.executeQuery()) {
                            List<Reservation> list = new ArrayList<>();
                            while (rrs.next()) {
                                list.add(new Reservation(
                                    rrs.getInt("id"),
                                    rrs.getString("numero"),
                                    rrs.getString("client_nom"),
                                    rrs.getInt("id_client"),
                                    rrs.getString("vehicule"),
                                    rrs.getTimestamp("date_debut").toLocalDateTime(),
                                    rrs.getTimestamp("date_fin").toLocalDateTime(),
                                    rrs.getString("destination"),
                                    rrs.getString("statut")
                                ));
                            }
                            reservationsParType.put(type, list);
                        }
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible de charger les réservations : " + e.getMessage());
                    alert.showAndWait();
                });
            }

            int finalTotal = total;
            Platform.runLater(() -> {
                pieChart.getData().clear();

                int index = 0;
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    String type = entry.getKey();
                    int count = entry.getValue();
                    double pourcentage = 100.0 * count / finalTotal;

                    PieChart.Data slice = new PieChart.Data(type, count);
                    pieChart.getData().add(slice);

                    // Tooltip avec pourcentage
                    Tooltip.install(slice.getNode(), new Tooltip(type + ": " + String.format("%.1f", pourcentage) + "%"));

                    // Affichage des réservations au clic
                    slice.getNode().setOnMouseClicked(event -> showReservations(type));

                    // Animation progressive
                    slice.getNode().setOpacity(0);
                    PauseTransition delay = new PauseTransition(Duration.millis(200 * index));
                    delay.setOnFinished(e -> {
                        slice.getNode().setOpacity(1);
                        // Animate scale
                        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(800), slice.getNode());
                        st.setFromX(0);
                        st.setFromY(0);
                        st.setToX(1);
                        st.setToY(1);
                        st.play();
                    });
                    delay.play();

                    // Highlight on hover
                    slice.getNode().setOnMouseEntered(e -> slice.getNode().setStyle("-fx-effect: dropshadow(gaussian, #1976d2, 16, 0.5, 0, 2);"));
                    slice.getNode().setOnMouseExited(e -> slice.getNode().setStyle(""));
                    index++;
                }

                pieChart.getStyleClass().add("chart-pie");
                totalLabel.setText("Nombre total de réservations : " + finalTotal);
            });
        }).start();
    }

    private void showReservations(String type) {
        List<Reservation> list = reservationsParType.get(type);
        if (list == null) return;

        javafx.scene.control.TableView<Reservation> tableView = new javafx.scene.control.TableView<>();
        
        // Création des colonnes
        javafx.scene.control.TableColumn<Reservation, String> numeroCol = new javafx.scene.control.TableColumn<>("Numéro");
        javafx.scene.control.TableColumn<Reservation, String> clientCol = new javafx.scene.control.TableColumn<>("Client");
        javafx.scene.control.TableColumn<Reservation, String> vehiculeCol = new javafx.scene.control.TableColumn<>("Véhicule");
        javafx.scene.control.TableColumn<Reservation, String> dateDebutCol = new javafx.scene.control.TableColumn<>("Date Début");
        javafx.scene.control.TableColumn<Reservation, String> dateFinCol = new javafx.scene.control.TableColumn<>("Date Fin");
        javafx.scene.control.TableColumn<Reservation, String> destinationCol = new javafx.scene.control.TableColumn<>("Destination");

        // Configuration des cellules
        numeroCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNumero()));
        clientCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getClient()));
        vehiculeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getVehicule()));
        dateDebutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDateDebut().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        dateFinCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDateFin().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        destinationCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDestination()));

        // Ajout des colonnes au tableau
        tableView.getColumns().addAll(numeroCol, clientCol, vehiculeCol, dateDebutCol, dateFinCol, destinationCol);
        
        // Configuration du tableau
        tableView.getItems().addAll(list);
        tableView.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(400);

        // Style du tableau
        tableView.setStyle("-fx-background-color: white; -fx-table-cell-border-color: transparent;");
        
        // Configuration des colonnes
        for (javafx.scene.control.TableColumn<Reservation, ?> column : tableView.getColumns()) {
            column.setPrefWidth(150);
            column.setMinWidth(100);
            column.setResizable(true);
        }

        // Row highlight on hover
        tableView.setRowFactory(tv -> {
            final javafx.scene.control.TableRow<Reservation> row = new javafx.scene.control.TableRow<>();
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (isNowHovered && !row.isEmpty()) {
                    row.setStyle("-fx-background-color: #e3eafc;");
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });

        // Création de la fenêtre
        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 16; -fx-background-color: white;");
        
        // Titre avec style
        Label titleLabel = new Label("Réservations - " + type);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");
        
        // Label pour le nombre total de réservations
        Label countLabel = new Label("Nombre total de réservations : " + list.size());
        countLabel.setStyle("-fx-font-size: 14px; -fx-padding: 0 0 10 0;");
        
        box.getChildren().addAll(titleLabel, countLabel, tableView);

        // Configuration de la fenêtre
        Stage stage = new Stage();
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX() + screenBounds.getWidth() * 0.05);
        stage.setY(screenBounds.getMinY() + screenBounds.getHeight() * 0.1);
        stage.setWidth(screenBounds.getWidth() * 0.9);
        stage.setHeight(screenBounds.getHeight() * 0.8);
        stage.setScene(new Scene(box));
        stage.setTitle("Détail des réservations - " + type);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
