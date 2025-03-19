package players.idjr.model;

import common.enumtype.GameType;
import common.enumtype.PlayerType;
import common.reseau.udp.MulticastNetworkChatterDecoderPlayer;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class SearchForGameModel {
    private MulticastNetworkChatterDecoderPlayer chatter;

    private ObservableList<PartieInfo> listePartie;
    private final Thread tReceiveInfoGames;
    private final PlayerType pt;
    private String nom;
    private final int nbJoueurMax;

    public SearchForGameModel(PlayerType pt, int nbJoueurMax) throws Exception {
        this.nbJoueurMax = nbJoueurMax;
        listePartie = FXCollections.observableArrayList();
        chatter = new MulticastNetworkChatterDecoderPlayer(listePartie, nbJoueurMax);
        this.pt = pt;

        tReceiveInfoGames = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    String msg = chatter.receiveMessage();
                    //System.out.println(msg);
                    chatter.DecoderPlayer(msg);
                }
            }
        });

        tReceiveInfoGames.start();
        researchGame();

        listePartie.addListener((ListChangeListener<? super PartieInfo>) c -> {
            c.next();
            if(c.wasAdded()) {
                //System.out.println("ici");
                new JoinedGameModel((PartieInfo)c.getList().getLast(),nom);
                this.stop();
            }
        });

    }

    public void setNom(String nom) {this.nom = nom;}
    public ObservableList<PartieInfo> getListePartie() {return this.listePartie;}

    public void researchGame() {
        String msg = "<RP identite=\""+ pt +"\" typep=\""+ GameType.JR +"\" taillep=\""+ nbJoueurMax +"\">";
        chatter.sendMessage(msg);
    }

    public void stop() {
        chatter.close();
        tReceiveInfoGames.interrupt();
    }

}
