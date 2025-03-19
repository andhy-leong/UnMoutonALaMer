package players.idjr.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GameScreenModel {
    private ObservableList<String> cartesMeteo;

    public GameScreenModel() {
        // Liste de test des cartes
        // ajout d'un caractère par rapport à la version du protocole
        // N : normal, G : grisée, S : selectionnée (inutilisée ici)
        cartesMeteo = FXCollections.observableArrayList(
            "Y01N", "Y02N", "Y03G", "G20N", "G21N", "G23G", "B27N", "B29N", "B31N", "P38N", "P41G", "R56N"
        );
    }

    public ObservableList<String> getWeatherCardsList() {
        return cartesMeteo;
    }

    public void setCartesMeteo(ObservableList<String> cartesMeteo) {
        this.cartesMeteo = cartesMeteo;
    }
}