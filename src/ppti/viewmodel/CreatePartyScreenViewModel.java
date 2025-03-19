package ppti.viewmodel;

import common.Config;
import common.locales.I18N;
import javafx.beans.binding.StringBinding;
import ppti.model.CreatePartyScreenModel;
import javafx.beans.property.*;
import ppti.view.singleton.SingletonView;


public class CreatePartyScreenViewModel {
    private CreatePartyScreenModel model;
    private SingletonView singleton;

    private StringProperty name;
    private DoubleProperty vitesseJeu;
    private IntegerProperty isThereSpy;
    private IntegerProperty nbJoueurMax;
    private IntegerProperty nbJoueurReelMax;

    private StringBinding nomPartieNameProperty;
    private StringBinding nombreJoueurLabelProperty;
    private StringBinding nombreJoueurReelLabelProperty;
    private StringBinding vitesseJeuLabelProperty;
    private StringBinding isThereSpyLabelProperty;
    private StringBinding isThereSpyCheckBoxProperty;
    private StringBinding createPartyButtonProperty;
    private StringBinding titleProperty;


    public CreatePartyScreenViewModel(CreatePartyScreenModel model, SingletonView singleton) {
        this.model = model;
        this.singleton = singleton;

        name = new SimpleStringProperty("DefaultPartyName");
        vitesseJeu = new SimpleDoubleProperty(1.0);
        isThereSpy = new SimpleIntegerProperty(0);
        nbJoueurMax = new SimpleIntegerProperty(3);
        nbJoueurReelMax = new SimpleIntegerProperty(0);

        /* Internationalisation */

        nomPartieNameProperty = I18N.createStringBinding("nomPartieLabelCreatePartyPPTI");
        nombreJoueurLabelProperty = I18N.createStringBinding("nombreJoueurLabelCreatePartyPPTI");
        nombreJoueurReelLabelProperty = I18N.createStringBinding("nombreJoueurReelLabelCreatePartyPPTI");
        vitesseJeuLabelProperty = I18N.createStringBinding("vitesseDeJeuLabelCreatePartyPPTI");
        isThereSpyLabelProperty = I18N.createStringBinding("isThereSpyLabelCreatePartyPPTI");
        isThereSpyCheckBoxProperty = I18N.createStringBinding("isThereSpyCheckBoxCreatePartyPPTI");
        createPartyButtonProperty = I18N.createStringBinding("creerButtonCreatePartyPPTI");
        titleProperty = I18N.createStringBinding("titleLabelCreatePartyPPTI");


        //viewFactory = new PPTIViewFactory();

        /* Listener pour les propriétés */
        vitesseJeu.addListener((observable, oldValue, newValue) -> {
            model.setVitesseJeu(newValue.doubleValue());
        });

        name.addListener((observable, oldValue, newValue) -> {
            model.setNomPartie(newValue);
        });

        isThereSpy.addListener((observable, oldValue, newValue) -> {
            model.setIsThereSpy((int) newValue);
        });

        nbJoueurMax.addListener((observable, oldValue, newValue) -> {
            model.setNbJoueurMax(newValue.intValue());
        });

        nbJoueurReelMax.addListener((observable, oldValue, newValue) -> {
           model.setNbJoueurReelMax(newValue.intValue());
        });
    }

    private void setInfoPartieValue() {
        model.setNbJoueurMax(this.nbJoueurMax.getValue().intValue());
        model.setNbJoueurReelMax(this.nbJoueurReelMax.getValue().intValue());
        model.setVitesseJeu(this.vitesseJeu.getValue());
        model.setNomPartie(this.name.getValue());
        model.setIsThereSpy(this.isThereSpy.getValue());
    }

    public void navigateToBotLevelScreen() {
        // Avant de créer la partie on set ses valeurs une par une si l'utilisateur ne change rien
        this.setInfoPartieValue();
        model.creerInfoPartie();
        if (Config.DEBUG_MODE) {
            System.out.println("model.getPartieInfo(): " + model.getPartieInfo().getNombreJoueurMax());
        }

        // On ajoute nos info (non complètes) à notre singleton pour que les vues se mettre à jour
        SingletonView.setInfo(this.model.getPartieInfo());

        // On vérifie qu'il y ai au moins 1 bot
        if(model.getPartieInfo().getNombreJoueurVirtuelMax() == 0) {
            singleton.setViewVisible(3);
        }else{
        	singleton.setViewVisible(2);
        }
    }

    public void navigateToPreviousScreen() {
    	singleton.setViewVisible(0);
    }

    public StringProperty nameProperty() {
        return this.name;
    }

    public DoubleProperty vitesseJeuProperty() {
        return this.vitesseJeu;
    }

    public IntegerProperty isThereSpyProperty() {
        return this.isThereSpy;
    }

    public IntegerProperty nbJoueurMaxProperty() {
        return this.nbJoueurMax;
    }

    public IntegerProperty nbJoueurReelMaxProperty() {
        return this.nbJoueurReelMax;
    }

    public void stop() {
        model.stop();
    }

    /* Internationalisation */

    public StringBinding getNomPartieNameProperty() {
        return nomPartieNameProperty;
    }

    public StringBinding getNombreJoueurLabelProperty() {
        return nombreJoueurLabelProperty;
    }

    public StringBinding getNombreJoueurReelLabelProperty() {
        return nombreJoueurReelLabelProperty;
    }

    public StringBinding getVitesseJeuLabelProperty() {
        return vitesseJeuLabelProperty;
    }

    public StringBinding getIsThereSpyLabelProperty() {
        return isThereSpyLabelProperty;
    }

    public StringBinding getIsThereSpyCheckBoxProperty() {
        return isThereSpyCheckBoxProperty;
    }

    public StringBinding getCreatePartyButtonProperty() {
        return createPartyButtonProperty;
    }

    public StringBinding getTitleProperty() {
        return titleProperty;
    }
}
