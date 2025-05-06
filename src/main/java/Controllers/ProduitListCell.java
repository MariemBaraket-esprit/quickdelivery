package Controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Produit;

public class ProduitListCell extends ListCell<Produit> {

    private final MainController mainController;

    public ProduitListCell(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    protected void updateItem(Produit produit, boolean empty) {
        super.updateItem(produit, empty);

        if (empty || produit == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Création de la carte produit
            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

            // En-tête de la carte
            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);
            header.setSpacing(10);

            // Titre du produit
            Label nomLabel = new Label(produit.getNom());
            nomLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            nomLabel.setTextFill(Color.web("#2c3e50"));

            // Badge de catégorie
            Label categorieLabel = new Label(produit.getCategorie());
            categorieLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3;");

            header.getChildren().addAll(nomLabel, categorieLabel);

            // Informations du produit
            VBox infoBox = new VBox(5);

            Label prixLabel = new Label("Prix: " + produit.getPrix() + " DT");
            prixLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            prixLabel.setTextFill(Color.web("#e74c3c"));

            Label tailleLabel = new Label("Taille: " + produit.getTaille());
            Label stockLabel = new Label("Stock: " + produit.getStock());

            if (produit.getStock() < 10) {
                stockLabel.setTextFill(Color.web("#e74c3c"));
            } else {
                stockLabel.setTextFill(Color.web("#2ecc71"));
            }

            infoBox.getChildren().addAll(prixLabel, tailleLabel, stockLabel);

            // Description (limitée)
            Label descriptionLabel = new Label(produit.getDescription());
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMaxWidth(Double.MAX_VALUE);
            descriptionLabel.setMaxHeight(60);
            descriptionLabel.setStyle("-fx-text-fill: #7f8c8d;");

            // Boutons d'action
            HBox actionBox = new HBox(10);
            actionBox.setAlignment(Pos.CENTER_RIGHT);

            Button viewButton = new Button("Détails");
            viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
            viewButton.setOnAction(e -> mainController.showProduitDetails(produit));

            Button editButton = new Button("Modifier");
            editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
            editButton.setOnAction(e -> mainController.handleEditProduit(produit));

            Button deleteButton = new Button("Supprimer");
            deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> mainController.handleDeleteProduit(produit));

            actionBox.getChildren().addAll(viewButton, editButton, deleteButton);

            // Ajouter tous les éléments à la carte
            card.getChildren().addAll(header, infoBox, descriptionLabel, actionBox);

            // Définir la carte comme graphique de la cellule
            setGraphic(card);
        }
    }
}