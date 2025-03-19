package players.idjr.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

public class ParametersScreenModel {
    private StringProperty playerName = new SimpleStringProperty("");
    private IntegerProperty maxPlayers = new SimpleIntegerProperty(3);

    public StringProperty playerNameProperty() {
        return playerName;
    }

    public IntegerProperty maxPlayersProperty() {
        return maxPlayers;
    }
}
