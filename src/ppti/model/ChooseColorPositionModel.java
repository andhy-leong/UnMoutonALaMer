package ppti.model;

import common.enumtype.Placement;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChooseColorPositionModel {
    private Map<JoueurInfo, BlockingQueue<String>> finalPlayer;
    // HashMap des couleurs et de leur disponibilité
    private HashMap<String, Boolean> colors;

    public ChooseColorPositionModel() {
        this.finalPlayer = null;
        // initialisation des couleurs
        colors = new HashMap<>();
        colors.put("color1", false);
        colors.put("color2", false);
        colors.put("color3", false);
        colors.put("color4", false);
        colors.put("color5", false);
        colors.put("color6", false);
        colors.put("color7", false);
        colors.put("color8", false);
        colors.put("color9", false);
        colors.put("color10", false);
    }

    public ArrayList<JoueurInfo> getJoueurInfos() {return new ArrayList<>(finalPlayer.keySet());}

    /**
     * Cette fonction permet de mettre une liste de joueur dans notre modele pour pouvoir la sauvegarder
     * @param finalPlayer
     */
    public void setFinalPlayer(Map<JoueurInfo,BlockingQueue<String>> finalPlayer) {this.finalPlayer = finalPlayer;}

    public boolean setCouleurJoueur(JoueurInfo joueur ,String couleur) {
        if (colors.get(couleur)) {
            return false;
        }

        if (joueur.getCouleurJoueur() != null) {
            colors.put(joueur.getCouleurJoueur(), false);
        }
        colors.put(couleur, true);
        joueur.setCouleurJoueur(couleur);
        return true;
    }

    public boolean setCouleurJoueur(JoueurInfo joueur) {
        Set<String> keys = colors.keySet();
        for (String key : keys) {
            if (!colors.get(key)) {
                setCouleurJoueur(joueur, key);
                return true;
            }
        }
        return false;
    }

    public void setPlacementJoueur(JoueurInfo joueur , Placement placement) {
        joueur.setPlacement(placement);
    }

    public HashMap<String, Boolean> getColors() {return colors;}
}
