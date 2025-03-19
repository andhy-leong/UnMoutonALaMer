package ppti.viewmodel;

import common.Config;
import common.enumtype.Placement;
import common.locales.I18N;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import ppti.model.JoueurInfo;
import ppti.model.ChooseColorPositionModel;
import ppti.view.EcranPositionnementView;
import ppti.view.singleton.SingletonView;

public class ChooseColorPositionViewModel {

    private BooleanProperty isDone;

    private StringProperty nomJoueur;
    private ChooseColorPositionModel ccpm;
    private LinkedList<JoueurInfo> allPlayer;
    private JoueurInfo currentPlayer;
    private ObservableList<JoueurInfo> JoueurPlace;
    private SingletonView singleton;
    private ObjectProperty<Map<JoueurInfo, BlockingQueue<String>>> joueurConnecte;
    private ObjectProperty<ArrayList<JoueurInfo>> joueurPlace;
    private HashMap<StackPane, JoueurInfo> tokenPlayer;

    private StringBinding startGameButton;

    /*
    * La liste de bouton nous permettra de dire à tout les boutons de renvoyer à uen même fonction du viewModèle
    * Quand la position sera choisie ( pour l'instant l'endroit où le joueur se trouve n'est pas important donc
    * Tout les boutons auront la même fonction )
    * Le boutton couleur sera utile pour valider l'opération du choix des couleurs tanids que le group toggle sera un des
    * deux groupes ( vu qu'ils sont tout deux binds à l'autre il n'y a pas de réel distinction entre la valeur des deux )
    * et on pourra à partir de la validation du boutton récupérer la valeur cotnenue dans le togglegroup
    * */
    public ChooseColorPositionViewModel(SingletonView singleton) {
        
    	ccpm = new ChooseColorPositionModel();
    	//transformation de la liste des joueurs linked list pour pop seulement le premier
    	allPlayer = null;
    	nomJoueur = new SimpleStringProperty("");
        // isDone permet de savoir si tout les joueurs ont ete place et si on peut passer a la vue de Validation
        isDone = new SimpleBooleanProperty(false);
        currentPlayer = null;

        JoueurPlace = FXCollections.observableArrayList();

        this.singleton = singleton;

        // récupération automatique de la liste de joueur connecté ici
        joueurConnecte = new SimpleObjectProperty<>();
        joueurConnecte.bind(SingletonView.getJoueur());
        // une fois la liste récupéré on modifie tout dans la fonction setFinalPlayer
        joueurConnecte.addListener((observable, oldValue, newValue) -> ccpm.setFinalPlayer(newValue));

        joueurPlace = new SimpleObjectProperty<>();
        joueurPlace.bind(SingletonView.getJoueurOrdre());
        joueurPlace.addListener((observable,oldValue,newValue) -> this.setFinalPlayer(newValue));

        // On récupère les cinq token de la vue positionnement pour les mettre dans une map
        tokenPlayer = new HashMap<>();

        /* Intenationalisation */
        startGameButton = I18N.createStringBinding("startGameButtonColorSelectionScreenPPTI");
    }
    
    public StringProperty nomJoueurProperty() {return nomJoueur;}
    public ObservableList<JoueurInfo> getJoueurPlace() {return JoueurPlace;}
    public BooleanProperty isDoneProperty() {return isDone;}
    public LinkedList<JoueurInfo> getAllPlayer() {return allPlayer;}

    /**
     * Cette methode permet d'ajouter a notre ViewModel la liste des joueurs a qui une couleur et une place va etre attribuer.
     * @param placementJoueur liste des joueurs contenant leur information mais aussi leur blocking queue permettant de leur envoyer un message (reseau)
     */
    public void setFinalPlayer(ArrayList<JoueurInfo> placementJoueur) {
        allPlayer = new LinkedList<>(placementJoueur);

        EcranPositionnementView view = (EcranPositionnementView) SingletonView.getViewList().get(5);

        for (JoueurInfo joueur : allPlayer) {
            for (StackPane c : view.getTokens()) {
                if (((Label) c.getChildren().getLast()).getText() == null) {
                    ((Label) c.getChildren().getLast()).setText(joueur.getNom());
                    view.bindPlayerToken(c, joueur);
                    tokenPlayer.put(c, joueur);
                    break;
                }
            }
        }
    }

    public boolean updatePlayer(Object value, JoueurInfo joueur) {
        if(value instanceof Placement) {
            ccpm.setPlacementJoueur(joueur, (Placement) value);

            if (joueur.getCouleurJoueur() == null) {
                return ccpm.setCouleurJoueur(joueur);
            }
        }else if (value instanceof String) {
            return ccpm.setCouleurJoueur(joueur, (String) value);
        }
        return true;
    }

    public void changeToGame() {

        for (JoueurInfo j : JoueurPlace) {
            //System.out.println(j.getNom() + " " + j.getCouleurJoueur() + " " + j.getPlacement());
        }

        ArrayList<JoueurInfo> joueurs = new ArrayList<>(tokenPlayer.values());

        // vérifier que tous les joueurs aient un placement
        for (JoueurInfo j : joueurs) {
            if (j.getPlacement() == null) {
                return;
            }
            //System.out.println(j.getNom() + " " + j.getCouleurJoueur() + " " + j.getPlacement());
        }

        Map<JoueurInfo, BlockingQueue<String>> m = new HashMap<>(joueurConnecte.getValue());

        if (Config.DEBUG_MODE) {
                System.out.println("JoueurPlace: " + JoueurPlace);
        }

        singleton.setJoueurPlace(joueurs);

        singleton.setViewVisible(12);
    }

    public JoueurInfo getPlayerByToken(StackPane token) {
        return tokenPlayer.get(token);
    }

    public HashMap<String, Boolean> getColors() {
        return ccpm.getColors();
    }

    /* Internationalisation */

    public StringBinding startGameButtonProperty() {
        return startGameButton;
    }
}
