package Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Utilisateur;
import services.UtilisateurService;

import java.sql.SQLException;
import java.util.List;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

public class ListeUtilisateurController {
    @FXML
    private TableView<Utilisateur> utilisateursTable;
    @FXML
    private TableColumn<Utilisateur, Integer> idColumn;
    @FXML
    private TableColumn<Utilisateur, String> emailColumn;
    @FXML
    private TableColumn<Utilisateur, String> telephoneColumn;
    @FXML
    private TableColumn<Utilisateur, String> typeColumn;
    @FXML
    private TableColumn<Utilisateur, String> statutColumn;
    @FXML
    private TableColumn<Utilisateur, Void> actionsColumn;

    private UtilisateurService utilisateurService;
    private ObservableList<Utilisateur> utilisateursList;



    @FXML
    public void initialize() {
        try {
            // Initialisation du service
            utilisateurService = new UtilisateurService();

            // Initialisation de la liste observable
            utilisateursList = FXCollections.observableArrayList();
            utilisateursTable.setItems(utilisateursList);

            // Configuration des colonnes
            idColumn.setCellValueFactory(new PropertyValueFactory<>("idUser"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeUtilisateur"));
            statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

            // Custom cell factory for type column
            typeColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String type, boolean empty) {
                    super.updateItem(type, empty);
                    if (empty || type == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().clear();
                    } else {
                        setText(type);
                        getStyleClass().clear();
                        getStyleClass().add("type-" + type.toLowerCase());
                    }
                }
            });

            // Custom cell factory for status column
            statutColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String statut, boolean empty) {
                    super.updateItem(statut, empty);
                    if (empty || statut == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().clear();
                    } else {
                        setText(statut);
                        getStyleClass().clear();
                        getStyleClass().add("status-" + statut.toLowerCase());
                    }
                }
            });

            // Configuration de la colonne d'actions
            setupActionsColumn();

            // Configuration des styles de cellules
            idColumn.setStyle("-fx-alignment: CENTER;");
            emailColumn.setStyle("-fx-alignment: CENTER-LEFT;");
            telephoneColumn.setStyle("-fx-alignment: CENTER;");
            typeColumn.setStyle("-fx-alignment: CENTER;");
            statutColumn.setStyle("-fx-alignment: CENTER;");

            // Configuration des tooltips
            emailColumn.setCellFactory(column -> {
                TableCell<Utilisateur, String> cell = new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);
                            setTooltip(new Tooltip(item));
                        }
                    }
                };
                return cell;
            });

            // Premier chargement des données
            Platform.runLater(this::chargerUtilisateurs);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");
            private final ComboBox<String> statusComboBox = new ComboBox<>();
            private final HBox container = new HBox(10); // 10 is the spacing

            {
                deleteButton.getStyleClass().add("button-supprimer");
                deleteButton.setOnAction(event -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    handleDelete(utilisateur);
                });

                // Setup status combo box
                statusComboBox.getItems().addAll("ACTIF", "INACTIF", "CONGE", "ABSENT");
                statusComboBox.setMaxWidth(120);
                statusComboBox.getStyleClass().add("combo-box-custom");

                statusComboBox.setOnAction(event -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    String newStatus = statusComboBox.getValue();
                    if (newStatus != null && !newStatus.equals(utilisateur.getStatut())) {
                        handleStatusChange(utilisateur, newStatus);
                    }
                });

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(statusComboBox, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    statusComboBox.setValue(utilisateur.getStatut());
                    setGraphic(container);
                }
            }
        });
    }

    private void handleDelete(Utilisateur utilisateur) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur " + utilisateur.getEmail() + " ?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    utilisateurService.deleteUtilisateur(utilisateur.getIdUser());
                    Platform.runLater(this::chargerUtilisateurs);
                    showSuccessAlert("Succès", "L'utilisateur a été supprimé avec succès.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void handleStatusChange(Utilisateur utilisateur, String newStatus) {
        try {
            utilisateur.setStatut(newStatus);
            utilisateurService.modifierUtilisateur(utilisateur);

            // Update the sidebar if the status change is for the current user
            MainDashboardController dashboardController = MainDashboardController.getInstance();
            if (dashboardController != null) {
                dashboardController.updateCurrentUser(utilisateur);
            }

            // Refresh the table
            Platform.runLater(this::chargerUtilisateurs);

            showSuccessAlert("Succès", "Le statut a été mis à jour avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la mise à jour du statut: " + e.getMessage());
            // Refresh the table to ensure consistent state
            Platform.runLater(this::chargerUtilisateurs);
        }
    }

    private void showSuccessAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void chargerUtilisateurs() {
        try {
            // Récupération des utilisateurs
            List<Utilisateur> nouveauxUtilisateurs = utilisateurService.getAllUtilisateurs();

            // Mise à jour de la liste sur le thread JavaFX
            Platform.runLater(() -> {
                utilisateursList.clear();
                utilisateursList.addAll(nouveauxUtilisateurs);
                utilisateursTable.refresh();
                System.out.println("Nombre d'utilisateurs chargés : " + utilisateursList.size());
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    showAlert("Erreur", "Erreur lors du chargement des utilisateurs: " + e.getMessage())
            );
        }
    }

    @FXML
    private void handleUpdate() {
        Utilisateur selectedUser = utilisateursTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            MainDashboardController dashboardController = MainDashboardController.getInstance();
            if (dashboardController != null) {
                dashboardController.handleModifierUtilisateur(selectedUser);
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner un utilisateur à modifier");
        }
    }

    @FXML
    private void handleAjouterUtilisateur() {
        MainDashboardController dashboardController = MainDashboardController.getInstance();
        if (dashboardController != null) {
            dashboardController.handleAjouterUtilisateur();
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}