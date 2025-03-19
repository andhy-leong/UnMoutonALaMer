package players.bots.botb;

import players.bots.common.BotTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import common.enumtype.PlayerType;

public class BotB extends BotTemplate {


    private static final Random random = new Random(); // Instance de Random initialisée ici


	public BotB(int verboseLevel) throws Exception {
        super(verboseLevel, PlayerType.BOTE); // Appel de la méthode pour générer le nom
        super.searchAndJoinGame();
        
    }

	public BotB(String name, int verboseLevel) throws Exception {
        super(name, verboseLevel, PlayerType.BOTE); // Appel de la méthode pour générer le nom
        super.searchAndJoinGame();
    }
	
    
    public void setName(String botName) {
        this.botName = "BOT_B_" + botName;
    }

    // Randomly generate the bot name in the format "BOT_F_XXXXXXXX"
    public String generateBotName() {
        String hexNumber = String.format("%08X", random.nextInt(0x10000000)); // Generates a value up to 0xFFFFFFF
        return "BOT_B_" + hexNumber; // Format: BOT_F_XXXXXXXX
    }

    @SuppressWarnings("unchecked")
	@Override
	public void startPlay() {
	    List<String> cartes = null;
	    BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
	    String carteMaree = null;
	    boolean jouerCarteHaute = false;
	    

	    // Thread pour recevoir les messages et les ajouter à la queue
	    Thread messageReceiver = new Thread(() -> {
	    	int msgNull = 0;
	        while (true) {
	            String msg = connexionPartieBot.getSocket().receiveMessage();
	            try {
	            	if(msg != null)
	            		messageQueue.put(msg); // Ajout du message dans la queue
	            	else
	            		msgNull++;
	            	if(msgNull >= 10) {
	            		BotTemplate.logger.log(Level.SEVERE, "PPTI fermé durant le jeu! Fermeture du bot");
	            		this.terminateBot();
	            	}
	            		
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                BotTemplate.logger.log(Level.SEVERE, "Message receiver interrupted");
	                break;
	            }
	        }
	    });
	    messageReceiver.start();
	    boolean play = true;
	    // Boucle principale de traitement des messages
	    while (play) {
	        try {
	            // Récupère un message de la queue ou le dernier message reçu s'il n'y en a pas
	            String msg = messageQueue.take();
	           
	            HashMap<String, Object> data = processMessages(msg);
	            if (data != null) {
	                switch ((String) data.get("header")) {
	                    case "DCJ":
	                        cartes = (List<String>) data.get("cartesmeteo");
	                        BotTemplate.logger.log(Level.INFO, "Liste des cartes reçues : "+cartes);
	                        break;

	                    case "RCMB":
	                    	if(((String) data.get("nomj")).equals(this.botName)) {
		                        String carteMareeTemp = (String) data.get("cartemaree");
		                        BotTemplate.logger.log(Level.INFO, "Carte marée la plus basse reçue");
		                        carteMaree = carteMareeTemp;
	                    	}
	                        break;
	                    case "RCMH":
	                    	if(((String) data.get("nomj")).equals(this.botName)) {
		                        String carteMareeTemp2 = (String) data.get("cartemaree");
		                       
		                        BotTemplate.logger.log(Level.INFO, "Carte marée la plus haute reçue");
		                        carteMaree = carteMareeTemp2;
	                    	}
	                        break;

	                    case "ITP":
	                        List<String> listCarteMaree = (List<String>) data.get("cartesmaree");
	                        BotTemplate.logger.log(Level.INFO, "Comparaison de la carte marée actuelle à celles distribuées");
	                        String cartePlusBasse = listCarteMaree.get(0).compareTo(listCarteMaree.get(1)) < 0 ? listCarteMaree.get(0) : listCarteMaree.get(1);
	                        String cartePlusHaute = listCarteMaree.get(0).compareTo(listCarteMaree.get(1)) < 0 ? listCarteMaree.get(1) : listCarteMaree.get(0);
	                        if(cartePlusBasse.compareTo("06") <= 0 && carteMaree == null) {
                        		jouerCarteHaute = true;
                        		
                        	}
	                        else if(carteMaree != null && carteMaree.compareTo(cartePlusBasse) > 0) {
	                        	
	                        	
	                        	if(carteMaree.compareTo(cartePlusBasse) > 0 && (Integer.valueOf(cartePlusHaute) - Integer.valueOf(cartePlusBasse) <= 3 || Integer.valueOf(carteMaree) - Integer.valueOf(cartePlusBasse) >= 5)) {
		                                jouerCarteHaute = true;
		                            }
		                        if(carteMaree.compareTo("04") <= 0 && !(Integer.valueOf(carteMaree) - Integer.valueOf(cartePlusBasse) <= 3)) {
		                        	jouerCarteHaute = false;
		                        	
		                        }
		                        if(cartePlusBasse.compareTo("01") == 0) {
		                        	jouerCarteHaute = true;
		                        	
		                        }
	                        	
	                        }
	                        break;

	                    case "DCM":
	                        if (cartes != null && !cartes.isEmpty()) {
	                            // Sélection de la carte à jouer selon la logique
	                            String carteChoisie = selectCardLogic(cartes, jouerCarteHaute);
	                            jouerCarteHaute = false;
	                            
	                            
	                            // Création du message de choix de carte
	                            String choixCarte = "<CCM cartemeteo=\"" + carteChoisie + "\" idp=\"" + data.get("idp") + 
	                                                "\" idm=\"" + data.get("idm") + "\" idt=\"" + data.get("idt") + "\"/>";
	                            
	                            BotTemplate.logger.log(Level.INFO, "Envoi de la carte : {0}", carteChoisie);
	                            connexionPartieBot.getSocket().sendMessage(choixCarte);
	                            cartes.remove(carteChoisie);
	                        }
	                        break;
	                    case "TLP":
	                    	play = false;
	                    	//messageReceiver.interrupt();
	                    	break;
	                    case "IFM":
	                    	carteMaree = null;
	                    	break;
	                    default:
	                        BotTemplate.logger.log(Level.WARNING, "Message non traité : {0}", data.get("header"));
	                        break;
	                }
	            }
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            BotTemplate.logger.log(Level.SEVERE, "Main loop interrupted");
	            break;
	        }
	    }
	}
    
    private String selectCardLogic(List<String> cartes, boolean jouerHauteCarte) {
    	String carteChoisie = null;
    	int valeurCarteMax = 0;
    	int valeurCarteMin = 60;
    	if(jouerHauteCarte) {
        	for(String carte : cartes){
        		
        		if(Integer.valueOf(carte.substring(1)) > valeurCarteMax) {
        			valeurCarteMax = Integer.valueOf(carte.substring(1));
        			carteChoisie = carte;
        			
        		}
        		
        	}
    		
    	}
    	else {
        	for(String carte : cartes){
        		
        		if(Integer.valueOf(carte.substring(1)) < valeurCarteMin) {
        			valeurCarteMin = Integer.valueOf(carte.substring(1));
        			carteChoisie = carte;
        			
        		}
        		
        	}
    		
    	}

    	
    	
		return carteChoisie;
    	
    	
    }
}
