package Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.Utilisateur;
import services.ProduitDao;
import models.Produit;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ClientProduitsController {

    @FXML
    private ComboBox<String> categorieComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private FlowPane produitsContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private BorderPane mainContainer;

    private ProduitDao produitDao;
    private ClientController clientController;
    private Utilisateur utilisateur;
    private List<Produit> allProduits;

    @FXML
    private void initialize() {
        produitDao = new ProduitDao();
        System.out.println("Initialisation de ClientProduitsController");

        // Charger les produits
        loadProduits();
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    void loadProduits() {
        try {
            allProduits = produitDao.getAll();
            System.out.println("Produits chargés: " + allProduits.size());

            // Charger les catégories dans le ComboBox
            List<String> categories = allProduits.stream()
                    .map(Produit::getCategorie)
                    .distinct()
                    .collect(Collectors.toList());

            categorieComboBox.setItems(FXCollections.observableArrayList(categories));
            categorieComboBox.getItems().add(0, "Toutes");
            categorieComboBox.getSelectionModel().selectFirst();

            // Ajouter un listener pour filtrer par catégorie
            categorieComboBox.setOnAction(e -> filterProduits());

            // Afficher les produits
            displayProduits(allProduits);

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des produits: " + e.getMessage());
            e.printStackTrace();
            if (clientController != null) {
                clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                        "Impossible de charger les produits: " + e.getMessage());
            }
        }
    }

    private void displayProduits(List<Produit> produits) {
        produitsContainer.getChildren().clear();

        for (Produit produit : produits) {
            // Créer une carte pour chaque produit
            VBox produitCard = createProduitCard(produit);
            produitsContainer.getChildren().add(produitCard);
        }

        statusLabel.setText("Total produits: " + produits.size());
    }

    private VBox createProduitCard(Produit produit) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setPrefHeight(300);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("product-card");

        // En-tête de la carte
        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("product-name");
        nomLabel.setWrapText(true);

        // Badge de catégorie
        Label categorieLabel = new Label(produit.getCategorie());
        categorieLabel.getStyleClass().add("category-badge");

        // Informations du produit
        Label prixLabel = new Label(produit.getPrix() + " DT");
        prixLabel.getStyleClass().add("price-label");

        Label tailleLabel = new Label("Taille: " + produit.getTaille());
        tailleLabel.getStyleClass().add("size-label");

        // Indicateur de stock
        HBox stockBox = new HBox(5);
        stockBox.setAlignment(Pos.CENTER_LEFT);

        Circle stockIndicator = new Circle(5);
        stockIndicator.getStyleClass().add("stock-indicator");

        if (produit.getStock() > 10) {
            stockIndicator.getStyleClass().add("stock-high");
        } else if (produit.getStock() > 0) {
            stockIndicator.getStyleClass().add("stock-medium");
        } else {
            stockIndicator.getStyleClass().add("stock-low");
        }

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockLabel.getStyleClass().add("stock-label");
        stockBox.getChildren().addAll(stockIndicator, stockLabel);

        // Description (limitée)
        Label descriptionLabel = new Label(produit.getDescription());
        descriptionLabel.getStyleClass().add("description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxHeight(60);

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(nomLabel, categorieLabel, prixLabel, tailleLabel, stockBox, descriptionLabel);

        // Ajouter un gestionnaire d'événements pour afficher les détails au clic
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-produit-details-integrated-view.fxml"));
                Parent detailsView = loader.load();

                ClientProduitDetailsController controller = loader.getController();
                controller.setProduit(produit);
                controller.setUtilisateur(utilisateur);
                controller.setClientController(clientController);

                // Remplacer le contenu principal par les détails du produit
                if (mainContainer != null) {
                    mainContainer.setCenter(detailsView);
                } else {
                    // Si mainContainer n'est pas disponible, utiliser une approche alternative
                    BorderPane parent = (BorderPane) produitsContainer.getParent();
                    parent.setCenter(detailsView);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de l'affichage des détails du produit: " + e.getMessage());
                e.printStackTrace();
                clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                        "Impossible d'afficher les détails du produit: " + e.getMessage());
            }
        });

        return card;
    }

    @FXML
    private void handleSearch() {
        filterProduits();
    }

    private void filterProduits() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        String categorie = categorieComboBox.getValue();

        List<Produit> filteredProduits = allProduits.stream()
                .filter(p -> (categorie.equals("Toutes") || p.getCategorie().equals(categorie))
                        && (searchTerm.isEmpty() || p.getNom().toLowerCase().contains(searchTerm)
                        || p.getDescription().toLowerCase().contains(searchTerm)))
                .collect(Collectors.toList());

        displayProduits(filteredProduits);
    }

    // Méthode pour revenir à la liste des produits
    public void retourListeProduits() {
        if (mainContainer != null) {
            mainContainer.setCenter(produitsContainer);
        }
    }
}