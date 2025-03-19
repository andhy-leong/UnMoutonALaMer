package ppti.viewmodel;

import common.locales.I18N;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import ppti.model.EspionQueue;
import ppti.model.GameScreenModel;
import ppti.model.JoueurInfo;
import ppti.model.ParentQueueTreatment;
import ppti.view.GameScreenView;
import ppti.view.singleton.SingletonView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class GameScreenViewModel {

    private GameScreenModel model;

    private SingletonView singleton;

    private ObjectProperty<PartieInfo> info;
    private LinkedList<JoueurInfo> allPlayer;
    private ObjectProperty<Map<JoueurInfo, BlockingQueue<String>>> joueurConnecte;
    private ObjectProperty<ArrayList<JoueurInfo>> JoueurPlace;
    private ObjectProperty<ArrayList<JoueurInfo>> JoueurOrdre;
    private ObjectProperty<ParentQueueTreatment> parentProperty;
    private ObjectProperty<EspionQueue> esp;
    private BooleanProperty allPlayerPlayed;
    // cette liste contient les joueurs qui sont connecté dans l'ordre de jeu, elle permet aussi de stocke les inforamtions des joueurs
    private ArrayList<JoueurInfo> joueurOrdreVM;
    private ListProperty<String> weatherCardsListProperty;
    //contient l'assossiation du nbBouee à la carte
    private Map<String,Double> carteBouee;
    // contient les cartes mélangées
    private ArrayList<String> carteDistrib;
    // contient tout les joueurs connecté à notre partie
    private Map<JoueurInfo,BlockingQueue<String>> joueurMess;
    // id de la manche
    private int manche;

    private BooleanProperty isEndGame;
    private BooleanProperty finManche;
    private StringProperty title;

    // internationalisation gameScreen
    private StringBinding roundPreLabelProperty;
    private StringBinding foldPreLabelProperty;
    private StringBinding confirmButtonProperty;
    private StringBinding menuButtonProperty;
    private StringBinding phasePreLabelProperty;
    private StringBinding endPostLabelProperty;
    private StringBinding endRoundLabelProperty;

    // internationalisation endRoundScreen
    private StringBinding scoreLabelProperty;
    private StringBinding newRoundButtonProperty;
    private StringBinding replayButtonProperty;
    private StringBinding endGameButtonProperty;

    public GameScreenViewModel(SingletonView singleton) throws Exception {
        this.singleton = singleton;

        /* Internationalisation */
        roundPreLabelProperty = I18N.createStringBinding("roundPreLabelGameScreenPPTI");
        foldPreLabelProperty = I18N.createStringBinding("foldPreLabelGameScreenPPTI");
        confirmButtonProperty = I18N.createStringBinding("confirmButtonGameScreenPPTI");
        menuButtonProperty = I18N.createStringBinding("menuButtonGameScreenPPTI");
        phasePreLabelProperty = I18N.createStringBinding("phasePreLabelGameScreenPPTI");
        endPostLabelProperty = I18N.createStringBinding("endPostLabelGameScreenPPTI");
        endRoundLabelProperty = I18N.createStringBinding("endRoundLabelGameScreenPPTI");

        scoreLabelProperty = I18N.createStringBinding("scoreLabelEndRoundScreenPPTI");
        newRoundButtonProperty = I18N.createStringBinding("newRoundButtonEndRoundScreenPPTI");
        replayButtonProperty = I18N.createStringBinding("replayButtonEndRoundScreenPPTI");
        endGameButtonProperty = I18N.createStringBinding("endGameButtonEndRoundScreenPPTI");

        /* Listener */
        model = new GameScreenModel();
        info = new SimpleObjectProperty<>();
        info.bind(SingletonView.getInfo());
        info.addListener((observable,oldValue,newValue) -> {
            model.setPartieInfo(newValue);
        });

        allPlayer = null;
        joueurConnecte = new SimpleObjectProperty<>();
        joueurConnecte.bind(SingletonView.getJoueur());
        joueurConnecte.addListener((observable,oldValue,newValue) -> {
            model.setFinalPlayer(newValue);
        });

        JoueurOrdre = new SimpleObjectProperty<>();
        JoueurOrdre.bind(SingletonView.getJoueurOrdre());
        JoueurOrdre.addListener((observable,oldValue,newValue) -> {
            model.setJoueurOrdreList(newValue);
        });

        parentProperty = new SimpleObjectProperty<>();
        parentProperty.bind(SingletonView.getParentQueue());
        parentProperty.addListener((observable,oldValue,newValue) -> {
            model.setParentQ(newValue);
        });

        JoueurPlace = new SimpleObjectProperty<>();
        JoueurPlace.bind(SingletonView.getJoueurPlace());
        JoueurPlace.addListener((observable, oldValue, newValue) -> {
            this.setFinalPlayer(joueurConnecte.getValue());
            model.setPlace(newValue);

            /*
            for (int i = 0; i < newValue.size(); i++) {
                JoueurInfo ji = newValue.get(i);
                model.addPlace(ji);
                //((GameScreenView) SingletonView.getViewList().get(12)).bindPlayerSpotToViewModel(ji);
            }
            */

            model.lancementJeu();
        });
        weatherCardsListProperty = new SimpleListProperty<>(FXCollections.observableArrayList(model.getWeatherCardsList()));
        model.getWeatherCardsList().addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    if (change.wasAdded()) {
                        // display the new weather card in the view

                    }

                    ObservableList<String> updatedList = model.getWeatherCardsList();

                    // Mettre à jour la propriété weatherCardsListProperty
                    weatherCardsListProperty.set(FXCollections.observableArrayList(updatedList));
                }
            }
        });

        esp = new SimpleObjectProperty<>();
        esp.bind(SingletonView.getEspionQueue());
        esp.addListener((observable, oldValue, newValue) -> {
            model.setEsp(newValue);
        });

        isEndGame = new SimpleBooleanProperty(false);
        isEndGame.bind(model.isEndGameProperty());

        title = new SimpleStringProperty("Manche x/y terminée");
        StringBinding mancheString = Bindings.createStringBinding(
                () ->  {
                    if(!isEndGame.get())
                        //return "Manche " + model.mancheProperty().get() + "/" + model.mancheMaxProperty().get() + " terminée";
                        return this.roundPreLabelProperty().get() + " " + model.mancheProperty().get() + "/" + model.mancheMaxProperty().get() + " " + this.endPostLabelProperty().get();
                    else
                        //return "Fin de Partie";
                        return this.endRoundLabelProperty().get();

                }, model.mancheProperty(), model.mancheMaxProperty(), isEndGame
        );
        title.bind(mancheString);
        finManche = new SimpleBooleanProperty();
        finManche.bind(model.finMancheProperty());

        finManche.addListener((observable,oldValue,newValue) -> {
            if(newValue) {
                singleton.setViewVisible(9);
            }
        });
    }

    public void setFinalPlayer(Map<JoueurInfo, BlockingQueue<String>> newValue) {
        allPlayer = new LinkedList<>(newValue.keySet());
    }
    public ListProperty<String> weatherCardsProperty() {
        return weatherCardsListProperty;
    }
    public BooleanProperty allPlayerPlayedProperty() {
        return model.PlayerPlayedProperty();
    }
    public IntegerProperty mancheProperty() {
        return model.mancheProperty();
    }
    public IntegerProperty pliProperty() {
        return model.pliProperty();
    }
    public void continuerPli() {
        model.continuePli();
    }
    public void continuerManche() {
        singleton.setViewVisible(12);
        model.continueManche();
    }
    public BooleanProperty showCardProperty(){return model.showCardProperty();}
    public ObservableMap<String,JoueurInfo> getIdUsers() {
        return model.getIdUsers();
    }
    public StringProperty mareeOneProperty() {return model.carteMareeOneProperty();}
    public StringProperty mareeTwoProperty() {return model.carteMareeTwoProperty();}
    public BooleanProperty isEndGameProperty() {
        return model.isEndGameProperty();
    }
    public void onReplayButtonClicked() {
        singleton.setViewVisible(12);
        model.replayGame();
    }
    public void onEndGameButtonClicked() {
        model.terminerParite();
        singleton.reinit();
    }
    public StringProperty titleProperty() {
        return title;
    }

    /* Internationalisation */
    // gameScreen
    public StringBinding roundPreLabelProperty() {
        return roundPreLabelProperty;
    }

    public StringBinding foldPreLabelProperty() {
        return foldPreLabelProperty;
    }

    public StringBinding confirmButtonProperty() {
        return confirmButtonProperty;
    }

    public StringBinding menuButtonProperty() {
        return menuButtonProperty;
    }

    public StringBinding phasePreLabelProperty() {
        return phasePreLabelProperty;
    }

    public StringBinding endPostLabelProperty() {
        return endPostLabelProperty;
    }

    public StringBinding endRoundLabelProperty() {
        return endRoundLabelProperty;
    }

    // endRoundScreen
    public StringBinding scoreLabelProperty() {
        return scoreLabelProperty;
    }

    public StringBinding newRoundButtonProperty() {
        return newRoundButtonProperty;
    }

    public StringBinding replayButtonProperty() {
        return replayButtonProperty;
    }

    public StringBinding endGameButtonProperty() {
        return endGameButtonProperty;
    }

    public DoubleProperty vitesseJeuProperty() {
        return model.vitesseJeuProperty();
    }
}
