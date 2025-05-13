package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import models.Reservation;
import models.Vehicule;
import models.Utilisateur;
import services.DataBaseConnection;
import utils.Session;
import java.sql.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;

public class ReservationDetailsController {
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> clientColumn;
    @FXML private TableColumn<Reservation, String> dateDebutColumn;
    @FXML private TableColumn<Reservation, String> dateFinColumn;
    @FXML private TableColumn<Reservation, String> destinationColumn;
    @FXML private TableColumn<Reservation, String> statutColumn;
    @FXML private TableColumn<Reservation, Void> actionsColumn;
    @FXML private Label vehiculeLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private Button backButton;

    private Vehicule vehicule;
    private Runnable onBack;
    private Utilisateur currentUser;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        currentUser = Session.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Erreur d'authentification", "Vous devez être connecté pour accéder à cette page.");
            return;
        }

        // Vérifier les droits d'accès
        boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                     "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        
        if (!isAdminOrResponsable && !"CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur())) {
            showError("Accès refusé", "Vous n'avez pas les droits nécessaires pour accéder à cette page.");
            return;
        }

        setupTableColumns();
    }

    private void setupTableColumns() {
        // Configuration des colonnes de base
        clientColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getClient()));
        dateDebutColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDateDebut().format(formatter)));
        dateFinColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDateFin().format(formatter)));
        destinationColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDestination()));
        statutColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut()));

        // Configuration de la colonne d'actions selon le rôle
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button approveButton = new Button("Approuver");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(10, approveButton, deleteButton);

            {
                approveButton.getStyleClass().add("button-success");
                deleteButton.getStyleClass().add("button-danger");
                
                approveButton.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    if (canApproveReservation(reservation)) {
                        handleApprove(reservation);
                    } else {
                        showError("Accès refusé", "Vous n'avez pas les droits pour approuver cette réservation.");
                    }
                });
                
                deleteButton.setOnAction(e -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    if (canDeleteReservation(reservation)) {
                        handleDelete(reservation);
                    } else {
                        showError("Accès refusé", "Vous n'avez pas les droits pour supprimer cette réservation.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                                 "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
                    
                    if (isAdminOrResponsable) {
                        // Pour les admins/responsables, montrer tous les boutons
                        approveButton.setVisible(true);
                        deleteButton.setVisible(true);
                        setGraphic(buttons);
                    } else if ("CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur()) && 
                             reservation.getIdClient() == currentUser.getIdUser()) {
                        // Pour les clients, montrer uniquement le bouton de suppression pour leurs propres réservations
                        approveButton.setVisible(false);
                        deleteButton.setVisible(true);
                        setGraphic(buttons);
                    } else {
                        // Pour les autres cas, ne montrer aucun bouton
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private boolean canApproveReservation(Reservation reservation) {
        if (currentUser == null) return false;
        return "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
               "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
    }

    private boolean canDeleteReservation(Reservation reservation) {
        if (currentUser == null) return false;
        boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                     "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        return isAdminOrResponsable || 
               ("CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur()) && 
                reservation.getIdClient() == currentUser.getIdUser());
    }

    private void handleApprove(Reservation reservation) {
        if (!canApproveReservation(reservation)) {
            showError("Accès refusé", "Vous n'avez pas les droits pour approuver cette réservation.");
            return;
        }

        System.out.println("Approbation appelée pour id: " + reservation.getId());
        String sql = "UPDATE reservation SET statut = 'APPROUVEE' WHERE id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservation.getId());
            int affected = stmt.executeUpdate();
            System.out.println("Nombre de lignes affectées : " + affected);
            if (affected > 0) {
                showInfo("Réservation approuvée !");
                loadReservations();
            } else {
                showError("Erreur", "Impossible d'approuver la réservation.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'approuver la réservation : " + e.getMessage());
        }
    }

    private void handleDelete(Reservation reservation) {
        if (!canDeleteReservation(reservation)) {
            showError("Accès refusé", "Vous n'avez pas les droits pour supprimer cette réservation.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "DELETE FROM reservation WHERE id = ?";
                try (Connection conn = DataBaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, reservation.getId());
                    int affected = stmt.executeUpdate();
                    if (affected > 0) {
                        showInfo("Réservation supprimée avec succès !");
                        loadReservations();
                    } else {
                        showError("Erreur", "Impossible de supprimer la réservation.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de supprimer la réservation : " + e.getMessage());
                }
            }
        });
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        if (vehicule != null) {
            vehiculeLabel.setText("Véhicule: " + vehicule.getImmatriculation() + " - " + 
                    vehicule.getMarque() + " " + vehicule.getModele());
            loadReservations();
        }
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    private void loadReservations() {
        if (vehicule == null || currentUser == null) return;

        String sql = "SELECT r.*, u.nom as client_nom " +
                    "FROM reservation r " +
                    "LEFT JOIN utilisateur u ON r.id_client = u.id_user " +
                    "WHERE r.vehicule = ? ";

        // Ajouter le filtre pour les clients
        if ("CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur())) {
            sql += "AND r.id_client = ? ";
        }

        sql += "ORDER BY r.date_debut DESC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, vehicule.getImmatriculation());
            
            // Ajouter le paramètre pour les clients
            if ("CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur())) {
                stmt.setInt(2, currentUser.getIdUser());
            }
            
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<Reservation> reservations = FXCollections.observableArrayList();
            
            while (rs.next()) {
                Reservation reservation = new Reservation(
                    rs.getInt("id"),
                    rs.getString("numero"),
                    rs.getString("client_nom") != null ? rs.getString("client_nom") : rs.getString("client"),
                    rs.getInt("id_client"),
                    rs.getString("vehicule"),
                    rs.getTimestamp("date_debut").toLocalDateTime(),
                    rs.getTimestamp("date_fin").toLocalDateTime(),
                    rs.getString("destination"),
                    rs.getString("statut")
                );
                reservations.add(reservation);
            }

            Platform.runLater(() -> {
                reservationsTable.setItems(reservations);
                totalReservationsLabel.setText("Nombre total de réservations: " + reservations.size());
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les réservations: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        if (onBack != null) {
            onBack.run();
        }
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
} 