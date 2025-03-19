package players.idjr.viewmodel;

import common.Config;
import players.idjr.model.ParametersScreenModel;
import players.idjr.services.NavigationServiceIDJR;
import common.navigation.NavigationService;
import esp.services.NavigationServiceESP;
import javafx.beans.property.StringProperty;
import common.locales.I18N;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;

public class ParametersScreenViewModel {
    private ParametersScreenModel model;
    private NavigationServiceIDJR navigationServiceIdjr;
    
    private StringBinding titleLabelProperty;
    private StringBinding playerNameInfosLabelProperty;
    private StringBinding searchGameButtonProperty;
    private StringBinding playerNameLabelProperty;
    private StringBinding playerCountLabelProperty;

    public ParametersScreenViewModel(ParametersScreenModel model,NavigationServiceIDJR navigationServiceIdjr) {
        this.model = model;
        this.navigationServiceIdjr = navigationServiceIdjr;

        
        titleLabelProperty = I18N.createStringBinding("titleLabelParameterScreenViewIDJR");
        playerNameInfosLabelProperty = I18N.createStringBinding("playerNameInfosLabelParameterScreenViewIDJR");
        searchGameButtonProperty = I18N.createStringBinding("searchGameParameterScreenViewIDJR");
        playerNameLabelProperty = I18N.createStringBinding("playerNameLabelParameterScreenViewIDJR");
        playerCountLabelProperty = I18N.createStringBinding("playerCountLabelParameterScreenViewIDJR");
        
    }

    public StringProperty playerNameProperty() {
        return model.playerNameProperty();
    }

    public IntegerProperty maxPlayersProperty() {
        return model.maxPlayersProperty();
    }

    public String getPlayerName() {
        return playerNameProperty().get();
    }

    public void onSearchGameButtonClicked() {
        String playerName = getPlayerName();
        int maxPlayers = getMaxPlayersFilter();
        if (Config.DEBUG_MODE) {
            System.out.println("Recherche de partie avec : " + playerName + ", " + maxPlayers + " joueurs max");
        }
        navigationServiceIdjr.navigateToConnectionScreen(playerName, maxPlayers);
    }

    public void onBackButtonClicked() {
        navigationServiceIdjr.navigateBack();
    }

    public boolean isValidPlayerName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        if (name.length() > 20) {
            return false;
        }
        
        return name.matches("^[a-zA-Z0-9'_]+$");
    }

    public int getMaxPlayersFilter() {
        return maxPlayersProperty().get();
    }
    
    public StringBinding titleLabelProperty() {
    	return titleLabelProperty;
    }
    public StringBinding playerNameInfosLabelProperty() {
    	return playerNameInfosLabelProperty;
    }
    public StringBinding searchGameButtonProperty() {
    	return searchGameButtonProperty;
    }
    public StringBinding playerNameLabelProperty() {
    	return playerNameLabelProperty;
    }
    public StringBinding playerCountLabelProperty() {
    	return playerCountLabelProperty;
    }
}
