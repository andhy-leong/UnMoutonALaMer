package players.bots.common;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import common.enumtype.GameType;
import common.enumtype.PlayerType;
import common.reseau.udp.inforecup.PartieInfo;

public class BotSearchForGameModel {
	
	protected static final Logger logger = BotTemplate.getLogger();
	
    private final BotMulticastNetworkChatter chatter;
    private final ArrayList<PartieInfo> listePartie;
    private final Thread tReceiveInfoGames;
    private final Thread tProcessQueue;
    private final BlockingQueue<String> messageQueue;
    public double timeSinceLastGameMsg = System.currentTimeMillis();

    public BotSearchForGameModel(PlayerType pt) throws Exception {
        this.listePartie = new ArrayList<PartieInfo>();
        this.chatter = new BotMulticastNetworkChatter(listePartie);
        this.messageQueue = new LinkedBlockingQueue<>();
        

        // Thread for receiving game information
        tReceiveInfoGames = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    String msg = chatter.receiveMessage();
                    if (msg != null) {
                        messageQueue.put(msg); // Add the message to the queue
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.SEVERE, e.getMessage());
                }
            }
        });

        // Thread for processing the queue
        tProcessQueue = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    // Take a message from the queue and process it
                    String msg = messageQueue.take(); // Blocks until a message is available
                    
                    logger.log(Level.FINE, msg);
                    String rpTest = extractHeader(msg);
                    if(!rpTest.equals("RP")) {
                    	chatter.decodeMessage(msg);
                    	timeSinceLastGameMsg = System.currentTimeMillis();
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.SEVERE, e.getMessage());
                }
            }
        });

        tReceiveInfoGames.start();
        tProcessQueue.start();
        initiateGameSearch(pt);

    }


    public ArrayList<PartieInfo> getListePartie() {
        return this.listePartie;
    }

    public void initiateGameSearch(PlayerType typeBot) {

	        String msg = "<RP identite=\"" + typeBot + "\" typep=\"" + GameType.MIX + "\" taillep=\""+ 5 +"\">";
	        chatter.sendMessage(msg);

    }
    
    private String extractHeader(String msg) {
        Pattern patternHeader = Pattern.compile("^<([A-Z]+)");
        Matcher matcherHeader = patternHeader.matcher(msg);
        return matcherHeader.find() ? matcherHeader.group(1) : "";
    }

    public void stop() {
        chatter.close();
        tReceiveInfoGames.interrupt();
        tProcessQueue.interrupt();
    }
}
