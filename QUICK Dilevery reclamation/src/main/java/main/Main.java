package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class Main extends Application {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Get the URL of the FXML file from the classpath
            URL loginFxml = getClass().getResource("/fxml/Login.fxml");
            if (loginFxml == null) {
                throw new Exception("Cannot find /fxml/Login.fxml");
            }

            // Create the FXMLLoader with explicit URL
            FXMLLoader loader = new FXMLLoader(loginFxml);

            // Load the FXML
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root);

            // Set the window properties
            primaryStage.setTitle("Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Show the window
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}