package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Employee;
import models.Offre;
import models.Utilisateur;
import services.ServiceEmployee;
import services.ServiceOffre;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PostulerOffre implements Initializable {
    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfPrenom;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfTelephone;
    @FXML
    private TextField tfCv;
    @FXML
    private Button btnParcourir;
    @FXML
    private Button btnValider;
    @FXML
    private Label lblOffreDetails;

    private Utilisateur currentUser;
    private Employee selectedOffre;
    private ServiceEmployee serviceEmployee;
    private ServiceOffre serviceOffre;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceEmployee = new ServiceEmployee();
        serviceOffre = new ServiceOffre();
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        if (user != null) {
            tfNom.setText(user.getNom());
            tfPrenom.setText(user.getPrenom());
            tfEmail.setText(user.getEmail());
            tfTelephone.setText(user.getTelephone());
        }
    }

    public void setSelectedOffre(Employee employee) {
        this.selectedOffre = employee;
        if (employee != null && lblOffreDetails != null) {
            Offre offre = serviceOffre.getOffreById(employee.getId_offre());
            if (offre != null) {
                lblOffreDetails.setText(String.format("Poste : %s\nType de contrat : %s", 
                    offre.getPoste(), offre.getType_contrat()));
            }
        }
    }

    @FXML
    private void handleBrowseButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner votre CV");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Documents Word", "*.doc", "*.docx")
        );
        
        File selectedFile = fileChooser.showOpenDialog(btnParcourir.getScene().getWindow());
        if (selectedFile != null) {
            tfCv.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onValiderClicked() {
        if (validateFields()) {
            try {
                // Créer un nouvel employé avec le statut "En attente"
                Employee employee = new Employee();
                employee.setNom(tfNom.getText());
                employee.setPrenom(tfPrenom.getText());
                employee.setEmail(tfEmail.getText());
                employee.setTelephone(tfTelephone.getText());
                employee.setCv_path(tfCv.getText());
                employee.setStatut_emploi("En attente");
                if (selectedOffre != null) {
                    employee.setId_offre(selectedOffre.getId_offre());
                }
                
                // Ajouter l'employé
                serviceEmployee.add(employee);
                
                // Afficher un message de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Votre candidature a été envoyée avec succès !");
                alert.showAndWait();
                
                // Fermer la fenêtre
                Stage stage = (Stage) btnValider.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur est survenue lors de l'envoi de votre candidature.");
                alert.showAndWait();
            }
        }
    }

    private boolean validateFields() {
        if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() || 
            tfEmail.getText().isEmpty() || tfTelephone.getText().isEmpty() || 
            tfCv.getText().isEmpty()) {
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs obligatoires.");
            alert.showAndWait();
            return false;
        }
        return true;
    }
} 