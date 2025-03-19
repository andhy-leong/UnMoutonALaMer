package ppti.viewmodel;

import common.locales.I18N;
import javafx.beans.binding.StringBinding;
import ppti.view.singleton.SingletonView;

public class StartingScreenViewModel {
    private SingletonView singleton;

    private StringBinding createButtonProperty;
    private StringBinding optionsButtonProperty;
    private StringBinding rulesButtonProperty;
    private StringBinding titleLabelProperty;
    private StringBinding quitButtonProperty;


    public StartingScreenViewModel(SingletonView singleton) {
    	this.singleton = singleton;

        createButtonProperty = I18N.createStringBinding("createButtonStartingScreenPPTI");
        optionsButtonProperty = I18N.createStringBinding("optionsButtonStartingScreenPPTI");
        rulesButtonProperty = I18N.createStringBinding("rulesButtonStartingScreenPPTI");
        titleLabelProperty = I18N.createStringBinding("titleLabelStartingScreenPPTI");
        quitButtonProperty = I18N.createStringBinding("quitButtonStartingScreenPPTI");
    }


    public void onCreateButtonClicked() {
        singleton.setViewVisible(1);;
    }

    public void onOptionsButtonClicked(boolean isScreenRotated) {
        singleton.updatePopUpScreenOrientation(isScreenRotated);
        singleton.setViewVisible(7);
    }

    public void onRulesButtonClicked(Boolean isScreenRotated) {
        singleton.updatePopUpScreenOrientation(isScreenRotated);
        singleton.setViewVisible(8);
    }

    public void onQuitButtonClicked() {
        singleton.quitGame();
    }


    /* Internationnalisation */

    public StringBinding getCreateButtonProperty() {
        return createButtonProperty;
    }

    public StringBinding getOptionsButtonProperty() {
        return optionsButtonProperty;
    }

    public StringBinding getRulesButtonProperty() {
        return rulesButtonProperty;
    }

    public StringBinding getTitleLabelProperty() {
        return titleLabelProperty;
    }

    public StringBinding getQuitButtonProperty() {
        return quitButtonProperty;
    }
}
