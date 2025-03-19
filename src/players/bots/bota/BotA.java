package players.bots.bota;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import common.enumtype.PlayerType;
import players.bots.common.BotTemplate;

public class BotA extends BotTemplate {

    
    private static final Random random = new Random(); // Instance de Random initialisée ici

    
    public BotA(int verboseLevel) throws Exception {
        super(verboseLevel, PlayerType.BOTF); // Appel de la méthode pour générer le nom
        super.searchAndJoinGame();
    }
    public BotA(String name, int verboseLevel) throws Exception {
        super(name, verboseLevel, PlayerType.BOTF); // Appel de la méthode pour générer le nom
        super.searchAndJoinGame();
    }
    
    
    public void setName(String botName) {
        this.botName = "BOT_A_" + botName;
    }

    // Randomly generate the bot name in the format "BOT_F_XXXXXXXX"
    public String generateBotName() {
        String hexNumber = String.format("%08X", random.nextInt(0x10000000)); // Generates a value up to 0xFFFFFFF
        return "BOT_A_" + hexNumber; // Format: BOT_F_XXXXXXXX
    }

    @SuppressWarnings("unchecked")
	@Override
    public void startPlay() {
    	List<String> cartes = null;
    	int msgNull = 0;
        while(true) {
        	String msg = connexionPartieBot.getSocket().receiveMessage();
        	 
            if(msg == null)
                msgNull++;
            if(msgNull >= 10) {
                BotTemplate.logger.log(Level.SEVERE, "PPTI fermé durant le jeu! Fermeture du bot");
                this.terminateBot();
            }
        	HashMap<String, Object> data = processMessages(msg);
        	if(data != null) {
	        	if(data.get("header").equals("DCJ")){
	        		cartes = (List<String>) data.get("cartesmeteo");
	        		BotTemplate.logger.log(Level.INFO, "{0} : Liste des cartes reçues", new Object[] {botName});
	        		
	        	}
	        	if(data.get("header").equals("DCM")) {
	        		if (cartes != null && !cartes.isEmpty()) {
	        		    // Créez une instance de Random
	        		    Random random = new Random();
	
	        		    // Générez un index aléatoire dans la plage de la taille de la liste
	        		    int randomIndex = random.nextInt(cartes.size());
	
	        		    // Récupérez la carte à cet index
	        		    String carteChoisie = cartes.get(randomIndex);
	        		    cartes.remove(carteChoisie);
	        		    String choixCarte = "<CCM cartemeteo=\""+carteChoisie+"\" idp=\""+data.get("idp")+"\" idm=\""+data.get("idp")+"\" idt=\""+data.get("idt")+"\"/>";
	        		    BotTemplate.logger.log(Level.INFO, "{0} : Envoie de la carte {0}", new Object[] {botName,carteChoisie});
	        		    connexionPartieBot.getSocket().sendMessage(choixCarte);
	        		  
	        		}
	        	}
	        	if(data.get("header").equals("TLP")) {
	        		
	        		break;
	        		
	        	}
	        	
        	}
        	
        }
    }
}
