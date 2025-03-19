package players.idjr.viewmodel;

import common.Config;
import common.locales.I18N;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import players.idjr.model.InitScreenModel;
import players.idjr.model.JoinedGameModel;
import players.idjr.services.NavigationServiceIDJR;

public class InitScreenViewModel {

    private InitScreenModel model;
    private NavigationServiceIDJR navigationService;
    private JoinedGameModel joinedGameModel;
    private StringProperty playerName = new SimpleStringProperty("");
    
    private StringBinding titleLabelProperty;
    private StringBinding stateMessageLabelProperty;
    private StringBinding playerCountLabelProperty;
    private StringBinding menuButtonProperty;

    private ListProperty<String> players = new SimpleListProperty<>(FXCollections.observableArrayList());
    private IntegerProperty playerCount = new SimpleIntegerProperty(0);

    public InitScreenViewModel(InitScreenModel initScreenModel, NavigationServiceIDJR navigationService, JoinedGameModel joinedGameModel) {
        this.model = initScreenModel;
        this.navigationService = navigationService;
        
        titleLabelProperty = I18N.createStringBinding("titleLabelInitScreenIDJR");
        stateMessageLabelProperty = I18N.createStringBinding("stateMessageLabelInitScreenIDJR");
        playerCountLabelProperty = I18N.createStringBinding("playerCountLabelInitScreenIDJR");
        menuButtonProperty = I18N.createStringBinding("menuButtonInitScreenIDJR");

        this.joinedGameModel = joinedGameModel;

        // Regarder les changements d'état du JoinedGameModel
        if (joinedGameModel != null) {
            // lien avec la liste des joueurs
            joinedGameModel.playersProperty().addListener((obs, oldList, newList) -> {
                if (newList != null) {
                    if (Config.DEBUG_MODE) {
                        System.out.println("InitScreenViewModel - Nouvelle liste de joueurs reçue: " + newList);
                    }
                    Platform.runLater(() -> {
                        players.clear();
                        players.addAll(newList);
                        playerCount.set(newList.size());
                        if (Config.DEBUG_MODE) {
                            System.out.println("InitScreenViewModel - Liste des joueurs mise à jour: " + players);
                            System.out.println("InitScreenViewModel - Nombre de joueurs mis à jour: " + playerCount.get());
                        }
                    });
                }
            });

            // Initialiser avec les joueurs actuels s'il y en a
            if (!joinedGameModel.playersProperty().isEmpty()) {
                if (Config.DEBUG_MODE) {
                    System.out.println("InitScreenViewModel - Initialisation avec les joueurs existants: " + joinedGameModel.playersProperty());
                }
                players.addAll(joinedGameModel.playersProperty());
                playerCount.set(joinedGameModel.playersProperty().size());
            }

            joinedGameModel.currentStateProperty().addListener((obs, oldState, newState) -> {
                if (newState == JoinedGameModel.ViewState.GAME_STARTED) {
                    Platform.runLater(() -> {
                        navigationService.navigateToGameScreen();
                    });
                }
            });
        }
    }

    public void onMenuButtonClicked() {
        navigationService.showPopUpMenu();
    }

    public StringProperty playerNameProperty() {
        return playerName;
    }

    public void setPlayerName(String name) {
        playerName.set(name);
    }

    public ListProperty<String> playersProperty() {
        return players;
    }

    public IntegerProperty playerCountProperty() {
        return playerCount;
    }

    public void updatePlayers(String listej) {
        if (Config.DEBUG_MODE) {
            System.out.println("InitScreenViewModel - updatePlayers appelé avec: " + listej);
        }
        String[] playersList = listej.split(",");
        model.setPlayers(playersList);
        Platform.runLater(() -> {
            players.clear();
            players.addAll(model.getPlayers());
            playerCount.set(model.getPlayerCount());
            if (Config.DEBUG_MODE) {
                System.out.println("InitScreenViewModel - updatePlayers terminé, nouvelle liste: " + players);
            }
        });
    }

    public JoinedGameModel getJoinedGameModel() {
        return joinedGameModel;
    }


    // internationalisation

    public StringBinding titleLabelProperty() {
    	return titleLabelProperty;
    }

    public StringBinding stateMessageLabelProperty() {
    	return stateMessageLabelProperty;
    }

    public StringBinding playerCountLabelProperty() {
    	return playerCountLabelProperty;
    }

    public StringBinding menuButtonProperty() {
    	return menuButtonProperty;
    }
}
