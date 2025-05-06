package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/gestion_commande";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection;

    private MyDataBase() {
        // Constructeur privé pour empêcher l'instanciation
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la base de données établie avec succès");
            } catch (ClassNotFoundException e) {
                System.err.println("Driver MySQL introuvable: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Connexion à la base de données fermée");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
}