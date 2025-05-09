package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.Utilisateur;
import services.UtilisateurService;

public class AjouterUtilisateurController {
    private Runnable onUserAdded;
    
    public void setOnUserAdded(Runnable callback) {
        this.onUserAdded = callback;
    }

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private DatePicker dateDebutContratPicker;
    @FXML private DatePicker dateFinContratPicker;
    @FXML private TextField salaireField;
    @FXML private ComboBox<String> typeUtilisateurComboBox;
    @FXML private ComboBox<String> statutComboBox;
    
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label telephoneError;
    @FXML private Label dateNaissanceError;
    @FXML private Label dateDebutContratError;
    @FXML private Label salaireError;
    @FXML private Label typeError;
    @FXML private Label statutError;

    private UtilisateurService utilisateurService;
    private boolean validationStarted = false;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            
            // Initialiser les ComboBox
            typeUtilisateurComboBox.getItems().addAll("MAGASINIER", "LIVREUR");
            statutComboBox.getItems().addAll("ACTIF", "INACTIF", "CONGE", "ABSENT");

            // Valeurs par défaut
            typeUtilisateurComboBox.setValue("MAGASINIER");
                statutComboBox.setValue("ACTIF");
            dateDebutContratPicker.setValue(LocalDate.now());

            // Cacher les messages d'erreur au début
            hideAllErrors();

            // Ajouter les écouteurs de validation
            setupValidationListeners();
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données", e.getMessage());
        }
    }

    private void hideAllErrors() {
        nomError.setVisible(false);
        prenomError.setVisible(false);
        telephoneError.setVisible(false);
        dateNaissanceError.setVisible(false);
        dateDebutContratError.setVisible(false);
        salaireError.setVisible(false);
        typeError.setVisible(false);
        statutError.setVisible(false);
    }

    private void setupValidationListeners() {
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        prenomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        telephoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        dateNaissancePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        dateDebutContratPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        salaireField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        typeUtilisateurComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
    }

    private void validateAllFields() {
        boolean isNomValid = validateNom();
        boolean isPrenomValid = validatePrenom();
        boolean isTelephoneValid = validateTelephone();
        boolean isDateNaissanceValid = validateDateNaissance();
        boolean isDateDebutContratValid = validateDateDebutContrat();
        boolean isSalaireValid = validateSalaire();
        boolean isTypeValid = validateType();
        boolean isStatutValid = validateStatut();

        // Appliquer le style d'erreur ou normal
        nomField.setStyle(!isNomValid ? "-fx-border-color: red;" : "");
        prenomField.setStyle(!isPrenomValid ? "-fx-border-color: red;" : "");
        telephoneField.setStyle(!isTelephoneValid ? "-fx-border-color: red;" : "");
        dateNaissancePicker.setStyle(!isDateNaissanceValid ? "-fx-border-color: red;" : "");
        dateDebutContratPicker.setStyle(!isDateDebutContratValid ? "-fx-border-color: red;" : "");
        salaireField.setStyle(!isSalaireValid ? "-fx-border-color: red;" : "");
        typeUtilisateurComboBox.setStyle(!isTypeValid ? "-fx-border-color: red;" : "");
        statutComboBox.setStyle(!isStatutValid ? "-fx-border-color: red;" : "");
            }

    private boolean validateNom() {
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            nomError.setVisible(true);
            return false;
        }
        if (nom.length() < 2) {
            nomError.setText("Le nom doit contenir au moins 2 caractères");
            nomError.setVisible(true);
            return false;
        }
        nomError.setVisible(false);
        return true;
    }

    private boolean validatePrenom() {
        String prenom = prenomField.getText().trim();
        if (prenom.isEmpty()) {
            prenomError.setText("Le prénom est obligatoire");
            prenomError.setVisible(true);
            return false;
        }
        if (prenom.length() < 2) {
            prenomError.setText("Le prénom doit contenir au moins 2 caractères");
            prenomError.setVisible(true);
            return false;
        }
        prenomError.setVisible(false);
        return true;
            }

    private boolean validateTelephone() {
        String telephone = telephoneField.getText().trim();
        if (telephone.isEmpty()) {
            telephoneError.setText("Le téléphone est obligatoire");
            telephoneError.setVisible(true);
            return false;
        }
        if (!telephone.matches("^[0-9]{8}$")) {
            telephoneError.setText("Le téléphone doit contenir exactement 8 chiffres");
            telephoneError.setVisible(true);
            return false;
            }
        try {
            if (utilisateurService.telephoneExists(telephone)) {
                telephoneError.setText("Ce numéro de téléphone est déjà utilisé");
                telephoneError.setVisible(true);
                return false;
            }
        } catch (SQLException e) {
            telephoneError.setText("Erreur lors de la vérification du téléphone");
            telephoneError.setVisible(true);
            return false;
        }
        telephoneError.setVisible(false);
        return true;
    }

    private boolean validateDateNaissance() {
        LocalDate dateNaissance = dateNaissancePicker.getValue();
        if (dateNaissance == null) {
            dateNaissanceError.setText("La date de naissance est obligatoire");
            dateNaissanceError.setVisible(true);
            return false;
        }
        if (dateNaissance.plusYears(18).isAfter(LocalDate.now())) {
            dateNaissanceError.setText("L'employé doit avoir au moins 18 ans");
            dateNaissanceError.setVisible(true);
            return false;
        }
        dateNaissanceError.setVisible(false);
        return true;
    }

    private boolean validateDateDebutContrat() {
        LocalDate dateDebut = dateDebutContratPicker.getValue();
        if (dateDebut == null) {
            dateDebutContratError.setText("La date de début de contrat est obligatoire");
            dateDebutContratError.setVisible(true);
            return false;
        }
        if (dateDebut.isBefore(LocalDate.now())) {
            dateDebutContratError.setText("La date de début de contrat ne peut pas être dans le passé");
            dateDebutContratError.setVisible(true);
            return false;
        }
        dateDebutContratError.setVisible(false);
        return true;
    }

    private boolean validateSalaire() {
        String salaire = salaireField.getText().trim();
        if (salaire.isEmpty()) {
            salaireError.setText("Le salaire est obligatoire");
            salaireError.setVisible(true);
            return false;
        }
        try {
            double salaireValue = Double.parseDouble(salaire);
            if (salaireValue < 1000) {
                salaireError.setText("Le salaire doit être d'au moins 1000");
                salaireError.setVisible(true);
                return false;
            }
        } catch (NumberFormatException e) {
            salaireError.setText("Le salaire doit être un nombre valide");
            salaireError.setVisible(true);
            return false;
        }
        salaireError.setVisible(false);
        return true;
    }

    private boolean validateType() {
        String type = typeUtilisateurComboBox.getValue();
        if (type == null || type.isEmpty()) {
            typeError.setText("Le type d'employé est obligatoire");
            typeError.setVisible(true);
            return false;
        }
        typeError.setVisible(false);
        return true;
    }

    private boolean validateStatut() {
        String statut = statutComboBox.getValue();
        if (statut == null || statut.isEmpty()) {
            statutError.setText("Le statut est obligatoire");
            statutError.setVisible(true);
            return false;
        }
        statutError.setVisible(false);
        return true;
    }

    @FXML
    private void handleEnregistrer() {
        validationStarted = true;
        validateAllFields();

        if (!validateNom() || !validatePrenom() || !validateTelephone() || 
            !validateDateNaissance() || !validateDateDebutContrat() || 
            !validateSalaire() || !validateType() || !validateStatut()) {
            return;
        }

            try {
            Utilisateur utilisateur = new Utilisateur();
                utilisateur.setNom(nomField.getText().trim());
                utilisateur.setPrenom(prenomField.getText().trim());
                utilisateur.setTelephone(telephoneField.getText().trim());
                utilisateur.setAdresse(adresseField.getText().trim());
                utilisateur.setDateNaissance(dateNaissancePicker.getValue());
                utilisateur.setDateDebutContrat(dateDebutContratPicker.getValue());
                utilisateur.setDateFinContrat(dateFinContratPicker.getValue());
            utilisateur.setSalaire(Double.parseDouble(salaireField.getText().trim()));
                utilisateur.setTypeUtilisateur(typeUtilisateurComboBox.getValue());
                utilisateur.setStatut(statutComboBox.getValue());

                    utilisateurService.ajouterUtilisateur(utilisateur);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Employé ajouté avec succès!");
            alert.showAndWait();

                if (onUserAdded != null) {
                    onUserAdded.run();
            } else {
                // Retourner à la liste des utilisateurs
                handleBack();
                }
            
            } catch (SQLException e) {
            showError("Erreur", "Erreur lors de l'ajout de l'employé: " + e.getMessage());
            }
        }

    @FXML
    private void handleAnnuler() {
        handleBack();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListeUtilisateurs.fxml"));
            Parent root = loader.load();
            
            MainDashboardController mainController = MainDashboardController.getInstance();
            mainController.getContentArea().getChildren().clear();
            mainController.getContentArea().getChildren().add(root);
            mainController.setPageTitle("Liste des utilisateurs");
        } catch (IOException e) {
            showError("Erreur", "Erreur lors du retour à la liste des utilisateurs: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 