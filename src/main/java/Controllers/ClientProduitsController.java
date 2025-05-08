package Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.Utilisateur;
import services.ProduitDao;
import models.Produit;
import utils.ImageUtils;

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
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        // Charger les produits une fois que l'utilisateur est défini
        loadProduits();
    }

    public void loadProduits() {
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
        card.setPrefHeight(350); // Increased height for image
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-cursor: hand;");

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(190);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        // Load image from path or use default
        Image productImage = ImageUtils.loadProductImage(produit.getImagePath());
        imageView.setImage(productImage);

        // Center the image and add a border
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-border-radius: 3; -fx-background-radius: 3;");
        imageContainer.setPadding(new Insets(5));

        // En-tête de la carte
        Label nomLabel = new Label(produit.getNom());
        nomLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nomLabel.setTextFill(Color.web("#2c3e50"));
        nomLabel.setWrapText(true);

        // Badge de catégorie
        Label categorieLabel = new Label(produit.getCategorie());
        categorieLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3;");

        // Informations du produit
        Label prixLabel = new Label("Prix: " + produit.getPrix() + " DT");
        prixLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        prixLabel.setTextFill(Color.web("#e74c3c"));

        Label tailleLabel = new Label("Taille: " + produit.getTaille());

        // Indicateur de stock
        HBox stockBox = new HBox(5);
        stockBox.setAlignment(Pos.CENTER_LEFT);

        Circle stockIndicator = new Circle(5);
        if (produit.getStock() > 10) {
            stockIndicator.setFill(Color.web("#2ecc71"));
        } else if (produit.getStock() > 0) {
            stockIndicator.setFill(Color.web("#f39c12"));
        } else {
            stockIndicator.setFill(Color.web("#e74c3c"));
        }

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockBox.getChildren().addAll(stockIndicator, stockLabel);

        // Description (limitée)
        Label descriptionLabel = new Label(produit.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxHeight(40); // Reduced height for description
        descriptionLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imageContainer, nomLabel, categorieLabel, prixLabel, tailleLabel, stockBox, descriptionLabel);

        // Ajouter un gestionnaire d'événements pour afficher les détails au clic
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client-produit-details-integrated-view.fxml"));
                Parent detailsView = loader.load();

                ClientProduitDetailsIntegratedController controller = loader.getController();
                controller.setProduit(produit);
                controller.setUtilisateur(utilisateur);
                controller.setClientController(clientController);
                controller.setProduitsController(this);

                // Remplacer le contenu principal par les détails du produit
                if (mainContainer != null) {
                    mainContainer.setCenter(detailsView);
                } else {
                    // Si mainContainer n'est pas disponible, utiliser une approche alternative
                    BorderPane parent = (BorderPane) produitsContainer.getScene().getRoot();
                    parent.setCenter(detailsView);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de l'affichage des détails du produit: " + e.getMessage());
                e.printStackTrace();
                clientController.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'affichage",
                        "Impossible d'afficher les détails du produit: " + e.getMessage());
            }
        });

        // Effet de survol
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #3498db; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.3), 10, 0, 0, 5); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-cursor: hand;"));

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
        } else if (produitsContainer.getScene() != null) {
            BorderPane parent = (BorderPane) produitsContainer.getScene().getRoot();
            parent.setCenter(produitsContainer);
        }
    }
}
