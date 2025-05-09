package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;

public class AjouterResponsableController {
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextArea adresseField;
    @FXML private DatePicker dateNaissanceField;
    @FXML private DatePicker dateDebutContratField;
    @FXML private DatePicker dateFinContratField;
    @FXML private TextField salaireField;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorMessage;
    @FXML private Circle photoCircle;
    @FXML private ImageView photoImageView;

    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label adresseError;
    @FXML private Label dateNaissanceError;
    @FXML private Label dateDebutContratError;
    @FXML private Label dateFinContratError;
    @FXML private Label salaireError;
    @FXML private Label statutError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;

    private UtilisateurService utilisateurService;
    private String newPhotoPath;
    private static final String UPLOAD_DIR = "uploads/profiles/";
    private File uploadDir;
    private Runnable onResponsableAdded;
    private static final String STYLE_ERROR = "-fx-border-color: red;";
    private static final String STYLE_NORMAL = "";
    private boolean validationStarted = false;

    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            
            uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            statutComboBox.getItems().addAll("ACTIF", "INACTIF", "CONGE", "ABSENT");
            statutComboBox.setValue("ACTIF");

            setDefaultPhoto();
            dateDebutContratField.setValue(LocalDate.now());
            
            // Les listeners ne seront actifs qu'après le premier clic sur Enregistrer
            setupValidationListeners();
            
            // Cacher tous les messages d'erreur au début
            hideAllErrors();
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    private void hideAllErrors() {
        nomError.setVisible(false);
        prenomError.setVisible(false);
        emailError.setVisible(false);
        telephoneError.setVisible(false);
        adresseError.setVisible(false);
        dateNaissanceError.setVisible(false);
        dateDebutContratError.setVisible(false);
        dateFinContratError.setVisible(false);
        salaireError.setVisible(false);
        statutError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);

        // Réinitialiser les styles des champs
        nomField.setStyle(STYLE_NORMAL);
        prenomField.setStyle(STYLE_NORMAL);
        emailField.setStyle(STYLE_NORMAL);
        telephoneField.setStyle(STYLE_NORMAL);
        adresseField.setStyle(STYLE_NORMAL);
        dateNaissanceField.setStyle(STYLE_NORMAL);
        dateDebutContratField.setStyle(STYLE_NORMAL);
        dateFinContratField.setStyle(STYLE_NORMAL);
        salaireField.setStyle(STYLE_NORMAL);
        passwordField.setStyle(STYLE_NORMAL);
        confirmPasswordField.setStyle(STYLE_NORMAL);
    }

    private void setupValidationListeners() {
        // Les listeners ne valident que si la validation a commencé (après le premier clic sur Enregistrer)
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        prenomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        telephoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        adresseField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        dateNaissanceField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        dateDebutContratField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        dateFinContratField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        salaireField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validationStarted) validateAllFields();
        });
    }

    private void validateAllFields() {
        boolean isNomValid = validateNom();
        boolean isPrenomValid = validatePrenom();
        boolean isEmailValid = validateEmail();
        boolean isTelephoneValid = validateTelephone();
        boolean isAdresseValid = validateAdresse();
        boolean isDateNaissanceValid = validateDateNaissance();
        boolean isDateDebutContratValid = validateDateDebutContrat();
        boolean isDateFinContratValid = validateDateFinContrat();
        boolean isSalaireValid = validateSalaire();
        boolean isPasswordValid = validatePassword();
        boolean isConfirmPasswordValid = validateConfirmPassword();

        // Appliquer le style d'erreur ou normal selon la validation
        nomField.setStyle(!isNomValid ? STYLE_ERROR : STYLE_NORMAL);
        prenomField.setStyle(!isPrenomValid ? STYLE_ERROR : STYLE_NORMAL);
        emailField.setStyle(!isEmailValid ? STYLE_ERROR : STYLE_NORMAL);
        telephoneField.setStyle(!isTelephoneValid ? STYLE_ERROR : STYLE_NORMAL);
        adresseField.setStyle(!isAdresseValid ? STYLE_ERROR : STYLE_NORMAL);
        dateNaissanceField.setStyle(!isDateNaissanceValid ? STYLE_ERROR : STYLE_NORMAL);
        dateDebutContratField.setStyle(!isDateDebutContratValid ? STYLE_ERROR : STYLE_NORMAL);
        dateFinContratField.setStyle(!isDateFinContratValid ? STYLE_ERROR : STYLE_NORMAL);
        salaireField.setStyle(!isSalaireValid ? STYLE_ERROR : STYLE_NORMAL);
        passwordField.setStyle(!isPasswordValid ? STYLE_ERROR : STYLE_NORMAL);
        confirmPasswordField.setStyle(!isConfirmPasswordValid ? STYLE_ERROR : STYLE_NORMAL);

        // Afficher tous les messages d'erreur
        nomError.setVisible(!isNomValid);
        prenomError.setVisible(!isPrenomValid);
        emailError.setVisible(!isEmailValid);
        telephoneError.setVisible(!isTelephoneValid);
        adresseError.setVisible(!isAdresseValid);
        dateNaissanceError.setVisible(!isDateNaissanceValid);
        dateDebutContratError.setVisible(!isDateDebutContratValid);
        dateFinContratError.setVisible(!isDateFinContratValid);
        salaireError.setVisible(!isSalaireValid);
        passwordError.setVisible(!isPasswordValid);
        confirmPasswordError.setVisible(!isConfirmPasswordValid);
    }

    private boolean validateNom() {
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            return false;
        }
        if (nom.length() < 2) {
            nomError.setText("Le nom doit contenir au moins 2 caractères");
            return false;
        }
        return true;
    }

    private boolean validatePrenom() {
        String prenom = prenomField.getText().trim();
        if (prenom.isEmpty()) {
            prenomError.setText("Le prénom est obligatoire");
            return false;
        }
        if (prenom.length() < 2) {
            prenomError.setText("Le prénom doit contenir au moins 2 caractères");
            return false;
        }
        return true;
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("L'email est obligatoire");
            return false;
        }
        if (!email.matches(".*@.*")) {
            emailError.setText("L'email doit contenir au moins un @");
            return false;
        }
        try {
            if (utilisateurService.emailExists(email)) {
                emailError.setText("Cet email est déjà utilisé");
                return false;
            }
        } catch (SQLException e) {
            emailError.setText("Erreur lors de la vérification de l'email");
            return false;
        }
        return true;
    }

    private boolean validateTelephone() {
        String telephone = telephoneField.getText().trim();
        if (telephone.isEmpty()) {
            telephoneError.setText("Le téléphone est obligatoire");
            return false;
        }
        if (!telephone.matches("^[0-9]{8}$")) {
            telephoneError.setText("Le téléphone doit contenir exactement 8 chiffres");
            return false;
        }
        try {
            if (utilisateurService.telephoneExists(telephone)) {
                telephoneError.setText("Ce numéro de téléphone est déjà utilisé");
                return false;
            }
        } catch (SQLException e) {
            telephoneError.setText("Erreur lors de la vérification du téléphone");
            return false;
        }
        return true;
    }

    private boolean validateAdresse() {
        String adresse = adresseField.getText().trim();
        if (adresse.isEmpty()) {
            return true;
        }
        if (adresse.length() < 5) {
            adresseError.setText("Si renseignée, l'adresse doit contenir au moins 5 caractères");
            adresseError.setVisible(true);
            return false;
        }
        adresseError.setVisible(false);
        return true;
    }

    private boolean validateDateNaissance() {
        LocalDate dateNaissance = dateNaissanceField.getValue();
        if (dateNaissance == null) {
            dateNaissanceError.setText("La date de naissance est obligatoire");
            return false;
        }
        if (dateNaissance.plusYears(18).isAfter(LocalDate.now())) {
            dateNaissanceError.setText("Le responsable doit avoir au moins 18 ans");
            return false;
        }
        return true;
    }

    private boolean validateDateDebutContrat() {
        LocalDate dateDebutContrat = dateDebutContratField.getValue();
        if (dateDebutContrat == null) {
            dateDebutContratError.setText("La date de début de contrat est obligatoire");
            return false;
        }
        if (dateDebutContrat.isBefore(LocalDate.now())) {
            dateDebutContratError.setText("La date de début de contrat ne peut pas être dans le passé");
            return false;
        }
        return true;
    }

    private boolean validateDateFinContrat() {
        LocalDate dateFinContrat = dateFinContratField.getValue();
        LocalDate dateDebutContrat = dateDebutContratField.getValue();
        
        if (dateFinContrat != null && dateDebutContrat != null) {
            if (dateFinContrat.isBefore(dateDebutContrat)) {
                dateFinContratError.setText("La date de fin ne peut pas être avant la date de début");
                dateFinContratError.setVisible(true);
                return false;
            }
        }
        dateFinContratError.setVisible(false);
        return true;
    }

    private boolean validateSalaire() {
        String salaire = salaireField.getText().trim();
        if (salaire.isEmpty()) {
            salaireError.setText("Le salaire est obligatoire");
            return false;
        }
        if (salaire.length() < 3) {
            salaireError.setText("Le salaire doit contenir au moins 3 caractères");
            return false;
        }
        try {
            double salaireValue = Double.parseDouble(salaire);
            if (salaireValue < 1000) {
                salaireError.setText("Le salaire doit être d'au moins 1000");
                return false;
            }
        } catch (NumberFormatException e) {
            salaireError.setText("Le salaire doit être un nombre valide");
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        String password = passwordField.getText();
        if (password.isEmpty()) {
            passwordError.setText("Le mot de passe est obligatoire");
            return false;
        }
        if (password.length() < 6) {
            passwordError.setText("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            passwordError.setText("Le mot de passe doit contenir au moins une majuscule");
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            passwordError.setText("Le mot de passe doit contenir au moins un chiffre");
            return false;
        }
        return true;
    }

    private boolean validateConfirmPassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (confirmPassword.isEmpty()) {
            confirmPasswordError.setText("La confirmation du mot de passe est obligatoire");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
            return false;
        }
        return true;
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) photoCircle.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = Paths.get(UPLOAD_DIR, fileName);
                
                // Copy file to uploads directory
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update UI with new photo
                Image image = new Image(targetPath.toUri().toString());
                photoCircle.setFill(new ImagePattern(image));
                
                // Save path for later
                newPhotoPath = fileName;
                
            } catch (IOException e) {
                showError("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    private void setDefaultPhoto() {
        photoCircle.setFill(javafx.scene.paint.Color.valueOf("#4CAF50"));
    }

    public void setOnResponsableAdded(Runnable callback) {
        this.onResponsableAdded = callback;
    }

    private void clearFields() {
        // Vider les champs de texte
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        adresseField.clear();
        salaireField.clear();
        passwordField.clear();
        confirmPasswordField.clear();

        // Réinitialiser les dates
        dateNaissanceField.setValue(null);
        dateDebutContratField.setValue(LocalDate.now());
        dateFinContratField.setValue(null);

        // Réinitialiser le statut
        statutComboBox.setValue("ACTIF");

        // Réinitialiser la photo
        setDefaultPhoto();
        newPhotoPath = null;

        // Cacher les messages d'erreur
        hideAllErrors();

        // Réinitialiser la validation
        validationStarted = false;
    }

    @FXML
    private void handleSave() {
        // Activer la validation
        validationStarted = true;
        
        // Valider tous les champs
        validateAllFields();

        // Vérifier si tous les champs sont valides
        if (!validateNom() || !validatePrenom() || !validateEmail() || 
            !validateTelephone() || !validateDateNaissance() ||
            !validateDateDebutContrat() || !validateDateFinContrat() || !validateSalaire() || 
            !validatePassword() || !validateConfirmPassword() || !validateAdresse()) {
            return;
        }

        try {
            Utilisateur responsable = new Utilisateur();
            responsable.setNom(nomField.getText().trim());
            responsable.setPrenom(prenomField.getText().trim());
            responsable.setEmail(emailField.getText().trim());
            responsable.setTelephone(telephoneField.getText().trim());
            responsable.setAdresse(adresseField.getText().trim());
            responsable.setDateNaissance(dateNaissanceField.getValue());
            responsable.setDateDebutContrat(dateDebutContratField.getValue());
            responsable.setDateFinContrat(dateFinContratField.getValue());
            responsable.setPassword(passwordField.getText());
            responsable.setSalaire(Double.parseDouble(salaireField.getText().trim()));
            responsable.setTypeUtilisateur("RESPONSABLE");
            responsable.setStatut(statutComboBox.getValue());
            responsable.setPhotoUrl(newPhotoPath);

            utilisateurService.ajouterUtilisateur(responsable);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Responsable ajouté avec succès!");
            alert.showAndWait();

            // Vider les champs après l'enregistrement réussi
            clearFields();
            
            if (onResponsableAdded != null) {
                onResponsableAdded.run();
            }
            
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ajout du responsable: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    @FXML
    private void handleCancel() {
        try {
            // Charger la vue de la liste des utilisateurs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListeUtilisateurs.fxml"));
            Parent root = loader.load();
            
            // Mettre à jour la zone de contenu
            MainDashboardController mainController = MainDashboardController.getInstance();
            mainController.getContentArea().getChildren().clear();
            mainController.getContentArea().getChildren().add(root);
            mainController.setPageTitle("Liste des utilisateurs");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors du retour à la liste des utilisateurs: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBack() {
        try {
            // Charger la vue de la liste des utilisateurs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListeUtilisateurs.fxml"));
            Parent root = loader.load();
            
            // Mettre à jour la zone de contenu
            MainDashboardController mainController = MainDashboardController.getInstance();
            mainController.getContentArea().getChildren().clear();
            mainController.getContentArea().getChildren().add(root);
            mainController.setPageTitle("Liste des utilisateurs");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors du retour à la liste des utilisateurs: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean hasChanges() {
        return !nomField.getText().trim().isEmpty() ||
               !prenomField.getText().trim().isEmpty() ||
               !emailField.getText().trim().isEmpty() ||
               !telephoneField.getText().trim().isEmpty() ||
               !adresseField.getText().trim().isEmpty() ||
               !salaireField.getText().trim().isEmpty() ||
               !passwordField.getText().isEmpty() ||
               dateNaissanceField.getValue() != null ||
               (dateDebutContratField.getValue() != null && !dateDebutContratField.getValue().equals(LocalDate.now())) ||
               dateFinContratField.getValue() != null ||
               newPhotoPath != null ||
               !statutComboBox.getValue().equals("ACTIF");
    }
} 