package players.bots.common;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.reseau.udp.MulticastNetworkChatter;
import common.reseau.udp.inforecup.PartieInfo;

public class BotMulticastNetworkChatter extends MulticastNetworkChatter implements AutoCloseable { // Implement AutoCloseable

    protected ArrayList<PartieInfo> availableParties;


    public BotMulticastNetworkChatter(ArrayList<PartieInfo> availableParties) throws Exception {
        super();
        this.availableParties = availableParties;
        ShutdownManager.getInstance().registerResource(this); // No cast needed now

    }

    @Override
    public void close() {
        // Add logic to release resources, if needed
        // For example, close any sockets or streams that were opened
        super.close(); // Call close on the parent class if it has a close method
    }

    public void decodeMessage(String msg) {
        if (msg != null) {
            String expHeader = "<(\\w+)\\s*";
            Pattern patternHeader = Pattern.compile(expHeader);
            Matcher matcherHeader = patternHeader.matcher(msg);
            String header = "";
            if (matcherHeader.find()) {
                header = matcherHeader.group(1);
            }

            if ("ACP".equals(header) || "AMAJP".equals(header)) {
                Pattern pattern = Pattern.compile("(\\w+)=\"([^\"]*)\"|(\\w+)=\"([\\d]+)\"");
                Matcher matcher = pattern.matcher(msg);

                String idp = null, ip = null, nomp = null, statut = null;
                int port = 0, nbj = 0, nbjrm = 0, nbjvm = 0;
                Integer nbjrc = null, nbjvc = null;
                int espa = 0;

                while (matcher.find()) {
                    if (matcher.group(1) != null) {
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
                                espa = Integer.parseInt(value); // Conversion de "1" en vrai ou faux
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
                    } 
                    
                }

                PartieInfo partie = (nbjrc != null && nbjvc != null)
                        ? new PartieInfo(idp, ip, port, nomp, nbj, nbjrm, nbjvm, espa, statut, nbjrc, nbjvc)
                        : new PartieInfo(idp, ip, port, nomp, nbj, nbjrm, nbjvm, espa, statut);
                if(statut.equals("ATTENTE")) {
                	
                	ajouterPartie(partie, nbj, nbjvc, nbjvm);
                }
                if(statut.equals("ANNULEE")) {
                	retirerPartie(partie);
                	
                }
            }
        }
    }
    private boolean testNbJ(int nbj) {
    	
    	return ((3 <= nbj) || ( nbj <= 5));
    	
    }
    
    private void ajouterPartie(PartieInfo partie,int nbj, Integer nbjvc, int nbjvm) {
    	
            if(nbjvc != null) {
                
                		if(testNbJ(nbj) && testNbJ(nbjvm) && nbjvc != nbjvm) {
                			availableParties.add(partie);
                		}
                		
                		if(testNbJ(nbj) && nbjvc != nbjvm && !testNbJ(nbjvm)) {
                			availableParties.add(partie);
                		}

                }	
            		
            
            else {
                
	            		if((testNbJ(nbj) && testNbJ(nbjvm))) {
	            			availableParties.add(partie);
	            			}
	            		
	            		if(testNbJ(nbj) && !testNbJ(nbjvm)) {
	            			availableParties.add(partie);
	            			
	            			}
	            		
            	
            	}
           
    	
    }
    private void retirerPartie(PartieInfo partie) {
    	
    	availableParties.remove(partie);
       
	
}
    

    public ArrayList<PartieInfo> getPartiesReçues() {
        return availableParties;
    }
}
