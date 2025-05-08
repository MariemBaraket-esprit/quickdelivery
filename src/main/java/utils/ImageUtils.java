package utils;

import javafx.scene.image.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ImageUtils {

    // Base directory for storing product images
    private static final String IMAGE_DIRECTORY = "src/main/resources/images/products/";

    // Default image to use when no image is available
    private static final String DEFAULT_IMAGE = "/images/default-product.png";

    /**
     * Saves an image file to the product images directory
     *
     * @param sourceFile The source image file
     * @param productId The ID of the product
     * @return The path where the image was saved
     * @throws IOException If an I/O error occurs
     */
    public static String saveProductImage(File sourceFile, int productId) throws IOException {
        // Create directory if it doesn't exist
        File directory = new File(IMAGE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Get file extension
        String fileName = sourceFile.getName();
        String extension = fileName.substring(fileName.lastIndexOf("."));

        // Create destination file path
        String destinationFileName = "product_" + productId + extension;
        Path destinationPath = Paths.get(IMAGE_DIRECTORY + destinationFileName);

        // Copy file
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Return the relative path to be stored in the database
        return "/images/products/" + destinationFileName;
    }

    /**
     * Loads an image from the given path
     *
     * @param imagePath The path to the image
     * @return The loaded image, or a default image if the path is null or the image cannot be loaded
     */
    public static Image loadProductImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) {
                return new Image(DEFAULT_IMAGE);
            }

            // Try to load from the path
            return new Image(imagePath);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            // Return default image if there's an error
            return new Image(DEFAULT_IMAGE);
        }
    }

    /**
     * Deletes a product image
     *
     * @param imagePath The path to the image
     * @return true if the image was deleted successfully, false otherwise
     */
    public static boolean deleteProductImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || imagePath.equals(DEFAULT_IMAGE)) {
            return false;
        }

        try {
            // Extract the filename from the path
            String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            Path filePath = Paths.get(IMAGE_DIRECTORY + fileName);

            // Delete the file
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting image: " + e.getMessage());
            return false;
        }
    }
}
