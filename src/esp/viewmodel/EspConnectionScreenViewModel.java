/**
 * Classe ViewModel pour l'écran de connexion ESP.
 * Gère la logique métier et l'état de la vue de l'écran de connexion.
 */
package esp.viewmodel;

import common.Config;
import common.locales.I18N;
import esp.model.JoinedGameModelESP;
import javafx.collections.FXCollections;
import players.idjr.services.NavigationServiceIDJR;
import common.enumtype.GameType;
import common.reseau.udp.inforecup.PartieInfo;
import esp.model.EspSearchForGameModel;
import esp.services.NavigationServiceESP;
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
import javafx.collections.ObservableMap;

import java.util.ArrayList;

public class EspConnectionScreenViewModel {
    private EspSearchForGameModel model;
    private NavigationServiceESP navigationService;
    private StringProperty stateMessage = new SimpleStringProperty();
    private ObjectProperty<ViewState> currentState = new SimpleObjectProperty<>(ViewState.DISPLAY_GAMES);
    private BooleanProperty noBotFilter = new SimpleBooleanProperty(false);
    private IntegerProperty maxPlayersFilter = new SimpleIntegerProperty(5);
    private JoinedGameModelESP joinedGameModel;
    private static int nbBotFilterCalled = 0;

    private StringBinding titleLabelProperty;
    private StringBinding homeButtonProperty;
    private StringBinding botPlayersCheckBoxProperty;
    private StringBinding stateMessageProperty;
    private StringBinding joinButtonProperty;
    private StringBinding refreshButtonProperty;
    private StringBinding filterLabelProperty;
    private StringBinding realPlayersLabelProperty;
    private StringBinding botPlayersLabelProperty;
    private StringBinding filterMaxLabelProperty;
    private StringBinding menuButtonProperty;
    private StringBinding joueurVirtuelLabelProperty;
    private BooleanProperty refreshButtonDisabled = new SimpleBooleanProperty(false);
    private BooleanProperty spyButtonDisabled = new SimpleBooleanProperty(false);
    private StringProperty name;

    private PartieInfo selectedGame;
    private ObservableMap<String, ArrayList<String>> message;

    /**
     * Énumération représentant les états possibles de l'écran de connexion.
     */
    public enum ViewState {
        /** État lors de l'affichage de la liste des parties disponibles */
        DISPLAY_GAMES,
        /** État lorsqu'une partie a été sélectionnée */
        GAME_SELECTED
    }

    /**
     * Crée une nouvelle instance de EspConnectionScreenViewModel.
     * @param navigationService Le service de navigation pour gérer les transitions d'écran
     */
    public EspConnectionScreenViewModel(NavigationServiceESP navigationService) {
        name = new SimpleStringProperty("");
        try {
            this.model = new EspSearchForGameModel(maxPlayersFilter.get());
            this.navigationService = navigationService;

            I18N.localeProperty().addListener((obs, oldLocale, newLocale) -> {
                updateViewForCurrentState();
            });

            labelToBind();
            updateStateMessage("stateMessage.displayGames");
        } catch (Exception e) {
            if (Config.DEBUG_MODE) {
                System.out.println("Erreur lors de la création du modèle : " + e.getMessage());
            }
            updateStateMessage("Erreur : " + e.getMessage());
        }
        message = FXCollections.observableHashMap();
    }

    /**
     * Initialise les liaisons pour toutes les étiquettes localisées.
     */
    private void labelToBind() {
        titleLabelProperty = I18N.createStringBinding("titleLabelConnectionScreenESP");
        homeButtonProperty = I18N.createStringBinding("homeButtonConnectionScreenESP");
        botPlayersCheckBoxProperty = I18N.createStringBinding("botPlayersCheckBoxConnectionScreenESP");
        stateMessageProperty = I18N.createStringBinding("stateMessage.displayGames");
        joinButtonProperty = I18N.createStringBinding("spyButtonConnectionScreenESP");
        refreshButtonProperty = I18N.createStringBinding("refreshButtonConnectionScreenESP");
        filterLabelProperty = I18N.createStringBinding("filterLabelConnectionScreenESP");
        realPlayersLabelProperty = I18N.createStringBinding("realPlayersLabelConnectionScreenESP");
        botPlayersLabelProperty = I18N.createStringBinding("botPlayersLabelConnectionScreenESP");
        filterMaxLabelProperty = I18N.createStringBinding("filterMaxLabelConnectionScreenESP");
        menuButtonProperty = I18N.createStringBinding("menuButtonConnectionScreenESP");
        joueurVirtuelLabelProperty = I18N.createStringBinding("joueurVirtuelLabelConnectionScreenESP");
    }

    /**
     * Obtient la propriété du message d'état.
     * @return La StringProperty contenant le message d'état actuel
     */
    public StringProperty stateMessageProperty() {
        return stateMessage;
    }

    /**
     * Obtient la propriété de l'état de vue actuel.
     * @return La ObjectProperty contenant l'état de vue actuel
     */
    public ObjectProperty<ViewState> currentStateProperty() {
        return currentState;
    }

    /**
     * Gère l'événement de clic sur le bouton menu.
     */
    public void onMenuButtonClicked() {
        navigationService.showPopUpMenu();
    }

    /**
     * Gère l'événement de clic sur le bouton accueil.
     */
    public void onHomeButtonClicked() {
        model.stop();
        navigationService.navigateToStartingScreen();
    }

    /**
     * Met à jour le message d'état avec un nouveau message localisé.
     * @param message La clé pour le message localisé à afficher
     */
    public void updateStateMessage(String message) {
        stateMessage.set(I18N.get(message));
    }

    /**
     * Définit l'état de vue actuel et met à jour la vue en conséquence.
     * @param newState Le nouvel état à définir
     */
    public void setViewState(ViewState newState) {
        currentState.set(newState);
        updateViewForCurrentState();
    }

    /**
     * Met à jour la vue en fonction de l'état actuel.
     */
    private void updateViewForCurrentState() {
        labelToBind();
        switch (currentState.get()) {
            case DISPLAY_GAMES:
            case GAME_SELECTED:
                updateStateMessage("stateMessage.displayGames");
                break;
        }
    }

    /**
     * Lance le processus de rejoindre la partie sélectionnée.
     */
    public void joinSelectedGame() {
        if (selectedGame != null && !spyButtonDisabled.get()) {
            joinedGameModel = new JoinedGameModelESP(selectedGame, "ESP",message);
            name.bind(joinedGameModel.gameNameProperty());
            // Désactiver le bouton
            spyButtonDisabled.set(true);
            navigationService.navigateToGameScreenViewEsp();
            
            // Réactiver le bouton après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> spyButtonDisabled.set(false));
                } catch (InterruptedException e) {
                    if (Config.DEBUG_MODE) {
                        System.out.println("Erreur lors du délai du bouton espionner : " + e.getMessage());
                    }
                }
            }).start();
        }
    }

    /**
     * Gère la sélection d'une partie dans la liste.
     * @param game La partie sélectionnée
     */
    public void selectGame(PartieInfo game) {
        // Si on clique sur la partie déjà sélectionnée, on la déselectionne
        if (game.equals(this.selectedGame)) {
            this.selectedGame = null;
            setViewState(ViewState.DISPLAY_GAMES);
        } else {
            this.selectedGame = game;
            setViewState(ViewState.GAME_SELECTED);
        }
    }

    /**
     * Obtient la liste filtrée des parties.
     * @return Une ObservableList des parties filtrées
     */
    public ObservableList<PartieInfo> getFilteredGames() {
        return model.getListePartie();
    }

    /**
     * Obtient la propriété du filtre sans bot.
     * @return La BooleanProperty pour le filtre sans bot
     */
    public BooleanProperty noBotFilterProperty() {
        return noBotFilter;
    }

    /**
     * Obtient la propriété du filtre de nombre maximum de joueurs.
     * @return La IntegerProperty pour le filtre de nombre maximum de joueurs
     */
    public IntegerProperty maxPlayersFilterProperty() {
        return maxPlayersFilter;
    }

    /**
     * Définit le filtre du nombre maximum de joueurs.
     * @param maxPlayers Le nombre maximum de joueurs à autoriser
     */
    public void setMaxPlayersFilter(int maxPlayers) {
        if (Config.DEBUG_MODE) {
            System.out.println("FILTRE MAX JOUEURS CHANGé: " + maxPlayers);
        }
        maxPlayersFilter.set(maxPlayers);
        model.setMaxPlayersFilter(maxPlayers);
        refreshGameList(); // Rafraîchir la liste avec le nouveau filtre
    }

    /**
     * Gère les changements du filtre de bots.
     * @param noBotFilter true pour filtrer les parties avec des bots, false pour les autoriser
     */
    public void onBotFilterChanged(boolean noBotFilter) {
        if (Config.DEBUG_MODE) {
            System.out.println("FILTRE BOT CHANGé: " + noBotFilter);
        }
        this.noBotFilter.set(noBotFilter);
        model.setNoBotFilter(noBotFilter);
        GameType gameType = noBotFilter ? GameType.JR : GameType.MIX;
        model.researchGame(gameType);
    }

    /**
     * Rafraîchit la liste des parties disponibles.
     */
    public void refreshGameList() {
        if (Config.DEBUG_MODE) {
            System.out.println("Rafraîchissement de la liste des parties");
        }
        if (!refreshButtonDisabled.get()) {
            GameType gameType = noBotFilter.get() ? GameType.JR : GameType.MIX;
            model.researchGame(gameType);
            
            // Désactiver le bouton
            refreshButtonDisabled.set(true);
            
            // Réactiver le bouton après 500ms
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    Platform.runLater(() -> refreshButtonDisabled.set(false));
                } catch (InterruptedException e) {
                    if (Config.DEBUG_MODE) {
                        System.out.println("Erreur lors du délai de rafraîchissement : " + e.getMessage());
                    }
                }
            }).start();
        }
    }

    /**
     * Obtient le modèle pour la partie rejointe.
     * @return L'instance de JoinedGameModel
     */
    public JoinedGameModelESP getJoinedGameModel() {
        return joinedGameModel;
    }

    public StringProperty nameProperty() {return name;}

    /* Getters pour l'internationalisation */

    /**
     * Obtient la propriété de l'étiquette du titre.
     * @return La StringBinding pour l'étiquette du titre
     */
    public StringBinding titleLabelProperty() {
        return titleLabelProperty;
    }

    /**
     * Obtient la propriété de l'étiquette du bouton accueil.
     * @return La StringBinding pour le bouton accueil
     */
    public StringBinding homeButtonProperty() {
        return homeButtonProperty;
    }

    /**
     * Obtient la propriété de la case à cocher des joueurs bots.
     * @return La StringBinding pour la case à cocher des joueurs bots
     */
    public StringBinding botPlayersCheckBoxProperty() {
        return botPlayersCheckBoxProperty;
    }

    /**
     * Obtient la propriété de l'étiquette du bouton rejoindre.
     * @return La StringBinding pour le bouton rejoindre
     */
    public StringBinding joinButtonProperty() {
        return joinButtonProperty;
    }

    /**
     * Obtient la propriété de l'étiquette du bouton rafraîchir.
     * @return La StringBinding pour le bouton rafraîchir
     */
    public StringBinding refreshButtonProperty() {
        return refreshButtonProperty;
    }

    /**
     * Obtient la propriété de l'étiquette du filtre.
     * @return La StringBinding pour l'étiquette du filtre
     */
    public StringBinding filterLabelProperty() {
        return filterLabelProperty;
    }

    /**
     * Obtient la propriété de l'étiquette des joueurs réels.
     * @return La StringBinding pour l'étiquette des joueurs réels
     */
    public StringBinding realPlayersLabelProperty() {
        return realPlayersLabelProperty;
    }

    /**
     * Obtient la propriété de l'étiquette des joueurs bots.
     * @return La StringBinding pour l'étiquette des joueurs bots
     */
    public StringBinding botPlayersLabelProperty() {
        return botPlayersLabelProperty;
    }

    /**
     * Obtient la propriété de l'étiquette du filtre maximum.
     * @return La StringBinding pour l'étiquette du filtre maximum
     */
    public StringBinding filterMaxLabelProperty() {
        return filterMaxLabelProperty;
    }

    /**
     * Obtient la propriété de l'étiquette du bouton menu.
     * @return La StringBinding pour le bouton menu
     */
    public StringBinding menuButtonProperty() {
        return menuButtonProperty;
    }

    /**
     * Obtient la propriété de l'étiquette des joueurs virtuels.
     * @return La StringBinding pour l'étiquette des joueurs virtuels
     */
    public StringBinding joueurVirtuelLabelProperty() {
        return joueurVirtuelLabelProperty;
    }

    /**
     * Obtient la propriété de désactivation du bouton rafraîchir.
     * @return La BooleanProperty pour l'état de désactivation du bouton rafraîchir
     */
    public BooleanProperty refreshButtonDisabledProperty() {
        return refreshButtonDisabled;
    }

    /**
     * Obtient la propriété de désactivation du bouton espion.
     * @return La BooleanProperty pour l'état de désactivation du bouton espion
     */
    public BooleanProperty spyButtonDisabledProperty() {
        return spyButtonDisabled;
    }

    public ObservableMap<String, ArrayList<String>> getMessage() {return message;}
}
