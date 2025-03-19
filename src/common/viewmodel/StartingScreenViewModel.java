package common.viewmodel;

import common.locales.I18N;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import players.idjr.services.NavigationServiceIDJR;
import common.navigation.NavigationService;
import esp.services.NavigationServiceESP;

public class StartingScreenViewModel {
	private NavigationService navigationService;
    private NavigationServiceIDJR navigationServiceIdjr;
    private NavigationServiceESP navigationServiceEsp;
    private boolean isSpyUser;
    
    private StringBinding titleLabelProperty;
    private StringBinding subtitleLabelProperty;
    private StringBinding playButtonProperty;
    private StringBinding optionsButtonProperty;
    private StringBinding helpButtonProperty;
    private StringBinding quitButtonProperty;

    public StartingScreenViewModel(NavigationService navigationService, NavigationServiceIDJR navigationServiceIdjr, NavigationServiceESP navigationServiceEsp ,  boolean isSpyUser) {
        this.navigationService = navigationService;
        this.navigationServiceIdjr = navigationServiceIdjr;
        this.navigationServiceEsp = navigationServiceEsp;
        this.isSpyUser = isSpyUser;
        
        titleLabelProperty = I18N.createStringBinding("titleLabelStartingScreenIDJR");
        
        if (isSpyUser) {
            subtitleLabelProperty = I18N.createStringBinding("subtitleLabelStartingScreenSpy");
            playButtonProperty = I18N.createStringBinding("connectButtonStartingScreenSpy");
        } else {
            playButtonProperty = I18N.createStringBinding("playButtonStartingScreenIDJR");
        }
        
        optionsButtonProperty = I18N.createStringBinding("optionsButtonStartingScreenIDJR");
        helpButtonProperty = I18N.createStringBinding("helpButtonStartingScreenIDJR");
        quitButtonProperty = I18N.createStringBinding("quitButtonStartingScreenIDJR");
    }

    public void onPlayButtonClicked() {
        if (isSpyUser) {
            navigationServiceEsp.navigateToEspConnectionScreen();
        } else {
            navigationServiceIdjr.navigateToParametersScreen();
        }
    }

    public void onOptionsButtonClicked() {
        navigationService.navigateToOptionsScreen();
    }

    public void onHelpButtonClicked() {
        navigationService.navigateToHelpScreen();
    }

    public void onQuitButtonClicked() {
        Platform.exit();;
    }
    
    public StringBinding titleLabelProperty() {
        return titleLabelProperty;
    }
    
    public StringBinding subtitleLabelProperty() {
        return subtitleLabelProperty;
    }
    
    public StringBinding playButtonProperty() {
        return playButtonProperty;
    }
    
    public StringBinding optionsButtonProperty() {
        return optionsButtonProperty;
    }
    
    public StringBinding helpButtonProperty() {
        return helpButtonProperty;
    }
    
    public StringBinding quitButtonProperty() {
        return quitButtonProperty;
    }
} 