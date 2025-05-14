package Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import models.Commande;
import models.Produit;
import services.CommandeDao;
import services.ProduitDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class StatistiquesController {

    @FXML private Label lblTotalVentes;
    @FXML private Label lblTotalCommandes;
    @FXML private Label lblValeurMoyenne;
    @FXML private Label lblTotalProduits;
    @FXML private Label lblProduitPlusVendu;
    @FXML private Label lblCategoriePrincipale;
    @FXML private Label lblCommandesMois;
    @FXML private Label lblQuantiteMoyenne;
    @FXML private Label lblJourPlusActif;

    @FXML private BarChart<String, Number> barChartVentesMensuelles;
    @FXML private PieChart pieChartCategories;
    @FXML private BarChart<String, Number> barChartTopProduits;
    @FXML private BarChart<String, Number> barChartStock;
    @FXML private BarChart<String, Number> barChartEvolutionCommandes;

    private final CommandeDao commandeService = new CommandeDao();
    private final ProduitDao produitService = new ProduitDao();

    @FXML
    public void initialize() {
        // Charger les données de manière asynchrone
        new Thread(this::chargerStatistiques).start();
    }

    private void chargerStatistiques() {
        try {
            // Récupérer les données
            List<Commande> commandes = commandeService.getAllCommandes();
            List<Produit> produits = produitService.getAllProduits();

            // Calculer les statistiques
            double totalVentes = commandes.stream().mapToDouble(Commande::getTotal).sum();
            int totalCommandes = commandes.size();
            double valeurMoyenne = totalCommandes > 0 ? totalVentes / totalCommandes : 0;

            // Statistiques produits
            int totalProduits = produits.size();

            // Trouver le produit le plus vendu
            Map<Integer, Integer> ventesParProduit = new HashMap<>();
            for (Commande commande : commandes) {
                ventesParProduit.put(commande.getProduitId(),
                        ventesParProduit.getOrDefault(commande.getProduitId(), 0) + commande.getQuantite());
            }

            String produitPlusVendu;
            if (!ventesParProduit.isEmpty()) {
                int idProduitPlusVendu = Collections.max(ventesParProduit.entrySet(), Map.Entry.comparingByValue()).getKey();
                produitPlusVendu = produits.stream()
                        .filter(p -> p.getId() == idProduitPlusVendu)
                        .findFirst()
                        .map(Produit::getNom)
                        .orElse("-");
            } else {
                produitPlusVendu = "-";
            }

            // Trouver la catégorie principale
            Map<String, Long> categoriesCount = produits.stream()
                    .collect(Collectors.groupingBy(Produit::getCategorie, Collectors.counting()));

            String categoriePrincipale;
            if (!categoriesCount.isEmpty()) {
                categoriePrincipale = Collections.max(categoriesCount.entrySet(), Map.Entry.comparingByValue()).getKey();
            } else {
                categoriePrincipale = "-";
            }

            // Statistiques commandes du mois en cours
            LocalDate now = LocalDate.now();
            int commandesMois = (int) commandes.stream()
                    .filter(c -> c.getDateCommande().getMonth() == now.getMonth() &&
                            c.getDateCommande().getYear() == now.getYear())
                    .count();

            // Quantité moyenne par commande
            double quantiteMoyenne = commandes.stream().mapToInt(Commande::getQuantite).average().orElse(0);

            // Jour le plus actif
            Map<String, Long> commandesParJour = commandes.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getDateCommande().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH),
                            Collectors.counting()));

            String jourPlusActif;
            if (!commandesParJour.isEmpty()) {
                jourPlusActif = Collections.max(commandesParJour.entrySet(), Map.Entry.comparingByValue()).getKey();
            } else {
                jourPlusActif = "-";
            }

            // Préparer les données pour les graphiques
            Map<String, Double> ventesMensuelles = getVentesMensuelles(commandes);
            Map<String, Integer> repartitionCategories = getRepartitionCategories(produits, commandes);
            Map<String, Integer> topProduits = getTopProduits(produits, commandes, 5);
            Map<String, Integer> stockProduits = getStockProduits(produits, 5);
            Map<String, Integer> evolutionCommandes = getEvolutionCommandes(commandes);

            // Mettre à jour l'interface utilisateur
            Platform.runLater(() -> {
                // Mettre à jour les labels
                lblTotalVentes.setText(String.format("%.2f dt", totalVentes));
                lblTotalCommandes.setText(String.valueOf(totalCommandes));
                lblValeurMoyenne.setText(String.format("%.2f dt", valeurMoyenne));
                lblTotalProduits.setText(String.valueOf(totalProduits));
                lblProduitPlusVendu.setText(produitPlusVendu);
                lblCategoriePrincipale.setText(categoriePrincipale);
                lblCommandesMois.setText(String.valueOf(commandesMois));
                lblQuantiteMoyenne.setText(String.format("%.1f articles", quantiteMoyenne));
                lblJourPlusActif.setText(jourPlusActif);

                // Mettre à jour les graphiques
                afficherBarChart(barChartVentesMensuelles, ventesMensuelles);
                afficherPieChart(pieChartCategories, repartitionCategories);
                afficherBarChart(barChartTopProduits, topProduits);
                afficherBarChart(barChartStock, stockProduits);
                afficherBarChart(barChartEvolutionCommandes, evolutionCommandes);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                // Afficher un message d'erreur si nécessaire
            });
        }
    }

    private Map<String, Double> getVentesMensuelles(List<Commande> commandes) {
        Map<String, Double> ventesMensuelles = new LinkedHashMap<>();

        // Initialiser tous les mois de l'année en cours
        for (Month month : Month.values()) {
            ventesMensuelles.put(month.getDisplayName(TextStyle.FULL, Locale.FRENCH), 0.0);
        }

        // Calculer les ventes pour chaque mois
        for (Commande commande : commandes) {
            if (commande.getDateCommande().getYear() == LocalDate.now().getYear()) {
                String mois = commande.getDateCommande().getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
                ventesMensuelles.put(mois, ventesMensuelles.getOrDefault(mois, 0.0) + commande.getTotal());
            }
        }

        return ventesMensuelles;
    }

    private Map<String, Integer> getRepartitionCategories(List<Produit> produits, List<Commande> commandes) {
        Map<String, Integer> repartition = new HashMap<>();

        // Calculer les ventes par catégorie
        for (Commande commande : commandes) {
            int produitId = commande.getProduitId();
            produits.stream()
                    .filter(p -> p.getId() == produitId)
                    .findFirst()
                    .ifPresent(produit -> {
                        String categorie = produit.getCategorie();
                        if (categorie != null && !categorie.isEmpty()) {
                            repartition.put(categorie, repartition.getOrDefault(categorie, 0) + commande.getQuantite());
                        }
                    });
        }

        return repartition;
    }

    private Map<String, Integer> getTopProduits(List<Produit> produits, List<Commande> commandes, int limit) {
        Map<Integer, Integer> ventesParProduit = new HashMap<>();

        // Calculer les ventes par produit
        for (Commande commande : commandes) {
            ventesParProduit.put(commande.getProduitId(),
                    ventesParProduit.getOrDefault(commande.getProduitId(), 0) + commande.getQuantite());
        }

        // Trier et limiter
        Map<String, Integer> topProduits = new LinkedHashMap<>();
        ventesParProduit.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> {
                    int produitId = entry.getKey();
                    produits.stream()
                            .filter(p -> p.getId() == produitId)
                            .findFirst()
                            .ifPresent(produit -> topProduits.put(produit.getNom(), entry.getValue()));
                });

        return topProduits;
    }

    private Map<String, Integer> getStockProduits(List<Produit> produits, int limit) {
        Map<String, Integer> stockProduits = new LinkedHashMap<>();

        // Trier par stock et limiter
        produits.stream()
                .sorted(Comparator.comparing(Produit::getStock))
                .limit(limit)
                .forEach(produit -> stockProduits.put(produit.getNom(), produit.getStock()));

        return stockProduits;
    }

    private Map<String, Integer> getEvolutionCommandes(List<Commande> commandes) {
        Map<String, Integer> evolutionCommandes = new LinkedHashMap<>();

        // Initialiser tous les mois de l'année en cours
        for (Month month : Month.values()) {
            evolutionCommandes.put(month.getDisplayName(TextStyle.SHORT, Locale.FRENCH), 0);
        }

        // Calculer le nombre de commandes pour chaque mois
        for (Commande commande : commandes) {
            if (commande.getDateCommande().getYear() == LocalDate.now().getYear()) {
                String mois = commande.getDateCommande().getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
                evolutionCommandes.put(mois, evolutionCommandes.getOrDefault(mois, 0) + 1);
            }
        }

        return evolutionCommandes;
    }

    private void afficherBarChart(BarChart<String, Number> barChart, Map<String, ? extends Number> data) {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

        barChart.getData().add(series);
    }

    private void afficherPieChart(PieChart pieChart, Map<String, Integer> data) {
        pieChart.getData().clear();

        data.forEach((key, value) -> pieChart.getData().add(new PieChart.Data(key, value)));
    }
}
