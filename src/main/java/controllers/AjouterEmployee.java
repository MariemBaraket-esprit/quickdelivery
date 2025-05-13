package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Employee;
import models.Offre;
import services.ServiceEmployee;
import services.ServiceOffre;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AjouterEmployee {

    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfPrenom;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfTelephone;
    @FXML
    private DatePicker tfDateEmb;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private TextField tfSalaire;
    @FXML
    private TextField tfcv;
    @FXML
    private ComboBox<String> cbOffres;

    @FXML private Button browseButton;

    private final ServiceEmployee serviceEmployee = new ServiceEmployee();
    private final ServiceOffre serviceOffre = new ServiceOffre();
    private ObservableList<Offre> offres;

    @FXML
    public void initialize() {
        cbStatut.setItems(FXCollections.observableArrayList("Actif", "Inactif"));

        offres = FXCollections.observableArrayList(serviceOffre.getAll());
        ObservableList<String> offreTitles = FXCollections.observableArrayList();
        for (Offre o : offres) {
            offreTitles.add(o.getPoste());
        }
        cbOffres.setItems(offreTitles);
    }

    @FXML
    private void onValiderClicked() {
        try {
            Employee e = new Employee();
            e.setNom(tfNom.getText());
            e.setPrenom(tfPrenom.getText());
            e.setEmail(tfEmail.getText());
            e.setTelephone(tfTelephone.getText());
            e.setDate_embauche(java.sql.Date.valueOf(tfDateEmb.getValue()));
            e.setStatut_emploi(cbStatut.getValue());
            e.setSalaire_actuel(Double.parseDouble(tfSalaire.getText()));
            e.setCv_path(tfcv.getText());

            String selectedPoste = cbOffres.getValue();
            Offre selectedOffre = offres.stream()
                    .filter(o -> o.getPoste().equals(selectedPoste))
                    .findFirst()
                    .orElse(null);

            if (selectedOffre != null) {
                e.setId_offre(selectedOffre.getId_offre());
                serviceEmployee.add(e);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Employé ajouté avec succès !");
                
                // Fermer la fenêtre actuelle
                Stage stage = (Stage) tfNom.getScene().getWindow();
                stage.close();
                
                // Recharger la page des employés
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlTessnim/Employee.fxml"));
                Parent content = loader.load();
                
                MainDashboardController mainController = MainDashboardController.getInstance();
                if (mainController != null) {
                    mainController.changeContent(content, "Gestion des Employés");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une offre valide.");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez vérifier les champs saisis.\n" + ex.getMessage());
        }
    }

    @FXML
    private void handleBrowseButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un CV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Create CVs directory if it doesn't exist
                Path destDir = Paths.get("Uploads");
                if (!Files.exists(destDir)) {
                    Files.createDirectories(destDir);
                }

                // Generate unique filename to prevent overwrites
                String filename = selectedFile.getName();
                String baseName = filename.replaceFirst("[.][^.]+$", "");
                String extension = filename.substring(filename.lastIndexOf('.'));
                Path destination;
                int counter = 1;
                do {
                    String newFilename = (counter == 1)
                            ? filename
                            : baseName + "_" + counter + extension;
                    destination = destDir.resolve(newFilename);
                    counter++;
                } while (Files.exists(destination));

                // Copy file to CVs directory
                Files.copy(
                        selectedFile.toPath(),
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );

                tfcv.setText(destination.toString());

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR,
                        "Erreur",
                        "Erreur lors de la copie du fichier: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

