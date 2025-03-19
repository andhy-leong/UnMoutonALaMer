/**
 * Classe de vue pour l'écran de connexion ESP.
 * Gère l'affichage des parties disponibles et fournit des fonctionnalités de filtrage et de sélection.
 */
package esp.view;

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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
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
import esp.services.NavigationServiceESP;
import esp.viewmodel.EspConnectionScreenViewModel;
import esp.viewmodel.EspConnectionScreenViewModel.ViewState;

public class EspConnectionScreenView extends BaseScreenView implements ListChangeListener<PartieInfo> {
    private VBox centerVBox;
    private ScrollPane scrollPane;
    private VBox scrollPaneVBox;
    private Button homeButton;
    private CheckBox botPlayersCheckBox;
    private Label stateMessage;
    private HBox filterHBox;
    private Button joinButton;
    private Button refreshButton;
    private GridPane selectedGame;
    private EspConnectionScreenViewModel viewModel;
    private Label filterLabel;
    private Label filterMaxLabel;
    private Label realPlayersLabel;
    private Label botPlayersLabel;
    private ToggleGroup playerCountButtonBar;
    private HBox playerCountButtons;

    /**
     * Crée une nouvelle instance de EspConnectionScreenView.
     * @param navigationService Le service de navigation pour gérer les transitions d'écran
     */
    public EspConnectionScreenView(NavigationServiceESP navigationServiceEsp) {
        this.viewModel = new EspConnectionScreenViewModel(navigationServiceEsp);
        this.modelTopBar = 3;

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

    /**
     * Crée tous les composants d'interface utilisateur utilisés dans la vue.
     */
    private void createComponents() {
        centerVBox = new VBox(10);
        scrollPane = new ScrollPane();
        scrollPaneVBox = new VBox(10);
        homeButton = new Button();
        botPlayersCheckBox = new CheckBox();
        stateMessage = new Label();
        filterHBox = new HBox(10);
        joinButton = new Button();
        refreshButton = new Button();
        joinButton.setDisable(true);
        filterLabel = new Label();
        filterMaxLabel = new Label();
        realPlayersLabel = new Label();
        botPlayersLabel = new Label();

        // Création des boutons de sélection du nombre de joueurs
        playerCountButtonBar = new ToggleGroup();
        playerCountButtons = new HBox(0);

        ToggleButton threePlayersButton = new ToggleButton("3");
        ToggleButton fourPlayersButton = new ToggleButton("4");
        ToggleButton fivePlayersButton = new ToggleButton("5");

        threePlayersButton.setUserData(3);
        fourPlayersButton.setUserData(4);
        fivePlayersButton.setUserData(5);


        threePlayersButton.setToggleGroup(playerCountButtonBar);
        fourPlayersButton.setToggleGroup(playerCountButtonBar);
        fivePlayersButton.setToggleGroup(playerCountButtonBar);

        fivePlayersButton.setSelected(true);

        threePlayersButton.getStyleClass().add("bot-button");
        fourPlayersButton.getStyleClass().add("bot-button");
        fivePlayersButton.getStyleClass().add("bot-button");

        playerCountButtons.getChildren().addAll(threePlayersButton, fourPlayersButton, fivePlayersButton);
        playerCountButtons.setAlignment(Pos.CENTER);
    }

    /**
     * Configure les propriétés et les gestionnaires d'événements des composants d'interface utilisateur.
     */
    private void setupComponents() {
        centerVBox.setAlignment(Pos.TOP_CENTER);
        centerVBox.setPadding(new Insets(0));

        stateMessage.getStyleClass().add("state-message");
        stateMessage.setWrapText(true);
        stateMessage.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        refreshButton.getStyleClass().add("refresh-button");
        
        //separteur pour separer les filtres du bouton pour refresh la liste des parties
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setPadding(new Insets(0, 10, 0, 10));
        
        filterHBox.setAlignment(Pos.CENTER_LEFT);
        filterHBox.setPadding(new Insets(10, 20, 10, 20));
        filterHBox.getStyleClass().add("filter-hbox");

        filterHBox.getChildren().addAll(
            filterLabel, 
            botPlayersCheckBox,
            playerCountButtons,
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

        joinButton.getStyleClass().add("join-button");

        refreshButton.setOnMousePressed(event -> {
            refreshButton.setScaleX(0.9);
            refreshButton.setScaleY(0.9);
        });
        refreshButton.setOnMouseReleased(event -> {
            refreshButton.setScaleX(1.0);
            refreshButton.setScaleY(1.0);
        });
    }

    /**
     * Crée un GridPane pour afficher les informations d'une partie.
     * @param game Les informations de la partie à afficher
     * @return Un GridPane contenant les informations formatées de la partie
     */
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

        int currentPlayers = game.getNombreCurrentJoueurReel() + game.getNombreCurrentJoueurBot();
        Label playerCountLabel = new Label(currentPlayers + "/" + game.getNombreJoueurMax());
        HBox playerInfoHBox = new HBox(5, playerCountLabel);
        playerInfoHBox.setAlignment(Pos.TOP_CENTER);
        gridPane.add(playerInfoHBox, 1, 0);

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

    /**
     * Organise la disposition des composants d'interface utilisateur.
     */
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

    /**
     * Ajuste la disposition de l'interface utilisateur en fonction des changements de taille d'écran.
     */
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

    /**
     * Lie les propriétés de la vue au ViewModel.
     */
    @Override
    protected void bindToViewModel() {
        viewModel.currentStateProperty().addListener((obs, oldState, newState) ->
                Platform.runLater(() -> updateViewForCurrentState(newState)));
        I18N.localeProperty().addListener((obs, oldLocale, newLocale) ->
                Platform.runLater(() -> updateViewForCurrentState(viewModel.currentStateProperty().get())));
        menuButton.setOnAction(event -> viewModel.onMenuButtonClicked());
        homeButton.setOnAction(event -> viewModel.onHomeButtonClicked());
        joinButton.setOnAction(event -> viewModel.joinSelectedGame());

        stateMessage.textProperty().bind(viewModel.stateMessageProperty());
        menuButton.textProperty().bind(viewModel.menuButtonProperty());
        homeButton.textProperty().bind(viewModel.homeButtonProperty());
        joinButton.textProperty().bind(viewModel.joinButtonProperty());
        joinButton.disableProperty().bind(
            viewModel.spyButtonDisabledProperty()
            .or(Bindings.createBooleanBinding(
                () -> viewModel.currentStateProperty().get() != ViewState.GAME_SELECTED,
                viewModel.currentStateProperty()
            ))
        );
        titleLabel.textProperty().bind(viewModel.titleLabelProperty());
        botPlayersCheckBox.textProperty().bind(viewModel.botPlayersCheckBoxProperty());
        refreshButton.textProperty().bind(viewModel.refreshButtonProperty());
        refreshButton.disableProperty().bind(viewModel.refreshButtonDisabledProperty());
        filterLabel.textProperty().bind(viewModel.filterLabelProperty());
        botPlayersLabel.textProperty().bind(viewModel.botPlayersLabelProperty());
        filterMaxLabel.textProperty().bind(viewModel.filterMaxLabelProperty());
        realPlayersLabel.textProperty().bind(viewModel.realPlayersLabelProperty());

        // Bind des filtres
        botPlayersCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (Config.DEBUG_MODE) {
                System.out.println("Vue: Changement filtre bot -> " + newVal);
            }
            viewModel.onBotFilterChanged(newVal);
        });

        // Bind du nombre de joueurs
        playerCountButtonBar.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Retirer la classe selected-button de tous les boutons
                playerCountButtons.getChildren().forEach(node -> {
                    if (node instanceof ToggleButton) {
                        node.getStyleClass().remove("selected-button");
                    }
                });
                
                // Ajouter la classe selected-button au bouton sélectionné
                ((ToggleButton) newVal).getStyleClass().add("selected-button");
                
                int maxPlayers = (int) newVal.getUserData();
                if (Config.DEBUG_MODE) {
                    System.out.println("Vue: Changement filtre max joueurs -> " + maxPlayers);
                }
                viewModel.setMaxPlayersFilter(maxPlayers);
            }
        });

        // Ajouter la classe selected-button au bouton initialement sélectionné
        playerCountButtons.getChildren().forEach(node -> {
            if (node instanceof ToggleButton && ((ToggleButton) node).isSelected()) {
                node.getStyleClass().add("selected-button");
            }
        });

        viewModel.getFilteredGames().addListener(this);

        viewModel.maxPlayersFilterProperty().addListener((obs, oldVal, newVal) -> {
            if (Config.DEBUG_MODE) {
                System.out.println("Vue: Changement filtre max joueurs -> " + newVal);
            }
            updateGameList();
        });

        updateGameList();

        refreshButton.setOnAction(event -> { 
            if (Config.DEBUG_MODE) {
                System.out.println("Vue: Rafraîchissement demandé");
            }
            viewModel.refreshGameList();
            clearGameList();
        });
    }

    /**
     * Gère les changements dans la liste des parties filtrées.
     * @param change L'événement de changement contenant les informations sur les modifications de la liste
     */
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

    /**
     * Met à jour la liste des parties affichées.
     */
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

    /**
     * Vérifie si une partie est déjà affichée dans la liste.
     * @param game La partie à vérifier
     * @return true si la partie est déjà dans la liste, false sinon
     */
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

    /**
     * Met à jour l'affichage d'une partie existante dans la liste.
     * @param updated Les informations mises à jour de la partie
     */
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

    /**
     * Met à jour la vue en fonction de l'état actuel.
     * @param newState Le nouvel état pour mettre à jour la vue
     */
    private void updateViewForCurrentState(ViewState newState) {
        switch (newState) {
            case DISPLAY_GAMES:
                setFiltersEnabled(true);
                homeButton.setDisable(false);
                setAllGamesEnabled(true);
                deselectGame();
                break;
            case GAME_SELECTED:
                setFiltersEnabled(false);
                homeButton.setDisable(false);
                setAllGamesEnabled(false);
                highlightSelectedGame();
                break;
        }
    }

    /**
     * Gère la sélection d'une partie dans la liste.
     * @param gridPane Le GridPane contenant la partie sélectionnée
     */
    private void selectGame(GridPane gridPane) {
        PartieInfo game = (PartieInfo) gridPane.getUserData();
        
        // Retirer la sélection visuelle de l'ancienne partie sélectionnée
        if (selectedGame != null) {
            selectedGame.getStyleClass().remove("game-grid-pane-selected");
            selectedGame.getStyleClass().add("game-grid-pane");
        }

        // Mettre à jour la sélection visuelle
        if (gridPane != this.selectedGame || viewModel.currentStateProperty().get() != ViewState.GAME_SELECTED) {
            gridPane.getStyleClass().add("game-grid-pane-selected");
        }
        
        this.selectedGame = gridPane;
        viewModel.selectGame(game);
    }

    /**
     * Désélectionne la partie actuellement sélectionnée.
     */
    private void deselectGame() {
        if (selectedGame != null) {
            selectedGame.getStyleClass().clear();
            selectedGame.getStyleClass().add("game-grid-pane");
            selectedGame = null;
        }
    }

    /**
     * Active ou désactive toutes les parties dans la liste.
     * @param enabled true pour activer les parties, false pour les désactiver
     */
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

    /**
     * Met en surbrillance la partie actuellement sélectionnée.
     */
    private void highlightSelectedGame() {
        if (selectedGame != null) {
            selectedGame.setDisable(false);
            selectedGame.getStyleClass().clear();
            selectedGame.getStyleClass().add("game-grid-pane-highlighted");
        }
    }

    /**
     * Active ou désactive les contrôles de filtrage.
     * @param enabled true pour activer les filtres, false pour les désactiver
     */
    private void setFiltersEnabled(boolean enabled) {
        botPlayersCheckBox.setDisable(!enabled);
        playerCountButtons.setDisable(!enabled);
    }

    /**
     * Obtient le ViewModel associé à cette vue.
     * @return L'instance de EspConnectionScreenViewModel
     */
    public EspConnectionScreenViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Définit le ViewModel pour cette vue.
     * @param viewModel Le ViewModel à définir
     */
    public void setViewModel(EspConnectionScreenViewModel viewModel) {
        this.viewModel = viewModel;
        bindToViewModel();
    }

    /**
     * Efface toutes les parties de la liste d'affichage.
     */
    private void clearGameList() {
        scrollPaneVBox.getChildren().clear();
    }
}
