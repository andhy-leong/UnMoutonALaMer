/**
 * Classe modèle responsable de la gestion de la recherche de parties pour les joueurs ESP.
 * Cette classe gère la communication réseau, le filtrage des parties et maintient les listes des parties disponibles.
 */
package esp.model;

import common.Config;
import common.enumtype.GameType;
import common.reseau.udp.MulticastNetworkChatterDecoderPlayer;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;

public class EspSearchForGameModel {
    private ObservableList<PartieInfo> games;
    private ObservableList<PartieInfo> filteredGames;
    private MulticastNetworkChatterDecoderPlayer chatter;
    private Thread tReceiveMessage;
    private int maxPlayersFilter;
    private boolean noBotFilter = false;

    /**
     * Crée une nouvelle instance de EspSearchForGameModel avec un filtre de nombre maximum de joueurs spécifié.
     * @param maxPlayersFilter Le nombre maximum de joueurs pour filtrer les parties
     */
    public EspSearchForGameModel(int maxPlayersFilter) {
        this.maxPlayersFilter = maxPlayersFilter;
        games = FXCollections.observableArrayList();
        filteredGames = FXCollections.observableArrayList();
        
        try {
            chatter = new MulticastNetworkChatterDecoderPlayer(games, maxPlayersFilter);
        } catch(Exception e) {
            if (Config.DEBUG_MODE) {
                System.err.println(this.getClass().getSimpleName() + ": impossible de lancer le chatter ");
            }
        }
        
        tReceiveMessage = new Thread(() -> {
            while(!Thread.interrupted()) {
                String msg = chatter.receiveMessage();
                if (Config.DEBUG_MODE) {
                    System.out.println("Message reçu tReceiveMesage " + msg);
                }
                chatter.DecoderPlayer(msg);
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

        tReceiveMessage.start();
        // Lancer une recherche initiale
        researchGame(GameType.MIX);
    }

    /**
     * Met à jour la liste des parties filtrées en fonction des paramètres de filtrage actuels.
     * Applique à la fois les filtres de bots et de nombre maximum de joueurs à la liste principale des parties.
     */
    private void updateFilteredGames() {
        Platform.runLater(() -> {
            List<PartieInfo> newFilteredGames = new ArrayList<>();
            if (Config.DEBUG_MODE) {
                System.out.println("\n=== Mise à jour des filtres ===");
                System.out.println("Nombre de parties total: " + games.size());
                System.out.println("Filtres actuels - NoBot: " + noBotFilter + ", MaxPlayers: " + maxPlayersFilter);
            }
            
            for (PartieInfo game : games) {
                if (isGameValid(game)) {
                    newFilteredGames.add(game);
                }
            }
            
            if (Config.DEBUG_MODE) {
                System.out.println("Nombre de parties après filtrage: " + newFilteredGames.size());
                System.out.println("=== Fin de la mise à jour ===\n");
            }
            filteredGames.setAll(newFilteredGames);
        });
    }

    /**
     * Obtient la raison pour laquelle une partie est filtrée ou marquée comme valide.
     * @param game La partie à vérifier
     * @return Une chaîne expliquant pourquoi la partie a été filtrée ou "Valide" si elle a passé tous les filtres
     */
    private String getFilterReason(PartieInfo game) {
        if (!"ATTENTE".equals(game.getStatus())) {
            return "Statut non en attente";
        }
        if (game.getEspionAutorise() != 1) {
            return "Espions non autorisés";
        }
        if (noBotFilter && game.getNombreJoueurVirtuelMax() > 0) {
            return "Bots non désirés";
        }
        if (game.getNombreJoueurMax() > maxPlayersFilter) {
            return "Trop de joueurs max (" + game.getNombreJoueurMax() + " > " + maxPlayersFilter + ")";
        }
        return "Valide";
    }

    /**
     * Vérifie si une partie répond à tous les critères de filtrage.
     * @param game La partie à valider
     * @return true si la partie passe tous les filtres, false sinon
     */
    private boolean isGameValid(PartieInfo game) {
        if (Config.DEBUG_MODE) {
            System.out.println("\n=== Vérification partie: " + game.getNomPartie() + " ===");
            System.out.println("Status: " + game.getStatus());
            System.out.println("Espions autorisés: " + game.getEspionAutorise());
            System.out.println("Nombre max joueurs virtuels: " + game.getNombreJoueurVirtuelMax());
            System.out.println("Nombre max joueurs: " + game.getNombreJoueurMax());
            System.out.println("Filtre noBot actif: " + noBotFilter);
            System.out.println("Filtre maxPlayers: " + maxPlayersFilter);
        }

        // Vérifier si la partie est en attente de joueurs
        if (!"ATTENTE".equals(game.getStatus())) {
            if (Config.DEBUG_MODE)
                System.out.println("❌ Partie non en attente");
            return false;
        }

        // Vérifier si les espions sont autorisés (critère le plus important)
        if (game.getEspionAutorise() != 1) {
            if (Config.DEBUG_MODE)
                System.out.println("❌ Espions non autorisés");
            return false;
        }

        // Vérifier le filtre de bots - Double vérification
        if (noBotFilter) {
            // Vérifie à la fois le max et le nombre actuel de bots
            if (game.getNombreJoueurVirtuelMax() > 0 || game.getNombreCurrentJoueurBot() > 0) {
                if (Config.DEBUG_MODE)
                    System.out.println("❌ Bots détectés avec filtre noBot actif");
                return false;
            }
        }

        // Vérifier le nombre maximum de joueurs
        if (game.getNombreJoueurMax() > maxPlayersFilter) {
            if (Config.DEBUG_MODE)
                System.out.println("❌ Trop de joueurs max");
            return false;
        }

        if (Config.DEBUG_MODE)
            System.out.println("✅ Partie valide");
        return true;
    }

    /**
     * Lance une recherche de partie avec le type de partie spécifié.
     * @param gameType Le type de partie à rechercher
     */
    public void researchGame(GameType gameType) {
        String msg = "<RP identite=\"ESP\" typep=\""+ gameType +"\" taillep=\""+ this.maxPlayersFilter +"\">";
        chatter.sendMessage(msg);
    }

    /**
     * Arrête toute communication réseau et nettoie les ressources.
     */
    public void stop() {
        if (tReceiveMessage != null) {
            tReceiveMessage.interrupt();
        }
        if (chatter != null) {
            chatter.close();
        }
    }

    /**
     * Obtient la liste filtrée des parties disponibles.
     * @return Une ObservableList des objets PartieInfo filtrés
     */
    public ObservableList<PartieInfo> getListePartie() {
        return filteredGames;
    }

    /**
     * Définit le statut du filtre de bots.
     * @param value true pour filtrer les parties avec des bots, false pour les autoriser
     */
    public void setNoBotFilter(boolean value) {
        if (this.noBotFilter != value) {
            if (Config.DEBUG_MODE) {
                System.out.println("Changement du filtre bot: " + value);
            }
            this.noBotFilter = value;
            updateFilteredGames();
        }
    }

    /**
     * Définit le filtre du nombre maximum de joueurs.
     * @param value Le nombre maximum de joueurs autorisés dans les parties filtrées
     */
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