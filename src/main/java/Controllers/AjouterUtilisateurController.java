package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class AjouterUtilisateurController {
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField adresseField;
    @FXML
    private DatePicker dateDebutContratPicker;
    @FXML
    private DatePicker dateFinContratPicker;
    @FXML
    private DatePicker dateNaissancePicker;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField salaireField;
    @FXML
    private ComboBox<String> typeUtilisateurComboBox;
    @FXML
    private ComboBox<String> statutComboBox;
    @FXML
    private Button enregistrerButton;
    @FXML
    private Label nomError;
    @FXML
    private Label prenomError;
    @FXML
    private Label emailError;
    @FXML
    private Label telephoneError;
    @FXML
    private Label adresseError;
    @FXML
    private Label typeError;
    @FXML
    private Label statutError;

    private UtilisateurService utilisateurService;
    private Utilisateur utilisateurToUpdate;
    private boolean isUpdateMode = false;
    private Runnable onUserAdded;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();

            // Initialize type utilisateur combo box with correct enum values
            typeUtilisateurComboBox.setItems(FXCollections.observableArrayList(
                    "ADMIN", "RESPONSABLE", "CLIENT", "LIVREUR", "MAGASINIER"
            ));

            // Initialize statut combo box
            statutComboBox.setItems(FXCollections.observableArrayList(
                    "CONGE", "ACTIF", "INACTIF", "ABSENT"
            ));

            // Set default value for statut in add mode
            if (!isUpdateMode) {
                statutComboBox.setValue("ACTIF");
            }

            // Add real-time validation listeners
            setupValidationListeners();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void setupValidationListeners() {
        // Nom validation
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (newValue.length() < 2) {
                    nomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    showTooltip(nomField, "Le nom doit contenir au moins 2 caractères");
                } else if (!newValue.matches("[\\p{L} -]+")) {
                    nomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    showTooltip(nomField, "Le nom ne doit contenir que des lettres, espaces et tirets");
                } else {
                    nomField.setStyle("");
                    removeTooltip(nomField);
                }
            } else {
                nomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showTooltip(nomField, "Le nom est obligatoire");
            }
        });

        // Prenom validation
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (newValue.length() < 2) {
                    prenomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    showTooltip(prenomField, "Le prénom doit contenir au moins 2 caractères");
                } else if (!newValue.matches("[\\p{L} -]+")) {
                    prenomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    showTooltip(prenomField, "Le prénom ne doit contenir que des lettres, espaces et tirets");
                } else {
                    prenomField.setStyle("");
                    removeTooltip(prenomField);
                }
            } else {
                prenomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showTooltip(prenomField, "Le prénom est obligatoire");
            }
        });

        // Email validation
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidEmail(newValue)) {
                emailField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showTooltip(emailField, "Format d'email invalide (exemple: user@domain.com)");
            } else {
                emailField.setStyle("");
                removeTooltip(emailField);
            }
        });

        // Phone validation
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    telephoneField.setText(newValue.replaceAll("[^\\d]", ""));
                } else if (newValue.length() != 8) {
                    telephoneField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    showTooltip(telephoneField, "Le numéro doit contenir exactement 8 chiffres");
                } else {
                    telephoneField.setStyle("");
                    removeTooltip(telephoneField);
                }
            } else {
                telephoneField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showTooltip(telephoneField, "Le téléphone est obligatoire");
            }
        });

        // Salary validation (optional field)
        salaireField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    double salaire = Double.parseDouble(newValue);
                    if (salaire < 0) {
                        salaireField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                        showTooltip(salaireField, "Le salaire ne peut pas être négatif");
                    } else {
                        salaireField.setStyle("");
                        removeTooltip(salaireField);
                    }
                } catch (NumberFormatException e) {
                    salaireField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    showTooltip(salaireField, "Le salaire doit être un nombre valide");
                }
            } else {
                salaireField.setStyle("");
                removeTooltip(salaireField);
            }
        });

        // Date validation
        dateNaissancePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                dateNaissancePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showTooltip(dateNaissancePicker, "La date de naissance est obligatoire");
            } else if (newValue.isAfter(LocalDate.now())) {
                dateNaissancePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showTooltip(dateNaissancePicker, "La date de naissance ne peut pas être dans le futur");
            } else {
                dateNaissancePicker.setStyle("");
                removeTooltip(dateNaissancePicker);
            }
        });

        // Contract dates validation (optional)
        dateDebutContratPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateContractDates();
        });

        dateFinContratPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateContractDates();
        });
    }

    private void validateContractDates() {
        LocalDate dateDebut = dateDebutContratPicker.getValue();
        LocalDate dateFin = dateFinContratPicker.getValue();
        LocalDate dateNaissance = dateNaissancePicker.getValue();

        if (dateDebut != null && dateNaissance != null && dateDebut.isBefore(dateNaissance)) {
            dateDebutContratPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showTooltip(dateDebutContratPicker, "La date de début ne peut pas être avant la date de naissance");
        } else if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            dateFinContratPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showTooltip(dateFinContratPicker, "La date de fin ne peut pas être avant la date de début");
        } else {
            dateDebutContratPicker.setStyle("");
            dateFinContratPicker.setStyle("");
            removeTooltip(dateDebutContratPicker);
            removeTooltip(dateFinContratPicker);
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showTooltip(Control control, String message) {
        Tooltip tooltip = new Tooltip(message);
        tooltip.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
        control.setTooltip(tooltip);
    }

    private void removeTooltip(Control control) {
        control.setTooltip(null);
    }

    public void setUtilisateurToUpdate(Utilisateur utilisateur) {
        this.utilisateurToUpdate = utilisateur;
        this.isUpdateMode = true;

        // Populate fields with user data
        nomField.setText(utilisateur.getNom());
        prenomField.setText(utilisateur.getPrenom());
        emailField.setText(utilisateur.getEmail());
        telephoneField.setText(utilisateur.getTelephone());
        adresseField.setText(utilisateur.getAdresse());
        dateNaissancePicker.setValue(utilisateur.getDateNaissance());
        dateDebutContratPicker.setValue(utilisateur.getDateDebutContrat());
        dateFinContratPicker.setValue(utilisateur.getDateFinContrat());

        // Don't show the actual password, leave the field empty
        passwordField.setText("");
        passwordField.setPromptText("Laissez vide pour garder l'ancien mot de passe");

        if (utilisateur.getSalaire() != null) {
            salaireField.setText(String.valueOf(utilisateur.getSalaire()));
        }
        typeUtilisateurComboBox.setValue(utilisateur.getTypeUtilisateur());
        statutComboBox.setValue(utilisateur.getStatut());
    }

    @FXML
    private void handleEnregistrer() {
        if (validateFields()) {
            try {
                Utilisateur utilisateur = isUpdateMode ? utilisateurToUpdate : new Utilisateur();
                utilisateur.setNom(nomField.getText().trim());
                utilisateur.setPrenom(prenomField.getText().trim());
                utilisateur.setEmail(emailField.getText().trim());
                utilisateur.setTelephone(telephoneField.getText().trim());
                utilisateur.setAdresse(adresseField.getText().trim());
                utilisateur.setDateNaissance(dateNaissancePicker.getValue());
                utilisateur.setDateDebutContrat(dateDebutContratPicker.getValue());
                utilisateur.setDateFinContrat(dateFinContratPicker.getValue());

                // Handle password differently in update mode
                if (isUpdateMode) {
                    // If password field is empty, keep the old password
                    String newPassword = passwordField.getText().trim();
                    if (!newPassword.isEmpty()) {
                        utilisateur.setPassword(newPassword);
                    } else {
                        utilisateur.setPassword(utilisateurToUpdate.getPassword());
                    }
                } else {
                    // For new users, password can be null or the entered value
                    String password = passwordField.getText().trim();
                    utilisateur.setPassword(password.isEmpty() ? null : password);
                }

                // Handle optional salary
                String salaire = salaireField.getText().trim();
                if (!salaire.isEmpty()) {
                    try {
                        utilisateur.setSalaire(Double.parseDouble(salaire));
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Le salaire doit être un nombre valide");
                        return;
                    }
                } else {
                    utilisateur.setSalaire(null);
                }

                utilisateur.setTypeUtilisateur(typeUtilisateurComboBox.getValue());
                utilisateur.setStatut(statutComboBox.getValue());

                if (isUpdateMode) {
                    utilisateur.setIdUser(utilisateurToUpdate.getIdUser());
                    utilisateurService.modifierUtilisateur(utilisateur);
                    showSuccessAlert("Succès", "Utilisateur modifié avec succès");
                } else {
                    utilisateurService.ajouterUtilisateur(utilisateur);
                    showSuccessAlert("Succès", "Utilisateur ajouté avec succès");
                }

                if (onUserAdded != null) {
                    onUserAdded.run();
                }
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            }
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        // Required fields
        if (nomField.getText().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
            nomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            isValid = false;
        }

        if (prenomField.getText().isEmpty()) {
            errors.append("- Le prénom est obligatoire\n");
            prenomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            isValid = false;
        }

        if (emailField.getText().isEmpty() || !isValidEmail(emailField.getText())) {
            errors.append("- L'email est invalide ou manquant\n");
            emailField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            isValid = false;
        }

        if (telephoneField.getText().isEmpty() || telephoneField.getText().length() != 8) {
            errors.append("- Le téléphone doit contenir exactement 8 chiffres\n");
            telephoneField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            isValid = false;
        }

        if (adresseField.getText().isEmpty()) {
            errors.append("- L'adresse est obligatoire\n");
            adresseField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            isValid = false;
        }

        if (dateNaissancePicker.getValue() == null) {
            errors.append("- La date de naissance est obligatoire\n");
            dateNaissancePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            isValid = false;
        }

        if (typeUtilisateurComboBox.getValue() == null) {
            errors.append("- Le type d'utilisateur est obligatoire\n");
            typeUtilisateurComboBox.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            isValid = false;
        }

        if (statutComboBox.getValue() == null) {
            errors.append("- Le statut est obligatoire\n");
            statutComboBox.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            isValid = false;
        }

        // Optional fields validation (only if they're not empty)
        if (!salaireField.getText().isEmpty()) {
            try {
                double salaire = Double.parseDouble(salaireField.getText());
                if (salaire < 0) {
                    errors.append("- Le salaire ne peut pas être négatif\n");
                    salaireField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                errors.append("- Le format du salaire est invalide\n");
                salaireField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                isValid = false;
            }
        }

        // Date validation (only if both contract dates are provided)
        if (dateDebutContratPicker.getValue() != null && dateFinContratPicker.getValue() != null) {
            if (dateFinContratPicker.getValue().isBefore(dateDebutContratPicker.getValue())) {
                errors.append("- La date de fin de contrat ne peut pas être avant la date de début\n");
                dateFinContratPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                isValid = false;
            }
        }

        if (!isValid) {
            showAlert("Erreur de validation", errors.toString());
        }

        return isValid;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        if (onUserAdded != null) {
            onUserAdded.run(); // This will refresh the users list
        }
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAnnuler() {
        if (onUserAdded != null) {
            onUserAdded.run();
        }
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    public void setOnUserAdded(Runnable callback) {
        this.onUserAdded = callback;
    }
}