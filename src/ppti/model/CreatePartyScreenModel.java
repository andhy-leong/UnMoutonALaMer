package ppti.model;

import common.Config;
import common.reseau.udp.MulticastNetwork;
import common.reseau.udp.inforecup.PartieInfo;

import java.io.IOException;
import java.util.Random;

public class CreatePartyScreenModel {

    private MulticastNetworkChatterDecoderPPTI mnc;
    private PartieInfo info;
    private TCPConnectionServer server;

    private double vitesseJeu;
    private int nbJoueurReelMax;
    private int nbJoueurVirtuel;
    private int nbJoueurMax;
    private int port;
    private String idp;
    private int isThereSpy;
    private String nomp;

    private Random r;


    public CreatePartyScreenModel() {
        r = new Random();
        port = r.nextInt(4096,65_535);
        vitesseJeu = 0;
        nbJoueurReelMax = 0;
        nbJoueurVirtuel = 0;
        nbJoueurMax = 0;
        isThereSpy = 0;
        nomp = "";

        mnc = null;
        info = null;
    }

    public void setVitesseJeu(double v) {this.vitesseJeu = v;}

    public void setNbJoueurReelMax(int i) {
        this.nbJoueurReelMax = i;
        this.nbJoueurVirtuel = this.nbJoueurMax - nbJoueurReelMax;
    }
    //public void setNbJoueurVirtuel(int i) {this.nbJoueurVirtuel = i;}
    public void setNbJoueurMax(int i) {
        this.nbJoueurMax = i;
        this.nbJoueurVirtuel = i - nbJoueurReelMax;
    }
    public void setNomPartie(String nom) {this.nomp = nom;}
    public PartieInfo getPartieInfo() {return this.info;}

    public void setIsThereSpy(int newValue) {this.isThereSpy = newValue;}

    public void creerInfoPartie() {
        idp = "P" + r.nextInt(0,9_999_999);
        info = new PartieInfo(idp, MulticastNetwork.getIpAddressSocket(),port,nomp,nbJoueurMax,nbJoueurReelMax,nbJoueurVirtuel,isThereSpy,"ATTENTE");
        info.setVitesseDeJeu(vitesseJeu);
    }

    public void stop() {
        if(mnc != null)
            mnc.close();

        if(server != null) {
            try {
                server.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
