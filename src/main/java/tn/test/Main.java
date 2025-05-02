package tn.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.utils.MyDataBase;
import tn.utils.MyDataBase;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Charger la vue principale qui contient le menu de navigation
            // Notez le chemin corrigé pour correspondre à l'emplacement réel des fichiers
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
            Parent root = loader.load();

            // Configurer la fenêtre principale
            primaryStage.setTitle("Système de Gestion des Produits et Commandes");
            primaryStage.setScene(new Scene(root, 1000, 700));
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            System.out.println("Application démarrée avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Fermer la connexion à la base de données lors de la fermeture de l'application
        MyDataBase.closeConnection();
        System.out.println("Application arrêtée, connexion à la base de données fermée");
    }

    public static void main(String[] args) {
        launch(args);
    }
}