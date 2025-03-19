package ppti.model;

import common.Config;
import common.enumtype.Placement;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.KeyFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.reseau.udp.inforecup.PartieInfo;
import javafx.collections.ObservableMap;
import javafx.util.Duration;

import static java.lang.Thread.sleep;

public class GameScreenModel {
    private ObservableList<String> cartesMeteo;

    private BooleanProperty playerPlayed;
    private ArrayList<BooleanProperty> PlayedProperties;

    private Map<JoueurInfo, BlockingQueue<String>> finalPlayer;
    private ArrayList<CarteMeteo> carteMeteoDistrib;
    private ArrayList<JoueurInfo> JoueurOrdre;
    private ArrayList<JoueurInfo> carteAGaucheOrdre;
    private BlockingQueue<String> parentQ;
    private PartieInfo info;

    private ArrayList<String> carteMareePioche;
    private String[] currentCarteMaree = new String[2];

    private ObservableMap<String,JoueurInfo> idUsers;

    private BooleanProperty isEndGame;

    // Thread permettant de traiter les messages des joueurs (cartes à envoyer/ retirer)
    private final Thread tMsg;

    private boolean stopPli;
    private boolean stopManche;

    private IntegerProperty mancheProperty;
    private IntegerProperty mancheMaxProperty;
    private IntegerProperty pliProperty;
    private BooleanProperty showCard;
    private StringProperty MareeOne;
    private StringProperty MareeTwo;
    private BooleanProperty finManche;

    private Timeline timeChoose;
    private DoubleProperty secondsRemaining;
    private boolean timerStopped;

    private GameSaver saver;
    private ParentQueueTreatment parentQueueTraite;
    private EspionQueue esp;

    public GameScreenModel() throws Exception {
        this.finalPlayer = null;
        cartesMeteo = FXCollections.observableArrayList(
                "Y01N", "Y02N", "Y03G", "G20N", "G21N", "G23G", "B27N", "B29N", "B31N", "P38N", "P41G", "R56N"
        );

        playerPlayed = new SimpleBooleanProperty(false);
        PlayedProperties = new ArrayList<>();
        showCard = new SimpleBooleanProperty();

        MareeOne = new SimpleStringProperty();
        MareeTwo = new SimpleStringProperty();

        carteMeteoDistrib = new ArrayList<>();
        idUsers = FXCollections.observableHashMap();

        isEndGame = new SimpleBooleanProperty();
        finManche = new SimpleBooleanProperty(false);

        stopPli = true;
        stopManche = true;

        secondsRemaining = new SimpleDoubleProperty();
        timerStopped = false;

        carteMareePioche = new ArrayList<>();

        saver = GameSaver.getInstance();

        carteAGaucheOrdre = new ArrayList<>();

        mancheProperty = new SimpleIntegerProperty(0);
        mancheMaxProperty = new SimpleIntegerProperty(0);
        pliProperty = new SimpleIntegerProperty(0);
        carteAGaucheOrdre = new ArrayList<>();

        //TODO utiliser un PauseTransition

        tMsg = new Thread(() -> {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    String msg  = parentQ.take();
                    if (Config.DEBUG_MODE) {
                        System.out.println("GameScreenModel : " + msg);
                    }
                    decodeMessage(msg);
                }
            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
            }
        },"ThreadMessageGame GameScreenModel - Equipe3a");

    }

    /**
     * Cette méthode renvoie un nouveau paquet de carte mélangé de manière aléatoire
     */
    public void melangerCarte() {

        for(int i = 1; i <= 60;i++) {
            if(i <= 12)
                carteMeteoDistrib.add(new CarteMeteo("Y",i));
            else if (i <= 24)
                carteMeteoDistrib.add(new CarteMeteo("G",i));
            else if (i <= 36)
                carteMeteoDistrib.add(new CarteMeteo("B",i));
            else if (i <= 48)
                carteMeteoDistrib.add(new CarteMeteo("P",i));
            else
                carteMeteoDistrib.add(new CarteMeteo("R",i));
        }

        // on récupère le set pour en faire une liste temporaire
        ArrayList<CarteMeteo> carteMeteoDistribTemp = new ArrayList<>(carteMeteoDistrib);
        Random rand = new Random();

        // On vide la liste initiale pour éviter les doublons
        carteMeteoDistrib.clear();

        // On ajoute des cartes de manière aléatoire depuis la liste temporaire
        while (!carteMeteoDistribTemp.isEmpty()) {
            int randomIndex = rand.nextInt(carteMeteoDistribTemp.size());
            carteMeteoDistrib.add(carteMeteoDistribTemp.remove(randomIndex));
        }
    }

    public void piocheCarteMaree() {
        carteMareePioche.clear();

        for(int i = 1;i<=12;i++) {
            if(i < 10) {
                carteMareePioche.add("0"+i);
                carteMareePioche.add("0"+i);
            } else {
                carteMareePioche.add(""+i);
                carteMareePioche.add(""+i);
            }

        }

        Random rand = new Random();
        ArrayList<String> randMaree = (ArrayList<String>) carteMareePioche.clone();

        carteMareePioche.clear();

        while(!randMaree.isEmpty()) {
            int randomIndex = rand.nextInt(randMaree.size());
            carteMareePioche.add(randMaree.get(randomIndex));
            randMaree.remove(randomIndex);
        }


        /*
        for(int i = 0;i < 24;i++) {
            carteMareePioche.add("12");
        }
        */
    }

    /**
     * Cette méthode tire deux cartes marée au hasard
     */
    public void tirerCarteMaree() {
        currentCarteMaree[0] = carteMareePioche.removeFirst();
        MareeOne.set(currentCarteMaree[0]);
        currentCarteMaree[1] = carteMareePioche.removeFirst();
        MareeTwo.set(currentCarteMaree[1]);
    }

    public void lancementJeu() {
        if(!tMsg.isAlive())
            tMsg.start();
        melangerCarte();
        paquetInitialEspion();
        PauseTransition pauseStart = new PauseTransition(Duration.seconds(5));
        pauseStart.setOnFinished(event -> {
            jouerManche();
        });
        pauseStart.play();
    }

    private void jouerManche() {
        //messageIP();
        int manche = mancheProperty.get();
        mancheProperty.set(manche+1);

        piocheCarteMaree();

        envoieCarteJoueur();
        paquetMeteoRestantEspion(mancheProperty.get());
        initManche(mancheProperty.get());
        paquetMareeEspion();
        jouerPli();
    }

    private void jouerPli() {
        int pli = pliProperty.get();
        pliProperty.set(pli+1);
        showCard.set(false);
        tirerCarteMaree();

        initPli(mancheProperty.get(),pliProperty.get());
        demandeCarte(mancheProperty.get(),pliProperty.get());
    }

    public void continuePli() {
        stopPli = false;
        timerStopped = false;
        secondsRemaining.set(info.getVitesseDeJeu());
        showCard.set(true);
        
        for(JoueurInfo j : JoueurOrdre) {
        	j.unsetJouerProperty();
        }
        
        PauseTransition pauseShowCard = new PauseTransition(Duration.seconds(3));
        pauseShowCard.setOnFinished(event -> {
            finRecuCarte(mancheProperty.get(),pliProperty.get());
            carteJouee(mancheProperty.get(),pliProperty.get());
            distribCarteMaree(mancheProperty.get(),pliProperty.get());
            actionPli(mancheProperty.get(),pliProperty.get());
        });
        pauseShowCard.play();
    }
    public void continueManche() {
        int manche = mancheProperty.get();
        mancheProperty.set(manche+1);
        pliProperty.set(0);
        finManche.set(false);

        piocheCarteMaree();
        paquetMareeEspion();
        
        decaleCarte();
        

        initManche(mancheProperty.get());
        jouerPli();
    }

    /**
     * Cette méthode permet d'envoyer les messages IP à tout les utilisateurs connecté à la partie pour qu'ils puissent savoir
     * l'ordre de passage
     *
     */
    private void messageIP() {
        StringBuilder msg = new StringBuilder("<IP listej=\"");
        for(int i = 0;i < JoueurOrdre.size();i++) {
            if(i < JoueurOrdre.size() -1 )
                msg.append(JoueurOrdre.get(i).getNom()).append(",");
            else
                msg.append(JoueurOrdre.get(i).getNom());
        }
        msg.append("\" idp=\"").append(info.getId()).append("\"/>");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }
    }

    /**
     * Cette méthode une fois le message de l'ordre des joueurs lancé va envoyer à chaque joueur les cartes dont il a besoin pour jouer
     */
    private void envoieCarteJoueur() {
        double nbBoue = 0.;
        for(int i = 0; i < JoueurOrdre.size(); i++) {
            nbBoue = 0;
            StringBuilder msg = new StringBuilder("<DCJ cartesmeteo=\"");
            ArrayList<CarteMeteo> cartesJoueur = new ArrayList<>();

            for(int j = 0;j < 12;j++) {
                if(j < 11)
                    msg.append(carteMeteoDistrib.getFirst().getNom()).append(",");
                else
                    msg.append(carteMeteoDistrib.getFirst().getNom()).append("\" ");

                cartesJoueur.add(carteMeteoDistrib.getFirst());
                // ajout du nombre de bouée au joueur
                nbBoue += carteMeteoDistrib.getFirst().getNbBouee();

                //on retire la première carte pour qu'elle ne soit pas récupéré une seconde fois
                carteMeteoDistrib.removeFirst();
            }

            msg.append("idp=\"").append(info.getId()).append("\"/>");
            // on envoie le message des cartes à notre joueur à la place i
            finalPlayer.get(JoueurOrdre.get(i)).add(msg.toString());
            idUsers.get(JoueurOrdre.get(i).getIdp()).setCartes(cartesJoueur);
            // ajout du nombre de bouee du joueur
            int finalI = i;
            int finalBouee = (int)nbBoue;

            if (Config.DEBUG_MODE) {
                System.out.println("GameScreenModel : Bouee de joueur " + JoueurOrdre.get(i).getNom() + ", " + finalBouee);
            }

            idUsers.get(JoueurOrdre.get(finalI).getIdp()).setMaxBouee(finalBouee);
            idUsers.get(JoueurOrdre.get(finalI).getIdp()).setCurrentBouee(finalBouee);
            idUsers.get(JoueurOrdre.get(finalI).getIdp()).setNbBouee(finalBouee);
            esp.envoieMsg(msg.toString());
        }
    }
    
    //TODO
    private void paquetInitialEspion() {
    	StringBuilder msg = new StringBuilder("<IPME paquetmeteo=\"");
    	for(int i = 0; i < carteMeteoDistrib.size(); i++) {
    		if(i != carteMeteoDistrib.size()-1) 
    			msg.append(carteMeteoDistrib.get(i).getNom()+",");
    			
    		
    		
    		else
    			msg.append(carteMeteoDistrib.get(i).getNom()+"\"");
    		
    	}
    	msg.append(" idp=\"").append(info.getId()).append("\"/>");
    	esp.envoieMsg(msg.toString());
    	
    }
    
    private void paquetMareeEspion() {
    	StringBuilder msg = new StringBuilder("<IPMA piochemaree=\"");
    	for(int i = 0; i < carteMareePioche.size(); i++) {
    		if(i != carteMareePioche.size()-1) 
    			msg.append(carteMareePioche.get(i)+",");
    			
    		
    		
    		else
    			msg.append(carteMareePioche.get(i)+"\"");
    		
    	}
    	msg.append(" idp=\"").append(info.getId()).append("\"/>");
    	esp.envoieMsg(msg.toString());
    	
    }
    
    private void paquetMeteoRestantEspion(int idm) {
    	StringBuilder msg = new StringBuilder("<ICMR paquetmeteo=\"");
    	for(int i = 0; i < carteMeteoDistrib.size(); i++) {
    		if(i != carteMeteoDistrib.size()-1) 
    			msg.append(carteMeteoDistrib.get(i).getNom()+",");
    			
    		
    		
    		else
    			msg.append(carteMeteoDistrib.get(i).getNom()+"\"");
    		
    	}
    	msg.append(" idp=\"").append(info.getId()).append("\"");
    	msg.append(" idm=\"M").append(idm < 10 ? "0"+idm : idm).append("\"/>");
    	esp.envoieMsg(msg.toString());
    	
    }
    private void joueurAJoueEspion(int idm, int idt, String idj, CarteMeteo carte) {
    	StringBuilder msg = new StringBuilder("<PVE nomj=\"");
    	msg.append(idj).append("\" ");
    	msg.append("cartemeteo=\"").append(carte.getNom()).append("\" ");
    	msg.append(" idp=\"").append(info.getId()).append("\"");
    	msg.append(" idm=\"M").append(idm < 10 ? "0"+idm : idm).append("\"");
    	msg.append(" idt=\"T").append(idt < 10 ? "0"+idt : idt).append("\"/>");
    	esp.envoieMsg(msg.toString());
    }

    /**
     * Cette methode permet d'initialiser une manche avec l'id de la manche
     * @param idm id de la manche actuelle
     */
    private void initManche(int idm) {
        String msg = "<IM idm=\"M0"+idm+"\" idp=\""+info.getId()+"\"/>";
        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg);
        }
        esp.envoieMsg(msg);
    }

    /**
     * Cette méthode permet d'initialiser un tour en donnant toutes les informations importantes (carte marée, bouée)
     * @param idm id de la manche
     * @param idt id du tour
     */
    private void initPli(int idm, int idt) {
        StringBuilder msg = new StringBuilder("<ITP");
        StringBuilder nbbouees = new StringBuilder(" nbbouees=\"");
        StringBuilder cartemareejoueur = new StringBuilder(" cartemareejoueur=\"");
        for(int i = 0;i < JoueurOrdre.size();i++) {

            JoueurInfo j = idUsers.get(JoueurOrdre.get(i).getIdp());

            if(i < JoueurOrdre.size() - 1)
                nbbouees.append(j.getCurrentBouee().toString()).append(",");
            else
                nbbouees.append(j.getCurrentBouee().toString()).append("\"");

            if(i < JoueurOrdre.size() - 1)
                cartemareejoueur.append(j.getCurrentCarteMaree()).append(",");
            else
                cartemareejoueur.append(j.getCurrentCarteMaree()).append("\"");
        }

        msg.append(nbbouees).append(cartemareejoueur);

        msg.append(" idt=\"").append("T").append(idt < 10 ? "0"+idt : idt).append("\"");
        msg.append(" cartesmaree=\"").append(currentCarteMaree[0]).append(",").append(currentCarteMaree[1]).append("\"");
        msg.append(" idp=\"").append(info.getId()).append("\"");
        msg.append(" idm=\"M").append(idm < 12 ? "0"+idm : idm).append("\"/>");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }
        esp.envoieMsg(msg.toString());
    }

    /**
     * Cette methode permet d'envoyer un message a tout les joueurs indiquant la fin de la manche actuelle
     * @param idm
     */
    private void initFinManche(int idm) {
        StringBuilder msg = new StringBuilder("<IFM");
        StringBuilder scores = new StringBuilder(" scores=\"");

        JoueurInfo[] topPlayers = searchForSmallestMaree();

        for(JoueurInfo j : JoueurOrdre) {
            idUsers.get(j.getIdp()).setScoreManche(idUsers.get(j.getIdp()).getCurrentBouee());
        }

        if(topPlayers[0] != null )
            idUsers.get(topPlayers[0].getIdp()).setScoreManche(idUsers.get(topPlayers[0].getIdp()).getScoreManche() + 1);

        if(topPlayers[1] != null )
            idUsers.get(topPlayers[1].getIdp()).setScoreManche(idUsers.get(topPlayers[1].getIdp()).getScoreManche() + 1);

        for(JoueurInfo j : JoueurOrdre) {
            int point = idUsers.get(j.getIdp()).getScoreManche();
            if(point < 10 && point > -1)
                scores.append("0").append(point).append(",");
            else
                scores.append(point).append(",");

            idUsers.get(j.getIdp()).addScore(point);
        }

        scores.deleteCharAt(scores.length() -1).append("\"");

        msg.append(scores);

        msg.append(" idp=\"").append(info.getId()).append("\"");
        msg.append(" idm=\"M0").append(idm).append("\"").append(" />");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }
        esp.envoieMsg(msg.toString());
    }

    private void finPartie() {
        StringBuilder msg = new StringBuilder("<IFP");
        msg.append(" idp=\"").append(info.getId()).append("\"/>");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
            int point = idUsers.get(j.getIdp()).getCurrentBouee();
            idUsers.get(j.getIdp()).addScore(point);
        }
        esp.envoieMsg(msg.toString());

        //saver.writeSaveFile(info,idUsers);
        saver.startFile(info);
        saver.playerScore(idUsers,JoueurOrdre);
        saver.writeFile(info);
    }

    public void terminerParite() {
        StringBuilder msg = new StringBuilder("<TLP");
        msg.append(" idp=\"").append(info.getId()).append("\"/>");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }
        esp.envoieMsg(msg.toString());
    }

    public void replayGame() {
        StringBuilder msg = new StringBuilder("<RNP");
        Random r = new Random();
        String newId = "P" + r.nextInt(0,9_999_999);
        msg.append(" idp=\"").append(info.getId()).append("\"");
        msg.append(" idnp=\"").append(newId).append("\" />");

        info.setIdGame(newId);
        mancheProperty.set(0);
        pliProperty.set(0);
        isEndGame.set(false);
        finManche.set(false);

        for(JoueurInfo j : JoueurOrdre) {
            idUsers.get(j.getIdp()).setScore(0);
            idUsers.get(j.getIdp()).replay();
            finalPlayer.get(j).add(msg.toString());
        }
        esp.envoieMsg(msg.toString());
        lancementJeu();
    }

    /**
     * Cette méthode permet d'envoyer un message disant au joueur qu'ils peuvent choisir une carte
     * @param idm id de la manche
     * @param idt id du tour
     */
    private void demandeCarte(int idm,int idt) {
        StringBuilder msg = new StringBuilder("<DCM idp=\"");
        msg.append(info.getId()).append("\" idm=\"M");
        msg.append(idm < 12 ? "0"+idm : idm);
        msg.append("\" idt=\"T");
        msg.append(idt < 10 ? "0"+idt : idt);
        msg.append("\"/>");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }
    }

    /**
     * Cette méthode permet d'envoyer le message de fin de reçu des cartes par les joueurs
     * @param idm id de la manche
     * @param idt id du tour
     */
    private void finRecuCarte(int idm,int idt) {
        StringBuilder msg = new StringBuilder("<CLC idp=\"");
        msg.append(info.getId()).append("\"");
        msg.append(" idm=\"M").append(idm < 12 ? "0"+idm : idm).append("\"");
        msg.append(" idt=\"T").append(idt < 10 ? "0"+idt : idt).append("\"/>");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }

        for(JoueurInfo j : idUsers.values()) {
            if(!j.getEstElimine())
                idUsers.get(j.getIdp()).aJouerProperty().set(false);
        }
    }
    
    private void carteJouee(int idm,int idt) {
        StringBuilder msg = new StringBuilder("<ICMJ listecartemeteo=\"");
        for(JoueurInfo j : JoueurOrdre) {
            msg.append(idUsers.get(j.getIdp()).getPlayedCard().getNom()).append(",");
        }

        msg.deleteCharAt(msg.length()-1).append("\"");
        msg.append(" idp=\"").append(info.getId()).append("\"");
        msg.append(" idm=\"M").append(idm < 12 ? "0"+idm : idm).append("\"");
        msg.append(" idt=\"T").append(idt < 10 ? "0"+idt : idt).append("\"/>");

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }
        esp.envoieMsg(msg.toString());
    }

    /**
     * Cette methode permet de distribuer les cartes marees au deux joueurs ayant les cartes les plus hautes
     * @param idm
     * @param idt
     */
    private void distribCarteMaree(int idm,int idt) {
        StringBuilder recuBasse = new StringBuilder("<RCMB nomj=\"");
        StringBuilder recuHaute = new StringBuilder("<RCMH nomj=\"");

        JoueurInfo plusHaut = null;
        JoueurInfo secondHaut = null;

        String hautMaree = "";
        String secondeMaree = "";

        if(Integer.parseInt(currentCarteMaree[0]) > Integer.parseInt(currentCarteMaree[1])) {
            hautMaree = currentCarteMaree[0];
            secondeMaree = currentCarteMaree[1];
        } else if (Integer.parseInt(currentCarteMaree[0]) < Integer.parseInt(currentCarteMaree[1])) {
            hautMaree = currentCarteMaree[1];
            secondeMaree = currentCarteMaree[0];
        }else {
            hautMaree = currentCarteMaree[0];
            secondeMaree = currentCarteMaree[1];
        }

        for(JoueurInfo joueur : idUsers.values()) {
            int valeur = joueur.getPlayedCard().getValeur();

            if(plusHaut == null) {
                plusHaut = joueur;
            } else if (valeur > plusHaut.getPlayedCard().getValeur() ) {
                secondHaut = plusHaut;
                plusHaut = joueur;
            }else if (secondHaut == null){
                secondHaut = joueur;
            }else if(valeur > secondHaut.getPlayedCard().getValeur()) {
                secondHaut = joueur;
            }
        }

        idUsers.get(plusHaut.getIdp()).setCurrentCarteMaree(secondeMaree);
        idUsers.get(secondHaut.getIdp()).setCurrentCarteMaree(hautMaree);

        recuBasse.append(plusHaut.getNom()).append("\" cartemaree=\"");
        recuBasse.append(idUsers.get(plusHaut.getIdp()).getCurrentCarteMaree()).append("\" idp=\"");
        recuBasse.append(info.getId()).append("\" idm=\"M");
        recuBasse.append(idm < 12 ? "0"+idm : idm).append("\" idt=\"T");
        recuBasse.append(idt < 10 ? "0"+idt : idt).append("\" />");

        recuHaute.append(secondHaut.getNom()).append("\" cartemaree=\"");
        recuHaute.append(idUsers.get(secondHaut.getIdp()).getCurrentCarteMaree()).append("\" idp=\"");
        recuHaute.append(info.getId()).append("\" idm=\"M");
        recuHaute.append(idm < 12 ? "0"+idm : idm).append("\" idt=\"T");
        recuHaute.append(idt < 10 ? "0"+idt : idt).append("\" />");

        if (Config.DEBUG_MODE) {
            System.out.println("Joueur ayant la carte maree la plus haute : " + secondHaut.getNom());
            System.out.println("Joueur ayant la carte maree la plus basse : " + plusHaut.getNom());
        }

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(recuHaute.toString());
            finalPlayer.get(j).add(recuBasse.toString());
            // on met les cartes dans la défausse
            idUsers.get(j.getIdp()).next();
        }
        esp.envoieMsg(recuHaute.toString());
        esp.envoieMsg(recuBasse.toString());
    }

    /**
     * Cette Methode permet d'envoyer le message contenant les actions faites durant ce tour, tout ce qui touche a la perte de bouee et l'elimination de joueur
     * @param idm
     * @param idt
     */
    private void actionPli(int idm,int idt) {
        StringBuilder msg = new StringBuilder("<RCPB");
        StringBuilder listej = new StringBuilder(" listej=\"");
        StringBuilder listeeffet = new StringBuilder(" listeeffet=\"");

        boolean cascade = true;

        while(cascade) {
            cascade = false;
            JoueurInfo[] topPlayers = searchForGreatestMaree();
            //System.out.println("Carte Maree de 0 " + topPlayers[0].getCurrentCarteMaree() + "\nCarte Maree de 1 " + (topPlayers[1] == null ? "pas de joueur 1" : topPlayers[1].getCurrentCarteMaree()));

            //dans le cas où tout les joueurs se sont fait éliminé dans la même manche
            if(topPlayers[0] != null) {
                int nbBoueeFirst = idUsers.get(topPlayers[0].getIdp()).getCurrentBouee();
                idUsers.get(topPlayers[0].getIdp()).setCurrentBouee(nbBoueeFirst - 1);
                if(idUsers.get(topPlayers[0].getIdp()).getEstElimine()) {
                    listej.append(topPlayers[0].getNom()).append(",");
                    listeeffet.append("E,");
                    cascade = true;
                }else {
                    listej.append(topPlayers[0].getNom()).append(",");
                    listeeffet.append("P,");
                }
            }


            if(topPlayers[1] != null) {
                int nbBoueeSecond = idUsers.get(topPlayers[1].getIdp()).getCurrentBouee();
                idUsers.get(topPlayers[1].getIdp()).setCurrentBouee(nbBoueeSecond - 1);

                if(idUsers.get(topPlayers[1].getIdp()).getEstElimine()) {
                    listej.append(topPlayers[1].getNom()).append(",");
                    listeeffet.append("E,");
                    cascade = true;
                }else {
                    listej.append(topPlayers[1].getNom()).append(",");
                    listeeffet.append("P,");
                }
            }
        }

        listej.deleteCharAt(listej.length() - 1).append("\"");
        listeeffet.deleteCharAt(listeeffet.length() - 1 ).append("\"");

        msg.append(listej).append(listeeffet);
        msg.append(" idp=\"").append(info.getId()).append("\"");
        msg.append(" idm=\"M").append(idm < 12 ? "0"+idm : idm).append("\"");
        msg.append(" idt=\"T").append(idt < 10 ? "0"+idt : idt).append("\"");
        msg.append(" />");

        if (Config.DEBUG_MODE) {
            System.out.println(msg);
        }

        for(JoueurInfo j : JoueurOrdre) {
            finalPlayer.get(j).add(msg.toString());
        }
        esp.envoieMsg(msg.toString());

        if(howManyAlive() <= 2 || pliProperty.get() == 12) {
            boolean isEnd = mancheProperty.get() == mancheMaxProperty.get();
            if(isEnd) {
                isEndGame.set(false);
                isEndGame.set(true);
            }else {
                isEndGame.set(true);
                isEndGame.set(false);
            }

            finManche.set(true);

            if(isEndGame.get()) {
                initFinManche(idm);
                finPartie();
            } else
                initFinManche(idm);

        } else {
            jouerPli();
        }
    }



    /**
     * Cette méthode permet de donner les cartes du joueurs présent à son suivant.
     */
    private void decaleCarte() {
        ArrayList<ArrayList<CarteMeteo>> cartes =  new ArrayList<>();
        int size = JoueurOrdre.size();
        // récupération de toute les cartes météos de chaque joueur
        for (JoueurInfo j : carteAGaucheOrdre) cartes.add(idUsers.get(j.getIdp()).nextRound());


        int index = size -1;
        for(int i = 0; i < size;i++) {
            idUsers.get(carteAGaucheOrdre.get(i).getIdp()).setCartes(cartes.get(index));
            index++;
            if(index >= size)
                index = 0;
        }

        for(JoueurInfo j : JoueurOrdre) {
            StringBuilder msg = new StringBuilder("<DCJ cartesmeteo=\"");
            double bouee = 0.;

            for(CarteMeteo cm : idUsers.get(j.getIdp()).getCartes()) {
                bouee += cm.getNbBouee();
                msg.append(cm.getNom()).append(",");
            }

            msg.deleteCharAt(msg.length()-1);
            msg.append("\"");
            msg.append(" idp=\"").append(info.getId()).append("\" />");

            idUsers.get(j.getIdp()).setMaxBouee((int)bouee);
            idUsers.get(j.getIdp()).setCurrentBouee((int)bouee);
            idUsers.get(j.getIdp()).setNbBouee((int)bouee);

            finalPlayer.get(j).add(msg.toString());
            esp.envoieMsg(msg.toString());
        }
    }

    /**
     * Regarde quel joueur a la plus grande carte maree et est en vie
     * @return retourne le joueur ayant la plus grande carte maree
     */
    private JoueurInfo[] searchForGreatestMaree() {
        JoueurInfo[] topPlayers = new JoueurInfo[2];
        topPlayers[0] = null;
        topPlayers[1] = null;

        for(JoueurInfo j : JoueurOrdre) {
            if(idUsers.get(j.getIdp()).getEstElimine()) continue;

            if(topPlayers[0] == null) {
                topPlayers[0] = idUsers.get(j.getIdp());
            }else if(Integer.parseInt(topPlayers[0].getCurrentCarteMaree()) == Integer.parseInt(idUsers.get(j.getIdp()).getCurrentCarteMaree())) {
                topPlayers[1] = idUsers.get(j.getIdp());
            }else if(Integer.parseInt(topPlayers[0].getCurrentCarteMaree()) < Integer.parseInt(idUsers.get(j.getIdp()).getCurrentCarteMaree())) {
                topPlayers[0] = idUsers.get(j.getIdp());
                topPlayers[1] = null;
            }
        }

        return topPlayers;
    }

    private JoueurInfo[] searchForSmallestMaree() {
        JoueurInfo[] topPlayers = new JoueurInfo[2];
        topPlayers[0] = null;
        topPlayers[1] = null;

        for(JoueurInfo j : JoueurOrdre) {
            if(idUsers.get(j.getIdp()).getEstElimine()) continue;

            if(topPlayers[0] == null) {
                topPlayers[0] = idUsers.get(j.getIdp());
            }else if(Integer.parseInt(topPlayers[0].getCurrentCarteMaree()) == Integer.parseInt(idUsers.get(j.getIdp()).getCurrentCarteMaree())) {
                topPlayers[1] = idUsers.get(j.getIdp());
            }else if(Integer.parseInt(topPlayers[0].getCurrentCarteMaree()) > Integer.parseInt(idUsers.get(j.getIdp()).getCurrentCarteMaree())) {
                topPlayers[0] = idUsers.get(j.getIdp());
                topPlayers[1] = null;
            }
        }

        return topPlayers;
    }

    /**
     * Cette methode permet de savoir le nombre de joueur encore en vie
     * @return le nombre de joueur encore en vie
     */
    private int howManyAlive() {
        int howMany = 0;

        for(JoueurInfo j : idUsers.values()) {
            if(!j.getEstElimine())
                howMany +=1;
        }

        return howMany;
    }

    /**
     * Methode permettant de lire des messages et les traiters en fonction de ce qu'ils sont et contiennent
     * @param msg message a traiter
     */
    private void decodeMessage(String msg) {
        if(msg != null) {
            Pattern pattern = Pattern.compile("<(\\w+)\\s*");
            Matcher matcher = pattern.matcher(msg);
            String header = "";

            if(matcher.find())
                header = matcher.group(1);

            String exp = "(\\w+)=\"([^\"]*)\"";
            pattern = Pattern.compile(exp);
            matcher = pattern.matcher(msg);
            String idJoueur = "";

            switch(header) {
                //message permettant de faire le choix d'une carte
                case "CCM":

                    String carte = "";

                    while(matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);

                        switch(key) {
                            case "cartemeteo" :
                                carte = value;
                                break;
                            case "idj" :
                                idJoueur = value;
                        }
                    }

                    boolean tricher = idUsers.get(idJoueur).setPlayedCard(carte);
                    if(!tricher)
                        parentQueueTraite.reinit();
                    else
                    	joueurAJoueEspion(mancheProperty.get(), pliProperty.get(), idUsers.get(idJoueur).getNom(), idUsers.get(idJoueur).getPlayedCard());

                    break;

                // message permettant de retirer la carte qui a ete jouee
                case "RCM" :
                    while(matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);

                        if (key.equals("idj"))
                            idJoueur = value;

                    }

                    idUsers.get(idJoueur).enleverCarte();
                    break;
            }
        }
    }

    private void stopTimer() {
        timeChoose.stop();

        if(playerPlayed.get())
            continuePli();
        else
            timerStopped = true;
    }
    
    public ObservableList<String> getWeatherCardsList() {
        return cartesMeteo;
    }
    public void setFinalPlayer(Map<JoueurInfo, BlockingQueue<String>> finalPlayer) {
        this.finalPlayer = finalPlayer;
    }

    public void setJoueurOrdreList(ArrayList<JoueurInfo> list) {
        JoueurOrdre = list;
    }
    public void setParentQ(ParentQueueTreatment queue) {
        parentQ = queue.getBlockingQueue();
        parentQueueTraite = queue;
    }
    public void setPartieInfo(PartieInfo pinfo) {
        info = pinfo;
        secondsRemaining.set(pinfo.getVitesseDeJeu());
        timeChoose = new Timeline(new KeyFrame(Duration.seconds(0.1),e-> {
            secondsRemaining.set(secondsRemaining.get() - 0.1);
            if(secondsRemaining.get() <= 0.0) {
                this.stopTimer();
            }
        }
        ));

        timeChoose.setCycleCount(Timeline.INDEFINITE);
    }
    public BooleanProperty PlayerPlayedProperty() {
        return playerPlayed;
    }
    public IntegerProperty mancheProperty() {return mancheProperty;}
    public IntegerProperty pliProperty() {return pliProperty;}
    public ObservableMap<String,JoueurInfo> getIdUsers() {
        return idUsers;
    }
    public BooleanProperty showCardProperty() {
        return showCard;
    }
    public StringProperty carteMareeOneProperty() {
        return MareeOne;
    }
    public StringProperty carteMareeTwoProperty() {
        return MareeTwo;
    }
    public BooleanProperty isEndGameProperty() {
        return isEndGame;
    }
    public IntegerProperty mancheMaxProperty() {return mancheMaxProperty;}
    public BooleanProperty finMancheProperty() {return finManche;}
    public DoubleProperty vitesseJeuProperty() {
        return this.secondsRemaining;
    }
    public void setEsp(EspionQueue esp) {
        this.esp = esp;
    }

    public void setPlace(ArrayList<JoueurInfo> list) {
        //TODO modifier pour que ça prenne le premier joueur de la liste et qu'à partir de lui les joueurs suviant soient séléctionné dans l'ordre des aiguilles d'une montre
        carteAGaucheOrdre = new ArrayList<>(); // Initialize here
        Placement[] OrdrePlacement = {Placement.TOP_LEFT,Placement.TOP_MIDDLE,Placement.TOP_RIGHT,Placement.RIGHT_TOP,Placement.RIGHT_BOTTOM,Placement.BOTTOM_RIGHT,Placement.BOTTOM_MIDDLE,Placement.BOTTOM_LEFT,Placement.LEFT_BOTTOM,Placement.LEFT_TOP};
        for(int i = 0;i < OrdrePlacement.length;i++) {
            for(JoueurInfo j : list) {
                if(OrdrePlacement[i].equals(j.getPlacement())) {
                    carteAGaucheOrdre.add(j);
                    //System.out.println(j.getNom());
                }
            }
        }


        //JoueurOrdre = ordre;

        mancheMaxProperty.set(JoueurOrdre.size());

        for (JoueurInfo joueurInfo : list) {
            joueurInfo.setProperty();
            idUsers.put(joueurInfo.getIdp(), joueurInfo);
            PlayedProperties.add(idUsers.get(joueurInfo.getIdp()).aJouerProperty());
        }

        playerPlayed.bind(Bindings.createBooleanBinding(
                () -> PlayedProperties.stream().allMatch(BooleanProperty::get),PlayedProperties.toArray(new BooleanProperty[0])
        ));

        playerPlayed.addListener((observable,oldValue,newValue) -> {
            if(newValue && !timerStopped)
                timeChoose.play();
            else if(newValue && timerStopped)
                continuePli();
        });
    }
}