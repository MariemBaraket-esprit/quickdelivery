package Controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Utilisateur;
import services.CommandeDao;
import models.Commande;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ClientCommandesController {

    @FXML
    private TableView<Commande> commandesTable;

    @FXML
    private TableColumn<Commande, LocalDate> dateColumn;

    @FXML
    private TableColumn<Commande, String> produitColumn;

    @FXML
    private TableColumn<Commande, Integer> quantiteColumn;

    @FXML
    private TableColumn<Commande, Double> totalColumn;

    @FXML
    private TableColumn<Commande, String> adresseColumn;

    @FXML
    private TableColumn<Commande, String> detailsColumn;

    @FXML
    private TableColumn<Commande, Void> actionsColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Button btnRefresh;

    private CommandeDao commandeDao;
    private ClientController clientController;
    private Utilisateur utilisateur;
    private DecimalFormat df = new DecimalFormat("0.00");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        try {
            commandeDao = new CommandeDao();
            System.out.println("Initialisation du contrôleur des commandes client");

            // Configurer les colonnes
            dateColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getDateCommande()));
            dateColumn.setCellFactory(column -> new TableCell<Commande, LocalDate>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(dateFormatter.format(date));
                    }
                }
            });

            produitColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getProduitNom()));

            quantiteColumn.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());

            totalColumn.setCellValueFactory(cellData ->
                    new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
            totalColumn.setCellFactory(column -> new TableCell<Commande, Double>() {
                @Override
                protected void updateItem(Double total, boolean empty) {
                    super.updateItem(total, empty);
                    if (empty || total == null) {
                        setText(null);
                    } else {
                        setText(df.format(total) + " DT");
                    }
                }
            });

            adresseColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getAdresse()));

            detailsColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDescription()));

            // Configuration de la colonne d'actions
            actionsColumn.setCellFactory(param -> new TableCell<Commande, Void>() {
                private final Button btnEdit = new Button("Modifier");
                private final Button btnDelete = new Button("Annuler");

                {
                    btnEdit.getStyleClass().add("button-edit");
                    btnDelete.getStyleClass().add("button-delete");

                    btnEdit.setOnAction(event -> {
                        Commande commande = getTableView().getItems().get(getIndex());
                        handleEditCommande(commande);
                    });

                    btnDelete.setOnAction(event -> {
                        Commande commande = getTableView().getItems().get(getIndex());
                        handleDeleteCommande(commande);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        // Créer un conteneur pour les boutons avec style
                        HBox buttons = new HBox(5);
                        buttons.getStyleClass().add("action-buttons-container");
                        buttons.getChildren().addAll(btnEdit, btnDelete);
                        setGraphic(buttons);
                    }
                }
            });

            System.out.println("Initialisation des colonnes terminée");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du contrôleur des commandes client: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'initialisation");
            alert.setHeaderText("Erreur lors de l'initialisation de la vue des commandes client");
            alert.setContentText("Détails: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
        System.out.println("ClientController défini dans ClientCommandesController");
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        System.out.println("Utilisateur défini dans le contrôleur des commandes client: " +
                (utilisateur != null ? "ID=" + utilisateur.getIdUser() : "null"));

        // Charger les commandes une fois que l'utilisateur est défini
        if (utilisateur != null) {
            loadCommandes();
        }
    }

    public void loadCommandes() {
        try {
            if (commandeDao == null) {
                commandeDao = new CommandeDao();
                System.out.println("CommandeDao initialisé dans loadCommandes");
            }

            List<Commande> commandes;

            // Vérifier si l'utilisateur est défini
            if (utilisateur != null) {
                // Charger uniquement les commandes de l'utilisateur actuel
                System.out.println("Chargement des commandes pour l'utilisateur ID: " + utilisateur.getIdUser());
                commandes = commandeDao.getCommandesByClient(utilisateur.getIdUser());
            } else {
                // Charger toutes les commandes si l'utilisateur n'est pas défini
                System.out.println("Utilisateur non défini, chargement de toutes les commandes");
                commandes = commandeDao.getAllCommandes();
            }

            commandesTable.setItems(FXCollections.observableArrayList(commandes));
            statusLabel.setText("Total commandes: " + commandes.size());
            System.out.println("Commandes chargées: " + commandes.size());

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des commandes: " + e.getMessage());
            e.printStackTrace();

            if (clientController != null) {
                clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                        "Impossible de charger les commandes: " + e.getMessage());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur SQL");
                alert.setHeaderText("Erreur de chargement des commandes");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void handleEditCommande(Commande commande) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/commande-edit-view.fxml"));
            Parent root = loader.load();

            CommandeEditController controller = loader.getController();
            controller.setCommande(commande);

            Stage stage = new Stage();
            stage.setTitle("Modifier une commande");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Après la fermeture de la fenêtre de modification, actualiser la liste
            stage.setOnHidden(event -> loadCommandes());

            stage.showAndWait();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre de modification: " + e.getMessage());
            e.printStackTrace();

            if (clientController != null) {
                clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                        "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur d'affichage");
                alert.setContentText("Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void handleDeleteCommande(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation d'annulation");
        alert.setHeaderText("Annuler la commande");
        alert.setContentText("Êtes-vous sûr de vouloir annuler la commande pour " + commande.getProduitNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = commandeDao.deleteCommande(commande.getId());
                if (success) {
                    loadCommandes();
                    if (clientController != null) {
                        clientController.showAlert(Alert.AlertType.INFORMATION, "Succès", "Annulation réussie",
                                "La commande a été annulée avec succès.");
                    } else {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Succès");
                        successAlert.setHeaderText("Annulation réussie");
                        successAlert.setContentText("La commande a été annulée avec succès.");
                        successAlert.showAndWait();
                    }
                } else {
                    if (clientController != null) {
                        clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'annulation",
                                "Impossible d'annuler la commande.");
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur");
                        errorAlert.setHeaderText("Erreur d'annulation");
                        errorAlert.setContentText("Impossible d'annuler la commande.");
                        errorAlert.showAndWait();
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de la suppression: " + e.getMessage());
                e.printStackTrace();

                if (clientController != null) {
                    clientController.showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur d'annulation",
                            e.getMessage());
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur SQL");
                    errorAlert.setHeaderText("Erreur d'annulation");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                }
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadCommandes();
    }
}