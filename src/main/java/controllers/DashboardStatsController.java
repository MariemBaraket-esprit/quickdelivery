package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import models.Utilisateur;
import services.UtilisateurService;
import services.DataBaseConnection;

public class DashboardStatsController implements Initializable {
    @FXML private Label statTotalPersonnelCount;
    @FXML private PieChart statPieChart;
    @FXML private Label statTotalClientsCount;
    @FXML private PieChart clientStatPieChart;
    @FXML private Label statTotalVehiculesCount;
    @FXML private PieChart vehiculeStatPieChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Statistiques Personnel & Client
        try {
            UtilisateurService utilisateurService = new UtilisateurService();
            List<Utilisateur> users = utilisateurService.getAllUtilisateurs();
            int totalPersonnel = 0;
            int totalClients = 0;
            int actif = 0, inactif = 0, conge = 0, absent = 0;
            int clientsActif = 0, clientsInactif = 0;
            for (Utilisateur user : users) {
                String type = user.getTypeUtilisateur();
                String statut = user.getStatut();
                if (type != null && type.equalsIgnoreCase("CLIENT")) {
                    totalClients++;
                    if (statut != null) {
                        switch (statut.toUpperCase()) {
                            case "ACTIF": clientsActif++; break;
                            case "INACTIF": clientsInactif++; break;
                        }
                    }
                } else {
                    totalPersonnel++;
                    if (statut != null) {
                        switch (statut.toUpperCase()) {
                            case "ACTIF": actif++; break;
                            case "INACTIF": inactif++; break;
                            case "CONGE": conge++; break;
                            case "ABSENT": absent++; break;
                        }
                    }
                }
            }
            statTotalPersonnelCount.setText(String.valueOf(totalPersonnel));
            statTotalClientsCount.setText(String.valueOf(totalClients));
            javafx.collections.ObservableList<PieChart.Data> pieChartData = javafx.collections.FXCollections.observableArrayList(
                new PieChart.Data("Actif", actif),
                new PieChart.Data("Inactif", inactif),
                new PieChart.Data("Congé", conge),
                new PieChart.Data("Absent", absent)
            );
            statPieChart.setData(pieChartData);
            statPieChart.setTitle("");
            statPieChart.setLegendVisible(true);
            statPieChart.setLabelsVisible(true);
            javafx.collections.ObservableList<PieChart.Data> clientPieChartData = javafx.collections.FXCollections.observableArrayList(
                new PieChart.Data("Actif", clientsActif),
                new PieChart.Data("Inactif", clientsInactif)
            );
            clientStatPieChart.setData(clientPieChartData);
            clientStatPieChart.setTitle("");
            clientStatPieChart.setLegendVisible(true);
            clientStatPieChart.setLabelsVisible(true);
            animatePieChart(pieChartData, actif + inactif + conge + absent);
            animatePieChart(clientPieChartData, clientsActif + clientsInactif);
        } catch (SQLException e) {
            statTotalPersonnelCount.setText("Erreur");
            statTotalClientsCount.setText("Erreur");
        }
        // Statistiques véhicules les plus utilisés
        Map<String, Integer> vehiculeCounts = new HashMap<>();
        int totalVehiculeReservations = 0;
        try (Connection conn = DataBaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                "SELECT v.immatriculation, v.modele, COUNT(*) as total " +
                "FROM reservation r JOIN vehicule v ON r.vehicule = v.immatriculation " +
                "GROUP BY v.immatriculation, v.modele " +
                "ORDER BY total DESC LIMIT 5")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String immat = rs.getString("immatriculation");
                String modele = rs.getString("modele");
                int count = rs.getInt("total");
                String label = modele + " (" + immat + ")";
                vehiculeCounts.put(label, count);
                totalVehiculeReservations += count;
            }
        } catch (SQLException e) {
            statTotalVehiculesCount.setText("Erreur");
        }
        statTotalVehiculesCount.setText(String.valueOf(totalVehiculeReservations));
        javafx.collections.ObservableList<PieChart.Data> vehiculePieChartData = javafx.collections.FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : vehiculeCounts.entrySet()) {
            vehiculePieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        vehiculeStatPieChart.setData(vehiculePieChartData);
        vehiculeStatPieChart.setTitle("");
        vehiculeStatPieChart.setLegendVisible(true);
        vehiculeStatPieChart.setLabelsVisible(true);
        animatePieChart(vehiculePieChartData, totalVehiculeReservations);
        for (PieChart.Data data : vehiculePieChartData) {
            double percent = (totalVehiculeReservations > 0) ? (data.getPieValue() / totalVehiculeReservations * 100) : 0;
            Tooltip.install(data.getNode(), new Tooltip(data.getName() + ": " + String.format("%.1f", percent) + "%"));
        }
    }

    // Animation homogène pour tous les camemberts
    private void animatePieChart(javafx.collections.ObservableList<PieChart.Data> pieChartData, double total) {
        for (PieChart.Data data : pieChartData) {
            if (data.getNode() != null) {
                data.getNode().setScaleX(0);
                data.getNode().setScaleY(0);
            }
        }
        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition();
        for (PieChart.Data data : pieChartData) {
            if (data.getNode() != null) {
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(400), data.getNode());
                st.setToX(1);
                st.setToY(1);
                st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                seq.getChildren().add(st);
            }
        }
        seq.play();
        for (PieChart.Data data : pieChartData) {
            if (data.getNode() != null) {
                data.getNode().setOnMouseEntered(e -> {
                    double percent = (total > 0) ? (data.getPieValue() / total * 100) : 0;
                    String originalLabel = data.getName();
                    data.setName(String.format("%s (%.1f%%)", originalLabel.split(" ")[0], percent));
                    javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), data.getNode());
                    st.setToX(1.08);
                    st.setToY(1.08);
                    st.play();
                });
                data.getNode().setOnMouseExited(e -> {
                    String originalLabel = data.getName().split(" ")[0];
                    data.setName(originalLabel);
                    javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), data.getNode());
                    st.setToX(1);
                    st.setToY(1);
                    st.play();
                });
            }
        }
    }
} 