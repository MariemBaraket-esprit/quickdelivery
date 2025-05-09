package utils;

import javafx.scene.image.Image;
import java.io.InputStream;

public class ImageLoader {
    
    public static Image loadImage(String path) {
        try {
            // Essayer de charger l'image depuis les ressources
            InputStream imageStream = ImageLoader.class.getResourceAsStream(path);
            if (imageStream != null) {
                return new Image(imageStream);
            }
            
            // Si l'image n'est pas trouvée dans les ressources, essayer avec l'URL directe
            return new Image(path);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image " + path + ": " + e.getMessage());
            // Retourner une image par défaut ou null
            return null;
        }
    }
} 