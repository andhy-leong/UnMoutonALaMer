package players.bots.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe pour gérer les messages liés à une partie, depuis l'initialisation jusqu'au déroulement des manches.
 */
public class GestionMessagePartie {
    public final ConnexionPartieBot messenger;
    protected static final Logger logger = BotTemplate.getLogger();

    /**
     * Constructeur pour initialiser la gestion des messages.
     *
     * @param messenger Instance de ConnexionPartieBot utilisée pour envoyer et recevoir des messages.
     */
    public GestionMessagePartie(ConnexionPartieBot messenger) {
        this.messenger = messenger;

    }


    /**
     * Extrait le type de message depuis le contenu du message.
     *
     * @param message Le message à analyser.
     * @return Le type de message (mot clé après le chevron ouvrant).
     */
    public String extractMessageType(String message) {
        Pattern pattern = Pattern.compile("<([A-Z]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "UNKNOWN";
    }
    /**
     * Gère les messages d'initialisation de la partie.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations sur l'initialisation de la partie.
     */
    public HashMap<String, Object> handleGameInitialization(String message) {
        HashMap<String, Object> result = new HashMap<>();
        
        // Définir un motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile("listej=\"([^\"]*)\" idp=\"(P\\d+)\"");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire les informations
            String[] joueurs = matcher.group(1).split(","); // Liste des joueurs
            String idp = matcher.group(2);                 // Identifiant de la partie

            // Ajouter les informations à la HashMap
            result.put("listej", List.of(joueurs));
            result.put("idp", idp);

            // Log des informations pour le débogage
            logger.log(Level.INFO, "Initialisation de la partie : joueurs = " + List.of(joueurs) + ", ID Partie = " + idp);
        } else {
            // Log en cas d'erreur
            logger.log(Level.SEVERE, "Message IP mal formaté : " + message);
        }

        return result;
    }

    
    /**
     * Gère les messages d'initialisation de la manche.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations sur l'initialisation de la manche.
     */
    public HashMap<String, Object> handleMatchInitialization(String message) {
        HashMap<String, Object> result = new HashMap<>();
        Pattern pattern = Pattern.compile("idm=\\\"(M\\d{2})\\\" idp=\\\"(P\\d+)\\\"");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String matchId = matcher.group(1); // Récupération de l'identifiant de la manche
            String gameId = matcher.group(2); // Récupération de l'identifiant de la partie

            // Ajouter les informations à la HashMap
            result.put("idm", matchId);
            result.put("idp", gameId);

            // Log pour le débogage
            logger.log(Level.INFO, "Initialisation de la manche : ID Manche = " + matchId + ", ID Partie = " + gameId);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE,"Message d'initialisation de manche mal formaté : " + message);
        }

        return result;
    }


    /**
     * Gère les messages de distribution des cartes et retourne les informations extraites.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations sur les cartes météo et l'identifiant de la partie.
     */
    public HashMap<String, Object> handleCardDistribution(String message) {
        HashMap<String, Object> result = new HashMap<>();
        Pattern pattern = Pattern.compile("cartesmeteo=\"([^\"]*)\" idp=\"(P\\d+)\"");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String cartes = matcher.group(1); // Récupération des cartes météo
            String idp = matcher.group(2);    // Récupération de l'identifiant de la partie

            // Séparer les cartes météo par la virgule
            String[] cartesArray = cartes.split(",");
            List<String> cartesList = new ArrayList<>();
            for (String carte : cartesArray) {
                cartesList.add(carte.trim()); // Ajouter chaque carte à la liste
            }

            // Ajouter les informations à la HashMap
            result.put("cartesmeteo", cartesList);
            result.put("idp", idp);

            // Log des informations pour le débogage
            logger.log(Level.INFO, "Distribution des cartes météo : " + cartesList + " pour la partie : " + idp);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE, "Message de distribution des cartes mal formaté : " + message);
        }

        return result;
    }





    /**
     * Gère les messages d'initialisation de tour ou de manche.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les données extraites du message.
     */
    public HashMap<String, Object> handleRoundInitialization(String message) {
        HashMap<String, Object> data = new HashMap<>();
        Pattern pattern = Pattern.compile(
                "nbbouees=\"([-0-9,XX]*)\" cartemareejoueur=\"([0-9,XX]*)\" idt=\"(T\\d{2})\" cartesmaree=\"([0-9,]*)\" idp=\"(P\\d+)\" idm=\"(M\\d{2})\""
            );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
        	String bouees = matcher.group(1);
        	String[] boueesArray = bouees.split(",");
            data.put("nbbouees",boueesArray);
            data.put("cartemareejoueur", matcher.group(2));
            data.put("idt", matcher.group(3));
            // Séparer les cartes météo par la virgule
            String cartes =  matcher.group(4);
            String[] cartesArray = cartes.split(",");
            List<String> cartesList = new ArrayList<>();
            for (String carte : cartesArray) {
                cartesList.add(carte.trim()); // Ajouter chaque carte à la liste
            }
            data.put("cartesmaree", cartesList);
            data.put("idp", matcher.group(5));
            data.put("idm", matcher.group(6));
        } else {
        	logger.log(Level.SEVERE,"Message d'initialisation de tour mal formaté : " + message);
        }
        return data;
    }

    /**
     * Gère les messages de demande de choix de carte météo.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les données extraites du message.
     */
    public HashMap<String, Object> handleWeatherCardChoice(String message) {
        HashMap<String, Object> data = new HashMap<>();
        Pattern pattern = Pattern.compile("idp=\"(P\\d+)\" idm=\"(M\\d{2})\" idt=\"(T\\d{2})\"");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            data.put("idp", matcher.group(1));
            data.put("idm", matcher.group(2));
            data.put("idt", matcher.group(3));
        } else {
        	logger.log(Level.SEVERE,"Message de choix de carte météo mal formaté : " + message);
        }
        return data;
    } 

    /**
     * Gère les actions des joueurs (choix ou retrait).
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations extraites du message.
     */
    public HashMap<String, Object> handlePlayerAction(String message) {
        // Créer une HashMap pour stocker les informations extraites
    	HashMap<String, Object> playerActionData = new HashMap<>();

        // Définir un motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile(
            "nomj=\"([^\"]*)\" actionj=\"(CHOIX|RETRAIT)\" idp=\"(P\\d+)\" idm=\"(M\\d{2})\" idt=\"(T\\d{2})\""
        );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire et ajouter les informations à la HashMap
            playerActionData.put("nomj", matcher.group(1)); // Nom du joueur
            playerActionData.put("actionj", matcher.group(2)); // Action (CHOIX ou RETRAIT)
            playerActionData.put("idp", matcher.group(3)); // Identifiant de la partie
            playerActionData.put("idm", matcher.group(4)); // Identifiant de la manche
            playerActionData.put("idt", matcher.group(5)); // Identifiant du tour

            // Log des informations pour le débogage
            logger.log(Level.INFO,"Action traitée : " + playerActionData);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE,"Message ICR mal formaté : " + message);
        }

        return playerActionData;
    }

    /**
     * Gère les messages de clôture des choix.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations extraites du message.
     */
    public HashMap<String, Object> handleChoiceClosure(String message) {
        // Créer une HashMap pour stocker les informations extraites
    	HashMap<String, Object> choiceClosureData = new HashMap<>();

        // Définir le motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile(
            "idp=\"(P\\d+)\" idm=\"(M\\d{2})\" idt=\"(T\\d{2})\""
        );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire et ajouter les informations à la HashMap
            choiceClosureData.put("idp", matcher.group(1)); // Identifiant de la partie
            choiceClosureData.put("idm", matcher.group(2)); // Identifiant de la manche
            choiceClosureData.put("idt", matcher.group(3)); // Identifiant du tour

            // Log des informations pour le débogage
            logger.log(Level.INFO,"Clôture des choix traitée : " + choiceClosureData);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE,"Message CLC mal formaté : " + message);
        }

        return choiceClosureData;
    }

    /**
     * Gère les messages informant des cartes météo jouées.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations extraites du message.
     */
    public HashMap<String, Object> handlePlayedWeatherCards(String message) {
        // Créer une HashMap pour stocker les informations extraites
        HashMap<String, Object> playedWeatherCardsData = new HashMap<>();

        // Définir le motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile(
            "listecartemeteo=\"([^\"]*)\" idp=\"(P\\d+)\" idm=\"(M\\d{2})\" idt=\"(T\\d{2})\""
        );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire les informations
            String[] cartesMeteo = matcher.group(1).split(","); // Liste des cartes météo jouées
            String idp = matcher.group(2);                     // Identifiant de la partie
            String idm = matcher.group(3);                     // Identifiant de la manche
            String idt = matcher.group(4);                     // Identifiant du tour

            // Ajouter les informations à la HashMap
            playedWeatherCardsData.put("listecartemeteo", List.of(cartesMeteo));
            playedWeatherCardsData.put("idp", idp);
            playedWeatherCardsData.put("idm", idm);
            playedWeatherCardsData.put("idt", idt);

            // Log des informations pour le débogage
            logger.log(Level.INFO, "Cartes météo jouées traitées : " + playedWeatherCardsData);
        } else {
            // Log en cas d'erreur
            logger.log(Level.SEVERE, "Message ICMJ mal formaté : " + message);
        }

        return playedWeatherCardsData;
    }


    /**
     * Gère les messages sur la carte marée la plus basse.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations extraites du message.
     */
    public HashMap<String, Object> handleLowestTideCard(String message) {
        // Créer une HashMap pour stocker les informations extraites
    	HashMap<String, Object> lowestTideCardData = new HashMap<>();

        // Définir le motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile(
            "nomj=\"([^\"]+)\" cartemaree=\"(\\d{2})\" idp=\"(P\\d+)\" idm=\"(M\\d{2})\" idt=\"(T\\d{2})\""
        );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire et ajouter les informations à la HashMap
            lowestTideCardData.put("nomj", matcher.group(1));        // Nom du joueur
            lowestTideCardData.put("cartemaree", matcher.group(2));  // Carte marée
            lowestTideCardData.put("idp", matcher.group(3));         // Identifiant de la partie
            lowestTideCardData.put("idm", matcher.group(4));         // Identifiant de la manche
            lowestTideCardData.put("idt", matcher.group(5));         // Identifiant du tour

            // Log des informations pour le débogage
            logger.log(Level.INFO,"Carte marée la plus basse traitée : " + lowestTideCardData);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE,"Message RCMB mal formaté : " + message);
        }

        return lowestTideCardData;
    }


    /**
     * Gère les messages sur la carte marée la plus haute.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations extraites du message.
     */
    public HashMap<String, Object> handleHighestTideCard(String message) {
        // Créer une HashMap pour stocker les informations extraites
    	HashMap<String, Object> highestTideCardData = new HashMap<>();

        // Définir le motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile(
            "nomj=\"([^\"]+)\" cartemaree=\"(\\d{2})\" idp=\"(P\\d+)\" idm=\"(M\\d{2})\" idt=\"(T\\d{2})\""
        );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire et ajouter les informations à la HashMap
            highestTideCardData.put("nomj", matcher.group(1));        // Nom du joueur
            highestTideCardData.put("cartemaree", matcher.group(2));  // Carte marée
            highestTideCardData.put("idp", matcher.group(3));         // Identifiant de la partie
            highestTideCardData.put("idm", matcher.group(4));         // Identifiant de la manche
            highestTideCardData.put("idt", matcher.group(5));         // Identifiant du tour

            // Log des informations pour le débogage
            logger.log(Level.INFO,"Carte marée la plus haute traitée : " + highestTideCardData);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE,"Message RCMH mal formaté : " + message);
        }

        return highestTideCardData;
    }


    /**
     * Gère les messages concernant la perte de bouées.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations extraites du message.
     */
    public HashMap<String, Object> handleBuoyLoss(String message) {
        // Créer une HashMap pour stocker les informations extraites
    	HashMap<String, Object> buoyLossData = new HashMap<>();

        // Définir le motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile(
            "listej=\"([^\"]*)\" listeeffet=\"([PE,]*)\" idp=\"(P\\d+)\" idm=\"(M\\d{2})\" idt=\"(T\\d{2})\""
        );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire les informations
            String[] listeJ = matcher.group(1).split(",");         // Liste des noms des joueurs
            String[] listeEffet = matcher.group(2).split(",");     // Liste des effets
            String idp = matcher.group(3);                        // Identifiant de la partie
            String idm = matcher.group(4);                        // Identifiant de la manche
            String idt = matcher.group(5);                        // Identifiant du tour

            // Ajouter les informations à la HashMap
            buoyLossData.put("listej", List.of(listeJ));
            buoyLossData.put("listeeffet", List.of(listeEffet));
            buoyLossData.put("idp", idp);
            buoyLossData.put("idm", idm);
            buoyLossData.put("idt", idt);

            // Log des informations pour le débogage
            logger.log(Level.INFO,"Perte de bouées traitée : " + buoyLossData);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE,"Message RCPB mal formaté : " + message);
        }

        return buoyLossData;
    }


    /**
     * Gère les messages de fin de manche.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations extraites du message.
     */
    public HashMap<String, Object> handleEndOfRound(String message) {
        // Créer une HashMap pour stocker les informations extraites
    	HashMap<String, Object> endOfRoundData = new HashMap<>();

        // Définir le motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile(
            "scores=\"([0-9,-]+)\" idp=\"(P\\d+)\" idm=\"(M\\d{2})\""
        );
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire les informations
            String[] scores = matcher.group(1).split(","); // Liste des scores des joueurs
            String idp = matcher.group(2);                // Identifiant de la partie
            String idm = matcher.group(3);                // Identifiant de la manche

            // Ajouter les informations à la HashMap
            endOfRoundData.put("scores", List.of(scores));
            endOfRoundData.put("idp", idp);
            endOfRoundData.put("idm", idm);

            // Log des informations pour le débogage
            logger.log(Level.INFO,"Fin de la manche traitée : " + endOfRoundData);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE,"Message IFM mal formaté : " + message);
        }

        return endOfRoundData;
    }


    /**
     * Gère les messages de fin de partie.
     *
     * @param message Le message à traiter.
     * @return Une HashMap contenant les informations extraites du message.
     */
    public HashMap<String, Object> handleEndOfGame(String message) {
        // Créer une HashMap pour stocker les informations extraites
    	HashMap<String, Object> endOfGameData = new HashMap<>();

        // Définir le motif regex pour extraire les informations du message
        Pattern pattern = Pattern.compile("idp=\"(P\\d+)\"");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            // Extraire l'identifiant de la partie
            String idp = matcher.group(1);

            // Ajouter l'information à la HashMap
            endOfGameData.put("idp", idp);

            // Log des informations pour le débogage
            logger.log(Level.INFO,"Fin de la partie traitée : " + endOfGameData);
        } else {
            // Log en cas d'erreur
        	logger.log(Level.SEVERE,"Message TLP mal formaté : " + message);
        }

        return endOfGameData;
    }


    /**
     * Envoie un message via le messenger.
     *
     * @param message Le message à envoyer.
     */
    public void sendMessage(String message) {
        messenger.sendMessage(message);
        logger.log(Level.INFO,"Message envoyé : " + message);
    }




}
