package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import models.Produit;
import models.Utilisateur;
import services.CommandeDao;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class ClientCommandeFormController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextArea txtInstructions;

    @FXML
    private Label lblArticles;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnConfirmer;

    @FXML
    private Button btnRetour;

    @FXML
    private CheckBox chkUtiliserAdresseParDefaut;

    private ClientController clientController;
    private ClientProduitsController produitsController;
    private Utilisateur utilisateur;
    private CommandeDao commandeDao;
    private DecimalFormat df = new DecimalFormat("0.00");
    private Produit produitCommande;
    private int quantiteCommande;
    private UtilisateurService utilisateurService;

    @FXML
    private void initialize() {
        try {
            commandeDao = new CommandeDao();
            utilisateurService = new UtilisateurService();
            System.out.println("Initialisation du formulaire de commande intégré");

            // Ajouter un écouteur pour la case à cocher
            if (chkUtiliserAdresseParDefaut != null) {
                chkUtiliserAdresseParDefaut.setSelected(true);
                chkUtiliserAdresseParDefaut.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal && utilisateur != null) {
                        remplirChampsAvecInfosUtilisateur();
                    } else {
                        // Vider les champs si la case est décochée
                        txtNom.clear();
                        txtAdresse.clear();
                        txtTelephone.clear();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public void setProduitsController(ClientProduitsController produitsController) {
        this.produitsController = produitsController;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        System.out.println("Utilisateur défini dans le formulaire: " +
                (utilisateur != null ? "ID=" + utilisateur.getIdUser() : "null"));

        // Remplir automatiquement les champs avec les informations de l'utilisateur
        if (utilisateur != null && (chkUtiliserAdresseParDefaut == null || chkUtiliserAdresseParDefaut.isSelected())) {
            remplirChampsAvecInfosUtilisateur();
        }
    }

    private void remplirChampsAvecInfosUtilisateur() {
        if (utilisateur != null) {
            // Nom complet
            String prenom = utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "";
            String nom = utilisateur.getNom() != null ? utilisateur.getNom() : "";
            txtNom.setText(prenom + " " + nom);

            // Adresse
            if (utilisateur.getAdresse() != null && !utilisateur.getAdresse().isEmpty()) {
                txtAdresse.setText(utilisateur.getAdresse());
            }

            // Téléphone
            if (utilisateur.getTelephone() != null && !utilisateur.getTelephone().isEmpty()) {
                txtTelephone.setText(utilisateur.getTelephone());
            }
        }
    }

    public void setProduitCommande(Produit produit, int quantite) {
        this.produitCommande = produit;
        this.quantiteCommande = quantite;
        System.out.println("Produit défini: " + (produit != null ? produit.getNom() : "null") +
                ", Quantité: " + quantite);

        // Mettre à jour le récapitulatif avec le produit sélectionné
        if (produit != null) {
            lblArticles.setText("Article: " + produit.getNom() + " x " + quantite);
            lblTotal.setText("Total: " + df.format(produit.getPrix() * quantite) + " DT");
        }
    }

    private boolean validateInputs() {
        // Si les champs sont vides mais que l'utilisateur est connecté, utiliser ses informations
        if ((txtNom.getText() == null || txtNom.getText().trim().isEmpty()) &&
                utilisateur != null && utilisateur.getNom() != null) {
            remplirChampsAvecInfosUtilisateur();
        }

        StringBuilder errorMessage = new StringBuilder();

        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            errorMessage.append("Le nom ne peut pas être vide.\n");
        }

        if (txtAdresse.getText() == null || txtAdresse.getText().trim().isEmpty()) {
            errorMessage.append("L'adresse ne peut pas être vide.\n");
        }

        if (txtTelephone.getText() == null || txtTelephone.getText().trim().isEmpty()) {
            errorMessage.append("Le téléphone ne peut pas être vide.\n");
        }

        // Vérifier que l'utilisateur est défini
        if (utilisateur == null) {
            errorMessage.append("Erreur: Utilisateur non défini.\n");
        }

        // Vérifier que le produit est défini
        if (produitCommande == null) {
            errorMessage.append("Erreur: Aucun produit sélectionné.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez corriger les erreurs suivantes:", errorMessage.toString());
            return false;
        }

        return true;
    }
    @FXML
    private void handleConfirmer() {
        System.out.println("Confirmation de commande");

        if (validateInputs()) {
            try {
                // Vérifier que le CommandeDao est initialisé
                if (commandeDao == null) {
                    commandeDao = new CommandeDao();
                }

                String adresse = txtAdresse.getText();
                String instructions = txtInstructions.getText() != null ? txtInstructions.getText() : "";

                System.out.println("Création d'une commande directe pour le produit: " +
                        produitCommande.getNom());

                // Vérifier le stock avant de passer la commande
                if (produitCommande.getStock() < quantiteCommande) {
                    showAlert(Alert.AlertType.ERROR, "Stock insuffisant",
                            "Stock insuffisant",
                            "Il n'y a que " + produitCommande.getStock() + " unités disponibles.");
                    return;
                }

                // Créer la commande
                boolean success = commandeDao.createDirectOrder(
                        utilisateur.getIdUser(),
                        produitCommande.getId(),
                        quantiteCommande,
                        adresse,
                        instructions
                );

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Commande créée avec succès",
                            "Votre commande a été enregistrée.");
                    
                    try {
                        // Charger le dashboard client
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientDashboard.fxml"));
                        Parent dashboardView = loader.load();
                        
                        // Configurer le contrôleur du dashboard
                        ClientDashboardController dashboardController = loader.getController();
                        dashboardController.initData(utilisateur);
                        
                        // Obtenir la scène actuelle et remplacer son contenu
                        Scene currentScene = btnConfirmer.getScene();
                        currentScene.setRoot(dashboardView);
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "Erreur de navigation",
                                "Impossible de retourner au dashboard.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur lors de la création de la commande",
                            "Une erreur est survenue lors de la création de la commande.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la création de la commande",
                        "Une erreur est survenue: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRetour() {
        // Revenir directement à la liste des produits
        if (produitsController != null) {
            produitsController.retourListeProduits();
        } else if (clientController != null) {
            clientController.handleProduits();
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        if (clientController != null) {
            clientController.showAlert(alertType, title, header, content);
        } else {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
}