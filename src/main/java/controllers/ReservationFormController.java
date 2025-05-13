package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import services.DataBaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Reservation;
import models.Vehicule;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import models.Utilisateur;
import javafx.concurrent.Worker;
import utils.Session;
import netscape.javascript.JSObject;

public class ReservationFormController {
    @FXML private Label pageTitle;
    @FXML private Label vehiculeLabel;
    @FXML private ComboBox<Utilisateur> clientComboBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private ComboBox<String> heureDebutComboBox;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> heureFinComboBox;
    @FXML private TextField destinationField;
    @FXML private WebView mapView;
    @FXML private Button pinDepartButton;
    @FXML private Button pinDestinationButton;
    @FXML private Label distanceLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private TextField departureField;
    @FXML private TextField mapCoordinatesField;
    @FXML private Button backButton;

    private Vehicule vehicule;
    private Reservation currentReservation;
    private Consumer<Reservation> onSave;
    private Runnable onBack;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private WebEngine webEngine;
    private String departLatLng;
    private String destinationLatLng;
    private Utilisateur currentUser;

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

        setupTimeComboBoxes();
        setupMapIntegration();
        
        // Charger la carte dans le WebView
        if (mapView != null) {
            webEngine = mapView.getEngine();
            String url = getClass().getResource("/html/map.html").toExternalForm();
            webEngine.load(url);
            // Bridge Java <-> JS
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javafxConnector", new JavaConnector());
                    webEngine.executeScript("window.java = { onMapChange: function(type, address, lat, lng) { javafxConnector.onMapChange(type, address, lat, lng); } };");
                }
            });
        }
        
        // Configurer le ComboBox des clients selon le rôle
        if ("CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur())) {
            // Pour les clients, désactiver le ComboBox et le pré-remplir avec leur compte
            clientComboBox.setDisable(true);
            clientComboBox.setVisible(false);
            loadCurrentUserAsClient();
        } else if (isAdminOrResponsable) {
            // Pour les admins/responsables, charger tous les clients
            loadClients();
        }
    }

    private void loadCurrentUserAsClient() {
        if (currentUser != null) {
            ObservableList<Utilisateur> singleUser = FXCollections.observableArrayList(currentUser);
            clientComboBox.setItems(singleUser);
            clientComboBox.setValue(currentUser);
        }
    }

    private void loadClients() {
        if (currentUser == null) return;

        boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                     "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        
        if (!isAdminOrResponsable) {
            showError("Accès refusé", "Vous n'avez pas les droits pour voir la liste des clients.");
            return;
        }

        try (Connection conn = DataBaseConnection.getConnection()) {
            String sql = "SELECT * FROM utilisateur WHERE type_utilisateur = 'CLIENT'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            ObservableList<Utilisateur> clients = FXCollections.observableArrayList();
            while (rs.next()) {
                Utilisateur client = new Utilisateur(
                    rs.getInt("id_user"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("type_utilisateur")
                );
                clients.add(client);
            }
            
            clientComboBox.setItems(clients);
            
            // Si c'est une modification, sélectionner le client actuel
            if (currentReservation != null) {
                for (Utilisateur client : clients) {
                    if (client.getIdUser() == currentReservation.getIdClient()) {
                        clientComboBox.setValue(client);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la liste des clients: " + e.getMessage());
        }
    }

    private void setupTimeComboBoxes() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 60; j += 30) {
                hours.add(String.format("%02d:%02d", i, j));
            }
        }
        heureDebutComboBox.setItems(hours);
        heureFinComboBox.setItems(hours);
    }

    private void setupMapIntegration() {
        if (departureField == null || destinationField == null) return;

        // Listen for JavaFX field changes
        departureField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateMapPoint("departure", newVal);
            setPointOnMapFromJava("departure", newVal);
        });
        destinationField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateMapPoint("destination", newVal);
            setPointOnMapFromJava("destination", newVal);
        });
    }

    private void updateMapPoint(String type, String address) {
        if (mapCoordinatesField == null) return;
        // Simuler la mise à jour des coordonnées
        mapCoordinatesField.setText(type + ": " + address);
    }

    // Synchronisation Java -> JS (carte)
    private void setPointOnMapFromJava(String type, String address) {
        if (webEngine != null && address != null && !address.isBlank()) {
            String script = String.format(
                "geocode('%s', function(result) { if(result) window.setPointFromJava('%s', result.display_name, parseFloat(result.lat), parseFloat(result.lon)); });",
                address.replace("'", "\\'"), type
            );
            webEngine.executeScript(script);
        }
    }

    public class JavaConnector {
        // Called from JS when map changes
        public void onMapChange(String type, String address, double lat, double lng) {
            javafx.application.Platform.runLater(() -> {
                if ("departure".equals(type)) {
                    departureField.setText(address);
                } else if ("destination".equals(type)) {
                    destinationField.setText(address);
                }
            });
        }
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        if (vehicule != null) {
            vehiculeLabel.setText(vehicule.getImmatriculation() + " - " +
                    vehicule.getMarque() + " " + vehicule.getModele());
        }
    }

    public void setReservation(Reservation reservation) {
        if (currentUser == null) return;

        this.currentReservation = reservation;
        if (reservation != null) {
            // Pré-remplir les champs avec les données de la réservation
            dateDebutPicker.setValue(reservation.getDateDebut().toLocalDate());
            heureDebutComboBox.setValue(reservation.getDateDebut().toLocalTime().format(timeFormatter));
            dateFinPicker.setValue(reservation.getDateFin().toLocalDate());
            heureFinComboBox.setValue(reservation.getDateFin().toLocalTime().format(timeFormatter));
            destinationField.setText(reservation.getDestination());
            
            // Vérifier les droits de modification
            boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                         "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
            
            if (!isAdminOrResponsable && "CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur())) {
                if (currentUser.getIdUser() != reservation.getIdClient()) {
                    showError("Accès refusé", "Vous n'avez pas le droit de modifier cette réservation.");
                    if (onBack != null) {
                        onBack.run();
                    }
                }
            }
        }
    }

    public void setOnSave(Consumer<Reservation> onSave) {
        this.onSave = onSave;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    public void handleSave() {
        if (currentUser == null) return;

        if (!validateForm()) return;

        try {
            LocalDateTime dateDebut = LocalDateTime.of(
                dateDebutPicker.getValue(),
                LocalTime.parse(heureDebutComboBox.getValue())
            );
            LocalDateTime dateFin = LocalDateTime.of(
                dateFinPicker.getValue(),
                LocalTime.parse(heureFinComboBox.getValue())
            );

            String numero = generateReservationNumber();
            Utilisateur client = clientComboBox.getValue();
            String destination = destinationField.getText();

            // Vérifier les droits de création/modification
            boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                         "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
            
            if (!isAdminOrResponsable && "CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur())) {
                if (currentReservation != null && currentUser.getIdUser() != currentReservation.getIdClient()) {
                    showError("Accès refusé", "Vous n'avez pas le droit de modifier cette réservation.");
                    return;
                }
                if (currentReservation == null && currentUser.getIdUser() != client.getIdUser()) {
                    showError("Accès refusé", "Vous ne pouvez créer une réservation que pour votre propre compte.");
                    return;
                }
            }

            // Créer la réservation
            Reservation newReservation = new Reservation(
                0, // L'ID sera généré par la base de données
                numero,
                client.getNom(),
                client.getIdUser(),
                vehicule.getImmatriculation(),
                dateDebut,
                dateFin,
                destination,
                "EN_ATTENTE" // Statut par défaut
            );

            // Sauvegarder dans la base de données
            saveReservationToDB(newReservation);

            if (onSave != null) {
                onSave.accept(newReservation);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de sauvegarder la réservation: " + e.getMessage());
        }
    }

    private void saveReservationToDB(Reservation reservation) {
        if (currentUser == null) return;

        try (Connection conn = DataBaseConnection.getConnection()) {
            String query;
            PreparedStatement pstmt;
            if (currentReservation != null) {
                // Vérifier les droits de modification
                boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                             "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
                
                if (!isAdminOrResponsable && "CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur())) {
                    if (currentUser.getIdUser() != currentReservation.getIdClient()) {
                        showError("Accès refusé", "Vous n'avez pas le droit de modifier cette réservation.");
                        return;
                    }
                }

                query = "UPDATE reservation SET client = ?, id_client = ?, date_debut = ?, date_fin = ?, destination = ?, statut = ? WHERE numero = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, reservation.getClient());
                pstmt.setInt(2, reservation.getIdClient());
                pstmt.setTimestamp(3, java.sql.Timestamp.valueOf(reservation.getDateDebut()));
                pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(reservation.getDateFin()));
                pstmt.setString(5, reservation.getDestination());
                pstmt.setString(6, reservation.getStatut());
                pstmt.setString(7, reservation.getNumero());
            } else {
                // Vérifier les droits de création
                boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                             "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
                
                if (!isAdminOrResponsable && "CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur())) {
                    if (currentUser.getIdUser() != reservation.getIdClient()) {
                        showError("Accès refusé", "Vous ne pouvez créer une réservation que pour votre propre compte.");
                        return;
                    }
                }

                query = "INSERT INTO reservation (numero, client, id_client, vehicule, date_debut, date_fin, destination, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, reservation.getNumero());
                pstmt.setString(2, reservation.getClient());
                pstmt.setInt(3, reservation.getIdClient());
                pstmt.setString(4, reservation.getVehicule());
                pstmt.setTimestamp(5, java.sql.Timestamp.valueOf(reservation.getDateDebut()));
                pstmt.setTimestamp(6, java.sql.Timestamp.valueOf(reservation.getDateFin()));
                pstmt.setString(7, reservation.getDestination());
                pstmt.setString(8, reservation.getStatut());
            }
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                showInfo("Succès", currentReservation != null ?
                        "Réservation modifiée avec succès!" : "Réservation enregistrée avec succès!");
                if (onBack != null) {
                    onBack.run();
                }
            } else {
                showError("Erreur", "Aucune modification n'a été effectuée.");
            }
        } catch (SQLException e) {
            showError("Erreur SQL", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        if (onBack != null) {
            onBack.run();
        }
    }

    @FXML
    public void handleBack() {
        if (onBack != null) {
            onBack.run();
        }
    }

    private boolean validateForm() {
        if (clientComboBox.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner un client.");
            return false;
        }
        if (dateDebutPicker.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner une date de début.");
            return false;
        }
        if (heureDebutComboBox.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner une heure de début.");
            return false;
        }
        if (dateFinPicker.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner une date de fin.");
            return false;
        }
        if (heureFinComboBox.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner une heure de fin.");
            return false;
        }
        if (destinationField.getText().trim().isEmpty()) {
            showError("Erreur", "Veuillez saisir une destination.");
            return false;
        }

        // Vérifier que le véhicule est EN_MARCHE
        if (vehicule == null || vehicule.getStatut() == null || !"EN_MARCHE".equals(vehicule.getStatut().name())) {
            showError("Véhicule indisponible", "Ce véhicule n'est pas disponible à la réservation (statut : " + (vehicule != null ? vehicule.getStatut() : "inconnu") + ").");
            return false;
        }

        // Vérifier que la date de fin est après la date de début
        LocalDateTime dateDebut = LocalDateTime.of(
            dateDebutPicker.getValue(),
            LocalTime.parse(heureDebutComboBox.getValue())
        );
        LocalDateTime dateFin = LocalDateTime.of(
            dateFinPicker.getValue(),
            LocalTime.parse(heureFinComboBox.getValue())
        );

        if (dateFin.isBefore(dateDebut)) {
            showError("Erreur", "La date de fin doit être après la date de début.");
            return false;
        }

        // Vérifier la disponibilité du véhicule
        if (!isVehicleAvailable(vehicule.getImmatriculation(), dateDebut, dateFin)) {
            // Chercher la prochaine date de disponibilité
            LocalDateTime nextAvailable = getNextAvailableDate(vehicule.getImmatriculation(), dateDebut, dateFin);
            String msg = "Le véhicule n'est pas disponible pour cette période.";
            if (nextAvailable != null) {
                msg += " Il sera disponible à partir du " + nextAvailable.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ".";
            }
            showError("Erreur", msg);
            return false;
        }

        return true;
    }

    private boolean isVehicleAvailable(String immatriculation, LocalDateTime dateDebut, LocalDateTime dateFin) {
        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM reservation WHERE vehicule = ? AND statut != 'ANNULEE' AND " +
                          "((date_debut <= ? AND date_fin >= ?) OR " +
                          "(date_debut <= ? AND date_fin >= ?) OR " +
                          "(date_debut >= ? AND date_fin <= ?))";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, immatriculation);
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(dateFin));
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(dateFin));
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(7, java.sql.Timestamp.valueOf(dateFin));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de vérifier la disponibilité du véhicule: " + e.getMessage());
        }
        return false;
    }

    // Nouvelle méthode pour trouver la prochaine date de disponibilité
    private LocalDateTime getNextAvailableDate(String immatriculation, LocalDateTime dateDebut, LocalDateTime dateFin) {
        try (Connection conn = DataBaseConnection.getConnection()) {
            String query = "SELECT MAX(date_fin) as max_fin FROM reservation WHERE vehicule = ? AND statut != 'ANNULEE' AND " +
                          "((date_debut <= ? AND date_fin >= ?) OR " +
                          "(date_debut <= ? AND date_fin >= ?) OR " +
                          "(date_debut >= ? AND date_fin <= ?))";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, immatriculation);
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(dateFin));
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(dateFin));
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(7, java.sql.Timestamp.valueOf(dateFin));
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getTimestamp("max_fin") != null) {
                return rs.getTimestamp("max_fin").toLocalDateTime();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateReservationNumber() {
        return "RES-" + System.currentTimeMillis();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handlePinDepart() {
        if (webEngine == null) {
            showError("Erreur", "La carte n'est pas initialisée. Veuillez réessayer.");
            return;
        }
        webEngine.executeScript("window.setDepartMode()");
    }

    @FXML
    private void handlePinDestination() {
        if (webEngine == null) {
            showError("Erreur", "La carte n'est pas initialisée. Veuillez réessayer.");
            return;
        }
        webEngine.executeScript("window.setDestinationMode()");
    }
}
