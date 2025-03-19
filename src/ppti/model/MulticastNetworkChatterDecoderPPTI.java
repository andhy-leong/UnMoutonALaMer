package ppti.model;

import common.Config;
import common.enumtype.GameType;
import common.reseau.udp.MulticastNetworkChatter;
import common.reseau.udp.inforecup.PartieInfo;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastNetworkChatterDecoderPPTI extends MulticastNetworkChatter {

    private GameType gt;
    private PartieInfo info;
    private int taillep;

    public MulticastNetworkChatterDecoderPPTI() throws Exception {
        super();
    }

    public void setInfo (PartieInfo info) {
        this.info = info;
    }
    public void setGameType(GameType gt) { this.gt = gt; }
    public void setTaillep(int taille) {this.taillep = taille; }

    public PartieInfo Info() {return this.info;}
    public GameType getGameType() { return this.gt;}
    public int getTaillep() { return this.taillep;}

    public PartieInfo DecoderPPTI(String msg) {
        if(msg != null) {

            String expHeader = "<(\\w+)\\s*";
            Pattern patternHeader = Pattern.compile(expHeader);
            Matcher matcherHeader = patternHeader.matcher(msg);
            String header = "";
            if(matcherHeader.find())
                header = matcherHeader.group(1);

            //System.out.println(header);

            String exp = "(\\w+)=\"([^\"]*)\"|(\\w+)=\"([\\d]+)\"";
            Pattern pattern = Pattern.compile(exp);
            Matcher matcher = pattern.matcher(msg);

            switch(header) {
                case "RP":

                    String identite = "";
                    String typePartie = "";
                    int taillep = 0;


                    while(matcher.find()) {
                        if(matcher.group(1) != null) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);
                            //System.out.println("key : " + key + " value : " + value);
                            switch(key) {
                                case "identite":
                                    identite = value;
                                    if (Config.DEBUG_MODE) {
                                        System.out.println(identite);
                                    }
                                    break;
                                case "typep":
                                    typePartie = value;
                                    if (Config.DEBUG_MODE) {
                                        System.out.println(typePartie);
                                    }
                                    break;
                                case "taillep":
                                    taillep = Integer.parseInt(value);
                                    if (Config.DEBUG_MODE) {
                                        System.out.println(taillep);
                                    }
                                    break;
                            }
                        } else if (matcher.group(3) != null) {
                            String key = matcher.group(3);
                            int value = Integer.parseInt(matcher.group(4));
                            switch(key) {

                            }
                        }
                    }

                    //System.out.println(this.gt);
                    this.sendMessage(info.AMAJPMessage());

                    break;

            }
            return null;
        }
        return null;
    }
}
