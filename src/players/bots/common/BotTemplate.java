package players.bots.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.enumtype.PlayerType;
import common.reseau.udp.inforecup.PartieInfo;

public abstract class BotTemplate {

	protected static Logger logger;

    protected String botName;
    protected PlayerType botLevel;
    protected PartieInfo partieInfo;
    protected BotMulticastNetworkChatter networkChatter;
    protected BotSearchForGameModel searchForGame;
    protected ConnexionPartieBot connexionPartieBot;
    protected GestionMessagePartie gestionMessage;
    protected int verboseLevel = 0;
    protected ArrayList<PartieInfo> availableParties = new ArrayList<>();
    protected Thread socketListener;
    protected String idJoueur;
    
    public BotTemplate(String botName, int verboseLevel, PlayerType botLevel) throws Exception {
    	this.setName(botName);
    	logger = Logger.getLogger(botName);
    	this.verboseLevel = verboseLevel;
    	configureLogging();
        this.searchForGame = new BotSearchForGameModel(botLevel);
        // Initialize the list and network chatter here
        ArrayList<PartieInfo> availableParties = searchForGame.getListePartie();
        this.networkChatter = new BotMulticastNetworkChatter(availableParties);
        this.botLevel = botLevel;
        this.gestionMessage = new GestionMessagePartie(connexionPartieBot);
       

        
        searchAndJoinGame();
    }

    public BotTemplate(int verboseLevel, PlayerType botLevel) throws Exception {
    	this.botName = generateBotName();
    	logger = Logger.getLogger(botName);
    	this.verboseLevel = verboseLevel;
    	configureLogging();
        this.searchForGame = new BotSearchForGameModel(botLevel);
        // Initialize the list and network chatter here
        ArrayList<PartieInfo> availableParties = searchForGame.getListePartie();
        this.networkChatter = new BotMulticastNetworkChatter(availableParties);
        this.botLevel = botLevel;
        this.gestionMessage = new GestionMessagePartie(connexionPartieBot);

        
        
    }
    public BotTemplate(String botName, int verboseLevel, PlayerType botLevel,String AMAJP) throws Exception {
   
    	setName(botName);
    	logger = Logger.getLogger(botName);
    	this.verboseLevel = verboseLevel;
    	configureLogging();
        this.searchForGame = new BotSearchForGameModel(botLevel);
        // Initialize the list and network chatter here
        ArrayList<PartieInfo> availableParties = searchForGame.getListePartie();
        this.networkChatter = new BotMulticastNetworkChatter(availableParties);
        this.botLevel = botLevel;
        this.gestionMessage = new GestionMessagePartie(connexionPartieBot);

        
        networkChatter.decodeMessage(AMAJP);
        PartieInfo info = networkChatter.availableParties.getFirst();
        joinGame(info);
        
    }
    public BotTemplate(int verboseLevel, PlayerType botLevel, String AMAJP) throws Exception {
    	
    	botName = generateBotName();
    	logger = Logger.getLogger(botName);
    	this.verboseLevel = verboseLevel;
    	configureLogging();
        this.searchForGame = new BotSearchForGameModel(botLevel);
        // Initialize the list and network chatter here
        ArrayList<PartieInfo> availableParties = searchForGame.getListePartie();
        this.networkChatter = new BotMulticastNetworkChatter(availableParties);
        this.botLevel = botLevel;
        this.gestionMessage = new GestionMessagePartie(connexionPartieBot);

        
        networkChatter.decodeMessage(AMAJP);
        PartieInfo info = networkChatter.availableParties.getFirst();
        joinGame(info);
        
    }
    public abstract String generateBotName();
    public abstract void startPlay();
    public abstract void setName(String botName);


    private void configureLogging() {
        // 1. Définir le niveau du logger selon verboseLevel
        Level targetLevel;
        switch (verboseLevel) {
            case 3 -> targetLevel = Level.FINE;
            case 2 -> targetLevel = Level.WARNING;
            case 1 -> targetLevel = Level.INFO;
            default -> targetLevel = Level.SEVERE;
        }
        logger.setLevel(targetLevel);

        // 2. Configurer le handler personnalisé
        Handler[] handlers = logger.getHandlers();
        List<Level> originalHandlerLevels = new ArrayList<>();
        
        if (handlers.length == 0) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(targetLevel); // Synchronisé avec le logger
            logger.addHandler(consoleHandler);
            handlers = logger.getHandlers(); // Actualiser la liste des handlers
        }
        logger.setUseParentHandlers(false); // Désactiver les handlers parents

        // 3. Sauvegarder les niveaux actuels
        for (Handler handler : handlers) {
            originalHandlerLevels.add(handler.getLevel());
        }

        // 4. Forcer temporairement le niveau à ALL pour afficher le message
        logger.setLevel(Level.ALL);
        for (Handler handler : handlers) {
            handler.setLevel(Level.ALL);
        }

        // 5. Afficher le message de log (niveau FINE pour correspondre au cas "verboseLevel=3")
        logger.log(Level.ALL, "Niveau de verbosité choisi : {0}", targetLevel);

        // 6. Restaurer les niveaux originaux
        logger.setLevel(targetLevel);
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].setLevel(originalHandlerLevels.get(i));
        }
    }

    protected void searchAndJoinGame() {

        boolean joinedGame = false;
        availableParties = searchForGame.getListePartie();
        while (!joinedGame) {
            try {
                Thread.sleep(5000); // Attendre avant de chercher à nouveau
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.WARNING, "Recherche de partie interrompue pour le bot {0}", botName);
                return;
            }

            List<PartieInfo> gamesSnapshot;
            synchronized (availableParties) {
                gamesSnapshot = new ArrayList<>(availableParties); // Crée une copie de la liste
            }

            Iterator<PartieInfo> iterator = gamesSnapshot.iterator();

            while (iterator.hasNext()) {
                PartieInfo info = iterator.next();
                logger.log(Level.INFO, "Bot {0} tente de rejoindre la partie : {1}", new Object[]{botName, info.getId()});

                joinGame(info);

                if (connexionPartieBot != null) {
                    try {
                        synchronized (connexionPartieBot.getStateLock()) {
                            while (connexionPartieBot.getCurrentState() == ConnexionPartieBot.ViewState.WAITING_AUTHORIZATION) {
                                connexionPartieBot.getStateLock().wait();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.log(Level.WARNING, "Bot {0} a été interrompu en attendant une réponse.", botName);
                        return;
                    }

                    if (connexionPartieBot.getCurrentState() == ConnexionPartieBot.ViewState.ACCEPTED) {
                        joinedGame = true;
                        this.idJoueur = connexionPartieBot.idJoueur;
                        logger.log(Level.INFO, "Bot {0} a rejoint la partie : {1}", new Object[]{botName, info.getId()});
                        break;
                    } else if (connexionPartieBot.getCurrentState() == ConnexionPartieBot.ViewState.REFUSED) {
                        logger.log(Level.INFO, "Bot {0} a été refusé par la partie : {1}", new Object[]{botName, info.getId()});
                        synchronized (availableParties) {
                            availableParties.remove(info); // Supprime en toute sécurité de la liste d'origine
                        
                        }
                }
            }
            }
            if(System.currentTimeMillis() - searchForGame.timeSinceLastGameMsg > 300000) {
            	logger.log(Level.INFO, "Bot {0} a attendu un message de partie pendant 5 minutes, aucun message reçu. Fermeture du bot", new Object[]{botName});
            	terminateBot();
            }
            if (!joinedGame) {
                logger.log(Level.INFO, "Bot {0} : Aucune partie n'a accepté le bot, nouvelle tentative...",new Object[]{botName});
                //searchForGame.initiateGameSearch(botLevel);
            }
            if(joinedGame) {
            	startSocketListen();
            	
            }
           
        }
                
    }

    private void joinGame(PartieInfo info) {
        this.partieInfo = info;
        try {
            connexionPartieBot = new ConnexionPartieBot(info, botName, botLevel);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Bot {0} : Erreur lors de la tentative de connexion à la partie \n {1}", new Object[]{botName,e.getMessage()});
        }
    }

    private void startSocketListen() {
    	
        while(true) {
        	String msg = connexionPartieBot.getSocket().receiveMessage();
        	String expHeader = "<(\\w+)\\s*";

            if(msg != null) {
                Pattern patternHeader = Pattern.compile(expHeader);
                Matcher matcherHeader = patternHeader.matcher(msg);
                String header = "";
                if (matcherHeader.find()) {
                    header = matcherHeader.group(1);
                }
                if("IP".equals(header)) {
                	logger.log(Level.INFO, "Id joueur : "+idJoueur);
                    this.startPlay();
                    
                }
               if("RNP".equals(header)) {
            	   logger.log(Level.INFO, "Id joueur : "+idJoueur); 
                    this.startPlay();
                   
                }
               if("TLP".equals(header)) {
            	   terminateBot();
            	   break;
            	   
               }
            }
        }
    }
    
    public void terminateBot() {
    	logger.log(Level.INFO, "Fermeture du bot : " + botName);
        long pid = ProcessHandle.current().pid();

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String[] commandArray;

            if (os.contains("win")) {
                // Commande pour Windows
                commandArray = new String[]{"taskkill", "/PID", String.valueOf(pid), "/F"};
            } else {
                // Commande pour Linux/macOS
                commandArray = new String[]{"kill", "-SIGTERM", String.valueOf(pid)};
            }

            // Exécuter la commande avec exec(String[] cmdarray)
            Process process = Runtime.getRuntime().exec(commandArray);

            // Attendre la fin du processus
            int exitCode = process.waitFor();
            logger.log(Level.INFO, "Commande exécutée avec code de sortie : {0}", exitCode);
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Erreur lors de la fermeture du bot : " + e.getMessage(), e);
        }
    	
    	
    }

    
    /**
     * Traite les messages en attente dans la file d'attente.
     */
    public HashMap<String, Object> processMessages(String message) {
        HashMap<String, Object> dataExtracted = null;

        if (message != null) {
            dataExtracted = new HashMap<>();
            String messageType = gestionMessage.extractMessageType(message);

            switch (messageType) {
            	case "IP":
	                dataExtracted.put("header", "IP");
	                dataExtracted.putAll(gestionMessage.handleGameInitialization(message)); 
	                break;
                case "IM":
                    dataExtracted.put("header", "IM");
                    dataExtracted.putAll(gestionMessage.handleMatchInitialization(message)); 
                    break;
                case "DCJ":
                    dataExtracted.put("header", "DCJ");
                    dataExtracted.putAll(gestionMessage.handleCardDistribution(message));
                    break;
                case "ITP":
                    dataExtracted.put("header", "ITP");
                    dataExtracted.putAll(gestionMessage.handleRoundInitialization(message));
                    break;
                case "DCM":
                    dataExtracted.put("header", "DCM");
                    dataExtracted.putAll(gestionMessage.handleWeatherCardChoice(message));
                    break;
                case "ICR":
                    dataExtracted.put("header", "ICR");
                    dataExtracted.putAll(gestionMessage.handlePlayerAction(message));
                    break;
                case "CLC":
                    dataExtracted.put("header", "CLC");
                    dataExtracted.putAll(gestionMessage.handleChoiceClosure(message));
                    break;
                case "RCMB":
                    dataExtracted.put("header", "RCMB");
                    dataExtracted.putAll(gestionMessage.handleLowestTideCard(message));
                    break;
                case "RCMH":
                    dataExtracted.put("header", "RCMH");
                    dataExtracted.putAll(gestionMessage.handleHighestTideCard(message));
                    break;
                case "RCPB":
                    dataExtracted.put("header", "RCPB");
                    dataExtracted.putAll(gestionMessage.handleBuoyLoss(message));
                    break;
                case "IFM":
                    dataExtracted.put("header", "IFM");
                    dataExtracted.putAll(gestionMessage.handleEndOfRound(message));
                    break;
                case "IFP":
                    dataExtracted.put("header", "IFP");
                    dataExtracted.putAll(gestionMessage.handleEndOfGame(message));
                    break;
                case "TLP":
                	terminateBot();
                	break;
                case "ADJ":
                	terminateBot();
                	break;
                case "RNP":
                	dataExtracted.put("header", "RNP");
                	break;
                case "ICMJ":
                	dataExtracted.put("header", "ICMJ");
                	dataExtracted.putAll(gestionMessage.handlePlayedWeatherCards(message));
                	break;
                default:
                	logger.log(Level.INFO, message);
                	dataExtracted.put("header", "NULL");
                	logger.log(Level.WARNING, "Bot {0} : Message non reconnu ou non pris en charge", new Object[]{botName});
                    break;
            }
        }
        
        return dataExtracted;
    }
    
    public void sendMessage(String msg) {

    	connexionPartieBot.sendMessage(msg);

    }

	public static Logger getLogger() {
		// TODO Auto-generated method stub
		return logger;
	}

}
