package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import models.Pointage;
import models.DemandeConge;
import models.Utilisateur;
import services.PointageService;
import services.PerformanceService;
import services.CongeService;
import services.UtilisateurService;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import java.sql.SQLException;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.util.List;
import java.util.stream.IntStream;
import java.time.Month;
import java.util.Map;
import java.util.TreeMap;
import javafx.scene.chart.XYChart;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.control.Tooltip;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import java.net.URL;
import java.util.ResourceBundle;

public class GestionPerformancesController implements Initializable {
    @FXML private ComboBox<Utilisateur> performanceUserCombo;
    @FXML private ComboBox<String> performanceMonthCombo;
    @FXML private ComboBox<Integer> performanceYearCombo;
    @FXML private Label scorePerformanceLabel;
    @FXML private Label joursPresentsLabel;
    @FXML private Label heuresTravailleesLabel;
    @FXML private LineChart<String, Number> performanceChart;

    @FXML private TableView<Pointage> pointageTable;
    @FXML private TableColumn<Pointage, String> pointageNomColumn;
    @FXML private TableColumn<Pointage, String> pointageTypeColumn;
    @FXML private TableColumn<Pointage, LocalDate> dateColumn;
    @FXML private TableColumn<Pointage, String> heureArriveeColumn;
    @FXML private TableColumn<Pointage, String> heureDepartColumn;
    @FXML private TableColumn<Pointage, String> tempsTravailColumn;
    @FXML private TableColumn<Pointage, String> statutColumn;

    @FXML private TableView<DemandeConge> congesTable;
    @FXML private TableColumn<DemandeConge, String> congeNomColumn;
    @FXML private TableColumn<DemandeConge, String> congeTypeColumn;
    @FXML private TableColumn<DemandeConge, LocalDate> dateDebutColumn;
    @FXML private TableColumn<DemandeConge, LocalDate> dateFinColumn;
    @FXML private TableColumn<DemandeConge, String> typeCongeColumn;
    @FXML private TableColumn<DemandeConge, String> statutCongeColumn;
    @FXML private TableColumn<DemandeConge, String> descriptionCongeColumn;
    @FXML private TableColumn<DemandeConge, Void> actionsCongeColumn;

    @FXML private RadioButton filterByMonthRadio;
    @FXML private RadioButton filterByDayRadio;
    @FXML private HBox monthFilterBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private DatePicker pointageDatePicker;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;

    @FXML private ComboBox<String> congeMonthCombo;
    @FXML private ComboBox<Integer> congeYearCombo;

    private PointageService pointageService;
    private PerformanceService performanceService;
    private CongeService congeService;
    private UtilisateurService utilisateurService;
    private Long currentUserId;
    private String currentSearchTerm = "";

    private void configureTableColumns() {
        // Configuration de la taille minimale de la table
        pointageTable.setMinWidth(1000);  // Augmenté pour accommoder les colonnes plus larges
        
        // Configuration des colonnes
        pointageNomColumn.setMinWidth(200);
        pointageNomColumn.setPrefWidth(200);
        pointageNomColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        pointageNomColumn.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));

        pointageTypeColumn.setMinWidth(120);
        pointageTypeColumn.setPrefWidth(120);
        pointageTypeColumn.setStyle("-fx-alignment: CENTER;");
        pointageTypeColumn.setCellValueFactory(new PropertyValueFactory<>("typeUtilisateur"));

        // Augmentation de la largeur de la colonne date
        dateColumn.setMinWidth(150);
        dateColumn.setPrefWidth(150);
        dateColumn.setStyle("-fx-alignment: CENTER;");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("datePointage"));
        dateColumn.setCellFactory(column -> new TableCell<Pointage, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-alignment: CENTER;");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // Augmentation de la largeur des colonnes d'heures
        heureArriveeColumn.setMinWidth(130);
        heureArriveeColumn.setPrefWidth(130);
        heureArriveeColumn.setStyle("-fx-alignment: CENTER;");
        heureArriveeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getHeureEntree() != null ? 
                cellData.getValue().getHeureEntree().format(DateTimeFormatter.ofPattern("HH:mm")) : ""
            )
        );

        heureDepartColumn.setMinWidth(130);
        heureDepartColumn.setPrefWidth(130);
        heureDepartColumn.setStyle("-fx-alignment: CENTER;");
        heureDepartColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getHeureSortie() != null ? 
                cellData.getValue().getHeureSortie().format(DateTimeFormatter.ofPattern("HH:mm")) : ""
            )
        );

        tempsTravailColumn.setMinWidth(130);
        tempsTravailColumn.setPrefWidth(130);
        tempsTravailColumn.setStyle("-fx-alignment: CENTER;");
        tempsTravailColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDureeTravailFormatted()
            )
        );

        statutColumn.setMinWidth(100);
        statutColumn.setPrefWidth(100);
        statutColumn.setStyle("-fx-alignment: CENTER;");
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Permettre le redimensionnement des colonnes
        pointageTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Style des en-têtes de colonnes pour assurer la lisibilité
        String headerStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: CENTER;";
        pointageNomColumn.setStyle(headerStyle);
        pointageTypeColumn.setStyle(headerStyle);
        dateColumn.setStyle(headerStyle);
        heureArriveeColumn.setStyle(headerStyle);
        heureDepartColumn.setStyle(headerStyle);
        tempsTravailColumn.setStyle(headerStyle);
        statutColumn.setStyle(headerStyle);
    }

    private void setupFilterControls() {
        // Configuration des mois
        monthComboBox.setItems(FXCollections.observableArrayList(
            "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        ));
        monthComboBox.setValue("Janvier");

        // Configuration des années (5 ans avant et après l'année courante)
        int currentYear = LocalDate.now().getYear();
        yearComboBox.setItems(FXCollections.observableArrayList(
            IntStream.rangeClosed(currentYear - 5, currentYear + 5).boxed().toList()
        ));
        yearComboBox.setValue(currentYear);

        // Configuration initiale
        pointageDatePicker.setValue(LocalDate.now());
        updateFilterVisibility();

        // Listeners pour les contrôles de filtrage
        filterByMonthRadio.selectedProperty().addListener((obs, oldVal, newVal) -> updateFilterVisibility());
        filterByDayRadio.selectedProperty().addListener((obs, oldVal, newVal) -> updateFilterVisibility());
    }

    private void updateFilterVisibility() {
        boolean isMonthFilter = filterByMonthRadio.isSelected();
        monthFilterBox.setVisible(isMonthFilter);
        monthFilterBox.setManaged(isMonthFilter);
        pointageDatePicker.setVisible(!isMonthFilter);
        pointageDatePicker.setManaged(!isMonthFilter);
    }

    private void setupSearchControls() {
        // Configuration du champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Recherche: ancien terme = '" + oldValue + "', nouveau terme = '" + newValue + "'");
            currentSearchTerm = newValue != null ? newValue.toLowerCase().trim() : "";
            loadData();
        });

        // Configuration du bouton d'effacement
        clearSearchButton.setOnAction(event -> {
            System.out.println("Effacement de la recherche");
            searchField.clear();
            currentSearchTerm = "";
            loadData();
        });

        // Initialisation
        currentSearchTerm = "";
        System.out.println("Contrôles de recherche initialisés");
    }

    private void setupPerformanceControls() {
        try {
            // Configuration des mois
            performanceMonthCombo.setItems(FXCollections.observableArrayList(
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
            ));
            
            // Définir le mois actuel
            int currentMonth = LocalDate.now().getMonthValue() - 1; // -1 car l'index commence à 0
            performanceMonthCombo.getSelectionModel().select(currentMonth);

            // Configuration des années
            int currentYear = LocalDate.now().getYear();
            performanceYearCombo.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(currentYear - 5, currentYear + 5).boxed().toList()
            ));
            performanceYearCombo.setValue(currentYear);

            // Configuration du ComboBox des utilisateurs (uniquement livreurs et magasiniers)
            ObservableList<Utilisateur> eligibleUsers = FXCollections.observableArrayList(
                utilisateurService.getAllUtilisateurs().stream()
                    .filter(u -> "LIVREUR".equalsIgnoreCase(u.getTypeUtilisateur()) || 
                               "MAGASINIER".equalsIgnoreCase(u.getTypeUtilisateur()))
                    .toList()
            );
            performanceUserCombo.setItems(eligibleUsers);
            
            // Configuration de l'affichage des utilisateurs dans le ComboBox
            performanceUserCombo.setCellFactory(param -> new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getPrenom() + " " + user.getNom() + " (" + user.getTypeUtilisateur() + ")");
                    }
                }
            });
            performanceUserCombo.setButtonCell(performanceUserCombo.getCellFactory().call(null));

            // Sélectionner le premier utilisateur par défaut si la liste n'est pas vide
            if (!eligibleUsers.isEmpty()) {
                performanceUserCombo.setValue(eligibleUsers.get(0));
            }

            // Listeners pour les mises à jour
            // Utiliser Platform.runLater pour éviter les mises à jour pendant l'initialisation
            performanceUserCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && performanceUserCombo.getScene() != null) {
                    updatePerformanceData();
                }
            });

            performanceMonthCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && performanceMonthCombo.getScene() != null) {
                    updatePerformanceData();
                }
            });

            performanceYearCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && performanceYearCombo.getScene() != null) {
                    updatePerformanceData();
                }
            });

        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des contrôles de performance: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePerformanceData() {
        try {
            if (performanceUserCombo == null || performanceUserCombo.getValue() == null || 
                performanceMonthCombo == null || performanceYearCombo == null || 
                performanceChart == null) {
                return; // Sortir silencieusement si les composants ne sont pas initialisés
            }

            Long userId = Long.valueOf(performanceUserCombo.getValue().getIdUser());
            int selectedYear = performanceYearCombo.getValue();
            int selectedMonth = performanceMonthCombo.getSelectionModel().getSelectedIndex() + 1;

            // Mise à jour du graphique
            updatePerformanceChart(userId, selectedYear, selectedMonth);

            // Mise à jour des statistiques
            updateStatistics();

        } catch (Exception e) {
            // Log l'erreur pour le débogage
            System.err.println("Erreur lors de la mise à jour des performances: " + e.getMessage());
            e.printStackTrace();
            
            // Ne pas afficher l'erreur si c'est pendant l'initialisation
            if (performanceUserCombo != null && performanceUserCombo.getScene() != null) {
                showError("Erreur", "Erreur lors de la mise à jour des performances: " + e.getMessage());
            }
        }
    }

    private void updatePerformanceChart(Long userId, int year, int month) {
        try {
            performanceChart.getData().clear();

            // Créer une seule série pour les heures travaillées
            XYChart.Series<String, Number> heuresSeries = new XYChart.Series<>();
            heuresSeries.setName("Heures travaillées");

            // Récupérer les pointages du mois sélectionné
            List<Pointage> pointages = pointageService.getPointagesUtilisateur(userId).stream()
                .filter(p -> {
                    LocalDate date = p.getDatePointage();
                    return date.getYear() == year && date.getMonthValue() == month;
                })
                .toList();

            // Créer un ensemble de tous les jours du mois
            YearMonth yearMonth = YearMonth.of(year, month);
            int daysInMonth = yearMonth.lengthOfMonth();
            
            // Grouper les heures par jour
            Map<Integer, Double> heuresParJour = new TreeMap<>();
            
            // Remplir les heures travaillées par jour
            for (Pointage p : pointages) {
                int jour = p.getDatePointage().getDayOfMonth();
                double heures = p.getDureeTravailHeures();
                if (heures > 0) {
                    heuresParJour.merge(jour, heures, Double::sum);
                }
            }

            // Ajouter les données au graphique pour chaque jour du mois
            for (int jour = 1; jour <= daysInMonth; jour++) {
                String jourStr = String.format("%02d", jour);
                double heures = heuresParJour.getOrDefault(jour, 0.0);
                
                // Ajouter le point avec le format "Jour\nHeures" ou juste "Jour" si pas d'heures
                String label = heures > 0 ? 
                    String.format("%d\n%.1fh", jour, heures) : 
                    String.valueOf(jour);
                
                heuresSeries.getData().add(new XYChart.Data<>(label, heures));
            }

            performanceChart.getData().add(heuresSeries);

            // Styliser le graphique
            heuresSeries.getNode().setStyle("-fx-stroke: #2196f3; -fx-stroke-width: 2px;");

            // Styliser les points de données
            for (XYChart.Data<String, Number> data : heuresSeries.getData()) {
                if (data.getNode() != null) {
                    double heures = data.getYValue().doubleValue();
                    String style;
                    
                    if (heures > 0) {
                        // Point bleu pour les jours travaillés
                        style = "-fx-background-color: #2196f3, white; " +
                               "-fx-background-insets: 0, 2; " +
                               "-fx-background-radius: 5px; " +
                               "-fx-padding: 5px;";
                    } else {
                        // Point gris clair pour les jours sans travail
                        style = "-fx-background-color: #e0e0e0, white; " +
                               "-fx-background-insets: 0, 2; " +
                               "-fx-background-radius: 5px; " +
                               "-fx-padding: 5px;";
                    }
                    
                    data.getNode().setStyle(style);

                    // Ajouter un tooltip
                    String jour = data.getXValue().split("\n")[0];
                    String tooltipText = heures > 0 ?
                        String.format("Jour %s\nHeures travaillées: %.1f", jour, heures) :
                        String.format("Jour %s\nAucune heure travaillée", jour);
                    
                    Tooltip tooltip = new Tooltip(tooltipText);
                    Tooltip.install(data.getNode(), tooltip);
                }
            }

            // Configurer l'axe X pour une meilleure lisibilité
            CategoryAxis xAxis = (CategoryAxis) performanceChart.getXAxis();
            xAxis.setTickLabelRotation(0);
            xAxis.setTickLabelGap(5);
            xAxis.setStyle("-fx-tick-label-font-size: 10px;");

            // Configurer l'axe Y
            NumberAxis yAxis = (NumberAxis) performanceChart.getYAxis();
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);  // Commencer à 0
            yAxis.setUpperBound(Math.max(12, heuresParJour.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(12.0) + 1));
            yAxis.setTickUnit(1);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la mise à jour du graphique: " + e.getMessage());
        }
    }

    private void setupCongeControls() {
        try {
            // Configuration des mois
            ObservableList<String> months = FXCollections.observableArrayList(
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
            );
            congeMonthCombo.setItems(months);
            
            // Définir le mois actuel
            int currentMonth = LocalDate.now().getMonthValue() - 1; // -1 car l'index commence à 0
            congeMonthCombo.getSelectionModel().select(currentMonth);

            // Configuration des années
            int currentYear = LocalDate.now().getYear();
            ObservableList<Integer> years = FXCollections.observableArrayList(
                IntStream.rangeClosed(currentYear - 5, currentYear + 5).boxed().toList()
            );
            congeYearCombo.setItems(years);
            congeYearCombo.setValue(currentYear);

            // Listeners pour les mises à jour
            congeMonthCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadConges();
                }
            });

            congeYearCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadConges();
                }
            });

            // Charger les congés initiaux
            loadConges();

        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des contrôles de congé: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la configuration des contrôles de congé: " + e.getMessage());
        }
    }

    private void loadConges() {
        try {
            if (congeMonthCombo.getSelectionModel().getSelectedItem() == null || congeYearCombo.getValue() == null) {
                return;
            }

            List<DemandeConge> allConges = congeService.getAllConges();
            
            // Obtenir l'index du mois sélectionné (0-11)
            int monthIndex = congeMonthCombo.getSelectionModel().getSelectedIndex();
            Integer year = congeYearCombo.getValue();
            
            if (monthIndex >= 0 && year != null) {
                Month selectedMonth = Month.values()[monthIndex];
                
                List<DemandeConge> filteredConges = allConges.stream()
                    .filter(conge -> {
                        LocalDate dateDebut = conge.getDateDebut();
                        LocalDate dateFin = conge.getDateFin();
                        YearMonth yearMonth = YearMonth.of(year, selectedMonth);
                        LocalDate firstDayOfMonth = yearMonth.atDay(1);
                        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
                        
                        // Un congé est inclus s'il commence, se termine ou chevauche le mois/année sélectionné
                        return (dateDebut.getMonth() == selectedMonth && dateDebut.getYear() == year) ||
                               (dateFin.getMonth() == selectedMonth && dateFin.getYear() == year) ||
                               (dateDebut.isBefore(firstDayOfMonth) && dateFin.isAfter(lastDayOfMonth));
                    })
                    .toList();
                
                updateCongesTable(filteredConges);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors du chargement des congés: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("Début de l'initialisation du contrôleur");
            
            // Initialisation des services
            pointageService = new PointageService();
            performanceService = new PerformanceService();
            congeService = new CongeService();
            utilisateurService = new UtilisateurService();

            // Configuration des contrôles de filtrage
            setupFilterControls();

            // Configuration de la recherche
            setupSearchControls();

            // Configuration des performances
            setupPerformanceControls();

            // Configuration des congés
            setupCongeControls();

            // Configuration des colonnes du tableau de pointage
            configureTableColumns();

            // Configuration des colonnes du tableau des congés
            configureCongesTable();

            // Ajout des listeners
            setupListeners();

            // Style global des tables
            pointageTable.setStyle("-fx-font-size: 14px;");
            congesTable.setStyle("-fx-font-size: 14px;");

            // Charger les données initiales
            loadData();
            loadConges();

            System.out.println("Initialisation du contrôleur terminée");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Une erreur est survenue: " + e.getMessage());
        }
    }

    public void initData(Long userId) {
        try {
            this.currentUserId = userId;
            
            // Récupérer l'utilisateur actuel
            Utilisateur currentUser = utilisateurService.getUtilisateurById(userId);
            
            // Gérer la visibilité de l'onglet Performances
            handlePerformanceTabVisibility(currentUser);
            
            // Sélectionner l'utilisateur actuel dans le ComboBox
            for (Utilisateur user : performanceUserCombo.getItems()) {
                if (user.getIdUser() == userId.intValue()) {
                    performanceUserCombo.setValue(user);
                    break;
                }
            }
            loadData();
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Erreur lors de l'initialisation des données: " + e.getMessage());
        }
    }

    private void handlePerformanceTabVisibility(Utilisateur currentUser) {
        try {
            // Récupérer le TabPane et l'onglet Performances
            TabPane tabPane = (TabPane) performanceUserCombo.getScene().lookup(".tab-pane-custom");
            if (tabPane != null) {
                for (Tab tab : tabPane.getTabs()) {
                    if (tab.getText().equals("Performances")) {
                        // Cacher l'onglet si l'utilisateur est un client
                        boolean isClient = "CLIENT".equalsIgnoreCase(currentUser.getTypeUtilisateur());
                        tab.setDisable(isClient);
                        
                        // Si l'onglet est caché et sélectionné, sélectionner le premier onglet non désactivé
                        if (isClient && tab.isSelected()) {
                            for (Tab otherTab : tabPane.getTabs()) {
                                if (!otherTab.isDisabled()) {
                                    tabPane.getSelectionModel().select(otherTab);
                                    break;
                                }
                            }
                        }
                        
                        // Si c'est un client, retirer l'onglet du TabPane
                        if (isClient) {
                            tabPane.getTabs().remove(tab);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la gestion de la visibilité de l'onglet Performances: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            System.out.println("Chargement des données...");
            
            // Chargement et filtrage des pointages
            List<Pointage> allPointages = pointageService.getAllPointages();
            List<Pointage> searchFilteredPointages;
            if (currentSearchTerm != null && !currentSearchTerm.isEmpty()) {
                searchFilteredPointages = allPointages.stream()
                    .filter(p -> matchesSearchCriteria(p, currentSearchTerm))
                    .toList();
            } else {
                searchFilteredPointages = allPointages;
            }
            
            // Filtrage selon le mode sélectionné
            final List<Pointage> filteredPointages;
            if (filterByMonthRadio.isSelected()) {
                int monthIndex = monthComboBox.getSelectionModel().getSelectedIndex();
                int year = yearComboBox.getValue();
                Month selectedMonth = Month.values()[monthIndex];
                
                filteredPointages = searchFilteredPointages.stream()
                    .filter(p -> {
                        LocalDate date = p.getDatePointage();
                        return date.getMonth() == selectedMonth && date.getYear() == year;
                    })
                    .toList();
            } else {
                LocalDate selectedDate = pointageDatePicker.getValue();
                if (selectedDate != null) {
                    filteredPointages = searchFilteredPointages.stream()
                        .filter(p -> p.getDatePointage().equals(selectedDate))
                        .toList();
                } else {
                    filteredPointages = searchFilteredPointages;
                }
            }
            
            // Mise à jour de la table des pointages
            updateTableContent(filteredPointages);
            
            // Mise à jour des statistiques si un utilisateur est sélectionné
            if (currentUserId != null) {
                updateStatistics();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private boolean matchesSearchCriteria(Pointage pointage, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty() || pointage == null) {
            return true;
        }

        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            // Convertir toutes les valeurs en chaînes minuscules pour une recherche insensible à la casse
            String nomComplet = pointage.getNomComplet() != null ? pointage.getNomComplet().toLowerCase() : "";
            String type = pointage.getTypeUtilisateur() != null ? pointage.getTypeUtilisateur().toLowerCase() : "";
            String date = pointage.getDatePointage() != null ? pointage.getDatePointage().format(dateFormatter).toLowerCase() : "";
            String heureEntree = pointage.getHeureEntree() != null ? pointage.getHeureEntree().format(timeFormatter).toLowerCase() : "";
            String heureSortie = pointage.getHeureSortie() != null ? pointage.getHeureSortie().format(timeFormatter).toLowerCase() : "";
            String statut = pointage.getStatut() != null ? pointage.getStatut().toLowerCase() : "";

            boolean matches = nomComplet.contains(searchTerm) ||
                            type.contains(searchTerm) ||
                            date.contains(searchTerm) ||
                            heureEntree.contains(searchTerm) ||
                            heureSortie.contains(searchTerm) ||
                            statut.contains(searchTerm);

            // Debug pour voir les correspondances
            if (matches) {
                System.out.println("Correspondance trouvée pour '" + searchTerm + "' dans:");
                System.out.println("- Nom: " + nomComplet);
                System.out.println("- Type: " + type);
                System.out.println("- Date: " + date);
                System.out.println("- Entrée: " + heureEntree);
                System.out.println("- Sortie: " + heureSortie);
                System.out.println("- Statut: " + statut);
            }

            return matches;
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void updateTableContent(List<Pointage> pointages) {
        try {
            // Vérifier que la table n'est pas null
            if (pointageTable == null) {
                System.err.println("ERREUR: pointageTable est null!");
                return;
            }

            // Créer une nouvelle ObservableList
            ObservableList<Pointage> pointageData = FXCollections.observableArrayList(pointages);
            
            // Vérifier les colonnes
            System.out.println("Vérification des colonnes:");
            System.out.println("- Nom: " + (pointageNomColumn != null ? "OK" : "NULL"));
            System.out.println("- Type: " + (pointageTypeColumn != null ? "OK" : "NULL"));
            System.out.println("- Date: " + (dateColumn != null ? "OK" : "NULL"));
            
            // Mettre à jour la table
            pointageTable.setItems(null); // Clear existing items
            pointageTable.setItems(pointageData); // Set new items
            pointageTable.refresh();
            
            System.out.println("Table mise à jour avec " + pointageData.size() + " entrées");
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de la table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatistics() throws SQLException {
        // Calcul des statistiques de performance
        double scorePerformance = performanceService.calculerScorePerformance(currentUserId);
        int joursPresents = pointageService.getJoursPresents(currentUserId);
        double heuresTravaillees = pointageService.getHeuresTravaillees(currentUserId);

        // Mise à jour des labels
        scorePerformanceLabel.setText(String.format("%.1f%%", scorePerformance));
        joursPresentsLabel.setText(String.valueOf(joursPresents));
        heuresTravailleesLabel.setText(String.format("%.1f h", heuresTravaillees));
    }

    private void configureCongesTable() {
        // Configuration des colonnes du tableau des congés
        congeNomColumn.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        congeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("typeUtilisateur"));
        
        // Configuration de la colonne date début
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(column -> new TableCell<DemandeConge, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // Configuration de la colonne date fin
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(column -> new TableCell<DemandeConge, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        typeCongeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statutCongeColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        // Configuration de la colonne description
        descriptionCongeColumn.setCellValueFactory(new PropertyValueFactory<>("motif"));
        descriptionCongeColumn.setCellFactory(column -> {
            TableCell<DemandeConge, String> cell = new TableCell<DemandeConge, String>() {
                private Text text;
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        text = new Text(item);
                        text.setWrappingWidth(200); // Largeur maximale du texte
                        setGraphic(text);
                    }
                }
            };
            // Ajouter un tooltip pour voir le texte complet
            cell.setOnMouseEntered(event -> {
                if (cell.getItem() != null && !cell.getItem().isEmpty()) {
                    Tooltip tooltip = new Tooltip(cell.getItem());
                    Tooltip.install(cell, tooltip);
                }
            });
            return cell;
        });

        // Configuration des actions
        actionsCongeColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approuverButton = new Button("Approuver");
            private final Button refuserButton = new Button("Refuser");
            private final HBox buttonsBox = new HBox(5, approuverButton, refuserButton);

            {
                approuverButton.setOnAction(event -> {
                    DemandeConge conge = getTableView().getItems().get(getIndex());
                    handleApprouverConge(conge);
                });

                refuserButton.setOnAction(event -> {
                    DemandeConge conge = getTableView().getItems().get(getIndex());
                    handleRefuserConge(conge);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DemandeConge conge = getTableView().getItems().get(getIndex());
                    if ("EN_ATTENTE".equals(conge.getStatut())) {
                        setGraphic(buttonsBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        // Style des colonnes
        congeNomColumn.setMinWidth(150);
        congeTypeColumn.setMinWidth(100);
        dateDebutColumn.setMinWidth(100);
        dateFinColumn.setMinWidth(100);
        typeCongeColumn.setMinWidth(120);
        statutCongeColumn.setMinWidth(100);
        descriptionCongeColumn.setMinWidth(200);
        actionsCongeColumn.setMinWidth(200);

        // Style du tableau
        congesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    private void updateCongesTable(List<DemandeConge> conges) {
        if (congesTable != null) {
            congesTable.setItems(FXCollections.observableArrayList(conges));
            congesTable.refresh();
        }
    }

    private void handleApprouverConge(DemandeConge conge) {
        try {
            congeService.updateStatutDemandeConge(conge.getId(), "APPROUVE");
            loadData();
            showSuccess("Congé approuvé avec succès");
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de l'approbation du congé: " + e.getMessage());
        }
    }

    private void handleRefuserConge(DemandeConge conge) {
        try {
            congeService.updateStatutDemandeConge(conge.getId(), "REFUSE");
            loadData();
            showSuccess("Congé refusé");
        } catch (Exception e) {
            showError("Erreur", "Erreur lors du refus du congé: " + e.getMessage());
        }
    }

    @FXML
    private void refreshConges() {
        loadData();
    }

    @FXML
    private void refreshPointage() {
        System.out.println("Rafraîchissement des données de pointage");
        loadData();
    }

    @FXML
    private void refreshPerformance() {
        loadData();
    }

    @FXML
    private void handlePointageDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PointageDialog.fxml"));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Pointage de présence");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            PointageDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isValid()) {
                loadData(); // Rafraîchir les données après un pointage réussi
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le dialogue de pointage: " + e.getMessage());
        }
    }

    @FXML
    private void handlePointage() {
        try {
            boolean isEntree = pointageService.isPointageEntree(currentUserId);
            if (isEntree) {
                pointageService.enregistrerEntree(currentUserId);
                showSuccess("Pointage d'entrée enregistré avec succès");
            } else {
                pointageService.enregistrerSortie(currentUserId);
                showSuccess("Pointage de sortie enregistré avec succès");
            }
            // Rafraîchir immédiatement les données après le pointage
            loadData();
        } catch (Exception e) {
            showError("Erreur", "Erreur lors du pointage: " + e.getMessage());
        }
    }

    @FXML
    private void handleDemandeConge() {
        try {
            // Création du dialogue
            Dialog<DemandeConge> dialog = new Dialog<>();
            dialog.setTitle("Nouvelle demande de congé");
            dialog.setHeaderText("Créer une demande de congé");

            // Création des champs du formulaire
            ComboBox<Utilisateur> employeCombo = new ComboBox<>();
            employeCombo.setPromptText("Sélectionner un employé");
            // Filtrer pour n'avoir que les livreurs et magasiniers
            ObservableList<Utilisateur> eligibleUsers = FXCollections.observableArrayList(
                utilisateurService.getAllUtilisateurs().stream()
                    .filter(u -> "LIVREUR".equalsIgnoreCase(u.getTypeUtilisateur()) || 
                               "MAGASINIER".equalsIgnoreCase(u.getTypeUtilisateur()))
                    .toList()
            );
            employeCombo.setItems(eligibleUsers);
            
            // Configuration de l'affichage des utilisateurs dans le ComboBox
            employeCombo.setCellFactory(param -> new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getPrenom() + " " + user.getNom() + " (" + user.getTypeUtilisateur() + ")");
                    }
                }
            });
            employeCombo.setButtonCell(employeCombo.getCellFactory().call(null));

            DatePicker dateDebutPicker = new DatePicker();
            DatePicker dateFinPicker = new DatePicker();
            
            ComboBox<String> typeCongeCombo = new ComboBox<>();
            typeCongeCombo.getItems().addAll(
                "Congé annuel",
                "Congé maladie",
                "Congé exceptionnel",
                "Autre"
            );
            
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Description du congé");
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setWrapText(true);

            // Création de la grille de formulaire
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            grid.add(new Label("Employé:"), 0, 0);
            grid.add(employeCombo, 1, 0);
            grid.add(new Label("Date de début:"), 0, 1);
            grid.add(dateDebutPicker, 1, 1);
            grid.add(new Label("Date de fin:"), 0, 2);
            grid.add(dateFinPicker, 1, 2);
            grid.add(new Label("Type de congé:"), 0, 3);
            grid.add(typeCongeCombo, 1, 3);
            grid.add(new Label("Description:"), 0, 4);
            grid.add(descriptionArea, 1, 4);

            // Configuration des boutons
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Validation des champs
            Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            // Activation du bouton OK si tous les champs obligatoires sont remplis
            BooleanBinding isValid = Bindings.createBooleanBinding(
                () -> employeCombo.getValue() != null &&
                      dateDebutPicker.getValue() != null &&
                      dateFinPicker.getValue() != null &&
                      typeCongeCombo.getValue() != null &&
                      !descriptionArea.getText().trim().isEmpty(),
                employeCombo.valueProperty(),
                dateDebutPicker.valueProperty(),
                dateFinPicker.valueProperty(),
                typeCongeCombo.valueProperty(),
                descriptionArea.textProperty()
            );
            okButton.disableProperty().bind(isValid.not());

            dialog.getDialogPane().setContent(grid);

            // Configuration du résultat
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    try {
                        Utilisateur selectedUser = employeCombo.getValue();
                        congeService.demanderConge(
                            Long.valueOf(selectedUser.getIdUser()),
                            dateDebutPicker.getValue(),
                            dateFinPicker.getValue(),
                            typeCongeCombo.getValue(),
                            descriptionArea.getText().trim()
                        );
                        return null;
                    } catch (Exception e) {
                        showError("Erreur", e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            // Affichage du dialogue et mise à jour des données
            dialog.showAndWait();
            loadData(); // Rafraîchir les données après la demande

        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la création de la demande de congé: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupListeners() {
        // Listener pour le changement d'utilisateur
        performanceUserCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Utilisateur sélectionné: " + newVal.getPrenom() + " " + newVal.getNom());
                currentUserId = Long.valueOf(newVal.getIdUser());
                loadData();
            }
        });

        // Listeners pour les contrôles de filtrage
        monthComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadData());
        yearComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadData());
        pointageDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadData());
        filterByMonthRadio.selectedProperty().addListener((obs, oldVal, newVal) -> loadData());
    }
} 