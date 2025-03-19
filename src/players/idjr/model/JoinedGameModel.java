package players.idjr.model;

import common.Config;
import common.enumtype.PlayerType;
import common.locales.I18N;
import common.reseau.tcp.TCPconnectionSocket;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import players.idjr.services.NavigationServiceIDJR;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class JoinedGameModel {

    public enum GameInstruction {
        WAITING_AUTHORIZATION,
        ROUND_START,
        CHOOSE_WEATHER_CARD,
        NO_TIDE_CARD_RECEIVED,
        LOST_BUOY,
        NO_BUOY_LOST,
        ELIMINATED,
        RECEIVED_LOW_TIDE_CARD,
        RECEIVED_HIGH_TIDE_CARD,
        ROUND_ENDED,
        DISCONNECTED;
    }

    private PartieInfo info;
    private TCPconnectionSocket socket;
    private String nom;
    private Thread tReceiveMessage;
    private String id;
    private int playerIndex = -1; // recupération de l'index du joueur dans la liste des joueurs
    private ObjectProperty<ViewState> currentState = new SimpleObjectProperty<>(ViewState.WAITING_AUTHORIZATION);
    private ListProperty<String> cartesMeteo = new SimpleListProperty<>(FXCollections.observableArrayList());
    private String currentIdm = "M01"; // pour l'ID de la manche courante
    private String currentIdt = "T01"; // pour l'ID du tour acutel

    // propriétés pour les informations de jeu
    private StringProperty nomPartie = new SimpleStringProperty("");
    private String nomPartieStr = ""; // Pour stocker le nom de la partie
    private StringProperty mancheLabel;
    private StringProperty pliLabel;
    private ObjectProperty<GameInstruction> currentInstruction = new SimpleObjectProperty<>(GameInstruction.WAITING_AUTHORIZATION);
    private StringProperty nbBouees = new SimpleStringProperty("0");
    private StringProperty score = new SimpleStringProperty("0");
    private StringProperty carteMareeRecue = new SimpleStringProperty("");

    // propriétés pour la fin de manche
    private StringProperty endRoundScore = new SimpleStringProperty("");
    private StringProperty endRoundOldScore = new SimpleStringProperty("");
    private StringProperty endRoundLostBuoys = new SimpleStringProperty("");

    private boolean isOurTurn = false; // Pour suivre si on a le droit de jouer
    private String lastSelectedCard = null; // Pour garder en mémoire la dernière carte sélectionnée

    private ListProperty<String> players = new SimpleListProperty<>(FXCollections.observableArrayList());

    private int scoreTotal = 0; // Score total de la partie
    private String lastRoundScore = "0"; // Score de la dernière manche jouée
    private int totalLostBuoys = 0; // Nombre total de bouées perdues
    private int roundLostBuoys = 0; // Nombre de bouées perdues dans la manche courante

    private NavigationServiceIDJR navigationService;

    private Map<String, Integer> playerTotalScores = new HashMap<>();
    private int playerRank = 1; // 1 = premier, etc.

    private ObjectProperty<Boolean> isConnected = new SimpleObjectProperty<>(true);
    private long disconnectionTime = 0;
    private Timeline disconnectionTimer;

    public enum ViewState {
        WAITING_AUTHORIZATION,
        ACCEPTED,
        REFUSED,
        INITIALIZED,
        GAME_STARTED
    }

    public JoinedGameModel(PartieInfo info, String nom) {
        mancheLabel = new SimpleStringProperty(I18N.createStringBinding("mancheLabelGameScreenIDJR").get() + " 1");
        pliLabel = new SimpleStringProperty("[Pli 1/12]");


        if (Config.DEBUG_MODE)
            System.out.println("creation de la connexion tcp");
        this.info = info;
        this.nom = nom;
        if (info != null) {
            nomPartieStr = info.getId();
            nomPartie.set(nomPartieStr);
        }
        try {
            socket = new TCPconnectionSocket(info.getIp(), info.getPort());
        } catch(Exception e) {
            //System.err.println("Model : impossible de se connecter a l'hote");
        }

        tReceiveMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    try {
                        String msg = socket.receiveMessage();
                        if (msg == null) {
                            handleDisconnection();
                            // On attend un peu avant de réessayer
                            Thread.sleep(1000);
                            continue;
                        }
                        Decoder(msg);
                    } catch (Exception e) {
                        //System.err.println("Erreur inattendue: " + e.getMessage());
                        handleDisconnection();
                        // On attend un peu avant de réessayer
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        });

        tReceiveMessage.start();
        sendAcceptationDemand();
    }

    public void sendAcceptationDemand() {
        socket.sendMessage("<DCP nomj=\""+nom+"\" identite=\""+ PlayerType.JR+"\" idp=\""+ info.getId()+"\">");
    }

    public void Decoder(String msg) {
        if(msg == null) {
            handleDisconnection();
            return;
        }

        try {
            // Vérification du format de base du message
            Pattern patternHeader = Pattern.compile("^<([A-Z]+)");
            Matcher matcherHeader = patternHeader.matcher(msg);
            if (!matcherHeader.find()) {
                //System.err.println("Message reçu mal formé (pas d'en-tête): " + msg);
                return;
            }

            String header = matcherHeader.group(1);
            if (Config.DEBUG_MODE)
                System.out.println(msg);

            String exp = "(\\w+)=\"([^\"]*)\"|(\\w+)=([\\d]+)";
            Pattern pattern = Pattern.compile(exp);
            Matcher matcher = pattern.matcher(msg);

            
            if (!msg.trim().endsWith("/>") && !msg.trim().endsWith(">")) {
                //System.err.println("Message reçu mal formé (pas de fermeture): " + msg);
                return;
            }

            switch (header) {
                case "DCJ":
                    try {
                        while(matcher.find()) {
                            if(matcher.group(1) != null) {
                                String key = matcher.group(1);
                                String value = matcher.group(2);
                                if(key.equals("cartesmeteo")) {
                                    // conversion de la liste de cartes en format attendu par l'interface
                                    String[] cartes = value.split(",");
                                    ObservableList<String> newCartes = FXCollections.observableArrayList();

                                    if (Config.DEBUG_MODE)
                                        System.out.println("Cartes reçues brutes: " + Arrays.toString(cartes));

                                    java.util.Set<String> cartesUniques = new java.util.LinkedHashSet<>();

                                    for(String carte : cartes) {
                                        if (carte == null || carte.isEmpty()) {
                                            continue;
                                        }
                                        // ignorer les cartes qui commencent par "idp="
                                        if (carte.startsWith("idp=")) {
                                            //System.err.println("Ignoré attribut idp mal placé: " + carte);
                                            continue;
                                        }
                                        // On verifie que la carte a le bon format
                                        if (carte.matches("[YGBPR]\\d{2}")) {
                                            // Si ce n'est pas notre tour, on grise les cartes
                                            String carteAvecEtat = carte + (isOurTurn ? "N" : "G");
                                            if (!cartesUniques.add(carteAvecEtat)) {
                                                //System.err.println("Carte en double détectée et ignorée: " + carte);
                                            }
                                        } else {
                                            //System.err.println("Format de carte invalide reçu: " + carte);
                                        }
                                    }

                                    // conversion du set en liste
                                    newCartes.addAll(cartesUniques);

                                    if (!newCartes.isEmpty()) {
                                        if (Config.DEBUG_MODE)
                                            System.out.println("Cartes météo valides uniques reçues (" + newCartes.size() + " cartes): " + newCartes);
                                        Platform.runLater(() -> cartesMeteo.setAll(newCartes));
                                    } else {
                                        //System.err.println("Aucune carte météo valide dans le message");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        //System.err.println("Erreur lors du traitement du message DCJ: " + e.getMessage());
                    }
                    break;

                case "IP":
                    try {
                        while(matcher.find()) {
                            if(matcher.group(1) != null) {
                                String key = matcher.group(1);
                                String value = matcher.group(2);
                                if(key.equals("listej") && value != null) {
                                    // recupération de notre index dans la liste des joueurs
                                    String[] joueurs = value.split(",");
                                    if (Config.DEBUG_MODE)
                                        System.out.println("IP reçu - Liste des joueurs: " + Arrays.toString(joueurs));
                                    // mise à jour la liste des joueurs
                                    Platform.runLater(() -> {
                                        players.clear();
                                        players.addAll(joueurs);
                                        if (Config.DEBUG_MODE)
                                            System.out.println("Liste des joueurs mise à jour: " + players);
                                    });
                                    for (int i = 0; i < joueurs.length; i++) {
                                        if (joueurs[i].equals(nom)) {
                                            playerIndex = i;
                                            if (Config.DEBUG_MODE)
                                                System.out.println("Index du joueur trouvé: " + playerIndex);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        //System.err.println("Erreur lors du traitement du message IP: " + e.getMessage());
                    }
                    // Déclencher la navigation vers l'écran d'initialisation
                    Platform.runLater(() -> {
                        currentState.set(ViewState.INITIALIZED);
                    });
                    break;

                case "ADP":
                    currentState.set(ViewState.ACCEPTED);
                    while(matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);
                        if(matcher.group(1) != null) {
                            id = value;
                            if (Config.DEBUG_MODE)
                                System.out.println(this.getClass().getSimpleName() + "id ->" + id);
                        }
                    }

                    break;

                case "RDP":
                    currentState.set(ViewState.REFUSED);
                    try {
                        this.socket.stop();
                        tReceiveMessage.interrupt();
                    } catch (IOException e) {
                        //System.err.println("Model : une erreur est survenue");
                    }
                    break;

                case "IM":
                    currentState.set(ViewState.GAME_STARTED);
                    while(matcher.find()) {
                        if(matcher.group(1) != null) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);
                            if(key.equals("idm")) {
                                currentIdm = value;
                                Platform.runLater(() -> {
                                    // Extraire le numéro de la manche (M01 -> 1)
                                    String mancheNum = value.substring(1);
                                    try {
                                        int num = Integer.parseInt(mancheNum);
                                        //mancheLabel.set("Manche " + value);
                                    mancheLabel.set(I18N.createStringBinding("mancheLabelGameScreenIDJR").get() + " " + num);
                                    } catch (NumberFormatException e) {
                                        mancheLabel.set("Manche " + mancheNum);
                                    }
                                    currentInstruction.set(GameInstruction.ROUND_START);
                                    // Réinitialiser la carte marée reçue au début d'une nouvelle manche
                                    carteMareeRecue.set("00");
                                    // Mettre le score de la dernière manche dans endRoundOldScore
                                    endRoundOldScore.set(lastRoundScore);
                                    // Réinitialiser le compteur de bouées perdues pour la nouvelle manche
                                    roundLostBuoys = 0;
                                });
                            }
                        }
                    }
                    break;

                case "ITP":
                    final String[] tourNumRef = {null};
                    String[] bouees = null;

                    // On parcourt tous les attributs d'abord
                    while(matcher.find()) {
                        if(matcher.group(1) != null) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);

                            if(key.equals("idt")) {
                                currentIdt = value;
                                // recuperer le numéro du tour (T01 -> 1)
                                tourNumRef[0] = value.substring(1);
                                Platform.runLater(() -> pliLabel.set("[Pli " + tourNumRef[0] + "/12]"));
                            }
                            else if(key.equals("nbbouees")) {
                                bouees = value.split(",");
                            }
                        }
                    }

                    // Si c'est le premier pli et qu'on a reçu les bouées, on les initialise
                    if (tourNumRef[0] != null && tourNumRef[0].equals("01") && bouees != null) {
                        if (playerIndex >= 0 && playerIndex < bouees.length) {
                            final String nbBoueesValue = bouees[playerIndex];
                            Platform.runLater(() -> {
                                nbBouees.set(nbBoueesValue);
                                if (Config.DEBUG_MODE)
                                    System.out.println("Initialisation des bouées: " + nbBoueesValue + " pour le joueur " + playerIndex);
                            });
                        } else {
                            //System.err.println("Index joueur invalide pour les bouées: " + playerIndex + " (nombre de joueurs: " + (bouees != null ? bouees.length : 0) + ")");
                        }
                    }

                    Platform.runLater(() -> currentInstruction.set(GameInstruction.ROUND_START));
                    break;

                case "DCM":
                    // Le pli commence, on active les cartes
                    isOurTurn = true;
                    Platform.runLater(() -> {
                        currentInstruction.set(GameInstruction.CHOOSE_WEATHER_CARD);
                        // On réactive les cartes
                        ObservableList<String> cartes = FXCollections.observableArrayList();
                        for (String carte : cartesMeteo) {
                            if (carte.charAt(3) != 'S') { // pas de modification des cartes sélectionnées
                                cartes.add(carte.substring(0, 3) + "N");
                            } else {
                                cartes.add(carte);
                            }
                        }
                        cartesMeteo.setAll(cartes);
                    });
                    break;

                case "ICR":
                    // infos sur le choix/retrait d'une carte par un joueur
                    while(matcher.find()) {
                        if(matcher.group(1) != null) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);
                            if(key.equals("nomj") && value.equals(nom)) {
                                //  notre action qui est confirmée
                                matcher.find(); // Passer au prochain groupe pour actionj
                                if(matcher.group(1).equals("actionj")) {
                                    String action = matcher.group(2);
                                    if(action.equals("CHOIX")) {
                                        // Notre carte a été jouée, on la supprime complètement de notre main
                                        Platform.runLater(() -> {
                                            ObservableList<String> cartes = FXCollections.observableArrayList(cartesMeteo);
                                            cartes.removeIf(carte -> carte.charAt(3) == 'S');
                                            cartesMeteo.setAll(cartes);
                                        });
                                    }
                                }
                            }
                        }
                    }
                    break;

                case "CLC":
                    // fin du pli
                    isOurTurn = false;
                    Platform.runLater(() -> {
                        // retrait de la carte qui était sélectionnée si on en a une
                        ObservableList<String> cartesTemp = FXCollections.observableArrayList(cartesMeteo);
                        if (lastSelectedCard != null) {
                            cartesTemp.removeIf(carte -> carte.startsWith(lastSelectedCard));
                            lastSelectedCard = null; // reset de la dernière carte sélectionnée
                        }

                        // désactivation des cartes restantes
                        ObservableList<String> cartes = FXCollections.observableArrayList();
                        for (String carte : cartesTemp) {
                            cartes.add(carte.substring(0, 3) + "G");
                        }
                        cartesMeteo.setAll(cartes);
                    });
                    break;

                case "RCPB":
                    try {
                        boolean joueurTrouve = false;
                        String[] joueurs = null;
                        String[] effets = null;

                        while(matcher.find()) {
                            if(matcher.group(1) != null) {
                                String key = matcher.group(1);
                                String value = matcher.group(2);
                                if(key.equals("listej")) {
                                    joueurs = value.split(",");
                                } else if(key.equals("listeeffet")) {
                                    effets = value.split(",");
                                }
                            }
                        }

                        // verification de la coherence des données
                        if (joueurs != null && effets != null && joueurs.length == effets.length) {
                            for (int i = 0; i < joueurs.length; i++) {
                                if (joueurs[i].equals(nom)) {
                                    joueurTrouve = true;
                                    final int currentBouees = Integer.parseInt(nbBouees.get());
                                    // si on est éliminé (E)
                                    if (effets[i].equals("E")) {
                                        // mise à jour des compteurs de bouées perdues avant de changer nbBouees
                                        roundLostBuoys += currentBouees;
                                        totalLostBuoys += currentBouees;
                                        
                                        Platform.runLater(() -> {
                                            nbBouees.set("0");
                                            currentInstruction.set(GameInstruction.ELIMINATED);
                                            // mise à jour immédiatement de l'affichage des bouées perdues
                                            endRoundLostBuoys.set(String.valueOf(roundLostBuoys));
                                        });
                                    }
                                    // si on perd une bouée (P)
                                    else if (effets[i].equals("P")) {
                                        if (currentBouees > 0) {
                                            // mise à jour des compteurs de bouées perdues avant de changer nbBouees
                                            roundLostBuoys++;
                                            totalLostBuoys++;
                                            
                                            Platform.runLater(() -> {
                                                nbBouees.set(String.valueOf(currentBouees - 1));
                                                currentInstruction.set(GameInstruction.LOST_BUOY);
                                                // mise à jour immédiatement de l'affichage des bouées perdues
                                                endRoundLostBuoys.set(String.valueOf(roundLostBuoys));
                                            });
                                        }
                                    }
                                    break;
                                }
                            }
                        } else {
                            //System.err.println("Message RCPB incohérent: listes de tailles différentes ou manquantes");
                        }
                    } catch (Exception e) {
                        //System.err.println("Erreur lors du traitement du message RCPB: " + e.getMessage());
                    }
                    break;

                case "RCMB":
                    // Réception d'une carte marée basse
                    while(matcher.find()) {
                        if(matcher.group(1) != null) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);
                            if(key.equals("nomj") && value.equals(nom)) {
                                // On cherche la carte marée
                                matcher.find();
                                if(matcher.group(1).equals("cartemaree")) {
                                    String carteMaree = matcher.group(2);
                                    Platform.runLater(() -> {
                                        carteMareeRecue.set(carteMaree);
                                        currentInstruction.set(GameInstruction.RECEIVED_LOW_TIDE_CARD);
                                    });
                                }
                            }
                        }
                    }
                    break;

                case "RCMH":
                    // Réception d'une carte marée haute
                    while(matcher.find()) {
                        if(matcher.group(1) != null) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);
                            if(key.equals("nomj") && value.equals(nom)) {
                                // On cherche la carte marée
                                matcher.find();
                                if(matcher.group(1).equals("cartemaree")) {
                                    String carteMaree = matcher.group(2);
                                    Platform.runLater(() -> {
                                        carteMareeRecue.set(carteMaree);
                                        currentInstruction.set(GameInstruction.RECEIVED_HIGH_TIDE_CARD);
                                    });
                                }
                            }
                        }
                    }
                    break;

                case "IFM":
                    // Fin de manche, traitement des scores
                    final int[] boueesInitiales = {0};

                    try {
                        boueesInitiales[0] = Integer.parseInt(nbBouees.get());
                    } catch (NumberFormatException e) {
                        //System.err.println("Erreur lors de la lecture du nombre de bouées: " + nbBouees.get());
                        boueesInitiales[0] = 0;
                    }

                    while(matcher.find()) {
                        if(matcher.group(1) != null) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);
                            if(key.equals("scores")) {
                                // Mettre à jour les scores de tous les joueurs
                                updatePlayerScores(value);
                                
                                String[] scores = value.split(",");
                                // Utiliser notre index pour récupérer notre score
                                if (playerIndex >= 0 && playerIndex < scores.length) {
                                    String mancheScore = scores[playerIndex];
                                    Platform.runLater(() -> {
                                        try {
                                            // Convertir le score de la manche (enlever le 0 initial si présent)
                                            int scoreManche = Integer.parseInt(mancheScore);
                                            
                                            // Mettre à jour le score total
                                            scoreTotal += scoreManche;
                                            
                                            // Sauvegarder le score de cette manche pour la prochaine
                                            lastRoundScore = String.valueOf(scoreManche);
                                            
                                            // Mettre à jour les propriétés
                                            score.set(String.valueOf(scoreTotal)); // Score total au milieu
                                            endRoundScore.set(String.valueOf(scoreManche)); // Score de la manche
                                            endRoundLostBuoys.set(String.valueOf(roundLostBuoys));
                                            endRoundOldScore.set(lastRoundScore); // Score de la manche précédente
                                            
                                            // message de fin de manche
                                            currentInstruction.set(GameInstruction.ROUND_ENDED);
                                            
                                            // griser toutes les cartes
                                            ObservableList<String> cartesGrisees = FXCollections.observableArrayList();
                                            for (String carte : cartesMeteo) {
                                                if (!carte.endsWith("G")) {
                                                    cartesGrisees.add(carte.substring(0, 3) + "G");
                                                } else {
                                                    cartesGrisees.add(carte);
                                                }
                                            }
                                            cartesMeteo.setAll(cartesGrisees);
                                            
                                            // Désactiver le tour
                                            isOurTurn = false;
                                            
                                            

                                        } catch (NumberFormatException e) {
                                            //System.err.println("Erreur lors du calcul des scores: " + e.getMessage());
                                        }
                                    });
                                }
                            }
                        }
                    }
                    break;

                case "IFP":
                    // Message de fin de partie
                    if (navigationService != null) {
                        Platform.runLater(() -> {

                        });
                    }
                    break;

                default:
                    if (Config.DEBUG_MODE) {
                        System.out.println("Message ignoré (en-tête inconnu): " + header);
                    }
                    break;
            }
        } catch (Exception e) {
            //System.err.println("Erreur lors du décodage du message: " + e.getMessage());
        }
    }

    public void cleanup() {
        if (disconnectionTimer != null) {
            disconnectionTimer.stop();
        }
    }

    public ObjectProperty<ViewState> currentStateProperty() {
        return currentState;
    }

    public ListProperty<String> cartesMeteoProperty() {
        return cartesMeteo;
    }

    public void sendCardChoice(String cardValue) {
        // Envoie du message CCM avec la carte choisie (sans l'état)
        String cardCode = cardValue.substring(0, 3);
        lastSelectedCard = cardCode; // stockage de la carte sélectionnée
        String message = String.format("<CCM cartemeteo=\"%s\" idp=\"%s\" idm=\"%s\" idt=\"%s\"/>", 
            cardCode, info.getId(), currentIdm, currentIdt);
        if (Config.DEBUG_MODE)
            System.out.println("Envoi du message de sélection: " + message);
        socket.sendMessage(message);
    }

    public void sendCardWithdrawal() {
        // Envoie du message RCM pour retirer la carte
        lastSelectedCard = null; // reset de la dernière carte sélectionnée
        String message = String.format("<RCM idp=\"%s\" idm=\"%s\" idt=\"%s\"/>", 
            info.getId(), currentIdm, currentIdt);
        if (Config.DEBUG_MODE)
            System.out.println("Envoi du message de retrait: " + message);
        socket.sendMessage(message);
    }

    // Getters pour les propriétés
    public String getPlayerName() {
        return nom;
    }

    public StringProperty nomPartieProperty() {
        return nomPartie;
    }

    public StringProperty mancheLabelProperty() {
        return mancheLabel;
    }

    public StringProperty pliLabelProperty() {
        return pliLabel;
    }

    public StringProperty nbBoueesProperty() {
        return nbBouees;
    }

    public StringProperty scoreProperty() {
        return score;
    }

    public StringProperty carteMareeRecueProperty() {
        return carteMareeRecue;
    }

    public ListProperty<String> playersProperty() {
        return players;
    }

    public StringProperty endRoundScoreProperty() {
        return endRoundScore;
    }

    public StringProperty endRoundOldScoreProperty() {
        return endRoundOldScore;
    }

    public StringProperty endRoundLostBuoysProperty() {
        return endRoundLostBuoys;
    }

    public ObjectProperty<GameInstruction> currentInstructionProperty() {
        return currentInstruction;
    }

    public void setNavigationService(NavigationServiceIDJR navigationService) {
        this.navigationService = navigationService;
    }

    private void updatePlayerScores(String scoresStr) {
        String[] scores = scoresStr.split(",");
        String[] currentPlayers = players.get().toArray(new String[0]);
        
        // Mettre à jour les scores totaux
        for (int i = 0; i < scores.length && i < currentPlayers.length; i++) {
            String player = currentPlayers[i];
            int scoreForRound = Integer.parseInt(scores[i]);
            playerTotalScores.merge(player, scoreForRound, Integer::sum);
        }
        
        // Calculer le classement du joueur actuel
        int currentPlayerScore = playerTotalScores.getOrDefault(nom, 0);
        playerRank = 1;
        for (int otherScore : playerTotalScores.values()) {
            if (otherScore > currentPlayerScore) {
                playerRank++;
            }
        }
    }

    public int getPlayerRank() {
        return playerRank;
    }

    public int getTotalLostBuoys() {
        return totalLostBuoys;
    }

    public ObjectProperty<Boolean> isConnectedProperty() {
        return isConnected;
    }


    public void handleDisconnection() {
        Platform.runLater(() -> {
            isConnected.set(false);
            disconnectionTime = System.currentTimeMillis();
            currentInstruction.set(GameInstruction.DISCONNECTED);
        });

        // essayer de fermer proprement le socket existant
        try {
            if (socket != null) {
                socket.stop();
            }
        } catch (IOException e) {
            //System.err.println("Erreur lors de la fermeture du socket: " + e.getMessage());
        }

        // lancer une tentative de reconnexion
        startReconnectionAttempts();
    }

    private void startReconnectionAttempts() {
        Thread reconnectionThread = new Thread(() -> {
            while (!Thread.interrupted() && !isConnected.get()) {
                try {
                    // tenter de créer une nouvelle connexion
                    TCPconnectionSocket newSocket = new TCPconnectionSocket(info.getIp(), info.getPort());
                    
                    // Si la connexion réussit, mettre à jour le socket et renvoyer la demande d'acceptation
                    socket = newSocket;
                    sendAcceptationDemand();
                    handleReconnection();
                    break;
                } catch (Exception e) {
                    if (Config.DEBUG_MODE) {
                        System.err.println("Tentative de reconnexion échouée: " + e.getMessage());
                    }
                }

                try {
                    // attendre 5 secondes avant la prochaine tentative
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        reconnectionThread.setDaemon(true);
        reconnectionThread.start();
    }

    public void handleReconnection() {
        Platform.runLater(() -> {
            isConnected.set(true);
            disconnectionTime = 0;
            // Retourner à l'instruction précédente ou à ROUND_START selon le contexte
            currentInstruction.set(GameInstruction.ROUND_START);
        });
    }
}
