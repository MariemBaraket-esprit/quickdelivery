package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import services.PointageService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.collections.FXCollections;

public class PointageDialogController {
    @FXML private ComboBox<Utilisateur> userComboBox;
    @FXML private RadioButton entreeRadio;
    @FXML private RadioButton sortieRadio;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private ToggleGroup typePointage;
    @FXML private CheckBox saisieManuelleCheck;
    @FXML private Button actualiserButton;

    private PointageService pointageService;
    private UtilisateurService utilisateurService;
    private Stage dialogStage;
    private boolean isValid = false;

    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            pointageService = new PointageService();

            // Charger uniquement les magasiniers et livreurs
            List<Utilisateur> users = utilisateurService.getAllUtilisateurs().stream()
                .filter(u -> "MAGASINIER".equalsIgnoreCase(u.getTypeUtilisateur()) || 
                           "LIVREUR".equalsIgnoreCase(u.getTypeUtilisateur()))
                .toList();
            
            if (users.isEmpty()) {
                System.out.println("Attention: Aucun magasinier ou livreur trouvé dans la base de données");
            } else {
                System.out.println("Nombre de magasiniers et livreurs chargés: " + users.size());
            }
            
            userComboBox.setItems(FXCollections.observableArrayList(users));

            // Configuration de l'affichage des utilisateurs dans le ComboBox
            userComboBox.setCellFactory(param -> new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getPrenom() + " " + user.getNom() + " (" + user.getTypeUtilisateur() + ")");
                    }
                }
            });
            userComboBox.setButtonCell(userComboBox.getCellFactory().call(null));

            // Configuration initiale avec la date et l'heure actuelles
            updateDateTime();

            // Configuration du mode de saisie
            saisieManuelleCheck.setSelected(false);
            datePicker.setDisable(true);
            heureField.setDisable(true);
            actualiserButton.setDisable(true);

            // Listener pour le changement de mode de saisie
            saisieManuelleCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                datePicker.setDisable(!newVal);
                heureField.setDisable(!newVal);
                actualiserButton.setDisable(!newVal);
                if (!newVal) {
                    updateDateTime();
                }
            });

            // Style du ComboBox pour une meilleure lisibilité
            userComboBox.setStyle("-fx-font-size: 14px;");
            userComboBox.setMinWidth(250);
            userComboBox.setPrefWidth(250);

        } catch (Exception e) {
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    @FXML
    private void handleActualiser() {
        updateDateTime();
    }

    private void updateDateTime() {
        datePicker.setValue(LocalDate.now());
        heureField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @FXML
    private void handleSubmit() {
        if (!validateInputs()) {
            return;
        }

        try {
            Utilisateur selectedUser = userComboBox.getValue();
            Long userId = Long.valueOf(selectedUser.getIdUser());
            boolean isEntree = entreeRadio.isSelected();

            if (saisieManuelleCheck.isSelected()) {
                // Utiliser la date et l'heure saisies manuellement
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(heureField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalDateTime dateTime = LocalDateTime.of(date, time);
                
                if (isEntree) {
                    pointageService.enregistrerEntreeManuelle(userId, dateTime);
                    showSuccess("Pointage d'entrée enregistré (saisie manuelle)");
                } else {
                    pointageService.enregistrerSortieManuelle(userId, dateTime);
                    showSuccess("Pointage de sortie enregistré (saisie manuelle)");
                }
            } else {
                // Utiliser la date et l'heure actuelles
                if (isEntree) {
                    pointageService.enregistrerEntree(userId);
                    showSuccess("Pointage d'entrée enregistré");
                } else {
                    pointageService.enregistrerSortie(userId);
                    showSuccess("Pointage de sortie enregistré");
                }
            }

            isValid = true;
            dialogStage.close();

        } catch (NumberFormatException e) {
            showError("Erreur", "ID utilisateur invalide");
        } catch (DateTimeParseException e) {
            showError("Erreur", "Format de date ou d'heure invalide");
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de l'enregistrement du pointage: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (userComboBox.getValue() == null) {
            errorMessage.append("Veuillez sélectionner un personnel\n");
        }

        if (datePicker.getValue() == null) {
            errorMessage.append("Veuillez sélectionner une date\n");
        }

        if (heureField.getText() == null || heureField.getText().trim().isEmpty()) {
            errorMessage.append("Veuillez saisir une heure\n");
        } else {
            try {
                LocalTime.parse(heureField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                errorMessage.append("Format d'heure invalide (utilisez HH:mm)\n");
            }
        }

        if (errorMessage.length() > 0) {
            showError("Données invalides", errorMessage.toString());
            return false;
        }

        return true;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isValid() {
        return isValid;
    }
} 