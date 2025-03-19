package ppti.view;

import common.ImageHandler;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import ppti.model.CarteMeteo;
import ppti.model.JoueurInfo;

import java.io.InputStream;

public class PlayerGridPane extends GridPane {

    private Label playerName;
    private Label playerScore;
    private ImageView weatherCard;
    private ImageView tideCard;
    private ImageView lifebeltCard;
    private StackPane discardPile;
    private ImageView turnedOverWeatherCard;
    private Label lifebeltCount;
    private Label maxLifebelt;
    private Label lifebeltSeparator;

    private ObjectProperty<CarteMeteo> carteJouee;
    private ObjectProperty<String> carteMaree;
    private ObjectProperty<CarteMeteo> defausse;
    private BooleanProperty ShowCard;

    public PlayerGridPane() {
        initializeComponents();
        setupLayout();
        setVisible(false);
        setMaxSize(650, 500);
        setMinSize(250, 200);
    }

    private void initializeComponents() {
        carteMaree = new SimpleObjectProperty<>();
        carteJouee = new SimpleObjectProperty<>();
        defausse = new SimpleObjectProperty<>();
        ShowCard = new SimpleBooleanProperty(false);

        // Initialize labels
        playerName = new Label("John Doe");
        playerScore = new Label("Score : 0");

        // Initialize ImageViews
        weatherCard = new ImageView();
        tideCard = new ImageView(); //createImageView("/common/images/carteMareeRetournee.png", "carte marée");
        lifebeltCard = new ImageView(createImage("/common/images/carteBouee.png", "carte bouée"));

        weatherCard.setFitHeight(60);
        weatherCard.setFitWidth(37.2);

        tideCard.setFitHeight(60);
        tideCard.setFitWidth(37.2);

        lifebeltCard.setFitHeight(60);
        lifebeltCard.setFitWidth(37.2);

        // Initialize discard pile
        turnedOverWeatherCard = new ImageView(); //createImageView("/common/images/carteMareeRetournee.png", "carte météo retournée");
        discardPile = new StackPane(turnedOverWeatherCard);

        turnedOverWeatherCard.setFitHeight(60);
        turnedOverWeatherCard.setFitWidth(37.2);

        // Initialize lifebelt count labels
        lifebeltCount = new Label("0");
        maxLifebelt = new Label("");
        lifebeltSeparator = new Label("/");
    }

    private Image createImage(String imagePath, String description) {
        InputStream imageStream = ImageHandler.loadImage(imagePath, description);
        return (imageStream != null) ? new Image(imageStream) : null;
    }

    private void setupLayout() {
        // Creating lifebelt HBox
        HBox lifebeltHBox = new HBox(lifebeltCount, lifebeltSeparator,maxLifebelt);

        // Adding elements to GridPane
        add(playerName, 2, 0);
        add(playerScore, 0, 2);
        add(weatherCard, 1, 0);
        add(tideCard, 1, 1);
        add(lifebeltCard, 2, 1);
        add(discardPile, 0, 1);
        add(lifebeltHBox, 2, 2);

        // Setting gaps and alignment
        setVgap(5);
        setHgap(10);
        setAlignment(Pos.CENTER);
    }

    public void setJoueurInfo(JoueurInfo joueur) {
        playerName.textProperty().bind(joueur.nomProperty());
        playerScore.textProperty().bind(joueur.scoreProperty().asString("%d"));
        maxLifebelt.textProperty().bind(joueur.maxBoueeProperty().asString("%d"));
        lifebeltCount.textProperty().bind(joueur.currentBoueeProperty().asString("%d"));

        carteJouee.bind(joueur.carteJoueeProprety());
        carteJouee.addListener((observable,oldValue,newValue) -> {
            if(newValue != null) {
                weatherCard.setImage(createImage("/common/images/carteMeteoRetournee.png","Carte retournée"));
            }else{
                weatherCard.setImage(null);
            }
        });
        carteMaree.bind(joueur.carteMareeProperty());
        carteMaree.addListener((observable,oldValue,newValue)-> {
            if(newValue != null && !newValue.equals("00")) {
                tideCard.setImage(createImage("/common/images/carteMaree"+newValue+".png","Carte Marée"));
            } else
                tideCard.setImage(null);
        });
        defausse.bind(joueur.defausseProperty());
        defausse.addListener((observable,oldValue,newValue)-> {
            turnedOverWeatherCard.setImage(createImage("/common/images/carteMeteoRetournee.png","Carte retournée"));
        });

        ShowCard.addListener((observable,oldValue,newValue) -> {
            if(newValue)
                weatherCard.setImage(createImage("/common/images/carteMeteo"+carteJouee.get().getNom()+".png","Carte jouée"));
        });

        this.getStyleClass().addAll("player-grid-pane", joueur.getCouleurJoueur());

    }

    public void setShowProperty(BooleanProperty booleanProperty) {
        ShowCard.bind(booleanProperty);
    }
}
