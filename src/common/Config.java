package common;

public class Config {
    /* Mettre true pour le mode développement, false pour la production */

    // Development mode
    public static final boolean IS_DEV_MODE = false;
    // Debug mode
    public static boolean DEBUG_MODE = false;

    // Base path for resources
    public static final String DEV_RESOURCES_PATH = "ressources";
    public static final String PROD_RESOURCES_PATH = "/";

    // Génération des chemins
    public static final String CSS_BASE_PATH = IS_DEV_MODE ? "/common/css/" : PROD_RESOURCES_PATH + "css/";
    public static final String IMAGE_BASE_PATH = IS_DEV_MODE ? DEV_RESOURCES_PATH + "/" : PROD_RESOURCES_PATH;

    static {
        // Si on est en mode release, on désactive le mode debug automatiquement
        if (!IS_DEV_MODE) {
            DEBUG_MODE = false;
        }
    }

    // recupère le chemin d'accès aux fichiers css
    public static String getCssPath(String cssFileName) {
        return CSS_BASE_PATH + cssFileName;
    }

    // recupère le chemin d'accès aux images
    public static String getImagePath(String imagePath) {
        return IMAGE_BASE_PATH + imagePath;
    }
}