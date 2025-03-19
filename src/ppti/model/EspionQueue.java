package ppti.model;

import common.reseau.udp.inforecup.PartieInfo;
import ppti.model.ClientHandler;
import ppti.model.JoueurInfo;

import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Cette classe permet d'ajouter des espions à notre partie sans les melangers avec des joueurs/BOT.
 * Une fois qjouté dans la liste de ClientHandler et childQueue ils recevront tous les messages sans distinction car les espions n'ont pas d'incidence sur la partie, donc n'ont pas besoin de message spécifique.
 */
public class EspionQueue {
    private List<ClientHandler> espions;
    private List<BlockingQueue<String>> childQueue;
    private Thread tDecoEspion;

    public EspionQueue() {
        this.espions = new ArrayList<>();
        this.childQueue = new ArrayList<>();
        tDecoEspion = new Thread(() -> {
            ArrayList<Integer> deleteEspions = new ArrayList<>();
            while(!Thread.currentThread().isInterrupted()){
                for(int i = 0; i < espions.size(); i++){
                    String msg = childQueue.get(i).poll();
                    if(msg != null){
                        if(msg.equals("ADJ")) {
                            deleteEspions.add(i);
                        }
                    }
                }

                for(int i = 0; i < deleteEspions.size(); i++){
                    espions.get(i).stopClientHandler();
                    espions.remove(deleteEspions.get(i).intValue());
                    childQueue.remove(deleteEspions.get(i).intValue());

                    for(int j = i; j < deleteEspions.size(); j++){
                        // on décale les autres index de - 1
                        deleteEspions.set(j,deleteEspions.get(j) - 1);
                    }
                }

                //une fois tout les espions supprimés (ceux déconnecté) on retourne à l'état de base
                deleteEspions.clear();
            }
        },"ThreadDeconnexionEspion - Equipe3a");
    }

    public void addEspion(ClientHandler espion, PartieInfo info, JoueurInfo joueur) {
        // on ajoute l'espion à notre liste de ClientHandler pour pouvoir le désactiver et supprimer plus tard
        espions.add(espion);
        // récupération de la Queue contenant les messages des enfants (seulement pour la déconnexion)
        childQueue.add(espion.getChildQueue());
        childQueue.getLast().add("<ADP idp=\"" + info.getId() + "\" idj=\"" + joueur.getIdp() + "\" />");
    }

    public void envoieMsg(String msg) {
        for(BlockingQueue<String> queue : childQueue) {
            queue.add(msg);
        }
    }
}
