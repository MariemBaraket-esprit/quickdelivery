package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Vehicule;
import org.json.JSONObject;
import javafx.scene.control.Tooltip;
import utils.Session;
import models.Utilisateur;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VehiculeCardController {

    @FXML private Label immatriculationLabel;
    @FXML private Label marqueLabel;
    @FXML private Label modeleLabel;
    @FXML private Label statutLabel;
    @FXML private Label typeLabel;
    @FXML private Label longueurLabel;
    @FXML private Label hauteurLabel;
    @FXML private Label largeurLabel;
    @FXML private Label poidsLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button reserveButton;
    @FXML private Button viewReservationsButton;
    @FXML private ImageView imageView;

    private Vehicule vehicule;
    private Runnable onEdit;
    private Runnable onDelete;
    private Runnable onReserve;
    private Runnable onViewReservations;

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        
        immatriculationLabel.setText(vehicule.getImmatriculation());
        marqueLabel.setText(vehicule.getMarque());
        modeleLabel.setText(vehicule.getModele());
        typeLabel.setText(vehicule.getType().toString());
        statutLabel.setText(vehicule.getStatut().toString());
        longueurLabel.setText(String.format("%.2f m", vehicule.getLongueur()));
        hauteurLabel.setText(String.format("%.2f m", vehicule.getHauteur()));
        largeurLabel.setText(String.format("%.2f m", vehicule.getLargeur()));
        if (poidsLabel != null) {
            poidsLabel.setText(String.format("%.2f kg", vehicule.getPoids()));
        }

        // Recherche d'image en ligne via Wikipedia
        afficherImageDepuisWikipedia(vehicule.getMarque() + " " + vehicule.getModele());

        Utilisateur currentUser = Session.getInstance().getCurrentUser();
        boolean isClient = currentUser != null && "CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur());
        if (editButton != null) {
            editButton.setVisible(!isClient);
            editButton.setManaged(!isClient);
        }
        if (deleteButton != null) {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
            deleteButton.setDisable(isClient);
            if (isClient) {
                deleteButton.setTooltip(new Tooltip("Seuls les responsables ou admins peuvent supprimer un véhicule."));
            } else {
                deleteButton.setTooltip(null);
            }
        }

        // Désactiver le bouton Réserver si le statut n'est pas EN_MARCHE
        if (reserveButton != null) {
            boolean reservable = vehicule.getStatut() != null && "EN_MARCHE".equals(vehicule.getStatut().name());
            reserveButton.setDisable(!reservable);
            if (!reservable) {
                reserveButton.setTooltip(new Tooltip("Ce véhicule n'est pas disponible à la réservation (statut : " + vehicule.getStatut() + ")"));
            } else {
                reserveButton.setTooltip(null);
            }
        }
    }

    private void afficherImageDepuisWikipedia(String nomVehicule) {
        String formatted = nomVehicule.trim().replace(" ", "_");
        String apiUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages&titles="
                + formatted + "&piprop=original";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject pages = json.getJSONObject("query").getJSONObject("pages");
                        String pageId = pages.keys().next();
                        JSONObject page = pages.getJSONObject(pageId);
                        if (page.has("original")) {
                            String imageUrl = page.getJSONObject("original").getString("source");
                            Platform.runLater(() -> {
                                imageView.setImage(new Image(imageUrl, true));
                            });
                        } else {
                            afficherImageParDefaut();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        afficherImageParDefaut();
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    afficherImageParDefaut();
                    return null;
                });
    }

    private void afficherImageParDefaut() {
        try (InputStream stream = getClass().getResourceAsStream("/images/vehicules/" + vehicule.getType().toString().toLowerCase() + ".png")) {
            if (stream != null) {
                Platform.runLater(() -> imageView.setImage(new Image(stream)));
            }
        } catch (Exception e) {
            System.out.println("Image locale introuvable pour le type : " + vehicule.getType());
        }
    }

    public void setOnEdit(Runnable onEdit) {
        this.onEdit = onEdit;
        editButton.setOnAction(e -> onEdit.run());
    }

    public void setOnDelete(Runnable onDelete) {
        this.onDelete = onDelete;
        deleteButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce véhicule ?");
            alert.setContentText("Immatriculation: " + (vehicule != null ? vehicule.getImmatriculation() : ""));
            ButtonType oui = new ButtonType("Oui");
            ButtonType non = new ButtonType("Non", ButtonType.CANCEL.getButtonData());
            alert.getButtonTypes().setAll(oui, non);
            alert.showAndWait().ifPresent(type -> {
                if (type == oui) {
                    onDelete.run();
                }
            });
        });
    }

    public void setOnReserve(Runnable onReserve) {
        this.onReserve = onReserve;
        reserveButton.setOnAction(e -> onReserve.run());
    }

    public void setOnViewReservations(Runnable onViewReservations) {
        this.onViewReservations = onViewReservations;
        viewReservationsButton.setOnAction(e -> onViewReservations.run());
    }

    @FXML
    public void handleViewReservations() {
        if (onViewReservations != null) {
            onViewReservations.run();
        }
    }

    @FXML
    public void handleModifier() {
        if (onEdit != null) {
            onEdit.run();
        }
    }

    @FXML
    public void handleSupprimer() {
        if (onDelete != null) {
            onDelete.run();
        }
    }

    @FXML
    public void handleReserver() {
        if (onReserve != null) {
            onReserve.run();
        }
    }

    @FXML
    public void initialize() {
        if (editButton != null) {
            editButton.setVisible(true);
            editButton.setManaged(true);
            editButton.setDisable(false);
        }
        if (deleteButton != null) {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
            deleteButton.setDisable(false);
        }
        if (reserveButton != null) {
            reserveButton.setVisible(true);
            reserveButton.setManaged(true);
            reserveButton.setDisable(false);
        }
        if (viewReservationsButton != null) {
            viewReservationsButton.setVisible(true);
            viewReservationsButton.setManaged(true);
            viewReservationsButton.setDisable(false);
        }
    }
}
