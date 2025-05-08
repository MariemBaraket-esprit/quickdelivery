package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

public class ClientCommandeFormIntegratedController {

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

                // Mettre à jour l'adresse de l'utilisateur si elle a changé
                if (utilisateur != null && !adresse.equals(utilisateur.getAdresse())) {
                    try {
                        utilisateur.setAdresse(adresse);
                        utilisateurService.modifierUtilisateur(utilisateur);
                        System.out.println("Adresse de l'utilisateur mise à jour");
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la mise à jour de l'adresse: " + e.getMessage());
                        // Ne pas bloquer la commande si la mise à jour de l'adresse échoue
                    }
                }

                boolean success = commandeDao.createDirectOrder(
                        utilisateur.getIdUser(),
                        produitCommande.getId(),
                        quantiteCommande,
                        adresse,
                        instructions
                );

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande passée",
                            "Votre commande a été passée avec succès.");

                    // SOLUTION DIRECTE et SIMPLIFIÉE: Charger directement la vue des produits
                    try {
                        System.out.println("Navigation vers la liste des produits...");

                        // Obtenir le container BorderPane racine
                        BorderPane mainContainer = (BorderPane) btnConfirmer.getScene().getRoot();

                        // Charger la vue des produits
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-produits-view.fxml"));
                        Parent produitsView = loader.load();

                        // Remplacer le contenu central du BorderPane
                        mainContainer.setCenter(produitsView);

                        // Configurer le contrôleur des produits
                        ClientProduitsController controller = loader.getController();
                        if (controller != null) {
                            // Transmettre les références nécessaires
                            if (clientController != null) {
                                controller.setClientController(clientController);
                            }
                            if (utilisateur != null) {
                                controller.setUtilisateur(utilisateur);
                            }

                            // Rafraîchir les produits
                            controller.loadProduits();
                        }
                    } catch (IOException e) {
                        System.err.println("Erreur lors du chargement de la vue des produits: " + e.getMessage());
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                                "Impossible de naviguer vers la liste des produits",
                                "Détails: " + e.getMessage());
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de commande",
                            "Impossible de passer la commande. Veuillez réessayer plus tard.");
                }
            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de la création de la commande: " + e.getMessage());
                e.printStackTrace();

                // Message d'erreur plus convivial pour l'utilisateur
                String errorMessage = "Une erreur est survenue lors de la création de la commande.";

                // Ajouter des détails spécifiques selon le type d'erreur SQL
                if (e.getMessage().contains("foreign key constraint")) {
                    errorMessage += "\nErreur de référence: L'utilisateur ou le produit n'existe pas.";
                } else if (e.getMessage().contains("duplicate")) {
                    errorMessage += "\nUne commande identique existe déjà.";
                } else if (e.getMessage().contains("connection")) {
                    errorMessage += "\nProblème de connexion à la base de données. Veuillez vérifier votre connexion internet.";
                } else {
                    errorMessage += "\nDétails techniques: " + e.getMessage();
                }

                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur de commande", errorMessage);
            } catch (Exception e) {
                System.err.println("Erreur inattendue lors de la création de la commande: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue",
                        "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.\n" +
                                "Si le problème persiste, contactez le support technique.");
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
