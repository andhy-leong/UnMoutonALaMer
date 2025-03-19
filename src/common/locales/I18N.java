package common.locales;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import common.Config;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class I18N {

    private static final ObjectProperty<Locale> myLocale;
    private static final ArrayList<Locale> supportedLocales = new ArrayList<Locale>();
    private static final String FOLDER_PATH = "langues";
    private static Path path;
    private static final String nameFile = "locale";

    static {
        if(Config.IS_DEV_MODE) {
            path = Paths.get("ressources\\common\\langues");
            generateSupportedLocales(path);
        }else {
            generateSupportedLocales(null);
        }

        myLocale = new SimpleObjectProperty<>(getDefaultLocale());
        myLocale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
    }

    public static ArrayList<Locale> getSupportedLocales(boolean regenerate) {
        if (regenerate) {
            supportedLocales.clear();
            if(Config.IS_DEV_MODE)
                generateSupportedLocales(path);
            else
                generateSupportedLocales(null);
        }
        return supportedLocales;
    }

    private static void generateSupportedLocales(Path p) {
        if(Config.IS_DEV_MODE) {
            try {
                Stream<String> filesNames = Files.list(p).map(path ->
                        path.getFileName().toString()).filter(s->s.endsWith("properties")).filter(s->s.startsWith(nameFile));

                filesNames.forEach(n->{
                    String codes[] = n.substring(0, n.length() - 11).split("_");
                    if (codes.length == 3)
                        supportedLocales.add(new Locale.Builder().setLanguage(codes[1]).setRegion(codes[2]).build());
                    else if (codes.length == 2)
                        supportedLocales.add(new Locale.Builder().setLanguage(codes[1]).build());
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            try {
                String jarPath = I18N.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
                        .getPath();

                try (JarFile jar = new JarFile(jarPath)) {
                    Enumeration<JarEntry> entries = jar.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        // Vérifie si le fichier est dans le bon dossier et est un fichier properties
                        if (name.startsWith(FOLDER_PATH) &&
                                name.endsWith("properties") &&
                                name.contains(nameFile)) {

                            // Extrait juste le nom du fichier
                            String fileName = name.substring(name.lastIndexOf('/') + 1);

                            // Traitement similaire à l'original pour extraire les codes de langue
                            String[] codes = fileName.substring(0, fileName.length() - 11).split("_");
                            if (codes.length == 3) {
                                supportedLocales.add(new Locale.Builder()
                                        .setLanguage(codes[1])
                                        .setRegion(codes[2])
                                        .build());
                            } else if (codes.length == 2) {
                                supportedLocales.add(new Locale.Builder()
                                        .setLanguage(codes[1])
                                        .build());
                            }
                        }
                    }
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Locale getDefaultLocale() {
        Locale sysDefault = Locale.getDefault();
        return supportedLocales.contains(sysDefault) ? sysDefault : supportedLocales.get(0);
    }

    public static Locale getLocale() {
        return myLocale.get();
    }

    public static ObjectProperty<Locale> getLocaleProperty() {
        return myLocale;
    }

    public static void setLocale(Locale locale) {
        localeProperty().set(locale);
        Locale.setDefault(locale);
    }

    public static ObjectProperty<Locale> localeProperty() {
        return myLocale;
    }

    public static String get(final String key, final Object... args) {
        ResourceBundle bundle = null;
        if(Config.IS_DEV_MODE)
            bundle = ResourceBundle.getBundle("common.langues.locale", getLocale());
        else
            bundle = ResourceBundle.getBundle("langues/locale", getLocale());
        return MessageFormat.format(bundle.getString(key), args);
    }

    public static StringBinding createStringBinding(final String key, Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), myLocale);
    }

    public static StringBinding createStringBinding(Callable<String> func) {
        return Bindings.createStringBinding(func, myLocale);
    }
    
    public static boolean containsKey(final String key) {
        try {
            ResourceBundle bundle;
            if (Config.IS_DEV_MODE) {
                bundle = ResourceBundle.getBundle("common.langues.locale", getLocale());
            } else {
                bundle = ResourceBundle.getBundle("langues/locale", getLocale());
            }
            return bundle.containsKey(key);
        } catch (MissingResourceException e) {
            return false;
        }
    }

}