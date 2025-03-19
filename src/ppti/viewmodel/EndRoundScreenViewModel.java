package ppti.viewmodel;

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import ppti.model.EndRoundScreenModel;
import ppti.view.singleton.SingletonView;

import java.util.HashMap;

public class EndRoundScreenViewModel {
    private EndRoundScreenModel model;
    private SingletonView singleton;
    private Boolean isEndGame;

    private StringProperty titleProperty;
    private StringProperty scoreLabelProperty;
    private StringProperty newRoundButtonProperty;
    private StringProperty replayButtonProperty;
    private StringProperty endGameButtonProperty;

    private MapProperty<String, Integer> playerScores;

    public EndRoundScreenViewModel(EndRoundScreenModel model, SingletonView singleton, Boolean isEndGame) {
        this.model = model;
        this.singleton = singleton;
        this.isEndGame = isEndGame;

        // --- provisoire ---
        // en attendant l'internationalisation
        scoreLabelProperty = new SimpleStringProperty("Score");
        if (isEndGame) {
            titleProperty = new SimpleStringProperty("Partie terminée");
            replayButtonProperty = new SimpleStringProperty("Rejouer");
            endGameButtonProperty = new SimpleStringProperty("Quitter");
        } else {
            titleProperty = new SimpleStringProperty("Manche x/y terminée");
            newRoundButtonProperty = new SimpleStringProperty("Nouvelle manche");
        }

        // --- provisoire ---
        ObservableMap<String, Integer> map = FXCollections.observableMap(new HashMap<>());
        playerScores = new SimpleMapProperty<>(map);
        playerScores.put("Player1", 10);
        playerScores.put("Player2", 20);
        playerScores.put("Player3", 30);
        playerScores.put("Player4", 40);
        playerScores.put("Player5", 50);
    }

    public Boolean isEndGame() {
        return isEndGame;
    }

    public MapProperty<String, Integer> playerScoresProperty() {
        return playerScores;
    }

    public void onNewRoundButtonClicked() {
        // TODO
    }

    public void onReplayButtonClicked() {
        // TODO
    }

    public void onEndGameButtonClicked() {
        // TODO
    }

    // getters properties

    public StringProperty titleProperty() {
        return titleProperty;
    }

    public StringProperty scoreLabelProperty() {
        return scoreLabelProperty;
    }

    public StringProperty newRoundButtonProperty() {
        return newRoundButtonProperty;
    }

    public StringProperty replayButtonProperty() {
        return replayButtonProperty;
    }

    public StringProperty endGameButtonProperty() {
        return endGameButtonProperty;
    }
}
