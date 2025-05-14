package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import models.Commande;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class CommandeListCell extends ListCell<Commande> {

    private final CommandeController commandeController;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CommandeListCell(CommandeController commandeController) {
        this.commandeController = commandeController;
    }

    @Override
    protected void updateItem(Commande commande, boolean empty) {
        super.updateItem(commande, empty);

        if (empty || commande == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        try {
            // Créer le conteneur principal
            VBox cardContainer = new VBox(10);
            cardContainer.getStyleClass().add("commande-card");
            cardContainer.setPadding(new Insets(15));

            // Ligne d'en-tête avec ID et date
            HBox headerBox = new HBox(10);
            headerBox.getStyleClass().add("commande-header");

            Label idLabel = new Label("Commande #" + commande.getId());
            idLabel.getStyleClass().add("commande-id");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label dateLabel = new Label(dateFormatter.format(commande.getDateCommande()));
            dateLabel.getStyleClass().add("commande-date");

            headerBox.getChildren().addAll(idLabel, spacer, dateLabel);

            // Informations principales
            HBox mainInfoBox = new HBox(20);
            mainInfoBox.getStyleClass().add("commande-main-info");

            // Colonne gauche: Client et Produit
            VBox leftColumn = new VBox(5);
            Label clientLabel = new Label("Client: " + commande.getClientNom());
            clientLabel.getStyleClass().add("commande-client");

            Label produitLabel = new Label("Produit: " + commande.getProduitNom());
            produitLabel.getStyleClass().add("commande-produit");

            leftColumn.getChildren().addAll(clientLabel, produitLabel);

            // Colonne centrale: Quantité et Total
            VBox centerColumn = new VBox(5);
            Label quantiteLabel = new Label("Quantité: " + commande.getQuantite());
            quantiteLabel.getStyleClass().add("commande-quantite");

            Label totalLabel = new Label("Total: " + String.format("%.2f DT", commande.getTotal()));
            totalLabel.getStyleClass().add("commande-total");

            centerColumn.getChildren().addAll(quantiteLabel, totalLabel);

            // Colonne droite: Adresse
            VBox rightColumn = new VBox(5);
            Label adresseLabel = new Label("Adresse: " + commande.getAdresse());
            adresseLabel.getStyleClass().add("commande-adresse");

            Label detailsLabel = new Label("Détails: " +
                    (commande.getDescription() != null ? commande.getDescription() : "Aucun"));
            detailsLabel.getStyleClass().add("commande-details");
            detailsLabel.setWrapText(true);

            rightColumn.getChildren().addAll(adresseLabel, detailsLabel);

            mainInfoBox.getChildren().addAll(leftColumn, centerColumn, rightColumn);
            HBox.setHgrow(rightColumn, Priority.ALWAYS);

            // Boutons d'action
            HBox actionsBox = new HBox(10);
            actionsBox.getStyleClass().add("action-buttons-container");
            actionsBox.setPadding(new Insets(10, 0, 0, 0));

            Button btnDetails = new Button("Détails");
            btnDetails.getStyleClass().add("button-details");
            btnDetails.setOnAction(event -> commandeController.handleCommandeDetails(commande));

            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("button-edit");
            btnEdit.setOnAction(event -> commandeController.handleEditCommande(commande));

            Button btnDelete = new Button("Supprimer");
            btnDelete.getStyleClass().add("button-delete");
            btnDelete.setOnAction(event -> commandeController.handleDeleteCommande(commande));

            Region actionSpacer = new Region();
            HBox.setHgrow(actionSpacer, Priority.ALWAYS);

            actionsBox.getChildren().addAll(actionSpacer, btnDetails, btnEdit, btnDelete);

            // Assembler tous les éléments
            cardContainer.getChildren().addAll(headerBox, mainInfoBox, actionsBox);

            setGraphic(cardContainer);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la cellule de commande: " + e.getMessage());
            e.printStackTrace();
            setText("Erreur d'affichage");
        }
    }
}