package ppti.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import ppti.model.PauseScreenModel;
import ppti.view.singleton.SingletonView;

public class PauseScreenViewModel {
    private PauseScreenModel model;
    private SingletonView singleton;

    private StringProperty titleProperty;
    private StringProperty settingsButtonProperty;
    private StringProperty resumeButtonProperty;
    private StringProperty quitButtonProperty;

    public PauseScreenViewModel(PauseScreenModel model, SingletonView singleton) {
        this.model = model;
        this.singleton = singleton;

        // --- provisoire ---
        // en attendant l'internationalisation
        titleProperty = new SimpleStringProperty("Jeu en pause");
        settingsButtonProperty = new SimpleStringProperty("Paramètres");
        resumeButtonProperty = new SimpleStringProperty("Reprendre");
        quitButtonProperty = new SimpleStringProperty("Quitter");
    }

    // actions buttons

    public void onSettingsButtonClicked() {
        // TODO
    }

    public void onResumeButtonClicked() {
        // TODO
    }

    public void onQuitButtonClicked() {
        // TODO
    }

    // getters properties

    public StringProperty titleProperty() {
        return titleProperty;
    }

    public StringProperty settingsButtonProperty() {
        return settingsButtonProperty;
    }

    public StringProperty resumeButtonProperty() {
        return resumeButtonProperty;
    }

    public StringProperty quitButtonProperty() {
        return quitButtonProperty;
    }
}
