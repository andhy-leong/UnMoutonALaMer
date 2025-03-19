package players.idjr;

import javafx.application.Application;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import common.ShutdownManager;

public class PreLaunch extends Application {
    private static IDJRApplication app;
    
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ShutdownManager.getInstance().shutdown();
        }));

        // Définir les propriétés pour le clavier virtuel et le tactile
        System.setProperty("com.sun.javafx.touch", "true");
        System.setProperty("com.sun.javafx.virtualKeyboard", "javafx");

        Application.launch(PreLaunch.class, args);
    }

    @Override
    public void start(Stage stage) {
        app = new IDJRApplication();
        app.start(stage);
    }

    public static StackPane getRoot() {
        return app.getRoot();
    }

    public static Stage getPrimaryStage() {
        return app.getPrimaryStage();
    }
}
