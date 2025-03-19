package esp;

import common.Config;
import common.ThemeManager;
import common.navigation.NavigationService;
import common.ui.BaseScreenView;
import common.ui.PopUpMenuView;
import common.ui.screens.HelpScreenView;
import common.ui.screens.OptionsScreenView;
import common.navigation.NavigationService;
import esp.view.GameScreenViewEsp;
import esp.viewmodel.EspConnectionScreenViewModel;
import common.ui.screens.StartingScreenView;
import esp.services.NavigationServiceESP;
import esp.view.EspConnectionScreenView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    protected Scene scene;
    private static Stage primaryStage;
    private StackPane root;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        root = new StackPane();
        scene = new Scene(root, 1280, 720);

        // appliquer le theme par défaut au démarrage (applyCurrentTheme)
        ThemeManager.applyCurrentTheme(scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Un mouton à la mer");
        
        
        NavigationService navigationService = new NavigationService(stage, root);
        NavigationServiceESP navigationServiceEsp = new NavigationServiceESP(stage, root);
        
        StartingScreenView startingView = new StartingScreenView(navigationService, null, navigationServiceEsp, true); // true = spy user
        HelpScreenView helpScreenView = new HelpScreenView(navigationService, true);
        OptionsScreenView optionsView = new OptionsScreenView(navigationService, null, navigationServiceEsp, true);
        EspConnectionScreenView connectionView = new EspConnectionScreenView(navigationServiceEsp);
        PopUpMenuView popUpMenuView = new PopUpMenuView(navigationService);
        GameScreenViewEsp gameScreenView = new GameScreenViewEsp(connectionView.getViewModel(),stage,navigationService);
        
        root.getChildren().addAll(
            startingView,
            helpScreenView,
            optionsView,
            connectionView,
            popUpMenuView,
            gameScreenView
        );
        
        navigationServiceEsp.initializeViews();
        navigationService.initializeViews();

        startingView.updateVisibility(true);
        helpScreenView.updateVisibility(false);
        optionsView.updateVisibility(false);
        connectionView.updateVisibility(false);
        popUpMenuView.updateVisibility(false);
        gameScreenView.setVisible(false);

        // Dans App.java
        primaryStage.setFullScreen(true); // Fullscreen
        if (!Config.IS_DEV_MODE) {
            primaryStage.setFullScreenExitHint(""); // Désactiver le message de notification
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Désactiver la sortie par Esc
        }

        primaryStage.show();
    }

    public void setViewVisible(BaseScreenView views) {
        // Cette méthode peut être utilisée pour gérer la visibilité des vues
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Platform.exit();
        System.exit(0);
    }

    public static void lancement(String[] args) {
        // Définir les propriétés pour le clavier virtuel et le tactile
        System.setProperty("com.sun.javafx.touch", "true");
        System.setProperty("com.sun.javafx.virtualKeyboard", "javafx");

        launch(args);
    }

}
