package players.bots.botc;

import players.bots.common.BotTemplate;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import common.enumtype.PlayerType;

/**
 * Bot C implémente une stratégie de jeu basée sur les chaînes de Markov.
 * Ce bot utilise un système de prise de décision qui s'adapte en fonction de l'état du jeu et de l'historique des parties.
 *
 * <p>Caractéristiques principales :
 * <ul>
 *   <li>Utilisation de chaînes de Markov pour la prise de décision</li>
 *   <li>Adaptation dynamique de la stratégie selon l'état du jeu</li>
 *   <li>Trois modes de jeu : survie, dominant et équilibré</li>
 * </ul>
 *
 * <p>Le bot maintient plusieurs structures de données importantes :
 * <ul>
 *   <li>État courant du jeu (bouées, cartes visibles, etc.)</li>
 *   <li>Historique des probabilités de transition</li>
 *   <li>Suivi des cartes jouées et distribuées</li>
 * </ul>
 *

 * @see BotTemplate
 * @see PlayerType
 */
public class BotC extends BotTemplate {

    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private List<String> cartesMain = null; // Cartes météo en main
    private List<String> cartesDistribuees = new ArrayList<>(); // Toutes les cartes distribuées
    private List<String> cartesJoueesManche = new ArrayList<>(); // Cartes jouées dans la manche en cours
    private Map<String, List<String>> cartesJoueurs = new HashMap<>(); // Cartes des autres joueurs
     private String carteMaree = null; // Carte marée actuelle du bot
    private int boueesInitiales;
    private int indexJoueur;
    private boolean premiereManche = true; // Indique si c'est la première manche

    // Nouvelles structures pour Markov
    /**
     * Classe interne représentant l'état du jeu à un moment donné.
     * Utilisée pour le calcul des probabilités de transition dans la chaîne de Markov.
     */
    private static class EtatJeu {
        int bouéesRestantes;
        List<Integer> cartesMaréeVisibles;
        List<Integer> cartesMain;
        int tourActuel;
        Map<Integer, Double> probabilitésTransition;
        
        public EtatJeu() {
            cartesMaréeVisibles = new ArrayList<>();
            cartesMain = new ArrayList<>();
            probabilitésTransition = new HashMap<>();
        }
    }
    
    private EtatJeu etatCourant;
    private Map<String, List<Double>> historiqueParties;
    private int tourActuel;
    private static final double SEUIL_SURVIE = 0.3;
    private static final double SEUIL_POSITION_DOMINANTE = 1.2;
    private static final double POIDS_SURVIE = 0.6;
    private static final double POIDS_VICTOIRE = 0.4;

    private static final Random random = new Random();

    /**
     * Constructeur principal du bot avec nom personnalisé.
     *
     * @param botName Nom du bot
     * @param verboseLevel Niveau de verbosité pour le logging
     * @throws Exception Si la connexion au jeu échoue
     */
    public BotC(String botName, int verboseLevel) throws Exception {
        super(botName, verboseLevel, PlayerType.BOTM);
        super.searchAndJoinGame();
    }

    /**
     * Constructeur secondaire du bot sans nom personnalisé.
     *
     * @param verboseLevel Niveau de verbosité pour le logging
     * @throws Exception Si la connexion au jeu échoue
     */
    public BotC(int verboseLevel) throws Exception {
        super(verboseLevel, PlayerType.BOTM);
        super.searchAndJoinGame();
    }

    /**
     * Génère un nom aléatoire pour le bot.
     *
     * @return String représentant le nom généré
     */
    public String generateBotName() {
        String hexNumber = String.format("%08X", random.nextInt(0x10000000));
        return "BOT_C_" + hexNumber;
    }

    /**
     * Définit le nom du bot en ajoutant le préfixe "BOT_C_".
     *
     * @param botName Nom de base du bot
     */
    @Override
    public void setName(String botName) {
        this.botName = "BOT_C_" + botName;
    }

    /**
     * Méthode principale de jeu du bot.
     * Gère la réception des messages et la logique de jeu.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void startPlay() {
        Thread messageReceiver = new Thread(() -> {
            while (true) {
                try {
                    String msg = connexionPartieBot.getSocket().receiveMessage();
                    if (msg != null) {
                        messageQueue.put(msg);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.SEVERE, "Message receiver interrupted");
                    break;
                }
            }
        });
        messageReceiver.start();

        boolean play = true;
        tourActuel = 0;
        etatCourant = new EtatJeu();
        
        while (play) {
            try {
                String msg = messageQueue.take();
                HashMap<String, Object> data = processMessages(msg);

                if (data != null) {
                    switch ((String) data.get("header")) {
                        case "IP":
                            String[] listeJoueur = (String[]) data.get("listej");
                            for (int i = 0; i < listeJoueur.length; i++) {
                                if (listeJoueur[i].equals(this.botName)) {
                                    indexJoueur = i;
                                }
                            }
                            break;

                        case "DCJ":
                            cartesMain = (List<String>) data.get("cartesmeteo");
                            cartesDistribuees.addAll(cartesMain);
                            cartesJoueurs.put(this.botName, new ArrayList<>(cartesMain));
                            logger.log(Level.INFO, "Cartes reçues en main : " + cartesMain);
                            break;

                        case "RCMB":
                            if(((String) data.get("nomj")).equals(this.botName)) {
                                String carteMareeTemp = (String) data.get("cartemaree");
                                logger.log(Level.INFO, "Carte marée la plus basse reçue");
                                carteMaree = carteMareeTemp;
                                etatCourant.cartesMaréeVisibles.add(Integer.parseInt(carteMareeTemp));
                            }
                            break;
                            
                        case "RCMH":
                            if(((String) data.get("nomj")).equals(this.botName)) {
                                String carteMareeTemp2 = (String) data.get("cartemaree");
                                logger.log(Level.INFO, "Carte marée la plus haute reçue");
                                carteMaree = carteMareeTemp2;
                                etatCourant.cartesMaréeVisibles.add(Integer.parseInt(carteMareeTemp2));
                            }
                            break;

                        case "ITP":
                            if (data.get("idt").equals("T01")) {
                                boueesInitiales = Integer.parseInt(((String[]) data.get("nbbouees"))[indexJoueur]);
                                etatCourant.bouéesRestantes = boueesInitiales;
                            }
                            if (!data.get("idm").equals("M01")) {
                                premiereManche = false;
                            }
                            tourActuel = 0; // Réinitialisation du compteur de tours
                            logger.log(Level.INFO, "Nombre de bouées initiales : " + boueesInitiales);
                            break;

                        case "DCM":
                            tourActuel++; // Incrémentation du compteur de tours
                            if (cartesMain != null && !cartesMain.isEmpty()) {
                                String carteChoisie = choisirCarteMarkov();
                                cartesMain.remove(carteChoisie);
                                cartesJoueesManche.add(carteChoisie);

                                String choixCarte = "<CCM cartemeteo=\"" + carteChoisie + "\" idp=\"" + data.get("idp") +
                                        "\" idm=\"" + data.get("idm") + "\" idt=\"" + data.get("idt") + "\"/>";
                                connexionPartieBot.getSocket().sendMessage(choixCarte);
                                logger.log(Level.INFO, "Tour " + tourActuel + " - Carte jouée : " + carteChoisie);
                            }
                            break;

                        case "ICMJ":
                            List<String> cartesJouees = (List<String>) data.get("listecartemeteo");
                            cartesJoueesManche.addAll(cartesJouees);
                            logger.log(Level.INFO, "Cartes jouées ce tour : " + cartesJouees);
                            break;

                        case "IFM":
                            carteMaree = null;
                            etatCourant.cartesMaréeVisibles.clear();
                            break;

                        case "TLP":
                            cartesJoueesManche.clear();
                            if (historiqueParties != null) {
                                logger.log(Level.INFO, "Historique des probabilités : " + historiqueParties);
                            }
                            play = false;
                            break;

                        default:
                            logger.log(Level.WARNING, "Message non traité : " + data.get("header"));
                            break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.SEVERE, "Main loop interrupted");
                break;
            }
        }
    }

    /**
     * Sélectionne la carte à jouer en utilisant l'algorithme de Markov.
     * 
     * @return String représentant la carte choisie
     */
    private String choisirCarteMarkov() {
        // Mise à jour de l'état courant
        mettreAJourEtatCourant();
        
        String carteChoisie = null;
        double meilleurScore = Double.NEGATIVE_INFINITY;
        double facteurRisque = calculerFacteurRisque();
        
        logger.log(Level.INFO, "Facteur de risque actuel : " + facteurRisque);
        logger.log(Level.INFO, "Tour actuel : " + tourActuel);
        
        // Mode de jeu basé sur le facteur de risque
        if (facteurRisque < SEUIL_SURVIE) {
            // Mode survie
            carteChoisie = choisirCarteSurvie();
            logger.log(Level.INFO, "Mode survie activé - Carte choisie : " + carteChoisie);
        } else if (calculerFacteurPosition() > SEUIL_POSITION_DOMINANTE) {
            // Mode dominant
            carteChoisie = choisirCarteDominante();
            logger.log(Level.INFO, "Mode dominant activé - Carte choisie : " + carteChoisie);
        } else {
            // Mode équilibré
            for (String carte : cartesMain) {
                double probaSurvie = calculerProbabiliteSurvie(carte);
                double probaVictoire = calculerProbabiliteVictoire(carte);
                
                double score = POIDS_SURVIE * probaSurvie + POIDS_VICTOIRE * probaVictoire;
                
                if (score > meilleurScore) {
                    meilleurScore = score;
                    carteChoisie = carte;
                }
            }
            logger.log(Level.INFO, "Mode équilibré - Carte choisie : " + carteChoisie + " avec score : " + meilleurScore);
        }
        
        // Mise à jour des probabilités de transition
        if (carteChoisie != null) {
            mettreAJourProbabilites(carteChoisie);
        }
        
        return carteChoisie != null ? carteChoisie : cartesMain.get(0);
    }
    
    /**
     * Met à jour l'état courant du jeu avec les informations actuelles.
     */
    private void mettreAJourEtatCourant() {
        if (etatCourant == null) {
            etatCourant = new EtatJeu();
        }
        
        etatCourant.bouéesRestantes = boueesInitiales;
        etatCourant.cartesMain = cartesMain.stream()
            .map(c -> Integer.parseInt(c.substring(1)))
            .collect(java.util.stream.Collectors.toList());
            
        if (carteMaree != null) {
            etatCourant.cartesMaréeVisibles.add(Integer.parseInt(carteMaree));
        }
    }
    
    /**
     * Sélectionne une carte en mode survie (privilégie les cartes basses).
     *
     * @return String représentant la carte choisie pour la survie
     */
    private String choisirCarteSurvie() {
        return cartesMain.stream()
            .min(Comparator.comparingInt(c -> Integer.parseInt(c.substring(1))))
            .orElse(cartesMain.get(0));
    }
    
    /**
     * Sélectionne une carte en mode dominant (stratégie agressive).
     *
     * @return String représentant la carte choisie en position dominante
     */
    private String choisirCarteDominante() {
        // En position dominante, on peut se permettre de jouer plus agressif
        if (tourActuel >= 9) {
            // En fin de manche, on cherche à obtenir le point bonus
            return cartesMain.stream()
                .min(Comparator.comparingInt(c -> Integer.parseInt(c.substring(1))))
                .orElse(cartesMain.get(0));
        } else {
            // Sinon on joue une carte moyenne-haute
            return cartesMain.stream()
                .filter(c -> {
                    int valeur = Integer.parseInt(c.substring(1));
                    return valeur >= 4 && valeur <= 9;
                })
                .findFirst()
                .orElse(cartesMain.get(0));
        }
    }
    
    /**
     * Met à jour les probabilités de transition après chaque coup joué.
     *
     * @param carteJouee La carte qui vient d'être jouée
     */
    private void mettreAJourProbabilites(String carteJouee) {
        int valeurCarte = Integer.parseInt(carteJouee.substring(1));
        
        // Mise à jour des probabilités de transition
        etatCourant.probabilitésTransition.merge(valeurCarte, 
            calculerProbabiliteSurvie(carteJouee), 
            (old, nouveau) -> (old * 0.7 + nouveau * 0.3)); // Moyenne pondérée
            
        // Sauvegarde dans l'historique
        if (historiqueParties == null) {
            historiqueParties = new HashMap<>();
        }
        
        String clePartie = String.format("M%02d_T%02d", premiereManche ? 1 : 2, tourActuel);
        historiqueParties.computeIfAbsent(clePartie, k -> new ArrayList<>())
            .add(calculerProbabiliteSurvie(carteJouee));
    }

    /**
     * Calcule le facteur de risque actuel basé sur plusieurs paramètres du jeu.
     *
     * @return double représentant le niveau de risque (0.0 à 1.0)
     */
    private double calculerFacteurRisque() {
        double ratioBoueesRestantes = (double) etatCourant.bouéesRestantes / boueesInitiales;
        double ratioToursRestants = (double) (12 - tourActuel) / 12;
        
        // Calcul du ratio de cartes hautes restantes
        long cartesHautes = cartesMain.stream()
            .filter(c -> Integer.parseInt(c.substring(1)) > 6)
            .count();
        double ratioCartesHautes = (double) cartesHautes / cartesMain.size();
        
        return ratioBoueesRestantes * ratioToursRestants * (1 - ratioCartesHautes);
    }
    
    /**
     * Évalue la position relative du bot par rapport aux autres joueurs.
     *
     * @return double représentant la force de la position (plus grand = meilleur)
     */
    private double calculerFacteurPosition() {
        // Calcul de la moyenne des bouées des autres joueurs
        double moyenneBouees = cartesJoueurs.values().stream()
            .mapToInt(List::size)
            .average()
            .orElse(0.0);
            
        double ratioBoueesPosition = moyenneBouees == 0 ? 1 : 
            (double) etatCourant.bouéesRestantes / moyenneBouees;
            
        // Calcul de la moyenne des cartes marée visibles
        double moyenneMaree = etatCourant.cartesMaréeVisibles.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
            
        double valeurMareeActuelle = carteMaree != null ? 
            Double.parseDouble(carteMaree) : 0.0;
            
        double ratioMaree = moyenneMaree == 0 ? 1 : 
            valeurMareeActuelle / moyenneMaree;
            
        return ratioBoueesPosition * (1 - ratioMaree);
    }
    
    /**
     * Calcule la probabilité de survie pour une carte donnée.
     *
     * @param carte La carte à évaluer
     * @return double représentant la probabilité de survie (0.0 à 1.0)
     */
    private double calculerProbabiliteSurvie(String carte) {
        int valeurCarte = Integer.parseInt(carte.substring(1));
        
        // Probabilité basée sur les cartes déjà jouées
        long cartesSupRestantes = cartesDistribuees.stream()
            .filter(c -> Integer.parseInt(c.substring(1)) > valeurCarte && 
                        !cartesJoueesManche.contains(c))
            .count();
            
        double probaSurvieBase = 1 - ((double) cartesSupRestantes / 
            Math.max(1, cartesDistribuees.size() - cartesJoueesManche.size()));
            
        // Ajustement selon la phase de la manche
        if (tourActuel <= 4) { // Début de manche
            probaSurvieBase *= valeurCarte <= 6 ? 1.2 : 0.8;
        } else if (tourActuel <= 8) { // Milieu de manche
            probaSurvieBase *= valeurCarte <= 9 ? 1.1 : 0.9;
        } else { // Fin de manche
            probaSurvieBase *= valeurCarte <= 3 ? 1.3 : 0.7;
        }
        
        return Math.min(1.0, Math.max(0.0, probaSurvieBase));
    }
    
    /**
     * Calcule la probabilité de victoire pour une carte donnée.
     *
     * @param carte La carte à évaluer
     * @return double représentant la probabilité de victoire (0.0 à 1.0)
     */
    private double calculerProbabiliteVictoire(String carte) {
        int valeurCarte = Integer.parseInt(carte.substring(1));
        
        // Base sur la position relative
        double probaVictoireBase = calculerFacteurPosition();
        
        // Ajustement selon la valeur de la carte
        if (tourActuel >= 9) { // En fin de manche
            probaVictoireBase *= valeurCarte <= 3 ? 1.5 : 0.5; // Favorise les petites cartes
        } else {
            probaVictoireBase *= (valeurCarte >= 4 && valeurCarte <= 9) ? 1.2 : 0.8;
        }
        
        return Math.min(1.0, Math.max(0.0, probaVictoireBase));
    }
}
