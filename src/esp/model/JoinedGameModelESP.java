package esp.model;

import common.Config;
import common.enumtype.PlayerType;
import common.reseau.tcp.TCPconnectionSocket;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JoinedGameModelESP {

    private PartieInfo info;
    private String nom;
    private String nomPartieStr;
    private TCPconnectionSocket socket;

    private StringProperty nomPartie;
    private ObservableMap<String, ArrayList<String>> message;
    private BlockingQueue<String> messagePPTI;
    private Thread tNetworkReader;
    private Thread tMessageParser;

    public JoinedGameModelESP(PartieInfo info, String nom, ObservableMap<String,ArrayList<String>> message) {
        this.message = message;

        if (Config.DEBUG_MODE)
            System.out.println("creation de la connexion tcp");
        this.info = info;
        this.nom = nom;
        nomPartie = new SimpleStringProperty();
        if (info != null) {
            nomPartieStr = info.getNomPartie();
            nomPartie.set(nomPartieStr);
        }
        try {
            socket = new TCPconnectionSocket(info.getIp(), info.getPort());
        } catch(Exception e) {
            //System.err.println("Model : impossible de se connecter a l'hote");
        }

        messagePPTI = new LinkedBlockingQueue<>();

        sendAcceptationDemand();
        startNetworkReader();
        startMessageParser();
    }

    public void sendAcceptationDemand() {
        socket.sendMessage("<DCP nomj=\""+nom+"\" identite=\""+ PlayerType.ESP+"\" idp=\""+ info.getId()+"\">");
    }

    private void startNetworkReader() {
        tNetworkReader = new Thread(() -> {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    String msg =socket.receiveMessage();
                    messagePPTI.put(msg);
                }
            } catch(InterruptedException e) {

            }
        },"ThreadNetworkReader - Equipe3a");
        tNetworkReader.start();
    }

    private void startMessageParser() {
        tMessageParser = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);

                    if(!messagePPTI.isEmpty()) {
                        Platform.runLater(() -> {
                            int batchSize = Math.min(10,messagePPTI.size());
                            for(int i = 0; i < batchSize; i++) {
                                String msg = messagePPTI.poll();
                                if(message != null)
                                    parseMessage(msg);
                            }
                        });
                    }
                } catch (InterruptedException e) {

                }

            }
        },"ThreadMessageParser - Equipe3a");
       tMessageParser.start();
    }

    /**
     * Parse un message pour extraire le header et les valeurs des attributs.
     *
     * @param msg Le message à analyser
     */
    public void parseMessage(String msg) {
        //System.out.println(msg);
        // Expression régulière pour extraire le header
        Pattern headerPattern = Pattern.compile("<(\\w+)");
        Matcher headerMatcher = headerPattern.matcher(msg);

        // Expression régulière pour extraire les valeurs des attributs
        Pattern valuePattern = Pattern.compile("\\w+=\"([^\"]*)\"");

        if (headerMatcher.find()) {
            // Extraction du header
            String header = headerMatcher.group(1);

            // Liste pour stocker les valeurs
            ArrayList<String> values = new ArrayList<>();

            // Parcourir toutes les valeurs des attributs
            Matcher valueMatcher = valuePattern.matcher(msg);
            while (valueMatcher.find()) {
                values.add(valueMatcher.group(1)); // Ajouter chaque valeur dans la liste
            }
            if(Config.DEBUG_MODE)
                System.out.println("header :" + header + "\nvalues" + values);

            // Stocker dans la HashMap
            message.put(header, values);
        }
    }

    public ObservableValue<String> gameNameProperty() {
        return nomPartie;
    }
}