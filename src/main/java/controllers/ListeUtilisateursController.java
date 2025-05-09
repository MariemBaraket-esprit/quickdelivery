package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.util.Callback;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ListeUtilisateursController {
    @FXML
    private TableView<Utilisateur> utilisateursTable;
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
    @FXML
    private TableColumn<Utilisateur, String> fullNameColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private ComboBox<String> typeFilterCombo;
    @FXML
    private ComboBox<String> statutFilterCombo;
    @FXML
    private VBox root;

    private UtilisateurService utilisateurService;
    private ObservableList<Utilisateur> utilisateursList;
    private ObservableList<Utilisateur> filteredList = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int itemsPerPage = 7;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        try {
            // Initialisation du service
            utilisateurService = new UtilisateurService();
            
            // Initialisation de la liste observable
            utilisateursList = FXCollections.observableArrayList();
            utilisateursTable.setItems(filteredList);
            
            // Configuration des colonnes
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeUtilisateur"));
            statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
            fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

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

            // --- SEARCH ---
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                currentPage = 1;
                updateFilteredList();
            });

            // --- PAGINATION ---
            prevPageButton.setOnAction(e -> {
                if (currentPage > 1) {
                    currentPage--;
                    updateFilteredList();
                }
            });
            nextPageButton.setOnAction(e -> {
                if (currentPage < totalPages) {
                    currentPage++;
                    updateFilteredList();
                }
            });

            // --- FILTERS ---
            typeFilterCombo.getItems().addAll("Tous", "CLIENT", "AUTRES");
            typeFilterCombo.setValue("Tous");
            statutFilterCombo.getItems().addAll("Tous", "ACTIF", "INACTIF", "CONGE", "ABSENT");
            statutFilterCombo.setValue("Tous");
            typeFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentPage = 1;
                updateFilteredList();
            });
            statutFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentPage = 1;
                updateFilteredList();
            });

        } catch (SQLException e) {
            showError("Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑️");
            private final ComboBox<String> statusComboBox = new ComboBox<>();
            private final HBox container = new HBox(10); // 10 is the spacing

            {
                deleteButton.getStyleClass().add("delete-icon-button");

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
        if (utilisateurService == null) {
            showError("Erreur: Service non initialisé");
            return;
        }
        
        try {
            List<Utilisateur> users = utilisateurService.getAllUtilisateurs();
            Platform.runLater(() -> {
                utilisateursList.setAll(users);
                updateFilteredList();
            });
        } catch (SQLException e) {
            Platform.runLater(() -> showError("Erreur lors du chargement des utilisateurs: " + e.getMessage()));
        }
    }

    private void updateFilteredList() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String typeFilter = typeFilterCombo.getValue();
        String statutFilter = statutFilterCombo.getValue();
        List<Utilisateur> filtered = utilisateursList.filtered(u -> {
            boolean matchesSearch = u.getFullName().toLowerCase().contains(search) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(search)) ||
                (u.getTelephone() != null && u.getTelephone().toLowerCase().contains(search)) ||
                (u.getTypeUtilisateur() != null && u.getTypeUtilisateur().toLowerCase().contains(search));
            boolean matchesType = true;
            if (typeFilter != null && !typeFilter.equals("Tous")) {
                if (typeFilter.equals("CLIENT")) {
                    matchesType = "CLIENT".equalsIgnoreCase(u.getTypeUtilisateur());
                } else if (typeFilter.equals("AUTRES")) {
                    matchesType = u.getTypeUtilisateur() != null && !"CLIENT".equalsIgnoreCase(u.getTypeUtilisateur());
                }
            }
            boolean matchesStatut = true;
            if (statutFilter != null && !statutFilter.equals("Tous")) {
                matchesStatut = statutFilter.equalsIgnoreCase(u.getStatut());
            }
            return matchesSearch && matchesType && matchesStatut;
        });
        // Pagination
        int totalItems = filtered.size();
        totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);
        filteredList.setAll(filtered.subList(fromIndex, toIndex));
        pageInfoLabel.setText("Page " + currentPage + "/" + totalPages);
        prevPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage == totalPages);
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

    @FXML
    public void handleAjouterResponsable() {
        MainDashboardController dashboardController = MainDashboardController.getInstance();
        if (dashboardController != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterResponsable.fxml"));
                Parent content = loader.load();
                
                AjouterResponsableController controller = loader.getController();
                controller.setOnResponsableAdded(this::chargerUtilisateurs);
                
                // Mettre à jour le contenu dans le dashboard
                StackPane contentArea = dashboardController.getContentArea();
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(content);
                    // Mettre à jour le titre de la page
                    dashboardController.setPageTitle("Ajouter un Responsable");
                }
            } catch (IOException e) {
                showError("Erreur lors du chargement du formulaire d'ajout de responsable: " + e.getMessage());
            }
        } else {
            showError("Erreur: Impossible d'accéder au contrôleur principal");
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void refreshUsersList() {
        chargerUtilisateurs();
    }
} 