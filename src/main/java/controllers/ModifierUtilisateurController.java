package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import utils.Session;

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
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private Button enregistrerButton;

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
            
            // Password show/hide logic
            setupPasswordToggle();
            
        } catch (SQLException e) {
            showError("Erreur", "Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    private void setupPasswordToggle() {
            togglePasswordButton.setOnAction(e -> {
                boolean showing = visiblePasswordField.isVisible();
                if (showing) {
                    passwordField.setText(visiblePasswordField.getText());
                    passwordField.setVisible(true);
                    passwordField.setManaged(true);
                    visiblePasswordField.setVisible(false);
                    visiblePasswordField.setManaged(false);
                    togglePasswordButton.setText("👁");
                } else {
                    visiblePasswordField.setText(passwordField.getText());
                    visiblePasswordField.setVisible(true);
                    visiblePasswordField.setManaged(true);
                    passwordField.setVisible(false);
                    passwordField.setManaged(false);
                    togglePasswordButton.setText("🙈");
                }
            });

            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!visiblePasswordField.isVisible()) {
                    visiblePasswordField.setText(newVal);
                }
            });

            visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (visiblePasswordField.isVisible()) {
                    passwordField.setText(newVal);
                }
            });
    }

    private void setupValidationListeners() {
        // Nom validation
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (newValue.length() < 2) {
                    nomField.getStyleClass().add("field-error");
                    nomError.setText("Le nom doit contenir au moins 2 caractères");
                    nomError.setVisible(true);
                } else if (!newValue.matches("[\\p{L} -]+")) {
                    nomField.getStyleClass().add("field-error");
                    nomError.setText("Le nom ne doit contenir que des lettres, espaces et tirets");
                    nomError.setVisible(true);
                } else {
                    nomField.getStyleClass().remove("field-error");
                    nomError.setVisible(false);
                }
            } else {
                nomField.getStyleClass().add("field-error");
                nomError.setText("Ce champ doit être rempli");
                nomError.setVisible(true);
            }
        });

        // Prenom validation
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (newValue.length() < 2) {
                    prenomField.getStyleClass().add("field-error");
                    prenomError.setText("Le prénom doit contenir au moins 2 caractères");
                    prenomError.setVisible(true);
                } else if (!newValue.matches("[\\p{L} -]+")) {
                    prenomField.getStyleClass().add("field-error");
                    prenomError.setText("Le prénom ne doit contenir que des lettres, espaces et tirets");
                    prenomError.setVisible(true);
                } else {
                    prenomField.getStyleClass().remove("field-error");
                    prenomError.setVisible(false);
                }
            } else {
                prenomField.getStyleClass().add("field-error");
                prenomError.setText("Ce champ doit être rempli");
                prenomError.setVisible(true);
            }
        });

        // Email validation
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!newValue.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    emailField.getStyleClass().add("field-error");
                    emailError.setText("Format d'email invalide (exemple: user@domain.com)");
                    emailError.setVisible(true);
                } else {
                    emailField.getStyleClass().remove("field-error");
                    emailError.setVisible(false);
                }
            } else {
                emailField.getStyleClass().add("field-error");
                emailError.setText("Ce champ doit être rempli");
                emailError.setVisible(true);
            }
        });

        // Phone validation
        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    telephoneField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (newValue.length() != 8) {
                    telephoneField.getStyleClass().add("field-error");
                    telephoneError.setText("Le numéro doit contenir exactement 8 chiffres");
                    telephoneError.setVisible(true);
                } else {
                    telephoneField.getStyleClass().remove("field-error");
                    telephoneError.setVisible(false);
                }
            } else {
                telephoneField.getStyleClass().add("field-error");
                telephoneError.setText("Ce champ doit être rempli");
                telephoneError.setVisible(true);
            }
        });

        // Salary validation (optional field)
        salaireField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    double salaire = Double.parseDouble(newValue);
                    if (salaire < 0) {
                        salaireField.getStyleClass().add("field-error");
                        generalError.setText("Le salaire ne peut pas être négatif");
                        generalError.setVisible(true);
                    } else {
                        salaireField.getStyleClass().remove("field-error");
                        generalError.setVisible(false);
                    }
                } catch (NumberFormatException e) {
                    salaireField.getStyleClass().add("field-error");
                    generalError.setText("Le salaire doit être un nombre valide");
                    generalError.setVisible(true);
                }
            } else {
                salaireField.getStyleClass().remove("field-error");
                generalError.setVisible(false);
            }
        });

        // Type utilisateur validation
        typeUtilisateurComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                typeUtilisateurComboBox.getStyleClass().add("field-error");
                typeError.setText("Ce champ doit être rempli");
                typeError.setVisible(true);
            } else {
                typeUtilisateurComboBox.getStyleClass().remove("field-error");
                typeError.setVisible(false);
            }
        });

        // Statut validation
        statutComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                statutComboBox.getStyleClass().add("field-error");
                statutError.setText("Ce champ doit être rempli");
                statutError.setVisible(true);
            } else {
                statutComboBox.getStyleClass().remove("field-error");
                statutError.setVisible(false);
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;
        
        // Reset all error states
        clearErrors();
        
        // Validate required fields
        if (nomField.getText().trim().isEmpty()) {
            nomField.getStyleClass().add("field-error");
            nomError.setText("Le nom est requis");
            nomError.setVisible(true);
            isValid = false;
        } else if (!nomField.getText().matches("[\\p{L} -]+")) {
            nomField.getStyleClass().add("field-error");
            nomError.setText("Le nom ne doit contenir que des lettres");
            nomError.setVisible(true);
            isValid = false;
        }
        
        if (prenomField.getText().trim().isEmpty()) {
            prenomField.getStyleClass().add("field-error");
            prenomError.setText("Le prénom est requis");
            prenomError.setVisible(true);
            isValid = false;
        } else if (!prenomField.getText().matches("[\\p{L} -]+")) {
            prenomField.getStyleClass().add("field-error");
            prenomError.setText("Le prénom ne doit contenir que des lettres");
            prenomError.setVisible(true);
            isValid = false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            emailField.getStyleClass().add("field-error");
            emailError.setText("L'email est requis");
            emailError.setVisible(true);
            isValid = false;
        } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailField.getStyleClass().add("field-error");
            emailError.setText("Format d'email invalide");
            emailError.setVisible(true);
            isValid = false;
        }
        
        if (telephoneField.getText().trim().isEmpty()) {
            telephoneField.getStyleClass().add("field-error");
            telephoneError.setText("Le téléphone est requis");
            telephoneError.setVisible(true);
            isValid = false;
        } else if (!telephoneField.getText().matches("\\d{8}")) {
            telephoneField.getStyleClass().add("field-error");
            telephoneError.setText("Le téléphone doit contenir 8 chiffres");
            telephoneError.setVisible(true);
            isValid = false;
        }
        
        if (typeUtilisateurComboBox.getValue() == null) {
            typeUtilisateurComboBox.getStyleClass().add("field-error");
            typeError.setText("Le type d'utilisateur est requis");
            typeError.setVisible(true);
            isValid = false;
        }
        
        if (statutComboBox.getValue() == null) {
            statutComboBox.getStyleClass().add("field-error");
            statutError.setText("Le statut est requis");
            statutError.setVisible(true);
            isValid = false;
        }
        
        // Validate salary if provided
        String salaire = salaireField.getText().trim();
        if (!salaire.isEmpty()) {
            try {
                double salaireValue = Double.parseDouble(salaire);
                if (salaireValue < 0) {
                    salaireField.getStyleClass().add("field-error");
                    generalError.setText("Le salaire ne peut pas être négatif");
                    generalError.setVisible(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                salaireField.getStyleClass().add("field-error");
                generalError.setText("Le salaire doit être un nombre valide");
                generalError.setVisible(true);
                isValid = false;
            }
        }
        
        return isValid;
    }

    private void clearErrors() {
        // Hide all error labels
        nomError.setVisible(false);
        prenomError.setVisible(false);
        emailError.setVisible(false);
        telephoneError.setVisible(false);
        typeError.setVisible(false);
        statutError.setVisible(false);
        generalError.setVisible(false);
        
        // Remove error styles
        nomField.getStyleClass().remove("field-error");
        prenomField.getStyleClass().remove("field-error");
        emailField.getStyleClass().remove("field-error");
        telephoneField.getStyleClass().remove("field-error");
        typeUtilisateurComboBox.getStyleClass().remove("field-error");
        statutComboBox.getStyleClass().remove("field-error");
        salaireField.getStyleClass().remove("field-error");
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
                currentUser.setNom(nomField.getText().trim());
                currentUser.setPrenom(prenomField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setTelephone(telephoneField.getText().trim());
                currentUser.setAdresse(adresseField.getText().trim());
                currentUser.setDateNaissance(dateNaissancePicker.getValue());
                currentUser.setDateDebutContrat(dateDebutContratPicker.getValue());
                currentUser.setDateFinContrat(dateFinContratPicker.getValue());
                
                // Handle password update
                String newPassword = passwordField.getText().trim();
                if (!newPassword.isEmpty()) {
                    currentUser.setPassword(newPassword);
                }
                
                // Handle optional salary
                String salaire = salaireField.getText().trim();
                if (!salaire.isEmpty()) {
                    try {
                        currentUser.setSalaire(Double.parseDouble(salaire));
                    } catch (NumberFormatException e) {
                        showError("Erreur", "Le salaire doit être un nombre valide");
                        salaireField.getStyleClass().add("field-error");
                        return;
                    }
                }
                
                currentUser.setTypeUtilisateur(typeUtilisateurComboBox.getValue());
                currentUser.setStatut(statutComboBox.getValue());
                
                utilisateurService.modifierUtilisateur(currentUser);
                showSuccess("Succès", "Les modifications ont été enregistrées avec succès");
                
                if (onUserModified != null) {
                    onUserModified.run();
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("email")) {
                    showError("Erreur", "Cet email est déjà utilisé");
                    emailField.getStyleClass().add("field-error");
                } else if (e.getMessage().contains("telephone")) {
                    showError("Erreur", "Ce numéro de téléphone est déjà utilisé");
                    telephoneField.getStyleClass().add("field-error");
                } else {
                    showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleBack() {
        if (onUserModified != null) {
            onUserModified.run();
        }
    }

    @FXML
    private void handleAnnuler() {
        if (onUserModified != null) {
            onUserModified.run();
        }
    }

    @FXML
    private void handleRetour() {
        if (onUserModified != null) {
            onUserModified.run();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 