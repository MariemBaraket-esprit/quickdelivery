package tn.test;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Utilisez un chemin absolu depuis la racine des ressources
            URL location = getClass().getResource("/views/main-view.fxml");

            if (location == null) {
                // Pour déboguer si le fichier n'est pas trouvé
                System.err.println("FXML file not found!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Gestion des Produits");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}