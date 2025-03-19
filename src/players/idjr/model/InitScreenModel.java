package players.idjr.model;

import java.util.ArrayList;
import java.util.List;

public class InitScreenModel {
    private List<String> players;

    public InitScreenModel() {
        this.players = new ArrayList<>();
    }

    public void setPlayers(String[] playersList) {
        this.players.clear();
        for (String player : playersList) {
            this.players.add(player);
        }
    }

    public List<String> getPlayers() {
        return new ArrayList<>(players);
    }

    public int getPlayerCount() {
        return players.size();
    }
}
