package esp.model;

import common.reseau.udp.MulticastNetworkChatterDecoderPlayer;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConnectionScreenModel {
    private MulticastNetworkChatterDecoderPlayer networkChatter;
    private ObservableList<PartieInfo> games;
    private int maxPlayersFilter;

    public ConnectionScreenModel(int maxPlayersFilter) {
        this.maxPlayersFilter = maxPlayersFilter;
        this.games = FXCollections.observableArrayList();
        try {
            this.networkChatter = new MulticastNetworkChatterDecoderPlayer(games, maxPlayersFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startChattingNetwork() {
        if (networkChatter != null) {
            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    String msg = networkChatter.receiveMessage();
                    if (msg != null) {
                        networkChatter.DecoderPlayer(msg);
                    }
                }
            }).start();
        }
    }

    public void stop() {
        if (networkChatter != null) {
            networkChatter.close();
        }
    }

    public ObservableList<PartieInfo> getGames() {
        return games;
    }
}
