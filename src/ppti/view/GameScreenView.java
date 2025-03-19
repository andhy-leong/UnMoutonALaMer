package ppti.view;

import common.ImageHandler;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.MapChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ppti.model.JoueurInfo;
import ppti.viewmodel.GameScreenViewModel;

import java.io.InputStream;
import java.util.List;

public class GameScreenView extends BaseView implements MapChangeListener<String,JoueurInfo> {

    private GameScreenViewModel viewModel;

    private Stage stage;

    private BorderPane borderPane;

    private GridPane[] listGridPane;


    private GridPane emptyGridPane;

    private ImageView weatherCard1;
    private ImageView weatherCard2;
    private ImageView stackCard;
    private ImageView turnedOverWeatherCard;
    private ImageView turnedOverTideCard;

    private Label phaseLabelUp;
    private Label roundLabelUp;
    private Label phaseLabelDown;
    private Label roundLabelDown;
    private ImageView roundArrow;


    private StackPane centerContent;
    private HBox centerHBoxContent;
    private VBox centerVboxContent;
    private HBox topContent;
    private HBox bottomContent;
    private VBox leftContent;
    private VBox rightContent;
    private Button optionsUpLeftButton;
    private Button optionsUpRightButton;
    private Button optionsDownLeftButton;
    private Button optionsDownRightButton;

    private Region space1;
    private Region space2;
    private Region space3;
    private Region space4;

    private Boolean debug = false;

    private Button confirmUp;
    private Button confirmDown;

    private StringProperty MareeOne;
    private StringProperty MareeTwo;

    private Label timeRemaningUp;
    private Label timeRemaningDown;

    public GameScreenView(Stage stage, GameScreenViewModel viewModel) throws Exception {
        this.stage = stage;

        this.viewModel = viewModel;

        customizeScreen();
        bindToViewModel();
    }


    @Override
    protected void customizeScreen() {

        createComponents();
        setupComponents();
        organizeComponents();

        // customizeContent();
    }

    @Override
    protected void bindToViewModel() {
        viewModel.getIdUsers().addListener(this);

        viewModel.weatherCardsProperty().addListener((observable, oldValue, newValue) -> {
            List<String> newWeatherCards = newValue;
            for (String card : newWeatherCards) {
                displayCard(card);
            }
        });

        confirmUp.disableProperty().bind(viewModel.allPlayerPlayedProperty());
        confirmDown.disableProperty().bind(viewModel.allPlayerPlayedProperty());

        confirmDown.setOnAction(e -> viewModel.continuerPli());
        confirmUp.setOnAction(e -> viewModel.continuerPli());

        phaseLabelUp.textProperty().bind(viewModel.phasePreLabelProperty().concat(" : ").concat(viewModel.mancheProperty().asString()));
        phaseLabelDown.textProperty().bind(viewModel.phasePreLabelProperty().concat(" : ").concat(viewModel.mancheProperty().asString()));

        roundLabelUp.textProperty().bind(viewModel.foldPreLabelProperty().concat(" : ").concat(viewModel.pliProperty().asString()));
        roundLabelDown.textProperty().bind(viewModel.foldPreLabelProperty().concat(" : ").concat(viewModel.pliProperty().asString()));

        MareeOne = new SimpleStringProperty("");
        MareeTwo = new SimpleStringProperty("");

        MareeOne.bind(viewModel.mareeOneProperty());
        MareeTwo.bind(viewModel.mareeTwoProperty());

        MareeOne.addListener((observable, oldValue, newValue) -> {
            InputStream imageIn = ImageHandler.loadImage("/common/images/carteMaree"+newValue+".png","Carte Marée 1");
            Image image = new Image(imageIn);
            weatherCard1.setImage(image);
        });

        MareeTwo.addListener((observable, oldValue, newValue) -> {
            InputStream imageIn = ImageHandler.loadImage("/common/images/carteMaree"+newValue+".png","Carte Marée 2");
            Image image = new Image(imageIn);
            weatherCard2.setImage(image);
        });


        timeRemaningUp.textProperty().bind(Bindings.createStringBinding(
                () -> viewModel.vitesseJeuProperty().get() > 0.0 ? String.format("%.1f",viewModel.vitesseJeuProperty().get()) : "", viewModel.vitesseJeuProperty()
        ));
        timeRemaningDown.textProperty().bind(timeRemaningUp.textProperty());
        timeRemaningDown.styleProperty().bind(timeRemaningUp.styleProperty());

        /* Internationalisation */
        // menu buttons
        optionsUpLeftButton.textProperty().bind(viewModel.menuButtonProperty());
        optionsUpRightButton.textProperty().bind(viewModel.menuButtonProperty());
        optionsDownLeftButton.textProperty().bind(viewModel.menuButtonProperty());
        optionsDownRightButton.textProperty().bind(viewModel.menuButtonProperty());
        // confirm buttons
        confirmUp.textProperty().bind(viewModel.confirmButtonProperty());
        confirmDown.textProperty().bind(viewModel.confirmButtonProperty());

    }

    @Override
    public void setViewModel(Object viewModel) {
        this.viewModel = (GameScreenViewModel) viewModel;
    }

    private void createComponents() {
        listGridPane = new GridPane[10];
        confirmUp = new Button();
        confirmUp.setRotate(180);
        confirmDown = new Button();

        // for using viewModel JoueurPlace
        for (int i = 0; i < 10; i++) {
            listGridPane[i] = new PlayerGridPane();
        }

        centerContent = new StackPane();
        centerVboxContent = new VBox();
        centerHBoxContent = new HBox();

        topContent = new HBox();
        bottomContent = new HBox();
        leftContent = new VBox();
        rightContent = new VBox();

        borderPane = new BorderPane();

        optionsUpLeftButton = new Button();
        optionsUpRightButton = new Button();
        optionsDownLeftButton = new Button();
        optionsDownRightButton = new Button();

        phaseLabelDown = new Label();
        roundLabelDown = new Label();
        phaseLabelUp = new Label();
        roundLabelUp = new Label();
        phaseLabelDown.setText(this.viewModel.phasePreLabelProperty().get() + " : 1");
        roundLabelDown.setText(this.viewModel.foldPreLabelProperty().get() + " : 1");
        phaseLabelUp.setText(this.viewModel.phasePreLabelProperty().get() + " : 1");
        roundLabelUp.setText(this.viewModel.foldPreLabelProperty().get() + " : 1");
        phaseLabelUp.setRotate(180);
        roundLabelUp.setRotate(180);

        weatherCard1 =new ImageView();
        weatherCard2 = new ImageView();

        timeRemaningUp = new Label();
        timeRemaningDown = new Label();

        timeRemaningUp.setStyle("-fx-font-size: 20px");

        timeRemaningUp.setRotate(180);

        InputStream stackCardStream = ImageHandler.loadImage("/common/images/carteMareeRetournee.png",
                "pioche carte météo");
        stackCard = ((stackCardStream != null) ? new ImageView(new Image(stackCardStream)) :
                        new ImageView());

        space1 = new Region();
        space2 = new Region();
        space3 = new Region();
        space4 = new Region();
    }

    private GridPane createPlayerSpot() {
        GridPane gridPane = new GridPane();
        gridPane.setMaxSize(0, 0); //650 500 when visible
        gridPane.setMinSize(0, 0); // 250 200 when visible
        // création des éléments de la grille
        Label playerName = new Label("John Doe"); // 0
        Label playerScore = new Label("Score : 0"); // 1

        InputStream weatherCardStream = ImageHandler.loadImage("/common/images/carteMeteoB25.png",
                "carte météo");
        ImageView weatherCard = ((weatherCardStream != null) ? new ImageView(new Image(weatherCardStream)) :
                        new ImageView());
        weatherCard.setFitHeight(60);
        weatherCard.setFitWidth(37.2);
        InputStream tideCardStream = ImageHandler.loadImage("/common/images/carteMareeRetournee.png",
                "carte marée");
        ImageView tideCard = ((tideCardStream != null) ? new ImageView(new Image(tideCardStream)) :
                        new ImageView());
        tideCard.setFitHeight(60);
        tideCard.setFitWidth(37.2);
        InputStream lifebeltCardStream = ImageHandler.loadImage("/common/images/carteBouee.png",
                "carte bouée");
        ImageView lifebeltCard = ((lifebeltCardStream != null) ? new ImageView(new Image(lifebeltCardStream)) :
                        new ImageView());
        lifebeltCard.setFitHeight(60);
        lifebeltCard.setFitWidth(37.2);
        //ImageView discardPile = null;
        InputStream turnedOverWeatherCardStream = ImageHandler.loadImage("/common/images/carteMareeRetournee.png",
                "carte météo retournée");
        ImageView turnedOverWeatherCard = ((turnedOverWeatherCardStream != null) ? new ImageView(new Image(turnedOverWeatherCardStream)) :
                        new ImageView());
        turnedOverWeatherCard.setFitHeight(60);
        turnedOverWeatherCard.setFitWidth(37.2);
        StackPane discardPile = new StackPane();
        discardPile.getChildren().addAll(turnedOverWeatherCard);
        // discardPile.getChildren().add(testCard);
        Label lifebeltCount = new Label("0");
        Label maxlifebelt = new Label("");
        Label lifebeltSeparator = new Label("/");
        HBox lifebeltHBox = new HBox();
        lifebeltHBox.getChildren().addAll(maxlifebelt, lifebeltSeparator, lifebeltCount);

        gridPane.setStyle("-fx-background-color: #e1b3b3; -fx-border-color: #000000; -fx-border-width: 2px;");

        gridPane.add(playerName, 2, 0);
        gridPane.add(playerScore, 0, 2);
        gridPane.add(weatherCard, 1, 0);
        gridPane.add(tideCard, 1, 1);
        gridPane.add(lifebeltCard, 2, 1);
        gridPane.add(discardPile, 0, 1);
        gridPane.add(lifebeltHBox, 2, 2);

        gridPane.setVgap(5);
        gridPane.setHgap(10);

        gridPane.setAlignment(Pos.CENTER);

        gridPane.setVisible(false);


        return gridPane;
    }

    public void bindPlayerSpotToViewModel(JoueurInfo joueur) {
        PlayerGridPane gridPane = (PlayerGridPane)listGridPane[joueur.getPlacement().ordinal()];
        gridPane.setJoueurInfo(joueur);
        gridPane.setShowProperty(viewModel.showCardProperty());
        gridPane.setVisible(true);
    }

    private void setupComponents() {
        String optionButtonStyle = "-fx-background-color: white; " +
                "-fx-border-color: black; " +
                "-fx-border-width: 2px; " +
                "-fx-font-size: 24px; " +
                "-fx-min-width: 225px; " +
                "-fx-min-height: 60px;" +
                "-fx-start-margin: 0px;";

        // center elements in top, bottom, right, left
        topContent.setAlignment(Pos.TOP_CENTER);
        topContent.setSpacing(20);
        //topContent.setStyle("-fx-background-color: #8ec5ff;");
        bottomContent.setAlignment(Pos.BOTTOM_CENTER);
        bottomContent.setSpacing(20);
        //bottomContent.setStyle("-fx-background-color: #ff8e8e;");
        leftContent.setAlignment(Pos.CENTER);
        // leftContent.setSpacing(10);
        //leftContent.setStyle("-fx-background-color: #8eff8e;");
        rightContent.setAlignment(Pos.CENTER);
        // rightContent.setSpacing(10);
        //rightContent.setStyle("-fx-background-color: #ff8eff;");

        phaseLabelDown.setStyle("-fx-font-size: 20;");
        roundLabelDown.setStyle("-fx-font-size: 20;");
        phaseLabelUp.setStyle("-fx-font-size: 20;");
        roundLabelUp.setStyle("-fx-font-size: 20;");

        stackCard.setRotate(90);

        int centerCardHeight = 150;
        int centerCardWidth = centerCardHeight * 62 / 100;

        weatherCard1.setFitHeight(centerCardHeight);
        weatherCard1.setFitWidth(centerCardWidth);

        weatherCard2.setFitHeight(centerCardHeight);
        weatherCard2.setFitWidth(centerCardWidth);

        stackCard.setFitHeight(centerCardHeight);
        stackCard.setFitWidth(centerCardWidth);

        HBox.setHgrow(space1, Priority.ALWAYS);
        HBox.setHgrow(space2, Priority.ALWAYS);
        HBox.setHgrow(space3, Priority.ALWAYS);
        HBox.setHgrow(space4, Priority.ALWAYS);

        // centerVboxContent.setStyle("-fx-padding: 5px 10px; -fx-margin: 5px 10px;");
        centerVboxContent.getChildren().addAll(timeRemaningUp, roundLabelUp, phaseLabelUp, stackCard, phaseLabelDown, roundLabelDown,timeRemaningDown);
        centerVboxContent.setStyle("-fx-background-color: #a18d5d; -fx-padding: 5px 10px; -fx-margin: 5px 10px;");
        centerVboxContent.setAlignment(Pos.CENTER);
        centerVboxContent.setMaxWidth(250);
        centerVboxContent.setMinWidth(100);
        centerHBoxContent.getChildren().addAll(weatherCard1, centerVboxContent, weatherCard2);
        centerHBoxContent.setAlignment(Pos.CENTER);
        // centerHBoxContent.setStyle("-fx-padding: 5px 10px;");
        centerContent.getChildren().add(centerHBoxContent);
        // centerContent.setStyle("-fx-padding: 5px 10px;");

        // for each option button, add style
        optionsUpLeftButton.setStyle(optionButtonStyle);
        optionsUpRightButton.setStyle(optionButtonStyle);
        optionsDownLeftButton.setStyle(optionButtonStyle);
        optionsDownRightButton.setStyle(optionButtonStyle);

        if (debug) {

            //set different background color for each content part
            topContent.setStyle("-fx-background-color: #8ec5ff; -fx-padding: 0 50px;");
            bottomContent.setStyle("-fx-background-color: #ff8e8e;-fx-padding: 0 50px;");
            leftContent.setStyle("-fx-background-color: #8eff8e; -fx-padding: 0;");
            rightContent.setStyle("-fx-background-color: #ff8eff; -fx-padding: 0;");
            centerContent.setStyle("-fx-background-color: #ffdf8e; -fx-padding: 0;");

            centerHBoxContent.setStyle("-fx-background-color: #ffeab5; -fx-padding: 5px 10px;");
            centerVboxContent.setStyle("-fx-background-color: #fff4dc; -fx-padding: 5px 10px; -fx-margin: 5px 10px;");

            space1.setStyle("-fx-background-color: #ffdf8e;");
            space2.setStyle("-fx-background-color: #ffdf8e;");
            space3.setStyle("-fx-background-color: #ffdf8e;");
            space4.setStyle("-fx-background-color: #ffdf8e;");

            for (int i = 0; i < 10; i++) {
                GridPane gridPane = listGridPane[i];

                // gridPane.setStyle("-fx-background-color: #e1b3b3; -fx-border-color: #000000; -fx-border-width: 2px;");

                gridPane.setVisible(true);

            }
        }

        stage.widthProperty().addListener((observable, oldValue, newValue) -> {

            double height = stage.getHeight();
            double width = newValue.doubleValue();

            // for each player spot
            for (int i = 0; i < 10; i++) {
                GridPane gridPane = listGridPane[i];
                if (gridPane != null) {

                    if (i < 3 || (i > 4 && i < 8)) {
                        // top and bottom spots
                        gridPane.setMaxWidth((width / 6));
                        gridPane.setMinWidth((width / 6));
                    } else {
                        // left and right spots
                        gridPane.setMaxHeight((width / 5.5));
                        gridPane.setMinHeight((width / 5.5));
                    }
                }
            }

            leftContent.setMaxWidth(width/5.5);
            leftContent.setMinWidth(width/5.5);
            rightContent.setMaxWidth(width/5.5);
            rightContent.setMinWidth(width/5.5);

            centerContent.setMaxWidth(width/3);


        });

        stage.heightProperty().addListener((observable, oldValue, newValue) -> {

            double height = newValue.doubleValue();
            double width = stage.getWidth();

            // for each player spot
            for (int i = 0; i < 10; i++) {
                GridPane gridPane = listGridPane[i];
                if (gridPane != null) {

                    if (i < 3 || (i > 4 && i < 8)) {
                        // top and bottom spots
                        gridPane.setMaxHeight(height / 4);
                        gridPane.setMinHeight(height / 4);
                    } else {
                        // left and right spots
                        gridPane.setMaxWidth((height / 4));
                        gridPane.setMinWidth((height / 4));
                    }

                    gridPane.getChildren().forEach(node -> {
                        if (node instanceof ImageView) {
                            ImageView imageView = (ImageView) node;
                            imageView.setFitHeight((height / 12));
                            imageView.setFitWidth((height / 12) * 0.62);
                        }
                        if (node instanceof StackPane) {
                            if (((StackPane) node).getChildren().get(0) instanceof ImageView) {
                                ImageView imageView = (ImageView) ((StackPane) node).getChildren().get(0);
                                imageView.setFitHeight((height / 12));
                                imageView.setFitWidth((height / 12) * 0.62);
                            }
                        }
                    });
                }
            }


            topContent.setMaxHeight((height / 4));
            topContent.setMinHeight((height / 4));


            bottomContent.setMaxHeight((height / 4));
            bottomContent.setMinHeight((height / 4));

            centerContent.setMaxHeight(height / 5);
            centerContent.setMinHeight(height / 5);

            leftContent.setMaxHeight((height / 2));
            leftContent.setMinHeight((height / 2));

            rightContent.setMaxHeight((height / 2));
            rightContent.setMinHeight((height / 2));

            if (debug) {
                // TODO: add logger
            }
        });
    }

    private void organizeComponents() {
        topContent.getChildren().addAll(optionsUpLeftButton, space1);
        bottomContent.getChildren().addAll(optionsDownLeftButton, space3);

        for (int i = 0; i < 10; i++) {
            GridPane gridPane = listGridPane[i];

            if (gridPane == null) {
                gridPane = new GridPane();
                gridPane.setMaxSize(600, 450);
                gridPane.setMinSize(200, 150);
                gridPane.setStyle(
                        "-fx-border-width: 2px; -fx-padding: 5px 10px;");
            }



            if (i < 3) {
                gridPane.setRotate(180);
                topContent.getChildren().add(gridPane);
            } else if (i < 5) {
                gridPane.setRotate(270);
                rightContent.getChildren().add(gridPane);
            } else if (i < 8) {
                bottomContent.getChildren().add(gridPane);
            } else {
                gridPane.setRotate(90);
                leftContent.getChildren().add(gridPane);
            }
        }

        topContent.getChildren().addAll(space2, optionsUpRightButton);
        bottomContent.getChildren().addAll(space4, optionsDownRightButton);

        super.addContainerRoot(topContent, leftContent, centerContent, rightContent, bottomContent);
    }

    private void displayCard(String cardImagePath) {
        if (cardImagePath == null) {
            return;
        }

        InputStream cardStream = ImageHandler.loadImage(cardImagePath, "carte météo");

        assert cardStream != null;
        if (!weatherCard1.isVisible()) {
            weatherCard1.setImage(new Image(cardStream));
            weatherCard1.setVisible(true);
        } else if (!weatherCard2.isVisible()) {
            weatherCard2.setImage(new Image(cardStream));
            weatherCard2.setVisible(true);
        }
    }

    private void hideCards() {
        weatherCard1.setVisible(false);
        weatherCard2.setVisible(false);
    }

    @Override
    public void onChanged(Change<? extends String, ? extends JoueurInfo> change) {
        if(change.wasAdded()) {
            bindPlayerSpotToViewModel(change.getValueAdded());
        }
    }
}


