package common;

import javafx.scene.Scene;

import java.util.Objects;

public class ThemeManager {
    private static String currentTheme = Config.getCssPath("light-theme1.css");

    private static void applyTheme(Scene scene, String theme) {
        if (scene == null) {
            if (Config.DEBUG_MODE) {
                System.err.println("Error applying theme: Scene is null");
            }
            return;
        }

        try {
            String stylesheet = Objects.requireNonNull(ThemeManager.class.getResource(theme)).toExternalForm();

            scene.getStylesheets().clear();
            scene.getStylesheets().add(stylesheet);
            currentTheme = theme;

            if (Config.DEBUG_MODE) {
                System.out.println("Theme applied: " + theme + " for " + scene.getRoot());
            }
        } catch (NullPointerException e) {
            if (Config.DEBUG_MODE) {
                System.err.println("Error: Theme file not found: " + theme + " for " + scene.getRoot() + " (Mode dev: " + Config.IS_DEV_MODE + ")");
            }
        } catch (Exception e) {
            if (Config.DEBUG_MODE) {
                System.err.println("Error: Theme could not be applied: " + theme + " for " + scene.getRoot());
            }
        }
    }

    public static void applyCurrentTheme(Scene scene) {
        applyTheme(scene, currentTheme);
    }

    public static void setTheme(Scene scene, int themeNumber, boolean isDarkTheme) {
        String theme = switch (themeNumber) {
            case 1 -> isDarkTheme ? Config.getCssPath("dark-theme1.css") : Config.getCssPath("light-theme1.css");
            case 2 -> isDarkTheme ? Config.getCssPath("dark-theme2.css") : Config.getCssPath("light-theme2.css");
            case 3 -> isDarkTheme ? Config.getCssPath("dark-theme3.css") : Config.getCssPath("light-theme3.css");
            default -> currentTheme;
        };
        applyTheme(scene, theme);
    }
}