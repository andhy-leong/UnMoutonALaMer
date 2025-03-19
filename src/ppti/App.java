package ppti;

import common.Config;
import common.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ppti.view.singleton.SingletonView;
import ppti.view.BaseView;

import java.util.Map;

public class App extends Application{

    protected Scene scene;
    private static Stage primaryStage;
    private StackPane root;
    
    private SingletonView singletonView;

    
	@Override
	public void start(Stage stage) throws Exception {

		primaryStage = stage;

		root = SingletonView.getInstance(stage);
        
        scene = new Scene(root, 1280, 720);

		// appliquer le theme par défaut au démarrage (applyCurrentTheme)
		ThemeManager.applyCurrentTheme(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Un mouton à la mer");

		// Dans App.java
		primaryStage.setFullScreen(true); // Fullscreen
		if (!Config.IS_DEV_MODE) {
			primaryStage.setFullScreenExitHint(""); // Désactiver le message de notification
			primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Désactiver la sortie par Esc
		}

        primaryStage.show();
	}
	
	@Override
	public void stop() throws Exception {
		Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();

		// Afficher les informations des threads actifs
		if(Config.DEBUG_MODE)
			System.out.println("Threads actifs :");
		for (Thread thread : allThreads.keySet()) {
			if (thread.getName().endsWith("Equipe3a")) {
				if(Config.DEBUG_MODE) {
					System.out.println("- Nom du thread : " + thread.getName());
					System.out.println("  État : " + thread.getState());
					System.out.println("  Est un daemon : " + thread.isDaemon());
				}
				thread.interrupt();
			}
		}
		// deconnxion de tout les joueurs plus simple grâce à ça
		((SingletonView)this.root).reinit();

		Platform.exit();
		System.exit(0);
	}
	
	public void setViewVisible(BaseView views) {
		
	}

	public static void lancement(String[] args) {
		// Définir les propriétés pour le clavier virtuel et le tactile
        System.setProperty("com.sun.javafx.touch", "true");
        System.setProperty("com.sun.javafx.virtualKeyboard", "javafx");

		launch(args);
	}

}
