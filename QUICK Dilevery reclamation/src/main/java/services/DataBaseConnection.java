package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/quick_dilevery"; // Changed to match your DB name
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static Connection connection;

    public static Connection getConnection() {
        try {
            // Load MySQL driver (important for Java 6+ compatibility)
            Class.forName("com.mysql.cj.jdbc.Driver");

            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to the database", e);
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}