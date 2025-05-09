package com.rached;

import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting application...");
            
            // Load FXML
            System.out.println("Loading FXML file...");
            URL loginFxml = getClass().getResource("/fxml/Login.fxml");
            if (loginFxml == null) {
                throw new Exception("Cannot find /fxml/Login.fxml");
            }
            System.out.println("FXML file found at: " + loginFxml);
            
            FXMLLoader loader = new FXMLLoader(loginFxml);
            Parent root = loader.load();
            
            // Load CSS
            System.out.println("Loading CSS file...");
            URL cssUrl = getClass().getResource("/styles/styles.css");
            if (cssUrl == null) {
                throw new Exception("Cannot find /styles/styles.css");
            }
            System.out.println("CSS file found at: " + cssUrl);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(cssUrl.toExternalForm());
            
            // Configure stage
            System.out.println("Configuring primary stage...");
            primaryStage.setTitle("Rached - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            
            // Show the stage
            System.out.println("Showing primary stage...");
            primaryStage.show();
            System.out.println("Application started successfully!");
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            
            showError("Erreur de démarrage", 
                     "Une erreur est survenue lors du démarrage de l'application:\n" + 
                     e.getMessage());
            
            Platform.exit();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 