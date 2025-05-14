package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import models.Utilisateur;

import java.io.IOException;

public class ClientNavBarController {

    @FXML
    private Button btnProduits;

    @FXML
    private Button btnCommandes;

    @FXML
    private Label lblUserName;

    private Utilisateur utilisateur;

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        if (utilisateur != null) {
            lblUserName.setText("Bienvenue " + utilisateur.getPrenom());
        }
    }

    @FXML
    private void handleProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/ClientProduits.fxml"));
            Parent produitsView = loader.load();
            
            ClientProduitsController controller = loader.getController();
            controller.setUtilisateur(utilisateur);
            
            // Obtenir le BorderPane parent et remplacer le contenu central
            BorderPane borderPane = (BorderPane) btnProduits.getScene().getRoot();
            borderPane.setCenter(produitsView);
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur
        }
    }

    @FXML
    private void handleCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/ClientCommandes.fxml"));
            Parent commandesView = loader.load();
            
            ClientCommandesController controller = loader.getController();
            controller.setUtilisateur(utilisateur);
            
            // Obtenir le BorderPane parent et remplacer le contenu central
            BorderPane borderPane = (BorderPane) btnCommandes.getScene().getRoot();
            borderPane.setCenter(commandesView);
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur
        }
    }
} 