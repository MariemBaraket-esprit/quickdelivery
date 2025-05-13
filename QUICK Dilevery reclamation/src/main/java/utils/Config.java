package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static Properties properties;

    static {
        properties = new Properties();
        boolean loaded = false;

        // Try multiple possible locations for config.properties
        String[] possiblePaths = {
                "config.properties",                      // Project root
                "./config.properties",                    // Current directory
                "../config.properties",                   // Parent directory
                "src/main/resources/config.properties",   // Maven resources
                System.getProperty("user.dir") + "/config.properties" // Absolute path
        };

        for (String path : possiblePaths) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    System.out.println("Found config file at: " + file.getAbsolutePath());
                    FileInputStream input = new FileInputStream(file);
                    properties.load(input);
                    input.close();
                    loaded = true;
                    break;
                }
            } catch (IOException e) {
                // Continue to next path
            }
        }

        if (!loaded) {
            System.out.println("Could not load config.properties file. Using environment variables.");
        }
    }

    public static String getOpenAIApiKey() {
        // First try to get from environment variable
        String apiKey = System.getenv("OPENAI_API_KEY");

        // If not found, try to get from properties file
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = properties.getProperty("openai.api.key");
        }

        // For debugging - don't include this in production code
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("API key not found in environment variables or config.properties");
        } else {
            System.out.println("API key found with length: " + apiKey.length());
        }

        return apiKey;
    }
}