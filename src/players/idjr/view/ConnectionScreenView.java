package players.idjr.view;

import java.util.ArrayList;
import java.util.List;

import common.locales.I18N;
import common.Config;
import common.reseau.udp.inforecup.PartieInfo;
import common.ui.BaseScreenView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import players.idjr.model.ConnectionScreenModel;
import players.idjr.services.NavigationServiceIDJR;
import players.idjr.viewmodel.ConnectionScreenViewModel;
import players.idjr.viewmodel.ConnectionScreenViewModel.ViewState;

public class ConnectionScreenView extends BaseScreenView implements ListChangeListener<PartieInfo> {
    private VBox centerVBox;
    private ScrollPane scrollPane;
    private VBox scrollPaneVBox;
    private Button homeButton;
    private CheckBox espionCheckBox;
    private CheckBox botPlayersCheckBox;
    private Label stateMessage;
    private HBox filterHBox;
    private Button joinButton;
    private Button refreshButton;
    private GridPane selectedGame;
    private ConnectionScreenViewModel viewModel;
    private Label filterLabel;
    private Label filterMaxLabel;
    private Label spyStatusLabel;
    private Label realPlayersLabel;
    private Label botPlayersLabel;

    public ConnectionScreenView(NavigationServiceIDJR navigationService) {
        this.viewModel = new ConnectionScreenViewModel(new ConnectionScreenModel(0), navigationService);
        this.modelTopBar = 4;

        super.init();
    }

    @Override
    protected void customizeScreen() {
        createComponents();
        setupComponents();
        organizeComponents();

        this.widthProperty().addListener((obs, oldVal, newVal) -> adjustForScreenSize());
        this.heightProperty().addListener((obs, oldVal, newVal) -> adjustForScreenSize());

        addContainerRoot(centerVBox, homeButton);
    }

    private void createComponents() {
        centerVBox = new VBox(10);
        scrollPane = new ScrollPane();
        scrollPaneVBox = new VBox(10);
        homeButton = new Button();
        espionCheckBox = new CheckBox();
        botPlayersCheckBox = new CheckBox();
        stateMessage = new Label();
        filterHBox = new HBox(10);
        joinButton = new Button();
        refreshButton = new Button();
        joinButton.setDisable(true);
        filterLabel = new Label();
        filterMaxLabel = new Label();
        spyStatusLabel = new Label();
        realPlayersLabel = new Label();
        botPlayersLabel = new Label();
    }

    private void setupComponents() {
        centerVBox.setAlignment(Pos.TOP_CENTER);
        centerVBox.setPadding(new Insets(0));

        stateMessage.getStyleClass().add("state-message");
        stateMessage.setWrapText(true);
        stateMessage.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        refreshButton.getStyleClass().add("refresh-button");
        // refreshButton.setOnMouseEntered(e -> refreshButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50; -fx-font-size: 14px;"));
        // refreshButton.setOnMouseExited(e -> refreshButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; -fx-font-size: 14px;"));
        
        //separteur pour separer les filtres du bouton pour refresh la liste des parties
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setPadding(new Insets(0, 10, 0, 10));
        
        filterHBox.setAlignment(Pos.CENTER_LEFT);
        filterHBox.setPadding(new Insets(10, 20, 10, 20));
        filterHBox.getStyleClass().add("filter-hbox");

        filterHBox.getChildren().addAll(
            filterLabel, 
            espionCheckBox, 
            botPlayersCheckBox,
            separator,
            refreshButton
        );

        scrollPane.setContent(scrollPaneVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().addAll("container","idjr-scroll-pane");
        scrollPaneVBox.getStyleClass().add("idjr-scroll-pane-vbox");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        scrollPaneVBox.setSpacing(10);

        homeButton.setAlignment(Pos.BOTTOM_LEFT);
        homeButton.getStyleClass().add("home-button");

        titleLabel.getStyleClass().add("title-idjr");

        // espionCheckBox.getStyleClass().add("espion-check-box");
        // botPlayersCheckBox.getStyleClass().add("bot-players-check-box");

        joinButton.getStyleClass().add("join-button");
    }

    private GridPane createGameGridPane(PartieInfo game) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(10));
        gridPane.setPrefHeight(80);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        col2.setPercentWidth(33.33);
        col3.setPercentWidth(33.33);
        gridPane.getColumnConstraints().addAll(col1, col2, col3);

        Label gameNameLabel = new Label(game.getNomPartie());
        gameNameLabel.getStyleClass().add("game-name-label");

        GridPane.setValignment(gameNameLabel, VPos.TOP);
        GridPane.setHalignment(gameNameLabel, HPos.LEFT);
        gridPane.add(gameNameLabel, 0, 0);

        ImageView playerIcon = new ImageView(/* image icone joueur */);
        int currentPlayers = game.getNombreCurrentJoueurReel() + game.getNombreCurrentJoueurBot();
        Label playerCountLabel = new Label(currentPlayers + "/" + game.getNombreJoueurMax());
        HBox playerInfoHBox = new HBox(5, playerIcon, playerCountLabel);
        playerInfoHBox.setAlignment(Pos.TOP_CENTER);
        gridPane.add(playerInfoHBox, 1, 0);

        spyStatusLabel = new Label();
        spyStatusLabel.textProperty().bind(Bindings.createStringBinding(
            () -> game.getEspionAutorise() == 1 ? viewModel.authorizedSpyLabelProperty().get() : viewModel.refusedSpyLabelProperty().get(),
            I18N.localeProperty()
        ));
        GridPane.setValignment(spyStatusLabel, VPos.BOTTOM);
        GridPane.setHalignment(spyStatusLabel, HPos.CENTER);
        gridPane.add(spyStatusLabel, 1, 1);

        realPlayersLabel = new Label();
        realPlayersLabel.textProperty().bind(Bindings.createStringBinding(
            () -> viewModel.realPlayersLabelProperty().get() + " " + game.getNombreCurrentJoueurReel() + "/" + game.getNombreJoueurReelMax(),
            I18N.localeProperty()
        ));
        GridPane.setValignment(realPlayersLabel, VPos.TOP);
        GridPane.setHalignment(realPlayersLabel, HPos.CENTER);
        gridPane.add(realPlayersLabel, 2, 0);

        botPlayersLabel = new Label();
        botPlayersLabel.textProperty().bind(Bindings.createStringBinding(
            () -> viewModel.joueurVirtuelLabelProperty().get() + " " + game.getNombreCurrentJoueurBot() + "/" + game.getNombreJoueurVirtuelMax(),
            I18N.localeProperty()
        ));
        GridPane.setValignment(botPlayersLabel, VPos.BOTTOM);
        GridPane.setHalignment(botPlayersLabel, HPos.CENTER);
        gridPane.add(botPlayersLabel, 2, 1);

        gridPane.getStyleClass().add("game-grid-pane");

        gridPane.setUserData(game);
        gridPane.setOnMouseClicked(event -> selectGame(gridPane));

        RowConstraints statusRow = new RowConstraints();
        statusRow.setVgrow(Priority.ALWAYS);
        gridPane.getRowConstraints().add(statusRow);

        return gridPane;
    }

    private void organizeComponents() {
        centerVBox.prefWidthProperty().bind(this.widthProperty());
        centerVBox.setSpacing(20);
        centerVBox.setPadding(new Insets(0));

        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
        
        topSpacer.setPrefHeight(0.5);    
        bottomSpacer.setPrefHeight(0.5);  

        centerVBox.getChildren().addAll(
            topSpacer,
            stateMessage,
            filterHBox,
            scrollPane,
            joinButton,
            bottomSpacer
        );

        VBox.setMargin(joinButton, new Insets(20, 0, 20, 0));
        BorderPane.setAlignment(homeButton, Pos.BOTTOM_LEFT);
        BorderPane.setMargin(homeButton, new Insets(10));
    }

    private void adjustForScreenSize() {
        double screenHeight = this.getHeight();
        double screenWidth = this.getWidth();
        
        double verticalSpacing = screenHeight * 0.02;    // 2% de la hauteur
        centerVBox.setSpacing(verticalSpacing);
        
        double topMargin = screenHeight * 0.05;    // 5% de la hauteur
        centerVBox.setPadding(new Insets(topMargin, screenWidth * 0.1, screenHeight * 0.05, screenWidth * 0.1));
        
        double scrollPaneHeight = screenHeight * 0.5;    // 50% de la hauteur de l'écran
        scrollPane.setPrefViewportHeight(scrollPaneHeight);
        
        double buttonWidth = Math.min(screenWidth * 0.3, 400);    // 30% de la largeur, max 400px
        joinButton.setPrefWidth(buttonWidth);
    }

    @Override
    protected void bindToViewModel() {
        viewModel.currentStateProperty().addListener((obs, oldState, newState) ->
                updateViewForCurrentState(newState));
        I18N.localeProperty().addListener((obs, oldLocale, newLocale) ->
                updateViewForCurrentState(viewModel.currentStateProperty().get()));
        menuButton.setOnAction(event -> viewModel.onMenuButtonClicked());
        homeButton.setOnAction(event -> viewModel.onHomeButtonClicked());
        joinButton.setOnAction(event -> viewModel.joinSelectedGame());

        stateMessage.textProperty().bind(viewModel.stateMessageProperty());
        menuButton.textProperty().bind(viewModel.menuButtonProperty());
        homeButton.textProperty().bind(viewModel.homeButtonProperty());
        joinButton.textProperty().bind(viewModel.joinButtonProperty());
        titleLabel.textProperty().bind(viewModel.titleLabelProperty());
        espionCheckBox.textProperty().bind(viewModel.espionCheckBoxProperty());
        botPlayersCheckBox.textProperty().bind(viewModel.botPlayersCheckBoxProperty());
        refreshButton.textProperty().bind(viewModel.refreshButtonProperty());
        filterLabel.textProperty().bind(viewModel.filterLabelProperty());
        botPlayersLabel.textProperty().bind(viewModel.botPlayersLabelProperty());
        filterMaxLabel.textProperty().bind(viewModel.filterMaxLabelProperty());
        realPlayersLabel.textProperty().bind(viewModel.realPlayersLabelProperty());


        // Bind des filtres
        espionCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (Config.DEBUG_MODE) {
                System.out.println("Vue: Changement filtre espion -> " + newVal);
            }
            viewModel.onSpyFilterChanged(newVal);
        });

        botPlayersCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (Config.DEBUG_MODE) {
                System.out.println("Vue: Changement filtre bot -> " + newVal);
            }
            viewModel.onBotFilterChanged(newVal);
        });

        viewModel.getFilteredGames().addListener(this);

        viewModel.maxPlayersFilterProperty().addListener((obs, oldVal, newVal) -> {

            if (Config.DEBUG_MODE) {
                System.out.println("Vue: Changement filtre max joueurs -> " + newVal);
            }

            updateGameList();
        });

        setPlayerName(viewModel.playerNameProperty().get());

        updateGameList();

        refreshButton.setOnAction(event -> { 
            if (Config.DEBUG_MODE) {
                System.out.println("Vue: Rafraîchissement demandé");
            }
            viewModel.refreshGameList();
            clearGameList();
        });
    }

    @Override
    public void onChanged(ListChangeListener.Change<? extends PartieInfo> change) {
        if (Config.DEBUG_MODE) {
            System.out.println("Vue: Changement détecté dans la liste des parties");
        }
        Platform.runLater(() -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasReplaced()) {
                    if (Config.DEBUG_MODE) {
                        System.out.println("Vue: Mise à jour de l'affichage - " + 
                            (change.wasAdded() ? "Ajout" : change.wasRemoved() ? "Suppression" : "Remplacement"));
                    }
                    updateGameList();
                }
            }
        });
    }

    private void updateGameList() {
        if (Config.DEBUG_MODE) {
            System.out.println("Vue: Mise à jour de la liste des parties");
            System.out.println("Nombre de parties filtrées: " + viewModel.getFilteredGames().size());
        }

        Platform.runLater(() -> {
            List<PartieInfo> currentGames = new ArrayList<>(viewModel.getFilteredGames());
            List<Node> toRemove = new ArrayList<>();
            for (Node node : scrollPaneVBox.getChildren()) {
                if (node instanceof GridPane) {
                    PartieInfo existingGame = (PartieInfo) node.getUserData();
                    if (!currentGames.contains(existingGame)) {
                        toRemove.add(node);
                    }
                }
            }

            if (!toRemove.isEmpty()) {
                scrollPaneVBox.getChildren().removeAll(toRemove);
                if (Config.DEBUG_MODE) {
                    System.out.println("Vue: " + toRemove.size() + " parties supprimées");
                }
            }

            for (PartieInfo game : currentGames) {
                if (!isGameInList(game)) {
                    GridPane gameEntry = createGameGridPane(game);
                    
                    // Configurer l'animation d'apparition
                    gameEntry.setOpacity(0);
                    gameEntry.setTranslateY(20);
                    scrollPaneVBox.getChildren().add(gameEntry);
                    
                    // Lancer l'animation
                    javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), gameEntry);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    
                    javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(300), gameEntry);
                    slideIn.setFromY(20);
                    slideIn.setToY(0);
                    
                    javafx.animation.ParallelTransition transition = new javafx.animation.ParallelTransition(fadeIn, slideIn);
                    transition.play();
                    
                    if (Config.DEBUG_MODE) {
                        System.out.println("Vue: Ajout de la partie " + game.getNomPartie());
                    }
                }
            }

            if (Config.DEBUG_MODE) {
                System.out.println("Vue: Nombre final de parties affichées: " + scrollPaneVBox.getChildren().size());
            }
        });
    }

    private boolean isGameInList(PartieInfo game) {
        for (Node node : scrollPaneVBox.getChildren()) {
            if (node instanceof GridPane) {
                PartieInfo existingGame = (PartieInfo) node.getUserData();
                if (existingGame.equals(game)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateExistingGame(PartieInfo updated) {
        Platform.runLater(() -> {
            for (Node node : scrollPaneVBox.getChildren()) {
                if (node instanceof GridPane) {
                    PartieInfo existingGame = (PartieInfo) node.getUserData();
                    if (existingGame.equals(updated)) {
                        int index = scrollPaneVBox.getChildren().indexOf(node);
                        GridPane gameEntry = createGameGridPane(updated);
                        scrollPaneVBox.getChildren().set(index, gameEntry);
                        if (Config.DEBUG_MODE) {
                            System.out.println("Vue: Mise à jour de la partie " + updated.getNomPartie());
                        }
                        break;
                    }
                }
            }
        });
    }

    private void updateViewForCurrentState(ViewState newState) {
        switch (newState) {
            case DISPLAY_GAMES:
                setFiltersEnabled(true);
                enableJoinGameButtons(false);
                homeButton.setDisable(false);
                setAllGamesEnabled(true);
                break;
            case GAME_SELECTED:
                setFiltersEnabled(false);
                enableJoinGameButtons(true);
                homeButton.setDisable(false);
                setAllGamesEnabled(false);
                highlightSelectedGame();
                break;
            case WAITING_AUTHORIZATION:
                setFiltersEnabled(false);
                enableJoinGameButtons(false);
                homeButton.setDisable(true);
                setAllGamesEnabled(false);
                highlightSelectedGame();
                updateSelectedGameStatus(this.viewModel.stateWaitingAuthorizationProperty().get(), 1);
                break;
            case ACCEPTED:
                setFiltersEnabled(false);
                enableJoinGameButtons(false);
                homeButton.setDisable(true);
                setAllGamesEnabled(false);
                highlightSelectedGame();
                updateSelectedGameStatus(this.viewModel.stateAcceptedProperty().get(), 2);
                break;
            case REFUSED:
                setFiltersEnabled(true);
                enableJoinGameButtons(false);
                homeButton.setDisable(false);
                setAllGamesEnabled(true);
                updateSelectedGameStatus(this.viewModel.stateRefusedProperty().get(), 0);
                break;
        }
    }

    private void enableJoinGameButtons(boolean enable) {
        joinButton.setDisable(!enable);
    }

    private void selectGame(GridPane gridPane) {
        PartieInfo game = (PartieInfo) gridPane.getUserData();
        ViewState currentState = viewModel.currentStateProperty().get();

        if (gridPane == this.selectedGame && currentState == ViewState.GAME_SELECTED) {
            deselectGame();
        } else if (currentState != ViewState.WAITING_AUTHORIZATION && currentState != ViewState.ACCEPTED) {
            if (selectedGame != null) {
                selectedGame.getStyleClass().remove("game-grid-pane-selected");
                selectedGame.getStyleClass().add("game-grid-pane");
            }

            gridPane.getStyleClass().add("game-grid-pane-selected");
            this.selectedGame = gridPane;
            viewModel.selectGame(game);
        }
    }

    private void deselectGame() {
        if (selectedGame != null) {
            selectedGame.getStyleClass().clear();
            selectedGame.getStyleClass().add("game-grid-pane");
            selectedGame = null;
            viewModel.setViewState(ViewState.DISPLAY_GAMES);
        }
    }

    private void setAllGamesEnabled(boolean enabled) {
        for (Node node : scrollPaneVBox.getChildren()) {
            if (node instanceof GridPane) {
                node.setDisable(!enabled);
                if (enabled) {
                    node.getStyleClass().remove("game-disabled");
                } else {
                    node.getStyleClass().add("game-disabled");
                }
            }
        }
    }

    private void highlightSelectedGame() {
        if (selectedGame != null) {
            selectedGame.setDisable(false);
            selectedGame.getStyleClass().clear();
            selectedGame.getStyleClass().add("game-grid-pane-highlighted");
        }
    }

    private void updateSelectedGameStatus(String status, int statusCode) {
        if (selectedGame != null) {
            Label statusLabel = (Label) selectedGame.lookup("#statusLabel");
            if (statusLabel == null) {
                statusLabel = new Label(status);
                statusLabel.setId("statusLabel");
                selectedGame.add(statusLabel, 0, 2, 3, 1);
            } else {
                statusLabel.setText(status);
            }

            if (statusCode == 2) {
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-accepted");
            } else if (statusCode == 0) {
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-refused");
            } else {
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("status-pending");
            }
        }
    }

    private void setFiltersEnabled(boolean enabled) {
        espionCheckBox.setDisable(!enabled);
        botPlayersCheckBox.setDisable(!enabled);
    }

    public ConnectionScreenViewModel getViewModel() {
        return viewModel;
    }

    public void setViewModel(ConnectionScreenViewModel viewModel) {
        this.viewModel = viewModel;
        bindToViewModel();
    }

    private void clearGameList() {
        scrollPaneVBox.getChildren().clear();
    }
}
