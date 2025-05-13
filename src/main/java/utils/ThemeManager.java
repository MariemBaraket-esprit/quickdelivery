package utils;

import javafx.scene.Scene;

public class ThemeManager {
    private static String currentTheme = "light";

    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeManager.class.getResource("/styles/styles.css").toExternalForm());
        if ("dark".equals(currentTheme)) {
            scene.getStylesheets().add(ThemeManager.class.getResource("/styles/dark-theme.css").toExternalForm());
        }
    }

    public static void setTheme(String theme) {
        currentTheme = theme;
    }

    public static String getTheme() {
        return currentTheme;
    }
} 