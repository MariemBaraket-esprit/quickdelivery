package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import javafx.collections.FXCollections;
import java.sql.Types;
import javafx.scene.control.Alert;
import java.time.Instant;

public class ModifierUtilisateurController {
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
    private DatePicker dateNaissancePicker;
    @FXML
    private DatePicker dateDebutContratPicker;
    @FXML
    private DatePicker dateFinContratPicker;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField salaireField;
    @FXML
    private ComboBox<String> typeUtilisateurComboBox;
    @FXML
    private ComboBox<String> statutComboBox;
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
    @FXML
    private Label generalError;

    private Utilisateur currentUser;
    private UtilisateurService utilisateurService;
    private Runnable onUserModified;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();

            // Initialize combo boxes
            typeUtilisateurComboBox.setItems(FXCollections.observableArrayList(
                    "ADMIN", "RESPONSABLE", "CLIENT", "LIVREUR", "MAGASINIER"
            ));

            statutComboBox.setItems(FXCollections.observableArrayList(
                    "CONGE", "ACTIF", "INACTIF", "ABSENT"
            ));

            // Setup validation listeners
            setupValidationListeners();

        } catch (SQLException e) {
            showError(nomError, "Erreur de connexion à la base de données");
        }
    }

    public void initData(Utilisateur user) {
        this.currentUser = user;
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            telephoneField.setText(currentUser.getTelephone());
            adresseField.setText(currentUser.getAdresse());

            // Set DatePicker values directly from LocalDate
            dateNaissancePicker.setValue(currentUser.getDateNaissance());
            dateDebutContratPicker.setValue(currentUser.getDateDebutContrat());
            dateFinContratPicker.setValue(currentUser.getDateFinContrat());

            if (currentUser.getSalaire() != null) {
                salaireField.setText(String.valueOf(currentUser.getSalaire()));
            }

            typeUtilisateurComboBox.setValue(currentUser.getTypeUtilisateur());
            statutComboBox.setValue(currentUser.getStatut());
        }
    }

    public void setOnUserModified(Runnable callback) {
        this.onUserModified = callback;
    }

    private void setupValidationListeners() {
        // Nom validation
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (newValue.length() < 2) {
                    nomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    nomError.setText("Le nom doit contenir au moins 2 caractères");
                    nomError.setVisible(true);
                } else if (!newValue.matches("[\\p{L} -]+")) {
                    nomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    nomError.setText("Le nom ne doit contenir que des lettres, espaces et tirets");
                    nomError.setVisible(true);
                } else {
                    nomField.setStyle("");
                    nomError.setVisible(false);
                }
            } else {
                nomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                nomError.setText("Le nom est obligatoire");
                nomError.setVisible(true);
            }
        });

        // Prenom validation
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (newValue.length() < 2) {
                    prenomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    prenomError.setText("Le prénom doit contenir au moins 2 caractères");
                    prenomError.setVisible(true);
                } else if (!newValue.matches("[\\p{L} -]+")) {
                    prenomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    prenomError.setText("Le prénom ne doit contenir que des lettres, espaces et tirets");
                    prenomError.setVisible(true);
                } else {
                    prenomField.setStyle("");
                    prenomError.setVisible(false);
                }
            } else {
                prenomField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                prenomError.setText("Le prénom est obligatoire");
                prenomError.setVisible(true);
            }
        });

        // Email validation
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidEmail(newValue)) {
                emailField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                emailError.setText("Format d'email invalide");
                emailError.setVisible(true);
            } else {
                emailField.setStyle("");
                emailError.setVisible(false);
            }
        });

        // Phone validation
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    telephoneField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (newValue.length() != 8) {
                    telephoneField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    telephoneError.setText("Le numéro doit contenir exactement 8 chiffres");
                    telephoneError.setVisible(true);
                } else {
                    telephoneField.setStyle("");
                    telephoneError.setVisible(false);
                }
            }
        });

        // Salary validation
        salaireField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    double salaire = Double.parseDouble(newValue);
                    if (salaire < 0) {
                        salaireField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    } else {
                        salaireField.setStyle("");
                    }
                } catch (NumberFormatException e) {
                    salaireField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                }
            } else {
                salaireField.setStyle("");
            }
        });

        // Date validations
        dateNaissancePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDates();
        });

        dateDebutContratPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDates();
        });

        dateFinContratPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDates();
        });
    }

    private void validateDates() {
        LocalDate dateNaissance = dateNaissancePicker.getValue();
        LocalDate dateDebut = dateDebutContratPicker.getValue();
        LocalDate dateFin = dateFinContratPicker.getValue();

        if (dateNaissance != null && dateNaissance.isAfter(LocalDate.now())) {
            dateNaissancePicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        } else {
            dateNaissancePicker.setStyle("");
        }

        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            dateFinContratPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        } else {
            dateFinContratPicker.setStyle("");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void populateFields() {
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            telephoneField.setText(currentUser.getTelephone());
            adresseField.setText(currentUser.getAdresse());

            loadUserData();

            if (currentUser.getSalaire() != null) {
                salaireField.setText(String.valueOf(currentUser.getSalaire()));
            }

            typeUtilisateurComboBox.setValue(currentUser.getTypeUtilisateur());
            statutComboBox.setValue(currentUser.getStatut());
        }
    }

    private void loadUserData() {
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone());
        adresseField.setText(currentUser.getAdresse());
        typeUtilisateurComboBox.setValue(currentUser.getTypeUtilisateur());
        statutComboBox.setValue(currentUser.getStatut());
        salaireField.setText(String.valueOf(currentUser.getSalaire()));

        // Set DatePicker values directly from LocalDate
        dateNaissancePicker.setValue(currentUser.getDateNaissance());
        dateDebutContratPicker.setValue(currentUser.getDateDebutContrat());
        dateFinContratPicker.setValue(currentUser.getDateFinContrat());
    }

    @FXML
    private void handleEnregistrer() {
        if (validateFields()) {
            try {
                currentUser.setNom(nomField.getText());
                currentUser.setPrenom(prenomField.getText());
                currentUser.setEmail(emailField.getText());
                currentUser.setTelephone(telephoneField.getText());
                currentUser.setAdresse(adresseField.getText());

                // Set LocalDate values directly from DatePickers
                currentUser.setDateNaissance(dateNaissancePicker.getValue());
                currentUser.setDateDebutContrat(dateDebutContratPicker.getValue());
                currentUser.setDateFinContrat(dateFinContratPicker.getValue());

                if (!salaireField.getText().isEmpty()) {
                    try {
                        double salaire = Double.parseDouble(salaireField.getText());
                        currentUser.setSalaire(salaire);
                    } catch (NumberFormatException e) {
                        showError(generalError, "Le salaire doit être un nombre valide");
                        return;
                    }
                }

                currentUser.setTypeUtilisateur(typeUtilisateurComboBox.getValue());
                currentUser.setStatut(statutComboBox.getValue());

                utilisateurService.modifierUtilisateur(currentUser);

                if (onUserModified != null) {
                    onUserModified.run();
                }

                Stage stage = (Stage) nomField.getScene().getWindow();
                stage.close();

            } catch (SQLException e) {
                showError(generalError, "Erreur lors de la modification de l'utilisateur");
            }
        }
    }

    @FXML
    private void handleBack() {
        if (onUserModified != null) {
            onUserModified.run(); // This will refresh the users list
        }
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAnnuler() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (isEmpty(nomField.getText())) {
            showError(nomError, "Le nom est obligatoire");
            isValid = false;
        }

        if (isEmpty(prenomField.getText())) {
            showError(prenomError, "Le prénom est obligatoire");
            isValid = false;
        }

        if (isEmpty(emailField.getText())) {
            showError(emailError, "L'email est obligatoire");
            isValid = false;
        } else if (!isValidEmail(emailField.getText())) {
            showError(emailError, "Format d'email invalide");
            isValid = false;
        }

        if (isEmpty(telephoneField.getText())) {
            showError(telephoneError, "Le téléphone est obligatoire");
            isValid = false;
        } else if (!telephoneField.getText().matches("^[0-9]{8}$")) {
            showError(telephoneError, "Le téléphone doit contenir exactement 8 chiffres");
            isValid = false;
        }

        if (typeUtilisateurComboBox.getValue() == null) {
            showError(typeError, "Le type d'utilisateur est obligatoire");
            isValid = false;
        }

        if (statutComboBox.getValue() == null) {
            showError(statutError, "Le statut est obligatoire");
            isValid = false;
        }

        if (!isEmpty(salaireField.getText())) {
            try {
                double salaire = Double.parseDouble(salaireField.getText());
                if (salaire < 0) {
                    showError(generalError, "Le salaire ne peut pas être négatif");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showError(generalError, "Le format du salaire est invalide");
                isValid = false;
            }
        } else {
            showError(generalError, "Le salaire est obligatoire");
            isValid = false;
        }

        LocalDate dateNaissance = dateNaissancePicker.getValue();
        LocalDate dateDebut = dateDebutContratPicker.getValue();
        LocalDate dateFin = dateFinContratPicker.getValue();

        if (dateNaissance != null && dateNaissance.isAfter(LocalDate.now())) {
            showError(nomError, "La date de naissance ne peut pas être dans le futur");
            isValid = false;
        }

        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            showError(nomError, "La date de fin de contrat ne peut pas être avant la date de début");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        nomError.setVisible(false);
        prenomError.setVisible(false);
        emailError.setVisible(false);
        telephoneError.setVisible(false);
        adresseError.setVisible(false);
        typeError.setVisible(false);
        statutError.setVisible(false);
        generalError.setVisible(false);

        nomField.setStyle("");
        prenomField.setStyle("");
        emailField.setStyle("");
        telephoneField.setStyle("");
        adresseField.setStyle("");
        typeUtilisateurComboBox.setStyle("");
        statutComboBox.setStyle("");
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void showSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Les modifications ont été enregistrées avec succès.");
        alert.showAndWait();
    }
}