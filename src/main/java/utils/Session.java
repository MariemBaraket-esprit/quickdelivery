package utils;

import models.Utilisateur;

public class Session {
    private static Session instance;
    private Utilisateur currentUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    public Utilisateur getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        currentUser = null;
    }
} 