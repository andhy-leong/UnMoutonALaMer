package players.idjr.viewmodel;

import common.Config;

import common.locales.I18N;
import players.idjr.model.ConnectionScreenModel;
import players.idjr.model.JoinedGameModel;
import players.idjr.services.NavigationServiceIDJR;
import common.enumtype.GameType;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class ConnectionScreenViewModel {
    private ConnectionScreenModel model;
    private NavigationServiceIDJR navigationService;
    private StringProperty stateMessage = new SimpleStringProperty();
    private ObjectProperty<ViewState> currentState = new SimpleObjectProperty<>(ViewState.DISPLAY_GAMES);
    private BooleanProperty spyFilter = new SimpleBooleanProperty(false);
    private BooleanProperty noBotFilter = new SimpleBooleanProperty(false);
    private IntegerProperty maxPlayersFilter = new SimpleIntegerProperty(5);
    private StringProperty playerName = new SimpleStringProperty("");
    private JoinedGameModel joinedGameModel;
    private static int nbBotFilterCalled = 0;

    private StringBinding titleLabelProperty;
    private StringBinding homeButtonProperty;
    private StringBinding espionCheckBoxProperty;
    private StringBinding botPlayersCheckBoxProperty;
    private StringBinding stateMessageProperty;
    private StringBinding joinButtonProperty;
    private StringBinding refreshButtonProperty;
    private StringBinding filterLabelProperty;
    private StringBinding realPlayersLabelProperty;
    private StringBinding botPlayersLabelProperty;
    private StringBinding filterMaxLabelProperty;
    private StringBinding menuButtonProperty;
    private StringBinding authorizedSpyLabelProperty;
    private StringBinding refusedSpyLabelProperty;
    private StringBinding joueurVirtuelLabelProperty;

    // String de l'état de la connexion pour chaque partie (pas le titre de la page)
    private StringBinding stateWaitingAuthorizationProperty;
    private StringBinding stateAcceptedProperty;
    private StringBinding stateRefusedProperty;

    public enum ViewState {
        DISPLAY_GAMES,
        GAME_SELECTED,
        WAITING_AUTHORIZATION,
        ACCEPTED,
        REFUSED
    }

    public ConnectionScreenViewModel(ConnectionScreenModel model, NavigationServiceIDJR navigationService) {
        this.model = model;
        this.navigationService = navigationService;


        I18N.localeProperty().addListener((obs, oldLocale, newLocale) -> {
            updateViewForCurrentState();
        });

        labelToBind();
        updateStateMessage("stateMessage.displayGamesIDJR");
    }

    private void labelToBind() {
        titleLabelProperty = I18N.createStringBinding("titleLabelConnectionScreenIDJR");
        homeButtonProperty = I18N.createStringBinding("homeButtonConnectionScreenIDJR");
        espionCheckBoxProperty = I18N.createStringBinding("espionCheckBoxConnectionScreenIDJR");
        botPlayersCheckBoxProperty = I18N.createStringBinding("botPlayersCheckBoxConnectionScreenIDJR");
        stateMessageProperty = I18N.createStringBinding("stateMessage.displayGamesIDJR");
        joinButtonProperty = I18N.createStringBinding("joinButtonConnectionScreenIDJR");
        refreshButtonProperty = I18N.createStringBinding("refreshButtonConnectionScreenIDJR");
        filterLabelProperty = I18N.createStringBinding("filterLabelConnectionScreenIDJR");
        realPlayersLabelProperty = I18N.createStringBinding("realPlayersLabelConnectionScreenIDJR");
        botPlayersLabelProperty = I18N.createStringBinding("botPlayersLabelConnectionScreenIDJR");
        filterMaxLabelProperty = I18N.createStringBinding("filterMaxLabelConnectionScreenIDJR");
        menuButtonProperty = I18N.createStringBinding("menuButtonConnectionScreenIDJR");
        authorizedSpyLabelProperty = I18N.createStringBinding("spyAuthorizedLabelConnectionScreenIDJR");
        refusedSpyLabelProperty = I18N.createStringBinding("spyRefusedLabelConnectionScreenIDJR");
        joueurVirtuelLabelProperty = I18N.createStringBinding("joueurVirtuelLabelConnectionScreenIDJR");

        stateWaitingAuthorizationProperty = I18N.createStringBinding("stateMessage.waitingAuthorization");
        stateAcceptedProperty = I18N.createStringBinding("stateMessage.acceptedShortLabel");
        stateRefusedProperty = I18N.createStringBinding("stateMessage.refusedShortLabel");
    }

    public StringProperty stateMessageProperty() {
        return stateMessage;
    }

    public ObjectProperty<ViewState> currentStateProperty() {
        return currentState;
    }

    public void onMenuButtonClicked() {
        navigationService.showPopUpMenu();
    }

    public void onHomeButtonClicked() {
        model.stop();
        navigationService.navigateToStartingScreen();
    }

    public void updateStateMessage(String message) {
        stateMessage.set(I18N.get(message));
    }

    public void setViewState(ViewState newState) {
        currentState.set(newState);
        updateViewForCurrentState();
    }

    public void startChattingNetwork() {
        model.startChattingNetwork();
    }

    private void updateViewForCurrentState() {
        labelToBind();
        switch (currentState.get()) {
            case DISPLAY_GAMES:
            case GAME_SELECTED:
            case REFUSED:
                updateStateMessage("stateMessage.displayGamesIDJR");
                break;
            case WAITING_AUTHORIZATION:
                updateStateMessage("stateMessage.waitingAuthorization");
                break;
            case ACCEPTED:
                updateStateMessage("stateMessage.accepted");
                break;
        }
    }

    public void joinSelectedGame() {
        PartieInfo selectedGame = model.getSelectedGame();
        if (selectedGame != null) {
            setViewState(ViewState.WAITING_AUTHORIZATION);
            joinedGameModel = new JoinedGameModel(selectedGame, playerName.get());
            joinedGameModel.currentStateProperty().addListener((obs, oldState, newState) -> {
                Platform.runLater(() -> {
                    switch (newState) {
                        case ACCEPTED:
                            setViewState(ViewState.ACCEPTED);
                            break;
                        case REFUSED:
                            setViewState(ViewState.REFUSED);
                            break;
                        case INITIALIZED:
                            navigationService.navigateToInitScreen();
                            break;
                    }
                });
            });
            joinedGameModel.sendAcceptationDemand();
        }
    }

    public ObservableList<PartieInfo> getFilteredGames() {
        return model.getGames();
    }

    public BooleanProperty spyFilterProperty() {
        return spyFilter;
    }

    public BooleanProperty noBotFilterProperty() {
        return noBotFilter;
    }

    public IntegerProperty maxPlayersFilterProperty() {
        return maxPlayersFilter;
    }

    public StringProperty playerNameProperty() {
        return playerName;
    }

    public void setPlayerName(String name) {
        playerName.set(name);
    }

    public void createOrUpdateGame(String id, String ip, int port, String nomPartie, int nombreJoueurMax, int nombreJoueurReelMax, int nombreJoueurVirtuelMax, int espionAutorise, String status) {
        try {
            model.createGame(id, ip, port, nomPartie, nombreJoueurMax, nombreJoueurReelMax, nombreJoueurVirtuelMax, espionAutorise, status);
        } catch (IllegalArgumentException e) {
            updateStateMessage("Erreur : " + e.getMessage());
        }
    }

    public void setMaxPlayersFilter(int maxPlayers) {
        if (Config.DEBUG_MODE) {
            System.out.println("FILTRE MAX JOUEURS CHANGé: " + maxPlayers);
        }
        maxPlayersFilter.set(maxPlayers);
        model.setMaxPlayersFilter(maxPlayers);
    }

    public void selectGame(PartieInfo game) {
        model.setSelectedGame(game);
        setViewState(ViewState.GAME_SELECTED);
    }

    public void onBotFilterChanged(boolean noBotFilter) {
        ConnectionScreenViewModel.nbBotFilterCalled+=1;
        if(ConnectionScreenViewModel.nbBotFilterCalled%2 == 0) {
            this.noBotFilter.set(noBotFilter);
            model.setNoBotFilter(noBotFilter);
            GameType gameType = noBotFilter ? GameType.JR : GameType.MIX;
            if (Config.DEBUG_MODE) {
                System.out.println("FILTRE BOT CHANGé: " + noBotFilter);
            }
            model.clearGames();
            model.researchGame(gameType);
        }
    }

    public void onSpyFilterChanged(boolean spyFilter) {
        if (Config.DEBUG_MODE) {
            System.out.println("FILTRE ESPION CHANGé: " + spyFilter);
        }
        this.spyFilter.set(spyFilter);
        model.setSpyFilter(spyFilter);
    }

    public void refreshGameList() {
        if (Config.DEBUG_MODE) {
            System.out.println("Rafraîchissement de la liste des parties");
        }
        model.clearGames();
        model.researchGame(noBotFilter.get() ? GameType.JR : GameType.MIX);
    }

    public JoinedGameModel getJoinedGameModel() {
        return joinedGameModel;
    }


    /* Intenationalisation */

    public StringBinding titleLabelProperty() {
        return titleLabelProperty;
    }

    public StringBinding homeButtonProperty() {
    	return homeButtonProperty;
    }

    public StringBinding espionCheckBoxProperty() {
    	return espionCheckBoxProperty;
    }

    public StringBinding botPlayersCheckBoxProperty() {
    	return botPlayersCheckBoxProperty;
    }

    public StringBinding joinButtonProperty() {
    	return joinButtonProperty;
    }

    public StringBinding refreshButtonProperty() {
    	return refreshButtonProperty;
    }

    public StringBinding filterLabelProperty() {
    	return filterLabelProperty;
    }

    public StringBinding realPlayersLabelProperty() {
    	return realPlayersLabelProperty;
    }

    public StringBinding botPlayersLabelProperty() {
    	return botPlayersLabelProperty;
    }

    public StringBinding filterMaxLabelProperty() {
    	return filterMaxLabelProperty;
    }

    public StringBinding menuButtonProperty() {
    	return menuButtonProperty;
    }

    public StringBinding authorizedSpyLabelProperty() {
    	return authorizedSpyLabelProperty;
    }

    public StringBinding refusedSpyLabelProperty() {
    	return refusedSpyLabelProperty;
    }

    public StringBinding joueurVirtuelLabelProperty() {
    	return joueurVirtuelLabelProperty;
    }


    public StringBinding stateWaitingAuthorizationProperty() {
    	return stateWaitingAuthorizationProperty;
    }

    public StringBinding stateAcceptedProperty() {
    	return stateAcceptedProperty;
    }

    public StringBinding stateRefusedProperty() {
    	return stateRefusedProperty;
    }
}
