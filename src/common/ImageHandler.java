package common;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gère le chargement des images en mode développement et compilé (avec ant en jar)
 */
public class ImageHandler {
    private static final String DEV_RESOURCES_PATH = "ressources";
    private static final String COMPILED_IMAGES_PATH = "images";
    
    /**
     * Charge une image depuis un chemin
     * @param path Chemin de l'image (doit commencer par /common/, /idjr/, /ppti/, /esp/ ou /bots/)
     * @throws RuntimeException si l'image n'est pas trouvée
     */
    public static InputStream loadImage(String path) {
        return loadImage(path, null);
    }

    /**
     * Charge une image avec un texte de secours
     * @param path Chemin de l'image (doit commencer par /common/, /idjr/, /ppti/, /esp/ ou /bots/)
     * @param fallbackText Texte à utiliser si l'image n'est pas trouvée
     * @return null si l'image n'est pas trouvée et qu'un texte de secours est fourni
     */
    public static InputStream loadImage(String path, String fallbackText) {
        // Normalisation du chemin
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        
        // Mode développement - cherche dans le dossier ressources/
        if (Config.IS_DEV_MODE) {
            try {
                //Path devPath = Paths.get(Config.DEV_RESOURCES_PATH, cleanPath);
                Path devPath = Paths.get(Config.getImagePath(cleanPath));
                if (Files.exists(devPath)) {
                    return Files.newInputStream(devPath);
                }
            } catch (Exception e) {
                // Continue avec le mode compilé
            }
        }
        
        // Mode compilé - cherche d'abord dans le dossier images/
        try {
            Path imagesPath = Paths.get(COMPILED_IMAGES_PATH, cleanPath);
            if (Files.exists(imagesPath)) {
                return Files.newInputStream(imagesPath);
            }
        } catch (Exception e) {
            // Continue avec la recherche dans le classpath
        }
        
        // Mode compilé (jar) - cherche dans le classpath
        try {
            // Essaie d'abord avec le chemin complet
            InputStream is = ImageHandler.class.getResourceAsStream("/" + cleanPath);
            if (is != null) {
                return is;
            }
            
            // Si le chemin commence par un préfixe de programme (idjr/, ppti/, etc.), 
            // essaie aussi sans ce préfixe car dans le JAR les ressources sont à la racine
            String[] prefixes = {"idjr/", "ppti/", "esp/", "bots/", "common/"};
            for (String prefix : prefixes) {
                if (cleanPath.startsWith(prefix)) {
                    String withoutPrefix = cleanPath.substring(prefix.length());
                    is = ImageHandler.class.getResourceAsStream("/" + withoutPrefix);
                    if (is != null) {
                        return is;
                    }
                }
            }
        } catch (Exception e) {
            // Continue
        }
        
        if (fallbackText != null) {
            return null;
        }

        if(Config.DEBUG_MODE)
            throw new RuntimeException("Impossible de charger l'image : " + path + " (mode=" + (Config.IS_DEV_MODE ? "dev" : "compiled") + ")");
        return null;
    }

    /**
     * Vérifie si une image existe
     * @param path Chemin de l'image (doit commencer par /common/, /idjr/, /ppti/, /esp/ ou /bots/)
     */
    public static boolean imageExists(String path) {
        try {
            loadImage(path);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
