package players.idjr.model;

import java.util.ArrayList;
import java.util.List;

import common.Config;
import common.enumtype.GameType;
import common.enumtype.PlayerType;
import common.reseau.udp.MulticastNetworkChatterDecoderPlayer;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConnectionScreenModel {
    private ObservableList<PartieInfo> games;
    private ObservableList<PartieInfo> filteredGames;
    private MulticastNetworkChatterDecoderPlayer chatter;
    private Thread tReceiveMessage;
    private PlayerType type;
    private GameType partieType;
    private int nbJoueurMax;
    private ObjectProperty<PartieInfo> selectedGame = new SimpleObjectProperty<>();
    private boolean spyFilter = false;
    private boolean noBotFilter = false;
    private int maxPlayersFilter = 5;

    public ConnectionScreenModel(int nbJoueurMax) {
        type = PlayerType.JR;
        games = FXCollections.observableArrayList();
        filteredGames = FXCollections.observableArrayList();
        this.nbJoueurMax = nbJoueurMax;
        this.maxPlayersFilter = nbJoueurMax;
        
        try {
            chatter = new MulticastNetworkChatterDecoderPlayer(games, nbJoueurMax);
        } catch(Exception e) {
            //System.err.println(this.getClass().getSimpleName() + ": impossible de lancer le chatter ");
        }
        
        tReceiveMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    String msg = chatter.receiveMessage();
                    if (Config.DEBUG_MODE) {
                        System.out.println("Message reçu tReceiveMesage " + msg);
                    }
                    chatter.DecoderPlayer(msg);
                }
            }
        });

        // Observer les changements dans la liste des parties
        games.addListener((javafx.collections.ListChangeListener.Change<? extends PartieInfo> c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                    updateFilteredGames();
                }
            }
        });
    }
    
    public void startChattingNetwork() {
        this.researchGame(GameType.MIX);
        tReceiveMessage.start();
    }

    public ObservableList<PartieInfo> getGames() {
        return filteredGames;
    }

    private void updateFilteredGames() {
        Platform.runLater(() -> {
            List<PartieInfo> newFilteredGames = new ArrayList<>();
            if (Config.DEBUG_MODE) {
                System.out.println("Mise à jour des filtres - Nombre de parties total: " + games.size());
                System.out.println("Filtres actuels - Espion: " + spyFilter + ", NoBot: " + noBotFilter + ", MaxPlayers: " + maxPlayersFilter);
            }
            
            for (PartieInfo game : games) {
                if (isGameValid(game)) {
                    newFilteredGames.add(game);
                } else {
                    if (Config.DEBUG_MODE) {
                        System.out.println("Partie filtrée: " + game.getNomPartie() + " - Raison: " + getFilterReason(game));
                    }
                }
            }
            
            if (Config.DEBUG_MODE) {
                System.out.println("Nombre de parties après filtrage: " + newFilteredGames.size());
            }
            filteredGames.setAll(newFilteredGames);
        });
    }

    private String getFilterReason(PartieInfo game) {
        if (!"ATTENTE".equals(game.getStatus())) {
            return "Statut non en attente";
        }
        if (game.getNombreJoueurMax() > maxPlayersFilter) {
            return "Trop de joueurs max (" + game.getNombreJoueurMax() + " > " + maxPlayersFilter + ")";
        }
        if (spyFilter && game.getEspionAutorise() != 1) {
            return "Espions non autorisés";
        }
        if (noBotFilter && game.getNombreJoueurVirtuelMax() > 0) {
            return "Bots non désirés";
        }
        int currentPlayers = game.getNombreCurrentJoueurReel() + game.getNombreCurrentJoueurBot();
        if (currentPlayers >= game.getNombreJoueurMax()) {
            return "Partie pleine";
        }
        if (game.getNombreCurrentJoueurReel() >= game.getNombreJoueurReelMax()) {
            return "Plus de place pour joueur réel";
        }
        return "Valide";
    }

    private boolean isGameValid(PartieInfo game) {
        // Vérifier si la partie est en attente de joueurs
        if (!"ATTENTE".equals(game.getStatus())) {
            return false;
        }

        // Vérifier le nombre maximum de joueurs
        if (game.getNombreJoueurMax() > maxPlayersFilter) {
            return false;
        }

        // Vérifier le filtre d'espions
        if (spyFilter && game.getEspionAutorise() != 1) {
            return false;
        }

        // Vérifier le filtre de bots
        if (noBotFilter && game.getNombreJoueurVirtuelMax() > 0) {
            return false;
        }

        // Vérifier si la partie est pleine
        int currentPlayers = game.getNombreCurrentJoueurReel() + game.getNombreCurrentJoueurBot();
        if (currentPlayers >= game.getNombreJoueurMax()) {
            return false;
        }

        // Vérifier si il y a encore de la place pour un joueur réel
        if (game.getNombreCurrentJoueurReel() >= game.getNombreJoueurReelMax()) {
            return false;
        }

        return true;
    }

    public void createGame(String id, String ip, int port, String nomPartie, int nombreJoueurMax, int nombreJoueurReelMax, int nombreJoueurVirtuelMax, int espionAutorise, String status) {
        if (nombreJoueurReelMax + nombreJoueurVirtuelMax != nombreJoueurMax) {
            throw new IllegalArgumentException("La somme des joueurs réels maximum et des bots maximum doit être égale au nombre maximum de joueurs.");
        }
        
        PartieInfo newGame = new PartieInfo(id, ip, port, nomPartie, nombreJoueurMax, nombreJoueurReelMax, nombreJoueurVirtuelMax, espionAutorise, status);
        Platform.runLater(() -> {
            boolean isSet = false;
            for(int i = 0; i < games.size(); i++) {
                if(games.get(i).equals(newGame)) {
                    games.set(i, newGame);
                    isSet = true;
                    break;
                }
            }
            if(!isSet) {
                games.add(newGame);
            }
            updateFilteredGames();
        });
    }
    
    public void stop() {
        if (tReceiveMessage != null) {
            tReceiveMessage.interrupt();
        }
        if (chatter != null) {
            chatter.close();
        }
    }

    public void setSelectedGame(PartieInfo game) {
        selectedGame.set(game);
    }

    public ObjectProperty<PartieInfo> selectedGameProperty() {
        return selectedGame;
    }

    public PartieInfo getSelectedGame() {
        return selectedGame.get();
    }

    public void researchGame(GameType gameType) {
        String msg = "<RP identite=\""+ type +"\" typep=\""+ gameType +"\" taillep=\""+ this.nbJoueurMax +"\">";
        chatter.sendMessage(msg);
    }
    
    public void clearGames() {
        Platform.runLater(() -> {
            games.clear();
            filteredGames.clear();
        });
    }

    // Setters pour les filtres
    public void setSpyFilter(boolean value) {
        if (this.spyFilter != value) {
            if (Config.DEBUG_MODE) {
                System.out.println("Changement du filtre espion: " + value);
            }
            this.spyFilter = value;
            updateFilteredGames();
        }
    }

    public void setNoBotFilter(boolean value) {
        if (this.noBotFilter != value) {
            if (Config.DEBUG_MODE) {
                System.out.println("Changement du filtre bot: " + value);
            }
            this.noBotFilter = value;
            updateFilteredGames();
        }
    }

    public void setMaxPlayersFilter(int value) {
        if (this.maxPlayersFilter != value) {
            if (Config.DEBUG_MODE) {
                System.out.println("Changement du filtre nombre max de joueurs: " + value);
            }
            this.maxPlayersFilter = value;
            updateFilteredGames();
        }
    }
}
