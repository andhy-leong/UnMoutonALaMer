package common.reseau.udp;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.reseau.udp.MulticastNetworkChatter;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.collections.ObservableList;
import common.Config;

public class MulticastNetworkChatterDecoderPlayer extends MulticastNetworkChatter {

    private ObservableList<PartieInfo> parties;
    private int nbJoueurMax;

    public MulticastNetworkChatterDecoderPlayer(ObservableList<PartieInfo> listePartie, int nbJoueurMax) throws IOException, Exception {
        super();
        this.parties = listePartie;
        this.nbJoueurMax = nbJoueurMax;
    }

    public void DecoderPlayer(String msg) {
        if(msg != null) {
            String expHeader = "<(\\w+)\\s*";
            Pattern patternHeader = Pattern.compile(expHeader);
            Matcher matcherHeader = patternHeader.matcher(msg);
            String header = "";
            while(matcherHeader.find()) {
                if(matcherHeader.group(1) != null)
                    header = matcherHeader.group(1);
                break;
            }

            if (Config.DEBUG_MODE) {
                System.out.println("Message reçu: " + msg);
            }

            switch(header) {
                case "ACP" :
                case "AMAJP":
                    // on decoupe notre message de sorte a recuperer soit une entree avec une cle un egal des guillemets ou sans guillemets pour pouvoir tout recueperer
                    //les parantheses permettent d'avoir des groupes qui pourront etre manipuler durant la reucperation des informations
                    String exp = "(\\w+)=\"([^\"]*)\"|(\\w+)=\"([\\d]+)\"";
                    Pattern pattern = Pattern.compile(exp);
                    Matcher matcher = pattern.matcher(msg);

                    String idp = null, ip = null, nomp = null, statut = null;
                    int port = 0, nbj = 0, nbjrm = 0, nbjvm = 0;
                    Integer nbjrc = null, nbjvc = null;
                    int espa = 0;

                    while(matcher.find()) {
                        if(matcher.group(1) != null) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);

                            switch (key) {
                                case "idp":
                                    idp = value;
                                    break;
                                case "ip":
                                    ip = value;
                                    break;
                                case "nomp":
                                    nomp = value;
                                    break;
                                case "espa":
                                    espa = Integer.parseInt(value);
                                    break;
                                case "statut":
                                    statut = value;
                                    break;
                                case "port":
                                    port = Integer.parseInt(value);
                                    break;
                                case "nbj":
                                    nbj = Integer.parseInt(value);
                                    break;
                                case "nbjrm":
                                    nbjrm = Integer.parseInt(value);
                                    break;
                                case "nbjvm":
                                    nbjvm = Integer.parseInt(value);
                                    break;
                                case "nbjrc":
                                    nbjrc = Integer.parseInt(value);
                                    break;
                                case "nbjvc":
                                    nbjvc = Integer.parseInt(value);
                                    break;
                            }
                        }else if (matcher.group(3) != null) {
                            String key = matcher.group(3);
                            int value = Integer.parseInt(matcher.group(4));
                            switch (key) {
                            }
                        }
                    }

                    PartieInfo partie = null;
                    if(nbjrc != null && nbjvc != null)
                        partie = new PartieInfo(idp,ip,port,nomp,nbj,nbjrm,nbjvm,espa,statut,nbjrc,nbjvc);
                    else
                        partie = new PartieInfo(idp,ip,port,nomp,nbj,nbjrm,nbjvm,espa,statut);
                    
                    if (partie.getNombreJoueurMax() <= this.nbJoueurMax) {
                        if (Config.DEBUG_MODE) {
                            System.out.println(partie);
                        }
                        parties.add(partie);
                    }
            }
        }
    }
}
