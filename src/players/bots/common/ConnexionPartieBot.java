package players.bots.common;

import common.Config;
import common.enumtype.PlayerType;
import common.reseau.tcp.TCPconnectionSocket;
import common.reseau.udp.inforecup.PartieInfo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnexionPartieBot {
	
	protected static final Logger logger = BotTemplate.getLogger();

    private PartieInfo info;
    TCPconnectionSocket socket;
    private Thread tReceiveMessage;
    private final Object stateLock = new Object();
    private String botName;
    public String idJoueur;
    private volatile ViewState currentState = ViewState.WAITING_AUTHORIZATION;
    private double timeWhenConnectionEstablished = System.currentTimeMillis();
    public enum ViewState {
        WAITING_AUTHORIZATION,
        ACCEPTED,
        REFUSED
    }

    public ConnexionPartieBot(PartieInfo info, String nom, PlayerType botType) {
        this.info = info;
        this.botName = nom;
        try {
            socket = new TCPconnectionSocket(info.getIp(), info.getPort());
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "Bot {0} : Erreur lors de la connexion au socket \n {1}", new Object[]{botName, e.getMessage()});
        }
        
        tReceiveMessage = new Thread(() -> {
            while (!Thread.interrupted()) {
                String msg = getSocket().receiveMessage();
                logger.log(Level.INFO, msg);
                String temp = decodeMessage(msg);
                if(temp != null)
                	this.idJoueur = temp;
                if(System.currentTimeMillis() - timeWhenConnectionEstablished >= 300000 && currentState.equals(ViewState.WAITING_AUTHORIZATION)) {
                	logger.log(Level.SEVERE, "Aucune réponse de la partie depuis 5 minutes, fermeture de la connexion");
                	decodeMessage("<RDP idp=\""+ info.getId()+"\">");
                	
                }

            }
        });
        if(socket != null) {
	        tReceiveMessage.start();
	        sendAcceptanceRequest(botType, nom);
        }
    }

    public void sendAcceptanceRequest(PlayerType botType, String nom) {
    	if(nom != null)
    		getSocket().sendMessage("<DCP nomj=\"" + nom + "\" identite=\"" + botType + "\" idp=\"" + info.getId() + "\">");
    	else
    		logger.log(Level.SEVERE, "Nom du bot null !");
    }

    public void sendMessage(String msg) {
    	
    	getSocket().sendMessage(msg);
    	
    }

    public String decodeMessage(String msg) {
    	String idj = null;
        if (msg != null) {
            String header = extractHeader(msg);
            synchronized (stateLock) {
                if (header.equals("ADP")) {
                    currentState = ViewState.ACCEPTED;
                    logger.log(Level.INFO, "Bot {0} : Accepté dans la partie {1}", new Object[]{botName, info.getId()});
                    tReceiveMessage.interrupt();
                    Pattern pattern = Pattern.compile("idj=\"(J\\d{1,4})\"");
                    Matcher matcher = pattern.matcher(msg);

                    if (matcher.find()) {
                        idj = matcher.group(1);
                        
                    }
                } else if (header.equals("RDP")) {
                	logger.log(Level.INFO, "Bot {0} : Refusé dans la partie {1}", new Object[]{botName, info.getId()});
                    currentState = ViewState.REFUSED;
                    closeConnection();
                }
                stateLock.notifyAll(); // Notify all threads waiting on this lock
                if(idj != null)
                	return idj; // Retourne l'identifiant du joueur
            }
        }
		return null;
    }
    


    public Object getStateLock() {
        return stateLock;
    }

    public ViewState getCurrentState() {
        return currentState;
    }

    private String extractHeader(String msg) {
        Pattern patternHeader = Pattern.compile("^<([A-Z]+)");
        Matcher matcherHeader = patternHeader.matcher(msg);
        return matcherHeader.find() ? matcherHeader.group(1) : "";
    }


    public void closeConnection() {
        try {
            socket.stop();
            tReceiveMessage.interrupt();
        } catch (IOException e) {
        	logger.log(Level.SEVERE, "Bot {0} : Erreur lors de la fermeture de la connexion! \n{1}", new Object[]{botName, e.getMessage()});
        }
    }

	public TCPconnectionSocket getSocket() {
		return socket;
	}
}
