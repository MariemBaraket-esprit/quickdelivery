package Controllers;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Produit;
import util.QRCodeGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class QRCodeGeneratorController implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private Label titleLabel;
    @FXML private ImageView qrCodeImageView;
    @FXML private Label idLabel;
    @FXML private Label nomLabel;
    @FXML private Label categorieLabel;
    @FXML private Label prixLabel;
    @FXML private Label stockLabel;
    @FXML private Button saveButton;
    @FXML private Button closeButton;

    private Produit produit;
    private WritableImage qrCodeImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger le CSS client
        Platform.runLater(() -> {
            try {
                String cssPath = getClass().getResource("/css/client.css").toExternalForm();
                mainPane.getScene().getStylesheets().add(cssPath);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement du CSS: " + e.getMessage());
            }
        });
    }

    public void setProduit(Produit produit) {
        this.produit = produit;

        // Mettre à jour le titre
        titleLabel.setText("QR Code pour " + produit.getNom());

        // Afficher les informations du produit
        idLabel.setText(String.valueOf(produit.getId()));
        nomLabel.setText(produit.getNom());
        categorieLabel.setText(produit.getCategorie());
        prixLabel.setText(produit.getPrix() + " DT");
        stockLabel.setText(String.valueOf(produit.getStock()));

        // Générer le QR code
        generateQRCode();
    }

    private void generateQRCode() {
        if (produit == null) return;

        // Créer le contenu du QR code
        String qrContent = createQRCodeContent();

        try {
            // Générer la matrice du QR code
            BitMatrix bitMatrix = QRCodeGenerator.generateQRCodeMatrix(qrContent, 300);

            // Convertir la matrice en image JavaFX
            qrCodeImage = new WritableImage(300, 300);
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    qrCodeImage.getPixelWriter().setColor(x, y,
                            bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // Afficher l'image
            qrCodeImageView.setImage(qrCodeImage);

        } catch (WriterException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de génération",
                    "Impossible de générer le QR code: " + e.getMessage());
        }
    }

    private String createQRCodeContent() {
        // Format: PRODUIT\nID: xxx\nNom: xxx\nCatégorie: xxx\nPrix: xxx\nStock: xxx
        StringBuilder content = new StringBuilder();
        content.append("PRODUIT\n");
        content.append("ID: ").append(produit.getId()).append("\n");
        content.append("Nom: ").append(produit.getNom()).append("\n");
        content.append("Catégorie: ").append(produit.getCategorie()).append("\n");
        content.append("Prix: ").append(produit.getPrix()).append(" DT\n");
        content.append("Taille: ").append(produit.getTaille()).append("\n");
        content.append("Stock: ").append(produit.getStock());

        return content.toString();
    }

    @FXML
    private void handleSave() {
        if (qrCodeImage == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun QR code",
                    "Aucun QR code n'a été généré.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le QR Code");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images PNG", "*.png")
        );

        // Nom de fichier par défaut basé sur le nom du produit
        String defaultFileName = produit.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "_QRCode.png";
        fileChooser.setInitialFileName(defaultFileName);

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // Convertir l'image JavaFX en BufferedImage
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(qrCodeImage, null);

                // Enregistrer l'image
                ImageIO.write(bufferedImage, "png", file);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "QR Code enregistré",
                        "Le QR code a été enregistré avec succès.");

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'enregistrement",
                        "Impossible d'enregistrer le QR code: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
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