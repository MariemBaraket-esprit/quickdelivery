package services;

import models.Utilisateur;
import java.sql.Connection;
import java.sql.SQLException;

public class UtilisateurService {
    private Connection connection;
    
    public UtilisateurService() throws SQLException {
        this.connection = DataBaseConnection.getConnection();
    }
    
    public Utilisateur login(String email, String password) {
        // For testing, accept any login
        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            Utilisateur user = new Utilisateur();
            user.setIdUser(1);
            user.setEmail(email);
            user.setNom("Test");
            user.setPrenom("User");
            user.setTypeUtilisateur("ADMIN");
            return user;
        }
        return null;
    }
    
    public boolean register(Utilisateur user) {
        // Always return true for testing
        return true;
    }
} 