package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Reservation;
import models.Vehicule;
import models.Utilisateur;
import utils.DatabaseConnection;
import javafx.scene.control.Label;
import utils.Session;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class VehiculesController {
    @FXML private TableView<Vehicule> vehiculesTable;
    @FXML private TableColumn<Vehicule, String> colImmatriculation;
    @FXML private TableColumn<Vehicule, String> colMarque;
    @FXML private TableColumn<Vehicule, String> colModele;
    @FXML private TableColumn<Vehicule, Vehicule.Statut> colStatut;
    @FXML private TableColumn<Vehicule, Vehicule.Type> colType;
    @FXML private TableColumn<Vehicule, Void> colActions;
    @FXML private Button ajouterButton;
    @FXML private GridPane vehiculesGrid;
    @FXML private Label pageTitle;
    @FXML private TextField searchField;
    @FXML private ComboBox<Vehicule.Statut> statutFilterCombo;
    @FXML private ComboBox<Vehicule.Type> typeFilterCombo;

    private final ObservableList<Vehicule> vehicules = FXCollections.observableArrayList();
    private final ObservableList<Vehicule> filteredVehicules = FXCollections.observableArrayList();
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

        // Configurer l'interface selon le rôle
        if (ajouterButton != null) {
            ajouterButton.setVisible(isAdminOrResponsable);
            ajouterButton.setManaged(isAdminOrResponsable);
            ajouterButton.setOnAction(e -> {
                if (isAdminOrResponsable) {
                    showVehiculeDialog(null);
                } else {
                    showError("Accès refusé", "Vous n'avez pas les droits pour ajouter un véhicule.");
                }
            });
        }

        // Initialiser les filtres
        if (statutFilterCombo != null) {
            statutFilterCombo.getItems().clear();
            statutFilterCombo.getItems().add(null); // Pour "Tous"
            statutFilterCombo.getItems().addAll(Vehicule.Statut.values());
            statutFilterCombo.setValue(null);
        }
        if (typeFilterCombo != null) {
            typeFilterCombo.getItems().clear();
            typeFilterCombo.getItems().add(null); // Pour "Tous"
            typeFilterCombo.getItems().addAll(Vehicule.Type.values());
            typeFilterCombo.setValue(null);
        }
        // Listeners pour recherche et filtres
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        if (statutFilterCombo != null) {
            statutFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        if (typeFilterCombo != null) {
            typeFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        loadVehiculesFromDB();
    }

    private void loadVehiculesFromDB() {
        if (currentUser == null) return;

        System.out.println("[DEBUG] Loading vehicles from DB...");
        vehicules.clear();
        String sql = "SELECT * FROM vehicule";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
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
            System.out.println("[DEBUG] Number of vehicles loaded: " + vehicules.size());
            applyFilters();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de base de données", "Impossible de charger les véhicules: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyFilters() {
        filteredVehicules.clear();
        String search = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        Vehicule.Statut statut = statutFilterCombo != null ? statutFilterCombo.getValue() : null;
        Vehicule.Type type = typeFilterCombo != null ? typeFilterCombo.getValue() : null;
        for (Vehicule v : vehicules) {
            boolean match = true;
            if (!search.isEmpty()) {
                String concat = (v.getImmatriculation() + " " + v.getMarque() + " " + v.getModele() + " " + v.getStatut() + " " + v.getType()).toLowerCase();
                match &= concat.contains(search);
            }
            if (statut != null) {
                match &= v.getStatut() == statut;
            }
            if (type != null) {
                match &= v.getType() == type;
            }
            if (match) {
                filteredVehicules.add(v);
            }
        }
        refreshVehiculeCards();
    }

    private void refreshVehiculeCards() {
        if (currentUser == null) return;

        vehiculesGrid.getChildren().clear();
        int column = 0;
        int row = 0;
        
        boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                     "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        
        for (Vehicule v : filteredVehicules) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlZayen/VehiculeCard.fxml"));
                Node card = loader.load();
                VehiculeCardController controller = loader.getController();
                controller.setVehicule(v);
                
                // Configurer les actions selon le rôle
                if (isAdminOrResponsable) {
                    controller.setOnEdit(() -> showVehiculeDialog(v));
                    controller.setOnDelete(() -> {
                        deleteVehiculeFromDB(v);
                        vehicules.remove(v);
                        applyFilters();
                    });
                } else {
                    // Pour les clients, désactiver les boutons d'édition et de suppression
                    controller.setOnEdit(null);
                    controller.setOnDelete(null);
                }
                
                // Les boutons de réservation et de visualisation sont disponibles pour tous
                controller.setOnReserve(() -> showReservationDialog(v));
                controller.setOnViewReservations(() -> showReservationsList(v));
                
                vehiculesGrid.add(card, column, row);
                
                column++;
                if (column > 1) {  // After 2 cards, move to next row
                    column = 0;
                    row++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showVehiculeDialog(Vehicule vehiculeToEdit) {
        if (currentUser == null) return;

        boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                     "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        
        if (!isAdminOrResponsable) {
            showError("Accès refusé", "Vous n'avez pas les droits pour modifier les véhicules.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlZayen/VehiculeForm.fxml"));
            Node form = loader.load();
            VehiculeFormController controller = loader.getController();
            if (vehiculeToEdit != null) {
                controller.setVehicule(vehiculeToEdit);
            }
            controller.setOnSave(v -> {
                if (vehiculeToEdit == null) {
                    addVehiculeToDB(v);
                } else {
                    updateVehiculeInDB(v);
                }
                loadVehiculesFromDB();
                showVehiculeCardsView();
            });
            controller.setOnBack(this::showVehiculeCardsView);
            vehiculesGrid.getChildren().clear();
            vehiculesGrid.add(form, 0, 0);
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showVehiculeCardsView() {
        vehiculesGrid.getChildren().clear();
        refreshVehiculeCards();
    }

    private void showReservationDialog(Vehicule vehicule) {
        if (currentUser == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlZayen/ReservationForm.fxml"));
            Node form = loader.load();
            ReservationFormController controller = loader.getController();
            controller.setVehicule(vehicule);
            controller.setOnSave(r -> {
                loadVehiculesFromDB();
                showVehiculeCardsView();
            });
            controller.setOnBack(this::showVehiculeCardsView);
            vehiculesGrid.getChildren().clear();
            vehiculesGrid.add(form, 0, 0);
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire de réservation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showReservationsList(Vehicule vehicule) {
        if (currentUser == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlZayen/ReservationDetailsView.fxml"));
            Node view = loader.load();
            ReservationDetailsController controller = loader.getController();
            controller.setVehicule(vehicule);
            controller.setOnBack(this::refreshVehiculeCards);
            
            if (pageTitle != null) {
                pageTitle.setText("Réservations - " + vehicule.getImmatriculation());
            }
            vehiculesGrid.getChildren().clear();
            vehiculesGrid.add(view, 0, 0);
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la liste des réservations: " + e.getMessage());
        }
    }

    private void addVehiculeToDB(Vehicule v) {
        if (currentUser == null) return;

        boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                     "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        
        if (!isAdminOrResponsable) {
            showError("Accès refusé", "Vous n'avez pas les droits pour ajouter un véhicule.");
            return;
        }

        String sql = "INSERT INTO vehicule (immatriculation, marque, modele, statut, type, longueur, hauteur, largeur, poids, dateEntretien, dateVisiteTechnique, dateVidange, dateAssurance, dateVignette) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getImmatriculation());
            stmt.setString(2, v.getMarque());
            stmt.setString(3, v.getModele());
            stmt.setString(4, v.getStatut().name());
            stmt.setString(5, v.getType().name());
            if (v.getLongueur() != null) stmt.setDouble(6, v.getLongueur()); else stmt.setNull(6, java.sql.Types.DOUBLE);
            if (v.getHauteur() != null) stmt.setDouble(7, v.getHauteur()); else stmt.setNull(7, java.sql.Types.DOUBLE);
            if (v.getLargeur() != null) stmt.setDouble(8, v.getLargeur()); else stmt.setNull(8, java.sql.Types.DOUBLE);
            if (v.getPoids() != null) stmt.setDouble(9, v.getPoids()); else stmt.setNull(9, java.sql.Types.DOUBLE);
            if (v.getDateEntretien() != null) stmt.setDate(10, java.sql.Date.valueOf(v.getDateEntretien())); else stmt.setNull(10, java.sql.Types.DATE);
            if (v.getDateVisiteTechnique() != null) stmt.setDate(11, java.sql.Date.valueOf(v.getDateVisiteTechnique())); else stmt.setNull(11, java.sql.Types.DATE);
            if (v.getDateVidange() != null) stmt.setDate(12, java.sql.Date.valueOf(v.getDateVidange())); else stmt.setNull(12, java.sql.Types.DATE);
            if (v.getDateAssurance() != null) stmt.setDate(13, java.sql.Date.valueOf(v.getDateAssurance())); else stmt.setNull(13, java.sql.Types.DATE);
            if (v.getDateVignette() != null) stmt.setDate(14, java.sql.Date.valueOf(v.getDateVignette())); else stmt.setNull(14, java.sql.Types.DATE);
            stmt.executeUpdate();
            System.out.println("[DEBUG] Vehicle added to database: " + v.getImmatriculation());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        applyFilters();
    }

    private void updateVehiculeInDB(Vehicule v) {
        if (currentUser == null) return;

        boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                     "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        
        if (!isAdminOrResponsable) {
            showError("Accès refusé", "Vous n'avez pas les droits pour modifier un véhicule.");
            return;
        }

        String sql = "UPDATE vehicule SET marque = ?, modele = ?, statut = ?, type = ?, longueur = ?, hauteur = ?, largeur = ?, poids = ?, dateEntretien = ?, dateVisiteTechnique = ?, dateVidange = ?, dateAssurance = ?, dateVignette = ? WHERE immatriculation = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getMarque());
            stmt.setString(2, v.getModele());
            stmt.setString(3, v.getStatut().name());
            stmt.setString(4, v.getType().name());
            if (v.getLongueur() != null) stmt.setDouble(5, v.getLongueur()); else stmt.setNull(5, java.sql.Types.DOUBLE);
            if (v.getHauteur() != null) stmt.setDouble(6, v.getHauteur()); else stmt.setNull(6, java.sql.Types.DOUBLE);
            if (v.getLargeur() != null) stmt.setDouble(7, v.getLargeur()); else stmt.setNull(7, java.sql.Types.DOUBLE);
            if (v.getPoids() != null) stmt.setDouble(8, v.getPoids()); else stmt.setNull(8, java.sql.Types.DOUBLE);
            if (v.getDateEntretien() != null) stmt.setDate(9, java.sql.Date.valueOf(v.getDateEntretien())); else stmt.setNull(9, java.sql.Types.DATE);
            if (v.getDateVisiteTechnique() != null) stmt.setDate(10, java.sql.Date.valueOf(v.getDateVisiteTechnique())); else stmt.setNull(10, java.sql.Types.DATE);
            if (v.getDateVidange() != null) stmt.setDate(11, java.sql.Date.valueOf(v.getDateVidange())); else stmt.setNull(11, java.sql.Types.DATE);
            if (v.getDateAssurance() != null) stmt.setDate(12, java.sql.Date.valueOf(v.getDateAssurance())); else stmt.setNull(12, java.sql.Types.DATE);
            if (v.getDateVignette() != null) stmt.setDate(13, java.sql.Date.valueOf(v.getDateVignette())); else stmt.setNull(13, java.sql.Types.DATE);
            stmt.setString(14, v.getImmatriculation());
            stmt.executeUpdate();
            System.out.println("[DEBUG] Vehicle updated in database: " + v.getImmatriculation());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        applyFilters();
    }

    private void deleteVehiculeFromDB(Vehicule v) {
        if (currentUser == null) return;

        boolean isAdminOrResponsable = "ADMIN".equalsIgnoreCase(currentUser.getTypeUtilisateur()) || 
                                     "RESPONSABLE".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        
        if (!isAdminOrResponsable) {
            showError("Accès refusé", "Vous n'avez pas les droits pour supprimer un véhicule.");
            return;
        }

        try {
            if (hasReservations(v.getImmatriculation())) {
                showError("Impossible de supprimer", "Ce véhicule a des réservations actives.");
                return;
            }

            String sql = "DELETE FROM vehicule WHERE immatriculation = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, v.getImmatriculation());
                int affected = stmt.executeUpdate();
                if (affected > 0) {
                    showInfo("Véhicule supprimé avec succès");
                    loadVehiculesFromDB();
                } else {
                    showError("Erreur", "Le véhicule n'a pas pu être supprimé.");
                }
            }
        } catch (SQLException e) {
            showError("Erreur de base de données", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean hasReservations(String immatriculation) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE vehicule = ? AND date_fin > CURRENT_TIMESTAMP";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, immatriculation);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 