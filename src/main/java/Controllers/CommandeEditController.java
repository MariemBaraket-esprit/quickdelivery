package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Commande;
import models.Produit;
import services.CommandeDao;
import services.ProduitDao;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class CommandeEditController {

    @FXML
    private ComboBox<Produit> cbProduit;

    @FXML
    private Spinner<Integer> spinnerQuantite;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextArea txtInstructions;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private Commande commande;
    private CommandeDao commandeDao;
    private ProduitDao produitDao;
    private DecimalFormat df = new DecimalFormat("0.00");

    @FXML
    private void initialize() {
        try {
            commandeDao = new CommandeDao();
            produitDao = new ProduitDao();

            // Charger les produits pour le ComboBox
            List<Produit> produits = produitDao.getAll();
            cbProduit.getItems().addAll(produits);

            // Configurer l'affichage des produits dans le ComboBox
            cbProduit.setCellFactory(lv -> new ListCell<Produit>() {
                @Override
                protected void updateItem(Produit produit, boolean empty) {
                    super.updateItem(produit, empty);
                    if (empty || produit == null) {
                        setText(null);
                    } else {
                        setText(produit.getNom() + " - " + df.format(produit.getPrix()) + " DT");
                    }
                }
            });

            cbProduit.setButtonCell(new ListCell<Produit>() {
                @Override
                protected void updateItem(Produit produit, boolean empty) {
                    super.updateItem(produit, empty);
                    if (empty || produit == null) {
                        setText(null);
                    } else {
                        setText(produit.getNom() + " - " + df.format(produit.getPrix()) + " DT");
                    }
                }
            });

            // Ajouter un écouteur pour mettre à jour le total lorsque le produit ou la quantité change
            cbProduit.valueProperty().addListener((obs, oldVal, newVal) -> updateTotal());

            // Configurer le spinner de quantité
            spinnerQuantite.valueProperty().addListener((obs, oldVal, newVal) -> updateTotal());

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation",
                    "Impossible d'initialiser le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setCommande(Commande commande) {
        this.commande = commande;

        try {
            // Sélectionner le produit actuel dans le ComboBox
            Produit produitActuel = null;
            for (Produit p : cbProduit.getItems()) {
                if (p.getId() == commande.getProduitId()) {
                    produitActuel = p;
                    break;
                }
            }

            if (produitActuel == null) {
                // Si le produit n'est pas dans la liste, le récupérer de la base de données
                produitActuel = produitDao.getById(commande.getProduitId());
                if (produitActuel != null) {
                    cbProduit.getItems().add(produitActuel);
                }
            }

            cbProduit.setValue(produitActuel);

            // Configurer le spinner avec la quantité actuelle
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, commande.getQuantite());
            spinnerQuantite.setValueFactory(valueFactory);

            // Remplir les autres champs
            txtAdresse.setText(commande.getAdresse());
            txtInstructions.setText(commande.getDescription());

            // Mettre à jour le total
            updateTotal();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger les détails de la commande: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTotal() {
        Produit produit = cbProduit.getValue();
        Integer quantite = spinnerQuantite.getValue();

        if (produit != null && quantite != null) {
            double total = produit.getPrix() * quantite;
            lblTotal.setText("Total: " + df.format(total) + " DT");
        } else {
            lblTotal.setText("Total: 0.00 DT");
        }
    }

    @FXML
    private void handleSave() {
        if (validateInputs()) {
            try {
                // Mettre à jour l'objet commande avec les nouvelles valeurs
                Produit produit = cbProduit.getValue();
                commande.setProduitId(produit.getId());
                commande.setProduitNom(produit.getNom());
                commande.setQuantite(spinnerQuantite.getValue());
                commande.setAdresse(txtAdresse.getText());
                commande.setDescription(txtInstructions.getText());

                // Calculer le nouveau total
                double total = produit.getPrix() * commande.getQuantite();
                commande.setTotal(total);

                // Enregistrer les modifications
                boolean success = commandeDao.updateCommande(commande);

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Modification réussie",
                            "La commande a été modifiée avec succès.");
                    closeStage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de modification",
                            "Impossible de modifier la commande.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur de modification",
                        e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (cbProduit.getValue() == null) {
            errorMessage.append("Veuillez sélectionner un produit.\n");
        }

        if (spinnerQuantite.getValue() == null || spinnerQuantite.getValue() < 1) {
            errorMessage.append("La quantité doit être supérieure à 0.\n");
        }

        if (txtAdresse.getText() == null || txtAdresse.getText().trim().isEmpty()) {
            errorMessage.append("L'adresse ne peut pas être vide.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Veuillez corriger les erreurs suivantes:",
                    errorMessage.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}