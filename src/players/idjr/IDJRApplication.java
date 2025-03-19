package players.idjr;

import common.Config;
import common.ui.PopUpMenuView;
import common.ui.screens.HelpScreenView;
import common.ui.screens.OptionsScreenView;
import common.ui.screens.StartingScreenView;
import common.navigation.NavigationService;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import players.idjr.services.NavigationServiceIDJR;
import players.idjr.view.*;

import static javafx.application.Application.launch;

public class IDJRApplication {
    private StackPane root;
    private Stage primaryStage;
    
    public void start(Stage stage) {
        primaryStage = stage;
        root = new StackPane();
        Scene scene = new Scene(root, 1280, 720);
        
        stage.setScene(scene);
        stage.setTitle("Un mouton à la mer");
        
        
        NavigationService navigationService = new NavigationService(stage, root);
        NavigationServiceIDJR navigationServiceIdjr = new NavigationServiceIDJR(stage, root);
        
        StartingScreenView startingView = new StartingScreenView(navigationService, navigationServiceIdjr, null, false); // false = not spy user
        OptionsScreenView optionsView = new OptionsScreenView(navigationService, navigationServiceIdjr, null, false);
        HelpScreenView helpView = new HelpScreenView(navigationService, false);
        ParametersScreenView parametersView = new ParametersScreenView(navigationServiceIdjr);
        ConnectionScreenView connectionView = new ConnectionScreenView(navigationServiceIdjr);
        InitScreenView initView = new InitScreenView(navigationServiceIdjr);
        GameScreenView gameView = new GameScreenView(navigationServiceIdjr);
        EndGameScreenView endGameView = new EndGameScreenView(navigationServiceIdjr);

        PopUpMenuView popUpMenuView = new PopUpMenuView(navigationService);
        
        root.getChildren().addAll(
            startingView,
            optionsView, 
            helpView,   
            parametersView,
            connectionView,
            initView,
            gameView,
            endGameView,
            popUpMenuView
        );
        
        navigationService.initializeViews();
        navigationServiceIdjr.initializeViews();

        startingView.updateVisibility(true);
        optionsView.updateVisibility(false);
        helpView.updateVisibility(false);
        parametersView.updateVisibility(false);
        connectionView.updateVisibility(false);
        initView.updateVisibility(false);
        gameView.updateVisibility(false);
        endGameView.updateVisibility(false);
        popUpMenuView.updateVisibility(false);

        stage.setFullScreen(true); // Fullscreen
        if (!Config.IS_DEV_MODE) {
            stage.setFullScreenExitHint(""); // Désactiver le message de notification
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Désactiver la sortie par Esc
        }

        stage.show();
    }

    public StackPane getRoot() {
        return root;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
} 