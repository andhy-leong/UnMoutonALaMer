package ppti.viewmodel;

import common.locales.I18N;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import ppti.model.JoueurInfo;
import ppti.model.PlayerConnectionModel;
import ppti.view.singleton.SingletonView;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class PlayerConnectionScreenViewModel {

    private PlayerConnectionModel model;
    private PartieInfo partieInfo;
    private SingletonView singleton;

    private ObjectProperty<PartieInfo> info;
    private ObjectProperty<ToggleGroup> radioPosition;
    private ArrayList<ToggleGroup> positionJeuToggleGroup;
    private boolean trigger = true;
    private boolean isUpdating = false;

    private StringBinding titleProperty;
    private StringBinding acceptButtonProperty;
    private StringBinding rejectButtonProperty;
    private StringBinding acceptedLabelProperty;
    private StringBinding positionLabelProperty;
    private StringBinding confirmButtonProperty;

    public PlayerConnectionScreenViewModel(PlayerConnectionModel model, SingletonView singleton) {
        this.model = model;
        this.singleton = singleton;
        this.partieInfo = model.getPartieInfo();

        info = new SimpleObjectProperty<PartieInfo>();
        info.bind(SingletonView.getInfo());

        info.addListener((observable, oldValue, newValue) -> {
            this.partieInfo = newValue;
            positionJeuToggleGroup = new ArrayList<>();
            model.setPartieInfo(partieInfo);
        });

        radioPosition = new SimpleObjectProperty<>();
        positionJeuToggleGroup = new ArrayList<>();
        radioPosition.addListener((observable,oldValue,newValue) ->{
            boolean isPossiblePlace = true;
            if(trigger) {
                if(positionJeuToggleGroup.isEmpty()) {
                    addPositionToggle(newValue);
                    newValue.getToggles().getFirst().setSelected(true);
                }else{
                    addPositionToggle(newValue);
                    newValue.getToggles().get(positionJeuToggleGroup.size() - 1).setSelected(true);
                }

                trigger = false;
            }else {
                trigger = true;
            }

        });

        /* Internationalisation */
        titleProperty = I18N.createStringBinding("titleLabelPlayerConnectionScreenPPTI");
        acceptButtonProperty = I18N.createStringBinding("acceptButtonPlayerConnectionScreenPPTI");
        rejectButtonProperty = I18N.createStringBinding("declineButtonPlayerConnectionScreenPPTI");
        acceptedLabelProperty = I18N.createStringBinding("acceptedLabelPlayerConnectionScreenPPTI");
        positionLabelProperty = I18N.createStringBinding("positionLabelPlayerConnectionScreenPPTI");
        confirmButtonProperty = I18N.createStringBinding("confirmButtonPlayerConnectionScreenPPTI");
    }

    public void navigateToColorSelectionScreen() {

        PartieInfo info = model.getPartieInfo();
        // on check si les conditions on ete remplie pour pouvoir changer de vue
        if(info.getNombreCurrentJoueurBot() + info.getNombreCurrentJoueurReel() == info.getNombreJoueurMax()){
            ArrayList<JoueurInfo> joueurPlace = new ArrayList<>();
            ArrayList<String> name = new ArrayList<>();

            for(int i = 0;i < info.getNombreJoueurMax();i++) {
                for(int j = 0;j < info.getNombreJoueurMax();j++) {
                    Toggle t = positionJeuToggleGroup.get(j).getSelectedToggle();
                    if(positionJeuToggleGroup.get(j).getToggles().indexOf(t) == i) {
                        name.add((String)t.getUserData());
                    }
                }
            }

            for(int i = 0;i < info.getNombreJoueurMax();i++){
                for(JoueurInfo j : model.getFinalPlayer().keySet()) {
                    if(j.getNom().equals(name.get(i))) {
                        joueurPlace.add(j);
                    }
                }
            }


            model.sendIp(joueurPlace);

            SingletonView.setJoueurOrdre(joueurPlace);
            SingletonView.setJoueur(model.getFinalPlayer());
            SingletonView.setParentQueue(model.getParentQueue());
            SingletonView.setEspionQueue(model.getEspionQueue());
            singleton.setViewVisible(5);


            model.clearClientList();
        }
    }

    public ObservableMap<JoueurInfo, BlockingQueue<String>> getChildQueue() {
        return model.getChildQueue();
    }

    public void bindToInfo(ObjectProperty<PartieInfo> info) {
        model.bindToInfo(info);
    }

    public void accepterJoueur(String IdJoueur) {
        model.accepterJoueur(IdJoueur);
    }

    public void rejeterJoueur(String IdJoueur) {
        model.rejeterJoueur(IdJoueur);
    }

    public void bindPosition(ObjectProperty<ToggleGroup> o) {radioPosition.bind(o);}

    public PartieInfo getPartieInfo() {
    	return this.partieInfo;
    }

    private void addPositionToggle(ToggleGroup group) {
        this.positionJeuToggleGroup.add(group);
        group.selectedToggleProperty().addListener((observable,oldValue,newValue)-> {
            int listIndex = positionJeuToggleGroup.indexOf(group);
            if (isUpdating) {
                return; // Ignorer si nous sommes en train de mettre à jour
            }

            if (oldValue != null) {
                try {
                    isUpdating = true; // Commencer la mise à jour
                    interchangePosition(listIndex, group.getToggles().indexOf(newValue), group.getToggles().indexOf(oldValue));
                } finally {
                    isUpdating = false; // Finir la mise à jour
                }
            }
        });
    }

    /**
     * Fonction permettant de changer le placement des joueurs en fonction des boutons radio selectionner
     * @param listIndex
     * @param newSelect
     * @param oldSelect
     */
    private void interchangePosition(int listIndex,int newSelect,int oldSelect) {
        for (int i = 0; i < positionJeuToggleGroup.size(); i++) {
            if (i != listIndex) {
                ToggleGroup group = positionJeuToggleGroup.get(i);
                if (group.getToggles().indexOf(group.getSelectedToggle()) == newSelect) {
                    group.getToggles().get(oldSelect).setSelected(true);
                    break;
                }
            }
        }
    }

    public void suspend() {
        try {
            model.suspend();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        model.cancel();
    }

    public void deconnexion() {
        model.decoTousJoueur();
    }

    /* Internationalisation */

    public StringBinding getTitleProperty() {
        return titleProperty;
    }

    public StringBinding getAcceptButtonProperty() {
        return acceptButtonProperty;
    }

    public StringBinding getRejectButtonProperty() {
        return rejectButtonProperty;
    }

    public StringBinding getAcceptedLabelProperty() {
        return acceptedLabelProperty;
    }

    public StringBinding getPositionLabelProperty() {
        return positionLabelProperty;
    }

    public StringBinding getConfirmButtonProperty() {
        return confirmButtonProperty;
    }

    public Property<Boolean> networkStartProperty() {
        return model.networkLaunchProperty();
    }
}
