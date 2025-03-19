package ppti.model;


import common.Config;
import common.enumtype.GameType;
import common.enumtype.PlayerType;
import common.reseau.udp.inforecup.PartieInfo;

import ppti.view.singleton.SingletonView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import javafx.animation.PauseTransition;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.util.Duration;

import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerConnectionModel {

    private PartieInfo partieInfo;
    private MulticastNetworkChatterDecoderPPTI chatter;
    private ServerSocket server;
    private Thread tUserConnect;
    private Thread tMulticastReceiver;

    private BlockingQueue<Entry<JoueurInfo,BlockingQueue<String>>> waitingQueue;

    private final Runnable rUserConnect;
    private final Runnable rMulticastReceiver;
    private GameType typep;

    private ArrayList<String> idUsed;

    private Random r;

    private BooleanProperty networkLaunch;

    private ParentQueueTreatment parentQT;
    private EspionQueue esp;

    //TODO a vider quand les joueurs retourne sur le menu principale
    private ArrayList<ClientHandler> parties;
    //TODO a recuperer
    private BlockingQueue<String> parentQueue;
    // Map permettant de parler à un enfant précis durant la phase de connexion
    private ObservableMap<JoueurInfo,BlockingQueue<String>> childQueue;
    //cette Map contiendra les joueurs finaux pour la partie
    private Map<JoueurInfo,BlockingQueue<String>> finalPlayer;

    //cet objet nous permet de regarder si la partie info a ete modifier et de la mettre dans ce model
    private ObjectProperty<PartieInfo> info;
    private boolean isSet;

    private ArrayList<String> nomJoueur;

    public PlayerConnectionModel(PartieInfo partie,SingletonView singleton) throws Exception {
        idUsed = new ArrayList<>();
        isSet = false;
        info = new SimpleObjectProperty<PartieInfo>();
        waitingQueue = new LinkedBlockingQueue<>();
        nomJoueur = new ArrayList<>();
        info.addListener((observable, oldValue, newValue) ->  {
            if(newValue.getNombreJoueurVirtuelMax() == 0) {
                this.typep = GameType.JR;
            } else if (newValue.getNombreJoueurVirtuelMax() == newValue.getNombreJoueurMax()) {
                this.typep = GameType.BOT;
            }else {
                this.typep = GameType.MIX;
            }

            this.setMultiCast(newValue);
        });

        networkLaunch = new SimpleBooleanProperty(false);

        this.partieInfo = partie;
        r = new Random();

        rUserConnect = new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        //on attend une connexion
                        Socket s  = server.accept();

                        //une fois une connexion récupérée un prend le premier message envoyé par l'utilisateur
                        String msg = new BufferedReader(new InputStreamReader(s.getInputStream())).readLine();

                        //on découpe ce message avec un pattern qui donnera deux groupe différent
                        Pattern pattern = Pattern.compile("(\\w+)=\"([^\"]*)\"|(\\w+)=([\\d]+)");
                        Matcher matcher = pattern.matcher(msg);

                        //ces variables nous permettrons de récupérer les informations essentiel du joueur
                        String nom = "";
                        String identite = null;

                        while(matcher.find()) {
                            String option = matcher.group(1);
                            String value = matcher.group(2);
                            switch(option) {
                                case "nomj":
                                    nom = value;
                                    break;
                                case "identite":
                                    identite = value;
                                    break;
                            }
                        }

                        //ici on filtre pour pouvoir avoir un JoueurInfo ayant de bonne information concernant notre joueur
                        if(identite != null) {

                            PlayerType type = PlayerType.valueOf(identite);

                            JoueurInfo joueur = new JoueurInfo(nom,type);
                            String idJoueur = null;
                            // on boucle jusqu'à avoir un id non utilisé
                            do {
                                idJoueur = "J" + r.nextInt(0,9999);
                                joueur.setIdp(idJoueur);
                            }while(idUsed.contains(idJoueur));

                            idUsed.add(idJoueur);
                            //on créé un nouveau ClientHandler pour pouvoir en suite déconnecter/accepter notre utilisateur
                            ClientHandler client = new ClientHandler(s,parentQueue);

                            if(nomJoueur.contains(nom)) {
                                client.getChildQueue().add("<RDP idp=\"" + partieInfo.getId() + "\" />");
                                continue;
                            }else {
                                nomJoueur.add(nom);
                            }

                            if(type.equals(PlayerType.JR) || type.toString().startsWith("BOT")){
                                //on l'ajoute à notre ObservableList pour qu'il puissse s'afficher sur l'écran du PPTI
                                parties.add(client);

                                parties.getLast().setJoueurInfo(joueur);
                                ajouterJoueur(joueur,parties.getLast().getChildQueue());
                                chatter.sendMessage(partieInfo.AMAJPMessage());
                            }else if(type.equals(PlayerType.ESP)) {
                                //partie espion
                                client.setJoueurInfo(joueur);
                                esp.addEspion(client,info.get(),joueur);
                            }
                        }

                    } catch (IOException e) {
                        if (Config.DEBUG_MODE) {
                            System.out.println("Socket fermé");
                        }
                        // le socket se ferme pour pouvoir regarder s'il ne doit pas s'arreter (permettant donc de fermer le reseau si besoin)
                    }
                }
            }
        };

        rMulticastReceiver = new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    try {
                        //on attend de recevoir un message
                        String msg = chatter.receiveMessage();
                        //on le traite
                        chatter.DecoderPPTI(msg);
                    } catch(Exception e) {
                        // pareil que pour le ServerSocket ici on ne fait rien car une exception est attendue pour que les threads
                        // puisse regarder s'ils doivent s'arreter
                    }
                }
            }
        };

        parentQueue = new LinkedBlockingQueue<String>();
        parentQT = new ParentQueueTreatment(parentQueue,singleton);
        esp = new EspionQueue();
        childQueue = FXCollections.observableMap(new HashMap<JoueurInfo,BlockingQueue<String>>());
        parties = new ArrayList<>();
        finalPlayer = new HashMap<>();

    }

    public ObservableMap<JoueurInfo, BlockingQueue<String>> getChildQueue() {
        return childQueue;
    }

    public void setMultiCast(PartieInfo partie) {
        // TODO trouver un moyen soit de faire redemarrer les chatter et server socket soit de recreer les objets pour etre en accord avec la partie info
        // isSet nous permet de savoir si les objets de reseau ont deja ete set une fois pour ne pas avoir a les recreer (ce qui pauserai des problemes)
        if(!isSet) {
            //on initialise les chatter ici pour ne pas avoir un mauvais port
            try {
                chatter = new MulticastNetworkChatterDecoderPPTI();
                //chatter.setTimeUp(500);

            }catch(Exception e) {
                e.printStackTrace();
            }

            isSet = true;
        }
        try {
            server = new ServerSocket(partie.getPort());
            //server.setSoTimeout(500);
        } catch (IOException e ) {
            //System.err.println("erreur dans la creation du socket PlayerConnectionModel");
        }

        //une fois toute finies on peut set les informations de notre chatter
        chatter.setInfo(partie);
        chatter.setGameType(typep);
        chatter.setTaillep(partie.getNombreJoueurMax());
        //puis on envoie le message de création de partie
        chatter.sendMessage(partie.ACPMessage());

        tUserConnect = new Thread(rUserConnect,"UserConnect PlayerConnectionModel - Equipe3a");
        tMulticastReceiver = new Thread(rMulticastReceiver,"MulticastReceiver PlayerConnectionModel - Equipe3a");

        //lancement des threads
        tUserConnect.start();
        tMulticastReceiver.start();

        networkLaunch.set(true);
    }

    public void ajouterJoueur(JoueurInfo joueur,BlockingQueue<String> queue) {
        if(childQueue.size() < partieInfo.getNombreJoueurMax()) {
            childQueue.put(joueur,queue);
        }else{
            waitingQueue.offer(new AbstractMap.SimpleEntry<>(joueur,queue));
        }
    }

    public void accepterJoueur(String idp) {
    	JoueurInfo joueurAccept = null;
        //on récupère le JoueurInfo du joueur a accepter
    	for(Entry<JoueurInfo,BlockingQueue<String>> e : this.childQueue.entrySet()) {
        	if(e.getKey().getIdp().equals(idp)) {
        		joueurAccept = e.getKey();
        	}
        }

        //on récupère la Queue de l'enfant pour pouvoir lui envoyer des messages
        BlockingQueue<String> childQ = this.childQueue.get(joueurAccept);

        //on regarde quel type de joueur on a récupéré
        if(joueurAccept.getPlayerType().equals(PlayerType.JR)) {
            //on regarde si le nombre de joueur réel est au max
        	if(partieInfo.getNombreCurrentJoueurReel() < partieInfo.getNombreJoueurReelMax()) {
        		//System.out.println("nombre joueur current : " + partie.getNombreCurrentJoueurReel() + "\nnombre joueur max " + partie.getNombreJoueurReelMax());
                //on envoie le message d'acceptation au joueur
                childQ.add("<ADP idp=\""+ partieInfo.getId()+"\" idj=\""+ joueurAccept.getIdp()+"\">");
                //on ajoute notre joueur à la finalQueue
                finalPlayer.put(joueurAccept,childQ);
            	partieInfo.setNombreCurrentJoueurReel(partieInfo.getNombreCurrentJoueurReel() + 1);
                if(partieInfo.getNombreCurrentJoueurBot() + partieInfo.getNombreCurrentJoueurReel() == partieInfo.getNombreJoueurReelMax())
                    partieInfo.setStatus("COMPLETE");
            	chatter.sendMessage(partieInfo.AMAJPMessage());
            }else {
                //si on est au max de joueur réel accepter on refuse automatiquement le dernier joueur
            	this.rejeterJoueur(idp);
            }
        }else if(!joueurAccept.getPlayerType().equals(PlayerType.JR) && ! joueurAccept.getPlayerType().equals(PlayerType.ESP)) {

            if(partieInfo.getNombreCurrentJoueurBot() < partieInfo.getNombreJoueurVirtuelMax()) {
                childQ.add("<ADP idp=\""+ partieInfo.getId()+"\" idj=\""+ joueurAccept.getIdp()+"\">");
                finalPlayer.put(joueurAccept,childQ);
            	partieInfo.setNombreCurrentJoueurBot(partieInfo.getNombreCurrentJoueurBot() + 1);
                if(partieInfo.getNombreCurrentJoueurBot() + partieInfo.getNombreCurrentJoueurReel() == partieInfo.getNombreJoueurReelMax())
                    partieInfo.setStatus("COMPLETE");
            	chatter.sendMessage(partieInfo.AMAJPMessage());
            }else {
            	this.rejeterJoueur(idp);
            }
        }
    }

    public void rejeterJoueur(String idp) {
        JoueurInfo joueurSupp = null;
        //on récupère notre joueur dans notre childQueue
        for(Entry<JoueurInfo,BlockingQueue<String>> e : this.childQueue.entrySet()) {
        	if(e.getKey().getIdp().equals(idp)) {
        		joueurSupp = e.getKey();
        	}
        }

        ClientHandler temp = null;
        idUsed.remove(joueurSupp.getIdp());

        //on lui envoie notre refus
        childQueue.get(joueurSupp).add("<RDP idp=\""+ partieInfo.getId()+"\">");
        //puis on le supprime de notre liste
        childQueue.remove(joueurSupp);

        int index = 0;
        for(int i = 0;i<parties.size();i++) {
            if(parties.get(i).getJoueurInfo().getIdp().equals(idp)) {
                temp = parties.get(i);
                break;
            }
        }

        final ClientHandler removed = temp;
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {;
            nomJoueur.remove(removed.getJoueurInfo().getNom());
            removed.stopClientHandler();
            parties.remove(removed);
        });
        pause.play();

        Entry<JoueurInfo,BlockingQueue<String>> suivant = waitingQueue.poll();
        if(suivant != null) {
            childQueue.put(suivant.getKey(),suivant.getValue());
        }
    }

    /**
     * Cette fonction permet de déconnecter tous les joueurs
     */
    public void decoTousJoueur() {
        this.clearClientList();
        for(Entry<JoueurInfo,BlockingQueue<String>> joueur : finalPlayer.entrySet()) {
            try {
                joueur.getValue().put("<ADJ idp=\"" + partieInfo.getId() + ">");
            } catch (InterruptedException e ) {}

            childQueue.remove(joueur.getKey());
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            for(ClientHandler client : parties) {
                client.stopClientHandler();
            }

            parties.clear();
            childQueue.clear();

            try{
                this.stop();
            } catch(IOException ex) {

            }

        });
        pause.play();
    }

    public void stop() throws IOException {
        chatter.close();
        server.close();

        tUserConnect.interrupt();
        tMulticastReceiver.interrupt();
        isSet = tUserConnect.isInterrupted() && tMulticastReceiver.isInterrupted();
    }

    public void suspend() throws InterruptedException{
        tUserConnect.interrupt();
        tMulticastReceiver.interrupt();

        tUserConnect = new Thread(rUserConnect,"UserConnect PlayerConnectionModel - Equipe3a");
        tMulticastReceiver = new Thread(rMulticastReceiver,"MulticastReceiver PlayerConnectionModel - Equipe3a");

        isSet = false;

        try {
            server.close();
        } catch( IOException e) {
            if (Config.DEBUG_MODE) {
                System.out.println("Socket fermer");
            }
        }

        networkLaunch.set(false);
    }

    public PartieInfo getPartieInfo() {
        return this.partieInfo;
    }
    public void setPartieInfo(PartieInfo partieInfo) {this.partieInfo = partieInfo;}
    public void bindToInfo(ObjectProperty<PartieInfo> info) {
        this.info.bind(info);
    }
    public BooleanProperty networkLaunchProperty() {
        return networkLaunch;
    }

    public ParentQueueTreatment getParentQueue() {
        return parentQT;
    }
    public EspionQueue getEspionQueue() {return esp;}

    public Map<JoueurInfo, BlockingQueue<String>> getFinalPlayer() {
        return this.finalPlayer;
    }

    public void cancel() {
        if(chatter != null) {
            partieInfo.setStatus("ANNULEE");
            chatter.sendMessage(partieInfo.AMAJPMessage());
        }
    }


    public void sendIp(ArrayList<JoueurInfo> JoueurOrdre) {
        StringBuilder msg = new StringBuilder("<IP listej=\"");
        for(int i = 0;i < JoueurOrdre.size();i++) {
            if(i < JoueurOrdre.size() -1 )
                msg.append(JoueurOrdre.get(i).getNom()).append(",");
            else
                msg.append(JoueurOrdre.get(i).getNom());
        }
        msg.append("\" idp=\"").append(partieInfo.getId()).append("\">");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }
        esp.envoieMsg(msg.toString());
    }

    public void clearClientList() {
        if(tUserConnect != null)
            tUserConnect.interrupt();
        if(tMulticastReceiver != null)
            tMulticastReceiver.interrupt();

        isSet = (tUserConnect == null ? false : tUserConnect.isInterrupted()) && (tMulticastReceiver == null ? false : tMulticastReceiver.isInterrupted());

        if(Config.DEBUG_MODE) {
            System.out.println("Thread arreter : rUser" + tUserConnect.isInterrupted() + "\nrMulticast" + tMulticastReceiver.isInterrupted());
        }

        Entry<JoueurInfo,BlockingQueue<String>> joueur = waitingQueue.poll();
        String msg = "<RDP idp=\"" + partieInfo.getId() + "\" />";
        ArrayList<ClientHandler> removedPlayer = new ArrayList<>();
        while(joueur!= null) {
            for(ClientHandler temp : parties) {
                if(temp.getJoueurInfo().equals(joueur.getKey())) {
                    temp.getChildQueue().add(msg);
                }
            }
            joueur = waitingQueue.poll();
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            while(!removedPlayer.isEmpty()) {
                ClientHandler temp = removedPlayer.removeFirst();
                temp.stopClientHandler();
                parties.remove(temp);
            }
        });
        pause.play();
    }


}
