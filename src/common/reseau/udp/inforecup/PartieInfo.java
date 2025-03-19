package common.reseau.udp.inforecup;

import common.enumtype.GameType;
import common.enumtype.PlayerType;

/*
 *
 * Classe permettant de centraliser les informations de nos parties.
 * Elle permettra principalement de mettre a jour le tableau des informations des parties en nous donnant toutes les infromations
 * necessaire pour savoir si oui ou non on veut (ou peut) les rejoindres.
 * Elle permettra aussi a filtrer les informations (comme par exemple le faites que l'on recherche
 * des joueurs humain un robot peut ne pas tenir compte de ces informations).
 */
public class PartieInfo {

    private String id;
    private final String ip;
    private final int port;
    private String nomPartie;
    private int nombreJoueurMax;
    private int nombreJoueurReelMax;
    private int nombreJoueurVirtuelMax;
    private int espionAutorise;
    private String status;  //peut etre une enumeration
    private int nombreCurrentJoueurReel;
    private int nombreCurrentJoueurBot;
    private PlayerType[] virtualPlayersTypes;
    private GameType gametype;
    private String[] virtualPlayersName;
    private double vitesseDeJeu;


    public PartieInfo(String id,String ip,int port,String nomPartie,int nombreJoueurMax,int nombreJoueurReelMax,int nombreJoueurVirtuelMax,int espa,String status) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.nomPartie = nomPartie;
        this.nombreJoueurMax = nombreJoueurMax;
        this.nombreJoueurReelMax = nombreJoueurReelMax;
        this.nombreJoueurVirtuelMax = nombreJoueurVirtuelMax;
        this.espionAutorise = espa;
        this.status = status;
        this.nombreCurrentJoueurReel = 0;
        this.nombreCurrentJoueurBot = 0;
        this.setVirtualPlayersTypes(new PlayerType[nombreJoueurVirtuelMax]);
        this.virtualPlayersName = new String[nombreJoueurMax];
        this.vitesseDeJeu = 30;
    }

    public PartieInfo(String id,String ip,int port,String nomPartie,int nombreJoueurMax,int nombreJoueurReelMax,int nombreJoueurVirtuelMax,int espionAutorise,String status, Integer nbjrc, Integer nbjvc) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.nomPartie = nomPartie;
        this.nombreJoueurMax = nombreJoueurMax;
        this.nombreJoueurReelMax = nombreJoueurReelMax;
        this.nombreJoueurVirtuelMax = nombreJoueurVirtuelMax;
        this.espionAutorise = espionAutorise;
        this.status = status;
        this.nombreCurrentJoueurReel = nbjrc;
        this.nombreCurrentJoueurBot = nbjvc;
        this.virtualPlayersTypes = new PlayerType[nombreJoueurVirtuelMax];
        this.virtualPlayersName = new String[nombreJoueurMax];
        this.vitesseDeJeu = 30;
    }

    
    //----- GETTERS -----//
    public String getId(){
    	return id;
    }
    
    public String getIp(){
    	return ip;
    }
    
    public int getPort(){
    	return port;
    }
    public String getNomPartie(){
    	return nomPartie;
    }
    
    public int getNombreJoueurMax(){
    	return nombreJoueurMax;
    }
    
    public int getNombreJoueurReelMax(){
    	return nombreJoueurReelMax;
    }
    
    public int getNombreJoueurVirtuelMax(){
    	return nombreJoueurVirtuelMax;
    }
    
    public int getEspionAutorise(){
    	return espionAutorise;
    }
    
    public String getStatus(){
    	return status;
    }
    
    public int getNombreCurrentJoueurReel(){
    	return nombreCurrentJoueurReel;
    }
    
    public int getNombreCurrentJoueurBot(){
    	return nombreCurrentJoueurBot;
    }
    
    public PlayerType[] getVirtualPlayersTypes(){
    	return virtualPlayersTypes;
    }
    
    public GameType getGametype(){
    	return this.gametype;
    }
    
    public String[] getVirtualPlayersName(){
    	return this.virtualPlayersName;
    }

    public double getVitesseDeJeu() {
        return this.vitesseDeJeu;
    }
    

    //----- SETTERS -----//
    public void setNomPartie (String nomPartie){
    	this.nomPartie = nomPartie;
    }
    
    public void setNombreJoueurMax(int nombreJoueurCurrent){
    	this.nombreJoueurMax = nombreJoueurCurrent;
    }
    
    public void setNombreJoueurReelMax(int nombreJoueurMax){
    	this.nombreJoueurReelMax = nombreJoueurMax;
    }
    
    public void setNombreJoueurVirtuelMax(int nombreJoueurVirtuelMax){
    	this.nombreJoueurVirtuelMax = nombreJoueurVirtuelMax;
    }
    
    public void setEspionAutorise(int espionAutorise){
    	this.espionAutorise = espionAutorise;
    }
    
    public void setStatus(String status){
    	this.status = status;
    }
    
    public void setNombreCurrentJoueurReel(int nombreCurrentJoueurReel){
    	this.nombreCurrentJoueurReel = nombreCurrentJoueurReel;
    }
    
    public void setNombreCurrentJoueurBot(int nombreCurrentJoueurBot){
    	this.nombreCurrentJoueurBot = nombreCurrentJoueurBot;
    }
    
    public void setVirtualPlayersTypes(PlayerType[] VirtualPlayersTypes){
    	this.virtualPlayersTypes = VirtualPlayersTypes;
    }

    public void setVitesseDeJeu(double vitesseDeJeu) {
        this.vitesseDeJeu = vitesseDeJeu;
    }
    
    public void setVirtualPlayerType(PlayerType type, int i) {
        this.virtualPlayersTypes[i] = type;
    }
    public void setIdGame(String newId) {this.id = newId;}
    public void setGameType(GameType game) {this.gametype = game;}
    public void setVirtualPlayerName(String name, int i) {this.virtualPlayersName[i] = name;}

    public String ACPMessage() {
        String msg = "<ACP ";

        msg += "idp=\""+id+"\" ";
        msg += "ip=\""+ip+"\" ";
        msg += "port=\""+port+"\" ";
        msg += "nomp=\""+nomPartie+"\" ";
        msg += "nbj=\""+nombreJoueurMax+"\" ";
        msg += "nbjrm=\""+nombreJoueurReelMax+"\" ";
        msg += "nbjvm=\""+nombreJoueurVirtuelMax+"\" ";
        msg += "espa=\""+espionAutorise+"\" ";
        msg += "statut=\""+status+"\"";

        msg+=">";
        return msg;
    }

    public String AMAJPMessage() {
        String msg = "<AMAJP ";

        msg += "idp=\""+id+"\" ";
        msg += "ip=\""+ip+"\" ";
        msg += "port=\""+port+"\" ";
        msg += "nomp=\""+nomPartie+"\" ";
        msg += "nbj=\""+nombreJoueurMax+"\" ";
        msg += "nbjrm=\""+nombreJoueurReelMax+"\" ";
        msg += "nbjvm=\""+nombreJoueurVirtuelMax+"\" ";
        msg += "nbjrc=\""+nombreCurrentJoueurReel+"\" ";
        msg += "nbjvc=\""+nombreCurrentJoueurBot+"\" ";
        msg += "espa=\""+espionAutorise+"\" ";
        msg += "statut=\""+status+"\"";

        msg+=">";
        return msg;
    }

    public String toString() {

        String str = "id : "+id+"\n";
        str += "ip : "+ip+"\n";
        str += "port : "+port+"\n";
        str += "nomPartie : "+nomPartie+"\n";
        str += "nombreJoueurMax : "+nombreJoueurMax+"\n";
        str += "nombreJoueurReelMax : "+nombreJoueurReelMax+"\n";
        str += "nombreJoueurVirtuelMax : "+nombreJoueurVirtuelMax+"\n";
        str += "espionAutorise : "+espionAutorise+"\n";
        str += "status : "+status+"\n";
        str += "nombreCurrentJoueurReel : "+nombreCurrentJoueurReel+"\n";
        str += "nombreCurrentJoueurBot : "+nombreCurrentJoueurBot+"\n";
        str += "VirtualPlayersTypes : ";
        for(PlayerType pt : virtualPlayersTypes) {
            str += pt + " ";
        }
        str += "\n";

        return str;
    }
    
    @Override
    public boolean equals(Object o) {
    	if(o instanceof PartieInfo) {
    		if(((PartieInfo)o).getId().equals(this.id))
    			return true;
    	}else {
    		return false;
    	}
	 
    	return false;
    		
    }



}
