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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Employee;
import models.Offre;
import models.Utilisateur;
import services.ServiceEmployee;
import services.ServiceOffre;
import utils.Session;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;

public class GestionEmployee implements Initializable {
    @FXML
    private TextField searchField;

    @FXML
    private ListView<Employee> employeeList;

    @FXML
    private javafx.scene.control.Button btnAjouter;

    @FXML
    private javafx.scene.control.Button btnModifier;

    @FXML
    private javafx.scene.control.Button btnSupprimer;

    @FXML
    private javafx.scene.control.Button btnPostuler;

    private ServiceEmployee serviceEmployee;
    private Utilisateur currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceEmployee = new ServiceEmployee();
        configureListView();
        loadEmployeeData();
        
        // Récupérer l'utilisateur courant depuis la session
        currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            configureAccess();
        }
    }

    private void configureAccess() {
        if (currentUser != null) {
            boolean isResponsable = "RESPONSABLE".equals(currentUser.getTypeUtilisateur());
            boolean isClient = "CLIENT".equals(currentUser.getTypeUtilisateur());
            
            // Configurer l'accès pour les boutons
            if (btnAjouter != null) btnAjouter.setVisible(isResponsable);
            if (btnModifier != null) btnModifier.setVisible(isResponsable);
            if (btnSupprimer != null) btnSupprimer.setVisible(isResponsable);
            if (btnPostuler != null) btnPostuler.setVisible(isClient);
            
            // Si c'est un client, on ne montre que les offres disponibles
            if (isClient) {
                List<Employee> employees = serviceEmployee.getAll();
                employees.removeIf(emp -> !"Disponible".equals(emp.getStatut_emploi()));
                ObservableList<Employee> filteredList = FXCollections.observableArrayList(employees);
                employeeList.setItems(filteredList);
            }
        }
    }

    private void configureListView() {
        employeeList.setCellFactory(param -> new ListCell<Employee>() {
            private final HBox hbox = new HBox();
            private final Label nom = createStyledLabel(80);
            private final Label prenom = createStyledLabel(80);
            private final Label email = createStyledLabel(140);
            private final Label telephone = createStyledLabel(80);
            private final Label dateEmb = createStyledLabel(100);
            private final Label statut = createStyledLabel(80);
            private final Label salaire = createStyledLabel(80);
            private final Label cv = createStyledLabel(100);
            private final Label offre = createStyledLabel(50);
            private final ImageView offreIcon = new ImageView();

            {
                offreIcon.setFitWidth(16);
                offreIcon.setFitHeight(16);
                hbox.getChildren().addAll(nom, prenom, email, telephone,
                        dateEmb, statut, salaire, cv, offre);
                hbox.setSpacing(0);
            }

            private Label createStyledLabel(double width) {
                Label label = new Label();
                label.setPrefWidth(width);
                label.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 1 0;");
                return label;
            }

            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                if (empty || employee == null) {
                    setGraphic(null);
                } else {
                    nom.setText(employee.getNom());
                    prenom.setText(employee.getPrenom());
                    email.setText(employee.getEmail());
                    telephone.setText(employee.getTelephone());
                    dateEmb.setText(employee.getDate_embauche().toString());
                    statut.setText(employee.getStatut_emploi());
                    salaire.setText(String.valueOf(employee.getSalaire_actuel()));

                    cv.setText(employee.getCv_path());
                    cv.setStyle("-fx-text-fill: blue; -fx-underline: true;");
                    cv.setOnMouseClicked(event -> {
                        try {
                            File file = new File(employee.getCv_path());
                            if (file.exists()) {
                                Desktop.getDesktop().open(file);
                            } else {
                                showAlert("Le fichier CV est introuvable !");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            showAlert("Erreur lors de l'ouverture du fichier");
                        }
                    });

                    // Offre column with icon
                    String offreDetails = getOffreDetails(employee.getId_offre());
                    offre.setText("");
                    Tooltip.install(offre, new Tooltip(offreDetails));
                    
                    // Gestion sécurisée de l'image
                    try {
                        Image image = new Image(getClass().getResourceAsStream("/icons/question-mark.png"));
                        if (image != null) {
                            offreIcon.setImage(image);
                        } else {
                            // Si l'image n'est pas trouvée, on utilise juste le texte
                            offre.setText("?");
                        }
                    } catch (Exception e) {
                        // En cas d'erreur, on utilise juste le texte
                        offre.setText("?");
                    }
                    offre.setGraphic(offreIcon);

                    setGraphic(hbox);
                }
            }
        });
    }

    @FXML
    private void onPostulerClicked() {
        if (!"CLIENT".equals(currentUser.getTypeUtilisateur())) {
            showAlert("Cette fonctionnalité est réservée aux clients.");
            return;
        }

        Employee selectedEmployee = employeeList.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucune offre sélectionnée");
            alert.setContentText("Veuillez sélectionner une offre à laquelle vous souhaitez postuler.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/PostulerOffre.fxml"));
            Parent root = loader.load();
            
            PostulerOffre controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setSelectedOffre(selectedEmployee);
            
            Stage stage = new Stage();
            stage.setTitle("Postuler à une offre");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture du formulaire");
            alert.setContentText("Impossible d'ouvrir le formulaire de postulation.");
            alert.showAndWait();
        }
    }

    @FXML
    private void onAjouterClicked() {
        if (!"RESPONSABLE".equals(currentUser.getTypeUtilisateur())) {
            showAlert("Vous n'avez pas les droits pour effectuer cette action.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/AjouterEmployee.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Ajouter un employé");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors du chargement de la page");
            alert.setContentText("Impossible de charger la page d'ajout d'employé.");
            alert.showAndWait();
        }
    }

    @FXML
    private void onModifierClicked() {
        if (!"RESPONSABLE".equals(currentUser.getTypeUtilisateur())) {
            showAlert("Vous n'avez pas les droits pour effectuer cette action.");
            return;
        }
        Employee selectedEmployee = employeeList.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucun employé sélectionné");
            alert.setContentText("Veuillez sélectionner un employé à modifier.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/ModifierEmployee.fxml"));
            Parent root = loader.load();
            ModifierEmployee controller = loader.getController();
            controller.setEmployee(selectedEmployee);
            
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Modifier un employé");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors du chargement de la page");
            alert.setContentText("Impossible de charger la page de modification d'employé.");
            alert.showAndWait();
        }
    }

    @FXML
    void onSupprimerClicked(ActionEvent event) {
        if (!"RESPONSABLE".equals(currentUser.getTypeUtilisateur())) {
            showAlert("Vous n'avez pas les droits pour effectuer cette action.");
            return;
        }
        Employee selectedEmp = employeeList.getSelectionModel().getSelectedItem();

        if (selectedEmp != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation de suppression");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Voulez-vous vraiment supprimer cet employé ?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    serviceEmployee.delete(selectedEmp);
                    loadEmployeeData();
                    showAlert("Employé supprimé avec succès!");
                }
            });
        } else {
            Alert errorAlert = new Alert(Alert.AlertType.WARNING);
            errorAlert.setTitle("Alerte");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Veuillez sélectionner un employé à supprimer!");
            errorAlert.showAndWait();
        }
    }

    @FXML
    void onSearchClicked(ActionEvent event) {
        String motCle = searchField.getText().trim();
        List<Employee> resultats = serviceEmployee.search(motCle);
        ObservableList<Employee> observableList = FXCollections.observableArrayList(resultats);
        employeeList.setItems(observableList);
    }

    @FXML
    void onOffreClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/Offre.fxml"));
            Parent content = loader.load();
            
            // Utiliser le MainDashboardController pour changer le contenu
            MainDashboardController mainController = MainDashboardController.getInstance();
            if (mainController != null) {
                mainController.changeContent(content, "Gestion des Offres");
            } else {
                // Fallback si le MainDashboardController n'est pas disponible
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(content);
                currentStage.setScene(scene);
                currentStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement de l'interface offre !");
        }
    }

    private String getOffreDetails(int idOffre) {
        ServiceOffre serviceOffre=new ServiceOffre();
        Offre o = serviceOffre.getOffreById(idOffre); // Make sure serviceOffre is initialized
        if (o != null) {
            return "Poste: " + o.getPoste() + "\nContrat: " + o.getType_contrat();
        }
        return "Offre non trouvée.";
    }

    private void loadEmployeeData() {
        List<Employee> employees = serviceEmployee.getAll();
        ObservableList<Employee> employeeList = FXCollections.observableArrayList(employees);
        this.employeeList.setItems(employeeList);
        searchField.setText("");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
