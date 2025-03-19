package players.idjr.viewmodel;

import common.locales.I18N;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import players.idjr.model.EndGameScreenModel;
import players.idjr.services.NavigationServiceIDJR;

public class EndGameScreenViewModel {
    private EndGameScreenModel model;
    private NavigationServiceIDJR navigationService;

    private StringBinding menuButtonProperty;
    private StringProperty partNameLabelProperty;
    private StringProperty topLabelProperty;
    private StringProperty playerNameProperty;

    private StringBinding scoreLabelProperty;
    private StringBinding lostBuoysLabelProperty;
    private StringBinding titleLabelProperty;

    private StringProperty scoreValueLabelProperty;
    private StringProperty lostBuoysValueLabelProperty;
    
    private StringProperty topLabelWinEndGameScreenIDJR;
    private StringProperty topLabelLooseEndGameScreenIDJR;
    private StringProperty topLabelLooseEndGameScreenIDJR2;

    public EndGameScreenViewModel(EndGameScreenModel model, NavigationServiceIDJR navigationService) {
        this.model = model;
        this.navigationService = navigationService;

        // internationalisation
        menuButtonProperty = I18N.createStringBinding("menuButtonEndGameScreenIDJR");
        scoreLabelProperty = I18N.createStringBinding("scoreLabelEndGameScreenIDJR");
        lostBuoysLabelProperty = I18N.createStringBinding("lostBuoysEndGameScreenIDJR");
        titleLabelProperty = I18N.createStringBinding("titleLabelEndGameScreenIDJR");
        
      
        

        // initialisation des propriétés
        partNameLabelProperty = new SimpleStringProperty("[Nom partie]");
        topLabelProperty = new SimpleStringProperty("[info gagné/perdu]");
        playerNameProperty = new SimpleStringProperty("[Nom joueur]");

        scoreValueLabelProperty = new SimpleStringProperty("0");
        lostBuoysValueLabelProperty = new SimpleStringProperty("0");
    }

    /* Menu actions */

    public void onMenuButtonClicked() {
        navigationService.showPopUpMenu();
    }

    /* Propertie getters */

    public StringProperty partNameLabelProperty() {
        return partNameLabelProperty;
    }

    public StringProperty topLabelProperty() {
        return topLabelProperty;
    }

    public StringProperty playerNameProperty() {
        return playerNameProperty;
    }

    public StringBinding scoreLabelProperty() {
        return scoreLabelProperty;
    }

    public StringBinding lostBuoysLabelProperty() {
        return lostBuoysLabelProperty;
    }

    public StringProperty scoreValueLabelProperty() {
        return scoreValueLabelProperty;
    }

    public StringProperty lostBuoysValueLabelProperty() {
        return lostBuoysValueLabelProperty;
    }
    
    public StringBinding titleLabelProperty() {
        return titleLabelProperty;
    }

    public StringBinding menuButtonProperty() {
        return menuButtonProperty;
    }

    public void setGameData(String playerName, String partName, String finalScore, String lostBuoys, int rank) {
        playerNameProperty.set(playerName);
        partNameLabelProperty.set(partName);
        scoreValueLabelProperty.set(finalScore);
        lostBuoysValueLabelProperty.set(lostBuoys);
        
        // Définir le message de fin en fonction du classement
        if (rank == 1) {
            topLabelProperty.set(I18N.get("topLabelWinEndGameScreenIDJR"));
        } else {
            String suffixe;
            if (I18N.containsKey("rankSuffixEndGameScreenIDJR." + rank)) {
                suffixe = I18N.get("rankSuffixEndGameScreenIDJR." + rank); 
            } else {
                suffixe = I18N.get("rankSuffixEndGameScreenIDJR.other"); 
            }

            topLabelProperty.set(String.format(I18N.get("topLabelLooseEndGameScreenIDJR"), rank, suffixe));
        }
    }
}
