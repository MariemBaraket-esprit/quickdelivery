@FXML
public void initialize() {
    try {
        // Initialize fields first
        if (nomField == null || prenomField == null || emailField == null || 
            passwordField == null || typeUtilisateurCombo == null) {
            System.err.println("Warning: Some FXML fields failed to inject");
            return;
        }

        // Try to initialize service
        try {
            utilisateurService = new UtilisateurService();
        } catch (Exception e) {
            System.err.println("Service initialization failed: " + e.getMessage());
            // Continue anyway
        }

        // Setup UI components
        setupValidationListeners();
        setupTypeUtilisateurCombo();
    } catch (Exception e) {
        e.printStackTrace();
    }
} 