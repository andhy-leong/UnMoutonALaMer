package players.idjr.viewmodel;

import common.Config;
import common.locales.I18N;
import javafx.beans.binding.StringBinding;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import players.idjr.model.GameScreenModel;
import players.idjr.model.JoinedGameModel;
import players.idjr.services.NavigationServiceIDJR;

public class GameScreenViewModel {
    private GameScreenModel model;
    private NavigationServiceIDJR navigationService;
    private JoinedGameModel joinedGameModel;


    private StringBinding menuButtonProperty;
    private SimpleStringProperty playerNameProperty;

    private SimpleStringProperty mancheLabelProperty;
    private SimpleStringProperty roundLabelProperty;
    private SimpleStringProperty partNameLabelProperty;
    private SimpleStringProperty topLabelProperty;
    private SimpleStringProperty buoysValueProperty;
    private SimpleStringProperty scoreValueProperty;
    private SimpleStringProperty carteMareeRecueProperty;

    private SimpleStringProperty endRoundScoreValueProperty;
    private SimpleStringProperty endRoundBuoysValueProperty;
    private SimpleStringProperty endRoundOldScoreValueProperty;
    
    private StringBinding sortingLabelProperty;
    private StringBinding crescentRadioButtonProperty;
    private StringBinding decrescentRadioButtonProperty;
    private StringBinding buoysLabelProperty;
    private StringBinding scoreLabelProperty;
    private StringBinding tideCardLabelProperty;
    private StringBinding endRoundScoreLabelProperty;
    private StringBinding endRoundLostBuoysLabelProperty;
    private StringBinding endRoundOldScoreLabelProperty;

    private StringBinding consigneWaitingAuthorizationProperty;
    private StringBinding consigneStartOfRoundProperty;
    private StringBinding consigneWeatherCardChoiceProperty;
    private StringBinding consigneNoTideCardProperty;
    private StringBinding consigneEliminatedProperty;
    private StringBinding consigneLostBuoyProperty;
    private StringBinding consigneNoBuoyLostProperty;
    private StringBinding consigneLowestTideCardProperty;
    private StringBinding consigneHighestTideCardProperty;
    private StringBinding consigneEndRoundProperty;
    private StringBinding consigneDisconnectedProperty;
    

    private Boolean invertedSorting = false;
    private long lastClickTimestamp = 0; // timestamp du dernier clic sur une carte

    private ListProperty<String> weatherCardsListProperty;

    private long lastInstructionTime = 0;
    private static final long INSTRUCTION_DELAY = 2000; // 2 secondes en millisecondes
    private JoinedGameModel.GameInstruction pendingInstruction = null;

    public GameScreenViewModel(GameScreenModel model, NavigationServiceIDJR navigationService, JoinedGameModel joinedGameModel) {
        this.model = model;
        this.navigationService = navigationService;
        this.joinedGameModel = joinedGameModel;

        menuButtonProperty = I18N.createStringBinding("menuButtonGameScreenIDJR");
        
        sortingLabelProperty = I18N.createStringBinding("sortingLabelGameScreenIDJR");
        crescentRadioButtonProperty = I18N.createStringBinding("crescentRadioButtonGameScreenIDJR");
        decrescentRadioButtonProperty = I18N.createStringBinding("decrescentRadioButtonGameScreenIDJR");
        buoysLabelProperty = I18N.createStringBinding("buoysLabelGameScreenIDJR");
        scoreLabelProperty = I18N.createStringBinding("scoreLabelGameScreenIDJR");
        tideCardLabelProperty = I18N.createStringBinding("tideCardLabelGameScreenIDJR");
        endRoundScoreLabelProperty = I18N.createStringBinding("endRoundScoreLabelGameScreenIDJR");
        endRoundLostBuoysLabelProperty = I18N.createStringBinding("endRoundLostBuoysLabelGameScreenIDJR");
        endRoundOldScoreLabelProperty = I18N.createStringBinding("endRoundOldScoreLabelGameScreenIDJR");

        consigneWaitingAuthorizationProperty = I18N.createStringBinding("consignes.waitingAuthorization");
        consigneStartOfRoundProperty = I18N.createStringBinding("consignes.startOfRound");
        consigneWeatherCardChoiceProperty = I18N.createStringBinding("consignes.weatherCardChoice");
        consigneNoTideCardProperty = I18N.createStringBinding("consignes.noTideCard");
        consigneEliminatedProperty = I18N.createStringBinding("consignes.eliminated");
        consigneLostBuoyProperty = I18N.createStringBinding("consignes.lostBuoy");
        consigneNoBuoyLostProperty = I18N.createStringBinding("consignes.noBuoyLost");
        consigneLowestTideCardProperty = I18N.createStringBinding("consignes.lowestTideCard");
        consigneHighestTideCardProperty = I18N.createStringBinding("consignes.highestTideCard");
        consigneEndRoundProperty = I18N.createStringBinding("consignes.endRound");
        consigneDisconnectedProperty = I18N.createStringBinding("consignes.disconnected");

        weatherCardsListProperty = new SimpleListProperty<>(FXCollections.observableArrayList(model.getWeatherCardsList()));

        // Initialisation des propriétés avec des valeurs par défaut
        partNameLabelProperty = new SimpleStringProperty("[Nom de la partie]");
        roundLabelProperty = new SimpleStringProperty("[Pli x/12]");
        mancheLabelProperty = new SimpleStringProperty("Manche 1");
        topLabelProperty = new SimpleStringProperty("[consignes texte haut]");

        playerNameProperty = new SimpleStringProperty("");
        buoysValueProperty = new SimpleStringProperty("0");
        scoreValueProperty = new SimpleStringProperty("0");
        carteMareeRecueProperty = new SimpleStringProperty("00");

        endRoundScoreValueProperty = new SimpleStringProperty("0");
        endRoundBuoysValueProperty = new SimpleStringProperty("0");
        endRoundOldScoreValueProperty = new SimpleStringProperty("0");


        // Initialiser avec les cartes du modèle
        weatherCardsListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        if (model != null && model.getWeatherCardsList() != null) {
            weatherCardsListProperty.setAll(model.getWeatherCardsList());
        }

        // Bind les propriétés avec le JoinedGameModel si disponible
        if (joinedGameModel != null) {
            // Récupération du pseudo du joueur depuis le JoinedGameModel
            playerNameProperty.set(joinedGameModel.getPlayerName());

            // Bind les autres labels
            partNameLabelProperty.bind(joinedGameModel.nomPartieProperty());

            // Bind du label Pli avec internationalisation
            joinedGameModel.pliLabelProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    if (newVal != null) {
                        String[] parts = newVal.split("[/ ]"); 
                        if (parts.length > 1) {
                            try {
                                String currentPli = parts[parts.length - 2].replaceAll("[^0-9]", ""); 
                                String maxPli = parts[parts.length - 1].replaceAll("[^0-9]", ""); 
                                
                                roundLabelProperty.bind(I18N.createStringBinding(() -> 
                                    String.format("%s %s/%s", 
                                        I18N.get("roundLabelGameScreenIDJR"),
                                        currentPli.trim(),
                                        maxPli.trim()
                                    )
                                ));
                            } catch (Exception e) {
                                roundLabelProperty.bind(I18N.createStringBinding(() -> newVal));
                            }
                        } else {
                            roundLabelProperty.bind(I18N.createStringBinding(() -> newVal));
                        }
                    }
                });
            });

            // Initialisation immédiat du binding pour le label manche
            String mancheText = joinedGameModel.mancheLabelProperty().get();
            if (mancheText != null) {
                String[] parts = mancheText.split(" ");
                if (parts.length > 1) {
                    try {
                        int mancheNum = Integer.parseInt(parts[parts.length - 1]);
                        mancheLabelProperty.bind(I18N.createStringBinding(() -> 
                            String.format("%s %d", 
                                I18N.get("mancheLabelGameScreenIDJR"), 
                                mancheNum
                            )
                        ));
                    } catch (NumberFormatException e) {
                        mancheLabelProperty.bind(I18N.createStringBinding(() -> mancheText));
                    }
                } else {
                    mancheLabelProperty.bind(I18N.createStringBinding(() -> mancheText));
                }
            }

            // Le listener pour les changements suivants de la manche
            joinedGameModel.mancheLabelProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    if (newVal != null) {
                        String[] parts = newVal.split(" ");
                        if (parts.length > 1) {
                            try {
                                int mancheNum = Integer.parseInt(parts[parts.length - 1]);
                                mancheLabelProperty.bind(I18N.createStringBinding(() -> 
                                    String.format("%s %d", 
                                        I18N.get("mancheLabelGameScreenIDJR"), 
                                        mancheNum
                                    )
                                ));
                            } catch (NumberFormatException e) {
                                mancheLabelProperty.bind(I18N.createStringBinding(() -> newVal));
                            }
                        } else {
                            mancheLabelProperty.bind(I18N.createStringBinding(() -> newVal));
                        }
                    }
                });
            });

            // Autres bindings
            buoysValueProperty.bind(joinedGameModel.nbBoueesProperty());
            carteMareeRecueProperty.bind(joinedGameModel.carteMareeRecueProperty());
        
            
            // Bind spécial pour le score total avec formatage
            joinedGameModel.scoreProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    if (newVal != null) {
                        try {
                            scoreValueProperty.set(formatScore(newVal)); 
                        } catch (Exception e) {
                            //System.err.println("Format de score invalide: " + newVal);
                            scoreValueProperty.set("0");
                        }
                    }
                });
            });
            
            // bind les propriétés de fin de manche
            // On utilise des listeners au lieu de bind direct pour pouvoir les modifier
            joinedGameModel.endRoundScoreProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    if (newVal != null) {
                        endRoundScoreValueProperty.set(formatScore(newVal));
                    }
                });
            });

            joinedGameModel.endRoundOldScoreProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    if (newVal != null) {
                        endRoundOldScoreValueProperty.set(formatScore(newVal));
                    }
                });
            });

            joinedGameModel.endRoundLostBuoysProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    if (newVal != null) {
                        endRoundBuoysValueProperty.set(formatScore(newVal));
                    }
                });
            });

            // Bind pour les instructions avec traduction et délai
            joinedGameModel.currentInstructionProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastInstructionTime < INSTRUCTION_DELAY) {
                        // Si le délai n'est pas écoulé, on stocke l'instruction en attente
                        pendingInstruction = newVal;
                        return;
                    }
                    updateInstruction(newVal);

                    // Réinitialiser les scores de fin de manche au début d'un nouveau round
                    if (newVal == JoinedGameModel.GameInstruction.ROUND_START) {
                        Platform.runLater(() -> {
                            endRoundScoreValueProperty.set("");
                            endRoundBuoysValueProperty.set("");
                            endRoundOldScoreValueProperty.set("");
                        });
                    }
                }
            });

            // Timer pour vérifier les instructions en attente
            java.util.Timer timer = new java.util.Timer(true);
            timer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    if (pendingInstruction != null) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastInstructionTime >= INSTRUCTION_DELAY) {
                            Platform.runLater(() -> {
                                updateInstruction(pendingInstruction);
                                pendingInstruction = null;
                            });
                        }
                    }
                }
            }, INSTRUCTION_DELAY, 500); // Vérifie toutes les 500ms

            // regarder les changements de cartes météo
            joinedGameModel.cartesMeteoProperty().addListener((obs, oldList, newList) -> {
                Platform.runLater(() -> {
                    if (newList != null) {
                        ObservableList<String> sortedList = FXCollections.observableArrayList(newList);
                        sortList(sortedList); // on applique le tri immédiatement
                        model.setCartesMeteo(sortedList);
                        weatherCardsListProperty.setAll(sortedList);
                    }
                });
            });

            // listener sur l'état de connexion
            joinedGameModel.isConnectedProperty().addListener((obs, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    if (!newValue) {
                        // déconnexion => griser toutes les cartes
                        ObservableList<String> currentCards = FXCollections.observableArrayList(weatherCardsListProperty);
                        for (int i = 0; i < currentCards.size(); i++) {
                            String card = currentCards.get(i);
                            if (card.charAt(3) != 'G') {
                                currentCards.set(i, card.substring(0, 3) + "G");
                            }
                        }
                        weatherCardsListProperty.setAll(currentCards);
                    } else {
                        // reconnexion => réactiver les cartes (état normal)
                        ObservableList<String> currentCards = FXCollections.observableArrayList(weatherCardsListProperty);
                        for (int i = 0; i < currentCards.size(); i++) {
                            String card = currentCards.get(i);
                            if (card.charAt(3) == 'G') {
                                currentCards.set(i, card.substring(0, 3) + "N");
                            }
                        }
                        weatherCardsListProperty.setAll(currentCards);
                    }
                });
            });
        }

        // ajout d'un listener à la liste du modèle
        if (model != null && model.getWeatherCardsList() != null) {
            model.getWeatherCardsList().addListener((ListChangeListener<String>) change -> {
                Platform.runLater(() -> {
                    while (change.next()) {
                        if (change.wasAdded() || change.wasRemoved()) {
                            ObservableList<String> updatedList = FXCollections.observableArrayList(model.getWeatherCardsList());
                            sortList(updatedList); // toujours appliquer le tri
                            weatherCardsListProperty.setAll(updatedList);
                        }
                    }
                });
            });
        }
    }

    private void updateInstruction(JoinedGameModel.GameInstruction instruction) {
        String translatedInstruction = translateInstruction(instruction);
        if (instruction == JoinedGameModel.GameInstruction.RECEIVED_LOW_TIDE_CARD || 
            instruction == JoinedGameModel.GameInstruction.RECEIVED_HIGH_TIDE_CARD) {
            translatedInstruction = String.format(translatedInstruction, carteMareeRecueProperty.get());
        }
        topLabelProperty.set(translatedInstruction);
        lastInstructionTime = System.currentTimeMillis();
    }

    private String translateInstruction(JoinedGameModel.GameInstruction instruction) {
        return switch (instruction) {
            case WAITING_AUTHORIZATION -> consigneWaitingAuthorizationProperty.get();
            case ROUND_START -> consigneStartOfRoundProperty.get();
            case CHOOSE_WEATHER_CARD -> consigneWeatherCardChoiceProperty.get();
            case NO_TIDE_CARD_RECEIVED -> consigneNoTideCardProperty.get();
            case LOST_BUOY -> consigneLostBuoyProperty.get();
            case NO_BUOY_LOST -> consigneNoBuoyLostProperty.get();
            case ELIMINATED -> consigneEliminatedProperty.get();
            case RECEIVED_LOW_TIDE_CARD -> consigneLowestTideCardProperty.get() + " (%s)";
            case RECEIVED_HIGH_TIDE_CARD -> consigneHighestTideCardProperty.get() + " (%s)";
            case ROUND_ENDED -> consigneEndRoundProperty.get();
            case DISCONNECTED -> consigneDisconnectedProperty.get();
            default -> "";
        };
    }

    public void updateWeatherCards(ObservableList<String> newCartes) {
        // créer une copie de la liste pour le tri
        ObservableList<String> sortedList = FXCollections.observableArrayList(newCartes);
        
        // appliquer le tri si nécessaire
        if (invertedSorting) {
            sortList(sortedList);
        } else {
            sortList(sortedList); // on trie toujours, mais en ordre croissant si invertedSorting est false
        }
        
        // mettre à jour la liste
        weatherCardsListProperty.set(sortedList);
    }

    /* Menu actions */

    public void onMenuButtonClicked() {
        navigationService.showPopUpMenu();
    }

    /* Sorting actions */

    public void onSortWeatherCards(Boolean inverted) {
        if (invertedSorting != inverted) {
            invertedSorting = inverted;
            //  la liste actuelle pour le tri
            ObservableList<String> currentList = FXCollections.observableArrayList(weatherCardsListProperty);
            sortList(currentList);
            weatherCardsListProperty.set(currentList);
        }
    }

    private void sortList(ObservableList<String> listToSort) {
        // Sort the list
        for (int i = 0; i < listToSort.size(); i++) {
            for (int j = i + 1; j < listToSort.size(); j++) {
                // verif du format des cartes
                String cardI = listToSort.get(i);
                String cardJ = listToSort.get(j);
                
                if (!isValidCardFormat(cardI) || !isValidCardFormat(cardJ)) {
                    continue;
                }

                String cardValueI = cardI.substring(1, 3);
                String cardValueJ = cardJ.substring(1, 3);
                
                try {
                    int valueI = Integer.parseInt(cardValueI);
                    int valueJ = Integer.parseInt(cardValueJ);

                    if (invertedSorting) { // descending
                        if (valueI < valueJ) {
                            String temp = listToSort.get(i);
                            listToSort.set(i, listToSort.get(j));
                            listToSort.set(j, temp);
                        }
                    } else { // ascending
                        if (valueI > valueJ) {
                            String temp = listToSort.get(i);
                            listToSort.set(i, listToSort.get(j));
                            listToSort.set(j, temp);
                        }
                    }
                } catch (NumberFormatException e) {
                    //System.err.println("Format de carte invalide ignoré: " + cardI + " ou " + cardJ);
                }
            }
        }
    }

    private boolean isValidCardFormat(String card) {
        // Format attendu: [YGBPR][0-9][0-9][NGS]
        return card != null && card.length() == 4 && 
               "YGBPR".contains(card.substring(0, 1)) &&
               Character.isDigit(card.charAt(1)) &&
               Character.isDigit(card.charAt(2)) &&
               "NGS".contains(card.substring(3));
    }

    /* Card click action */

    public void onWeatherCardClicked(String cardValue) {
        // verif qu'au moins 1 seconde s'est écoulée depuis le dernier clic
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTimestamp < 1000) {
            return; // on ignore le clic si moins d'une seconde s'est écoulée pour eviter les spams
        }
        lastClickTimestamp = currentTime;

        // vérifier si la carte n'est pas grisé
        if (cardValue.charAt(3) != 'G') {
            String newCardValue;
            ObservableList<String> updatedList = FXCollections.observableArrayList(weatherCardsListProperty);

            if (cardValue.charAt(3) == 'S') {
                if (Config.DEBUG_MODE) {
                    System.out.println("Désélection de la carte: " + cardValue);
                }
                // envoie du message de retrait de carte d'abord
                if (joinedGameModel != null) {
                    joinedGameModel.sendCardWithdrawal();
                }

                // si elle est déjà selectionné, la déselectionner
                newCardValue = cardValue.substring(0, 3) + 'N';
                
                // mise à jour la liste après l'envoi du message
                int cardIndex = updatedList.indexOf(cardValue);
                if (cardIndex != -1) {
                    updatedList.set(cardIndex, newCardValue);
                    updateWeatherCards(updatedList);
                }
            } else {
                if (Config.DEBUG_MODE) {
                    System.out.println("Sélection de la carte: " + cardValue);
                }
                // vérifier s'il y a une autre carte sélectionnée
                for (int i = 0; i < updatedList.size(); i++) {
                    if (updatedList.get(i).charAt(3) == 'S') {
                        // déselectionner l'autre carte
                        String otherCardValue = updatedList.get(i).substring(0, 3) + 'N';
                        updatedList.set(i, otherCardValue);
                        break;
                    }
                }

                // envoie du message de choix de carte
                if (joinedGameModel != null) {
                    joinedGameModel.sendCardChoice(cardValue);
                }

                // sélectionner la carte actuelle
                newCardValue = cardValue.substring(0, 3) + 'S';
                
                // mise à jour la liste après l'envoi du message
                int cardIndex = updatedList.indexOf(cardValue);
                if (cardIndex != -1) {
                    updatedList.set(cardIndex, newCardValue);
                    updateWeatherCards(updatedList);
                }
            }
        }
    }

    /* Propertie getters */

    public StringBinding menuButtonProperty() {
        return menuButtonProperty;
    }

    public StringProperty partNameLabelProperty() {
        return partNameLabelProperty;
    }

    public StringProperty roundLabelProperty() {
        return roundLabelProperty;
    }

    public StringProperty topLabelProperty() {
        return topLabelProperty;
    }

    public StringProperty titleProperty() {
        return mancheLabelProperty;
    }

    public StringProperty playerNameProperty() {
        return playerNameProperty;
    }

    public StringProperty buoysValueProperty() {
        return buoysValueProperty;
    }

    public StringProperty scoreValueProperty() {
        return scoreValueProperty;
    }

    public ListProperty<String> weatherCardsProperty() {
        return weatherCardsListProperty;
    }

    public StringProperty endRoundScoreValueProperty() {
        return endRoundScoreValueProperty;
    }

    public StringProperty endRoundBuoysValueProperty() {
        return endRoundBuoysValueProperty;
    }

    public StringProperty endRoundOldScoreValueProperty(){
    	return endRoundOldScoreValueProperty;
    }

    public StringProperty carteMareeRecueProperty() {
        return carteMareeRecueProperty;
    }


    public StringBinding sortingLabelProperty() {
        return sortingLabelProperty;
    }

    public StringBinding crescentRadioButtonProperty() {
        return crescentRadioButtonProperty;
    }

    public StringBinding decrescentRadioButtonProperty() {
        return decrescentRadioButtonProperty;
    }

    public StringBinding buoysLabelProperty() {
        return buoysLabelProperty;
    }

    public StringBinding scoreLabelProperty() {
        return scoreLabelProperty;
    }

    public StringBinding tideCardLabelProperty() {
        return tideCardLabelProperty;
    }

    public StringBinding endRoundScoreLabelProperty() {
        return endRoundScoreLabelProperty;
    }

    public StringBinding endRoundLostBuoysLabelProperty() {
        return endRoundLostBuoysLabelProperty;
    }

    public StringBinding endRoundOldScoreLabelProperty() {
        return endRoundOldScoreLabelProperty;
    }

    private String formatScore(String score) {
        try {
            // conversion du score en entier pour supprimer les zéros inutiles
            int scoreValue = Integer.parseInt(score);
            return String.valueOf(scoreValue);
        } catch (NumberFormatException e) {
            return "0";
        }
    }

    public JoinedGameModel getJoinedGameModel() {
        return joinedGameModel;
    }
    
}
