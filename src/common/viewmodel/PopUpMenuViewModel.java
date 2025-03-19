package common.viewmodel;

import common.locales.I18N;
import common.navigation.NavigationService;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;

public class PopUpMenuViewModel {
    private NavigationService navigationService;
    
    private StringBinding menuButtonProperty;
    private StringBinding helpButtonProperty;
    private StringBinding optionsButtonProperty;
    private StringBinding quitButtonProperty;


    public PopUpMenuViewModel(NavigationService navigationService) {
        this.navigationService = navigationService;
        
        menuButtonProperty = I18N.createStringBinding("menuButtonPopUpMenuIDJR");
        helpButtonProperty = I18N.createStringBinding("helpButtonPopUpMenuIDJR");
        optionsButtonProperty = I18N.createStringBinding("optionsButtonPopUpMenuIDJR");
        quitButtonProperty = I18N.createStringBinding("quitButtonPopUpMenuIDJR");
    }

    public void onOptionsButtonClicked() {
        navigationService.hidePopUpMenu();
        navigationService.navigateToOptionsScreen();
    }

    public void onHelpButtonClicked() {
        navigationService.hidePopUpMenu();
        navigationService.navigateToHelpScreen();
    }

    public void onQuitButtonClicked() {
        navigationService.hidePopUpMenu();
        Platform.exit();
    }

    public void onMenuButtonClicked() {
        navigationService.hidePopUpMenu();
    }
    
    public StringBinding menuButtonProperty() {
    	return menuButtonProperty;
    }
    
    public StringBinding helpButtonProperty() {
    	return helpButtonProperty;
    }
    
    public StringBinding optionsButtonProperty() {
    	return optionsButtonProperty;
    }
    
    public StringBinding quitButtonProperty() {
    	return quitButtonProperty;
    }
} 