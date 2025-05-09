package services;

import javafx.scene.chart.XYChart;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceService {
    private final Map<Long, Integer> livraisonsReussiesParUtilisateur;
    private final Map<Long, Map<LocalDate, Double>> performancesJournalieres;

    public PerformanceService() {
        this.livraisonsReussiesParUtilisateur = new ConcurrentHashMap<>();
        this.performancesJournalieres = new ConcurrentHashMap<>();
    }

    public double calculerScorePerformance(Long userId) {
        Map<LocalDate, Double> performances = performancesJournalieres.getOrDefault(userId, new ConcurrentHashMap<>());
        if (performances.isEmpty()) return 0.0;

        return performances.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public int getNombreLivraisonsReussies(Long userId) {
        return livraisonsReussiesParUtilisateur.getOrDefault(userId, 0);
    }

    public double calculerPrimeEstimee(Long userId) {
        double scorePerformance = calculerScorePerformance(userId);
        int livraisonsReussies = getNombreLivraisonsReussies(userId);

        // Formule de calcul de la prime:
        // Base de 100 DT + bonus performance (jusqu'à 100 DT) + bonus livraisons (2 DT par livraison)
        double primeBase = 100.0;
        double bonusPerformance = (scorePerformance / 100.0) * 100.0;
        double bonusLivraisons = livraisonsReussies * 2.0;

        return primeBase + bonusPerformance + bonusLivraisons;
    }

    public void enregistrerLivraisonReussie(Long userId) {
        livraisonsReussiesParUtilisateur.merge(userId, 1, Integer::sum);
        
        // Mise à jour du score de performance
        LocalDate aujourdhui = LocalDate.now();
        Map<LocalDate, Double> performances = performancesJournalieres.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        performances.merge(aujourdhui, 1.0, (old, val) -> {
            double newScore = old + 0.5; // Augmente le score de 0.5 point par livraison
            return Math.min(newScore, 100.0); // Plafonne à 100
        });
    }

    public void enregistrerLivraisonEchouee(Long userId) {
        LocalDate aujourdhui = LocalDate.now();
        Map<LocalDate, Double> performances = performancesJournalieres.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        performances.merge(aujourdhui, 0.0, (old, val) -> {
            double newScore = old - 1.0; // Diminue le score de 1 point par échec
            return Math.max(newScore, 0.0); // Plancher à 0
        });
    }

    public XYChart.Series<String, Number> getPerformanceData(Long userId) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Score de Performance");

        Map<LocalDate, Double> performances = performancesJournalieres.getOrDefault(userId, new ConcurrentHashMap<>());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        performances.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(30) // Limiter aux 30 derniers jours
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(
                        entry.getKey().format(formatter),
                        entry.getValue()
                    ));
                });

        return series;
    }
} 