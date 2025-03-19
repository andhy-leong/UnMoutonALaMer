package players.idjr.services;

import common.Config;
import common.navigation.NavigationService;
import common.ui.BaseScreenView;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import players.idjr.model.ConnectionScreenModel;
import players.idjr.model.EndGameScreenModel;
import players.idjr.model.GameScreenModel;
import players.idjr.model.InitScreenModel;
import players.idjr.model.JoinedGameModel;
import common.ShutdownManager;
import players.idjr.view.ConnectionScreenView;
import players.idjr.view.EndGameScreenView;
import players.idjr.view.GameScreenView;
import players.idjr.view.InitScreenView;
import players.idjr.view.ParametersScreenView;
import players.idjr.viewmodel.ConnectionScreenViewModel;
import players.idjr.viewmodel.EndGameScreenViewModel;
import players.idjr.viewmodel.GameScreenViewModel;
import players.idjr.viewmodel.InitScreenViewModel;

public class NavigationServiceIDJR extends NavigationService {
    private ParametersScreenView parametersScreenView;
    private ConnectionScreenView connectionScreenView;
    private InitScreenView initScreenView;
    private GameScreenView gameScreenView;
    private EndGameScreenView endGameScreenView;

    public NavigationServiceIDJR(Stage stage, StackPane root) {
        super(stage, root);
    }

    @Override
    public void initializeViews() {
        super.initializeViews();
        parametersScreenView = findView(ParametersScreenView.class);
        connectionScreenView = findView(ConnectionScreenView.class);
        initScreenView = findView(InitScreenView.class);
        gameScreenView = findView(GameScreenView.class);
        endGameScreenView = findView(EndGameScreenView.class);
    }

    @Override
    protected <T> T findView(Class<T> viewClass) {
    	
        // Pour les vues spécifiques à l'un des deux types, retourner null si on ne les trouve pas
        if (viewClass == ParametersScreenView.class ||
            viewClass == ConnectionScreenView.class ||
            viewClass == InitScreenView.class ||
            viewClass == GameScreenView.class ||
            viewClass == EndGameScreenView.class) {
            return root.getChildren().stream()
                .filter(node -> viewClass.isInstance(node))
                .map(node -> (T) node)
                .findFirst()
                .orElse(null);
        }
        // Pour les autres vues (communes), utiliser la méthode parent
        return super.findView(viewClass);
    }

    public void navigateToParametersScreen() {
        if (parametersScreenView != null) {
            navigateTo(parametersScreenView);
        }
    }

    public void navigateToInitScreen() {
        if (initScreenView != null && connectionScreenView != null) {
            String playerName = connectionScreenView.getViewModel().playerNameProperty().get();
            
            // Récupérer le JoinedGameModel depuis le ConnectionScreenViewModel
            JoinedGameModel joinedGameModel = connectionScreenView.getViewModel().getJoinedGameModel();
            
            InitScreenViewModel viewModel = new InitScreenViewModel(new InitScreenModel(), this, joinedGameModel);
            viewModel.setPlayerName(playerName);
            initScreenView.setViewModel(viewModel);
            navigateTo(initScreenView);
        }
    }

    public void navigateToConnectionScreen(String playerName, int maxPlayersFilter) {
        if (connectionScreenView != null) {
            ConnectionScreenViewModel viewModel = new ConnectionScreenViewModel(
                new ConnectionScreenModel(maxPlayersFilter), 
                this
            );
            viewModel.setMaxPlayersFilter(maxPlayersFilter);
            viewModel.setPlayerName(playerName);
            connectionScreenView.setViewModel(viewModel);
            viewModel.startChattingNetwork();
            navigateTo(connectionScreenView);
        } 
    }

    public void navigateToGameScreen() {
        if (gameScreenView != null && initScreenView != null) {
            JoinedGameModel joinedGameModel = initScreenView.getViewModel().getJoinedGameModel();
            
            // Créer le ViewModel avec le JoinedGameModel
            GameScreenViewModel viewModel = new GameScreenViewModel(new GameScreenModel(), this, joinedGameModel);
            gameScreenView.setViewModel(viewModel);
            
            joinedGameModel.setNavigationService(this);
            
            navigateTo(gameScreenView);
        }
    }

    public void navigateToEndGameScreen() {
        if (endGameScreenView != null && gameScreenView != null) {
            JoinedGameModel joinedGameModel = gameScreenView.getViewModel().getJoinedGameModel();
            
            // Créer le ViewModel avec les données finales
            EndGameScreenViewModel viewModel = new EndGameScreenViewModel(new EndGameScreenModel(), this);
            
            // Mettre à jour les données du ViewModel
            viewModel.setGameData(
                joinedGameModel.getPlayerName(),
                joinedGameModel.nomPartieProperty().get(),
                joinedGameModel.scoreProperty().get(),
                String.valueOf(joinedGameModel.getTotalLostBuoys()),
                joinedGameModel.getPlayerRank()
            );
            
            endGameScreenView.setViewModel(viewModel);
            navigateTo(endGameScreenView);
        }
    }
    
    @Override
    protected void navigateTo(BaseScreenView view) {
        hideAllScreens();
        if (currentView != null && currentView != startingScreenView) {
            super.navigationHistory.push(currentView);
            if (Config.DEBUG_MODE) {
                System.out.println(">>> Navigation: Ajout à l'historique - " + currentView.getClass().getSimpleName());
                System.out.println(">>> État de l'historique: " + getHistoryState());
            }
        }
        currentView = view;
        if (Config.DEBUG_MODE) {
            System.out.println(">>> Navigation: Vue courante changée pour - " + view.getClass().getSimpleName());
        }
        view.updateVisibility(true);
    }

    public void shutdownApplication() {
        ShutdownManager.getInstance().shutdown();
        Platform.exit();
        System.exit(0);
    }
} 
