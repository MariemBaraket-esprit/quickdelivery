package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Offre;
import services.ServiceOffre;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class GestionOffre implements Initializable {
    @FXML
    private Label coldatepub;

    @FXML
    private Label coldescription;

    @FXML
    private Label colformulairecondidature;

    @FXML
    private Label colposte;

    @FXML
    private Label colsalaire;

    @FXML
    private Label colstatut;

    @FXML
    private Label coltypecontrat;

    @FXML
    private Button onajouterclicked;

    @FXML
    private Button onchercherclicked8;

    @FXML
    private Button onmodifierclicked;

    @FXML
    private Button onsupprimerclicked;

    @FXML
    private TextField searchField;

    // ➕ Exemple de champs pour la saisie
    @FXML
    private TextField tfPoste;
    @FXML
    private TextField tfTypeContrat;
    @FXML
    private TextField tfDescription;
    @FXML
    private TextField tfSalaire;
    @FXML
    private TextField tfStatut;
    @FXML
    private TextField tfFormulaire;

    //Champs nécessaires pour la rempli du tableau
    @FXML
    private TableView<Offre> tableOffres;

    @FXML
    private TableColumn<Offre, String> posteColumn;
    @FXML
    private TableColumn<Offre, String> descriptionColumn;
    @FXML
    private TableColumn<Offre, String> typeContratColumn;
    @FXML
    private TableColumn<Offre, Double> salaireColumn;
    @FXML
    private TableColumn<Offre, String> statutColumn;
    @FXML
    private TableColumn<Offre, String> formulaireColumn;
    @FXML
    private TableColumn<Offre, Date> datePubColumn;

    @FXML
    private ListView<Offre> listViewOffers;

    ServiceOffre service = new ServiceOffre();

    private void loadOffersInListView() {
        List<Offre> offres = service.getAll();
        ObservableList<Offre> observableList = FXCollections.observableArrayList(offres);

        listViewOffers.setItems(observableList);
        listViewOffers.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Offre offre, boolean empty) {
                super.updateItem(offre, empty);
                if (empty || offre == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(
                            new Label("Poste: " + offre.getPoste()),
                            new Label("Salaire: " + offre.getSalaire()),
                            new Label("Statut: " + offre.getStatut())
                    );
                    vbox.setSpacing(5);
                    setGraphic(vbox);
                }
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureListView();
        loadOffers();
        setupSelectionListener();
    }

    private void loadOffers() {
        List<Offre> offres = service.getAll();
        ObservableList<Offre> observableList = FXCollections.observableArrayList(offres);
        listViewOffers.setItems(observableList);
        searchField.setText("");
    }

    private void configureListView() {
        listViewOffers.setCellFactory(param -> new ListCell<Offre>() {
            private final HBox hbox = new HBox();
            private final Label poste = createStyledLabel(120);
            private final Label description = createStyledLabel(150);
            private final Label typeContrat = createStyledLabel(120);
            private final Label salaire = createStyledLabel(80);
            private final Label statut = createStyledLabel(80);
            private final Label formulaire = createStyledLabel(150);
            private final Label datePub = createStyledLabel(100);

            {
                hbox.getChildren().addAll(poste, description, typeContrat,
                        salaire, statut, formulaire, datePub);
                hbox.setSpacing(0);
            }

            private Label createStyledLabel(double width) {
                Label label = new Label();
                label.setPrefWidth(width);
                label.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 1 0;");
                return label;
            }

            @Override
            protected void updateItem(Offre offre, boolean empty) {
                super.updateItem(offre, empty);
                if (empty || offre == null) {
                    setGraphic(null);
                } else {
                    poste.setText(offre.getPoste());
                    description.setText(offre.getDescription());
                    typeContrat.setText(offre.getType_contrat());
                    salaire.setText(String.valueOf(offre.getSalaire()));
                    statut.setText(offre.getStatut());
                    formulaire.setText(offre.getFormulaire_candidature());
                    datePub.setText(offre.getDate_publication().toString());
                    setGraphic(hbox);
                }
            }
        });
    }


    // Méthode appelée au clic sur le bouton "Ajouter"
    @FXML
    void onAjouterClicked(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxmlTessnim/AjouterOffre.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une offre");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadOffers();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement de la page d'ajout: " + e.getMessage());
        }
    }

    @FXML
    void onModifierClicked(ActionEvent event) {
        Offre selected = listViewOffers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une offre à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/ModifierOffre.fxml"));
            Parent root = loader.load();

            ModifierOffre controller = loader.getController();
            controller.setOffer(selected);

            Stage stage = new Stage();
            stage.setTitle("Modifier une offre");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadOffers();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement de la page de modification: " + e.getMessage());
        }
    }

    @FXML
    void onSupprimerClicked(ActionEvent event) {
        Offre selectedOffre = listViewOffers.getSelectionModel().getSelectedItem();

        if (selectedOffre != null) {
            // Confirmation avant suppression
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation de suppression");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Voulez-vous vraiment supprimer cette offre ?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    service.delete(selectedOffre);
                    loadOffers(); // Rafraîchir la table après suppression
                    showAlert("Offre supprimée avec succés!");
                }
            });
        } else {
            // Aucun élément sélectionné
            Alert errorAlert = new Alert(Alert.AlertType.WARNING);
            errorAlert.setTitle("Alerte");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Veuillez sélectionner une offre à supprimer.");
            errorAlert.showAndWait();
        }
    }

    @FXML
    void onsearchClicked(ActionEvent event) {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadOffers();
            return;
        }

        List<Offre> allOffers = service.getAll();
        ObservableList<Offre> filteredOffers = FXCollections.observableArrayList();

        for (Offre offre : allOffers) {
            if (offre.getPoste().toLowerCase().contains(searchText)) {
                filteredOffers.add(offre);
            }
        }

        listViewOffers.setItems(filteredOffers);
    }

    @FXML
    void onEmployeeClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/Employee.fxml"));
            Parent content = loader.load();
            
            // Utiliser le MainDashboardController pour changer le contenu
            MainDashboardController mainController = MainDashboardController.getInstance();
            if (mainController != null) {
                mainController.changeContent(content, "Gestion des Employés");
            } else {
                // Fallback si le MainDashboardController n'est pas disponible
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(content);
                currentStage.setScene(scene);
                currentStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement de la page des employés: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Ajouter un listener pour afficher les détails de l'offre sélectionnée
    private void setupSelectionListener() {
        listViewOffers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                colposte.setText(newSelection.getPoste());
                coldescription.setText(newSelection.getDescription());
                coltypecontrat.setText(newSelection.getType_contrat());
                colsalaire.setText(String.valueOf(newSelection.getSalaire()));
                colstatut.setText(newSelection.getStatut());
                colformulairecondidature.setText(newSelection.getFormulaire_candidature());
                coldatepub.setText(newSelection.getDate_publication().toString());
            }
        });
    }
}
