package Controllers;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Produit;
import services.ProduitDao;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QRCodeScannerController implements Initializable {

    @FXML private BorderPane mainPane; // Ajoutez cette ligne
    @FXML private ImageView previewImageView;
    @FXML private Label noImageLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private VBox resultBox;
    @FXML private Label idLabel;
    @FXML private Label nomLabel;
    @FXML private Label categorieLabel;
    @FXML private Label prixLabel;
    @FXML private Label stockLabel;
    @FXML private Button uploadButton;
    @FXML private Button scanButton;
    @FXML private Button cancelButton;

    private ProduitDao produitDao;
    private ClientController clientController;
    private Produit scannedProduit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        produitDao = new ProduitDao();

        // Initialiser l'état de l'interface
        resultBox.setVisible(false);
        scanButton.setDisable(true);

        // Charger le CSS client
        Platform.runLater(() -> {
            try {
                if (mainPane.getScene() != null) {
                    String cssPath = getClass().getResource("/css/client.css").toExternalForm();
                    mainPane.getScene().getStylesheets().add(cssPath);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du CSS: " + e.getMessage());
            }
        });
    }
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image QR Code");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) uploadButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Afficher l'image sélectionnée
                Image image = new Image(selectedFile.toURI().toString());
                previewImageView.setImage(image);
                noImageLabel.setVisible(false);

                // Activer le bouton de scan
                scanButton.setDisable(false);

                // Mettre à jour le statut
                statusLabel.setText("Image chargée. Cliquez sur Scanner pour analyser.");

                // Cacher les résultats précédents
                resultBox.setVisible(false);

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                        "Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleScan() {
        if (previewImageView.getImage() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucune image",
                    "Veuillez d'abord charger une image contenant un QR code.");
            return;
        }

        // Afficher l'indicateur de progression
        progressIndicator.setVisible(true);
        statusLabel.setText("Analyse en cours...");

        // Exécuter l'analyse dans un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                // Convertir l'image JavaFX en BufferedImage
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(previewImageView.getImage(), null);

                // Analyser le QR code
                LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                Result result = new MultiFormatReader().decode(bitmap);
                String decodedText = result.getText();

                // Traiter le résultat sur le thread JavaFX
                Platform.runLater(() -> {
                    processQRCodeResult(decodedText);
                    progressIndicator.setVisible(false);
                    statusLabel.setText("Analyse terminée avec succès.");
                });

            } catch (Exception e) {
                // Gérer l'erreur sur le thread JavaFX
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'analyse",
                            "Impossible de détecter un QR code valide: " + e.getMessage());
                    progressIndicator.setVisible(false);
                    statusLabel.setText("Échec de l'analyse. Veuillez réessayer.");
                });
            }
        }).start();
    }

    private void processQRCodeResult(String decodedText) {
        // Vérifier si le texte décodé correspond au format attendu
        if (decodedText.startsWith("PRODUIT")) {
            // Extraire les informations du produit
            Map<String, String> productInfo = parseQRCodeContent(decodedText);

            if (productInfo.containsKey("ID")) {
                try {
                    int productId = Integer.parseInt(productInfo.get("ID"));

                    // Rechercher le produit dans la base de données
                    Produit produit = produitDao.getById(productId);

                    if (produit != null) {
                        // Produit trouvé, afficher les détails
                        scannedProduit = produit;
                        displayProductDetails(produit);
                    } else {
                        // Produit non trouvé, afficher les informations du QR code
                        displayQRCodeInfo(productInfo);
                    }

                } catch (NumberFormatException | SQLException e) {
                    showAlert(Alert.AlertType.WARNING, "Avertissement", "Format invalide",
                            "L'ID du produit n'est pas un nombre valide.");

                }
            } else {
                // Aucun ID trouvé, afficher les informations brutes
                displayQRCodeInfo(productInfo);
            }

        } else {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Format non reconnu",
                    "Le QR code ne contient pas d'informations de produit valides.");
        }
    }

    private Map<String, String> parseQRCodeContent(String content) {
        Map<String, String> result = new HashMap<>();

        // Diviser le contenu en lignes
        String[] lines = content.split("\n");

        // Ignorer la première ligne (PRODUIT)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];

            // Rechercher le format "Clé: Valeur"
            Pattern pattern = Pattern.compile("([^:]+):\\s*(.+)");
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                String key = matcher.group(1).trim();
                String value = matcher.group(2).trim();
                result.put(key, value);
            }
        }

        return result;
    }

    private void displayProductDetails(Produit produit) {
        // Afficher les détails du produit dans l'interface
        idLabel.setText(String.valueOf(produit.getId()));
        nomLabel.setText(produit.getNom());
        categorieLabel.setText(produit.getCategorie());
        prixLabel.setText(produit.getPrix() + " DT");
        stockLabel.setText(String.valueOf(produit.getStock()));

        // Afficher la section de résultat
        resultBox.setVisible(true);

        // Ajouter un bouton pour voir les détails complets
        Button viewDetailsButton = new Button("Voir les détails complets");
        viewDetailsButton.getStyleClass().add("view-details-button");
        viewDetailsButton.setOnAction(event -> {
            handleViewDetails();
        });

        // Vérifier si le bouton existe déjà pour éviter les doublons
        if (!resultBox.getChildren().stream().anyMatch(node -> node instanceof Button)) {
            resultBox.getChildren().add(viewDetailsButton);
        }
    }

    private void displayQRCodeInfo(Map<String, String> info) {
        // Afficher les informations du QR code
        idLabel.setText(info.getOrDefault("ID", "N/A"));
        nomLabel.setText(info.getOrDefault("Nom", "N/A"));
        categorieLabel.setText(info.getOrDefault("Catégorie", "N/A"));
        prixLabel.setText(info.getOrDefault("Prix", "N/A"));
        stockLabel.setText(info.getOrDefault("Stock", "N/A"));

        // Afficher la section de résultat
        resultBox.setVisible(true);

        // Ajouter un message indiquant que le produit n'est pas dans la base de données
        Label notFoundLabel = new Label("Ce produit n'existe pas dans la base de données.");
        notFoundLabel.getStyleClass().add("not-found-label");

        // Vérifier si le label existe déjà pour éviter les doublons
        if (!resultBox.getChildren().stream().anyMatch(node ->
                node instanceof Label && ((Label) node).getText().equals(notFoundLabel.getText()))) {
            resultBox.getChildren().add(notFoundLabel);
        }
    }

    private void handleViewDetails() {
        if (scannedProduit != null && clientController != null) {
            // Fermer la fenêtre de scan
            handleClose();

            // Ouvrir la fenêtre de détails du produit
            clientController.showProductDetails(scannedProduit);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}