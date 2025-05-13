package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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
import java.time.ZoneId;

public class ModifierEmployee {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private TextField tfTelephone;
    @FXML private DatePicker tfDateEmb;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextField tfSalaire;
    @FXML private TextField tfcv;
    @FXML private ComboBox<String> cbOffres;
    @FXML private Button browseButton;

    private Employee currentEmployee;
    private final ServiceEmployee serviceEmployee = new ServiceEmployee();
    private final ServiceOffre serviceOffre = new ServiceOffre();

    public void setEmployee(Employee e) {
        this.currentEmployee = e;

        tfNom.setText(e.getNom());
        tfPrenom.setText(e.getPrenom());
        tfEmail.setText(e.getEmail());
        tfTelephone.setText(e.getTelephone());
        tfDateEmb.setValue(((java.sql.Date) e.getDate_embauche()).toLocalDate());
        cbStatut.setValue(e.getStatut_emploi());
        tfSalaire.setText(String.valueOf(e.getSalaire_actuel()));
        tfcv.setText(e.getCv_path());

        // Load offer title
        Offre offre = serviceOffre.getOffreById(e.getId_offre());
        if (offre != null) {
            cbOffres.setItems(FXCollections.observableArrayList(offre.getPoste()));
            cbOffres.setValue(offre.getPoste());
        }
    }

    @FXML
    private void initialize() {
        cbStatut.setItems(FXCollections.observableArrayList("actif", "inactif"));
    }

    @FXML
    private void onModifierClicked() {
        try {
            currentEmployee.setNom(tfNom.getText());
            currentEmployee.setPrenom(tfPrenom.getText());
            currentEmployee.setEmail(tfEmail.getText());
            currentEmployee.setTelephone(tfTelephone.getText());
            currentEmployee.setDate_embauche(java.sql.Date.valueOf(tfDateEmb.getValue()));
            currentEmployee.setStatut_emploi(cbStatut.getValue());
            currentEmployee.setSalaire_actuel(Double.parseDouble(tfSalaire.getText()));
            currentEmployee.setCv_path(tfcv.getText());

            serviceEmployee.update(currentEmployee);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Employé modifié avec succès !");
            ((Stage) tfNom.getScene().getWindow()).close();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification :\n" + ex.getMessage());
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
