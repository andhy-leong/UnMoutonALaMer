package players.idjr.view;

import common.locales.I18N;
import common.ui.BaseScreenView;
import common.ui.pane.CircularPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import players.idjr.model.JoinedGameModel;
import players.idjr.services.NavigationServiceIDJR;
import players.idjr.viewmodel.InitScreenViewModel;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javafx.application.Platform;

public class InitScreenView extends BaseScreenView {

    private InitScreenViewModel viewModel;

    private VBox centerVBox;
    private Label stateMessageLabel;
    private HBox hbox;
    private Label playerCountLabel;
    private Label playerCountValue;
    private StackPane stackPane;
    private CircularPane circularPane;

    private Timeline dotsAnimation;
    private RotateTransition circleRotation;
    private String baseMessage;
    private int dotCount = 0;
    private StringProperty animatedMessageProperty;

    public InitScreenView(NavigationServiceIDJR navigationService) {
        this.modelTopBar = 4;
        this.animatedMessageProperty = new SimpleStringProperty();
        super.init();
    }

    @Override
    protected void customizeScreen() {
        createComponents();
        setupComponents();
        organizeComponents();
        setupAnimations();

        addContainerRoot(centerVBox, null);

        // Démarrer les animations
        startAnimations();
    }

    private void createComponents() {
        //center components
        centerVBox = new VBox();
        stateMessageLabel = new Label();
        baseMessage = I18N.get("stateMessageLabelInitScreenIDJR");
        hbox = new HBox();
        playerCountLabel = new Label("[connected players]");
        playerCountValue = new Label("0");
        stackPane = new StackPane();
        circularPane = new CircularPane();

        // cercle du cercle
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(150);
        circle.setFill(javafx.scene.paint.Color.TRANSPARENT);
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        circle.setStrokeWidth(2);

    }

    private void setupComponents() {
        centerVBox.setAlignment(Pos.TOP_CENTER);
        centerVBox.setPadding(new Insets(20));

        stateMessageLabel.getStyleClass().add("state-message");

        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);


        circularPane.setRadius(150);

        stackPane.setMinSize(350, 350);
        stackPane.setMaxSize(350, 350);
        stackPane.setAlignment(Pos.CENTER);
    }

    private void organizeComponents() {
        centerVBox.prefWidthProperty().bind(this.widthProperty().multiply(0.8));
        centerVBox.setSpacing(20);
        centerVBox.setPadding(new Insets(20,0,20,0));

        hbox.getChildren().addAll(playerCountLabel, playerCountValue);

        // cercle et du CircularPane dans le même StackPane
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(150);
        circle.setFill(javafx.scene.paint.Color.TRANSPARENT);
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        circle.setStrokeWidth(2);


        javafx.scene.shape.MoveTo moveTo = new javafx.scene.shape.MoveTo(0, -150);
        javafx.scene.shape.LineTo line1 = new javafx.scene.shape.LineTo(10, -140);
        javafx.scene.shape.LineTo line2 = new javafx.scene.shape.LineTo(-10, -140);
        javafx.scene.shape.LineTo line3 = new javafx.scene.shape.LineTo(0, -150);


        stackPane.getChildren().addAll(circle, circularPane);

        centerVBox.getChildren().addAll(
                stateMessageLabel,
                hbox,
                stackPane);
    }

    private void setupAnimations() {
        // animation des points
    	dotsAnimation = new Timeline(
    	    new KeyFrame(Duration.seconds(0.5), e -> {
    	        dotCount = (dotCount + 1) % 4;
    	        String dots = ".".repeat(dotCount);
    	        //par defaut en attendant le viewmodel
    	        animatedMessageProperty.set(baseMessage + dots);
    	    })
    	);
        dotsAnimation.setCycleCount(Timeline.INDEFINITE);

        // animation de rotation du cercle
        circleRotation = new RotateTransition(Duration.seconds(20), stackPane);
        circleRotation.setByAngle(360);
        circleRotation.setCycleCount(Timeline.INDEFINITE);
        circleRotation.setInterpolator(javafx.animation.Interpolator.LINEAR);
    }
    

    private void startAnimations() {
        dotsAnimation.play();
        circleRotation.play();
    }

    private void stopAnimations() {
        if (dotsAnimation != null) {
            dotsAnimation.stop();
        }
        if (circleRotation != null) {
            circleRotation.stop();
        }
    }

    @Override
    protected void bindToViewModel() {
        // on ne fait rien ici car le viewModel n'est pas encore défini
    }

    public void setViewModel(InitScreenViewModel viewModel) {
        this.viewModel = viewModel;
        //  le binding maintenant que le viewModel est défini
        if (viewModel != null) {

            //System.out.println("InitScreenView - Initialisation du ViewModel");
            
            //arreter l'animation existant et créer une nouvelle avec le viewModel
            dotsAnimation.stop();
            dotsAnimation = new Timeline(
                    new KeyFrame(Duration.seconds(0.5), e -> {
                        dotCount = (dotCount + 1) % 4;
                        String dots = ".".repeat(dotCount);
                        animatedMessageProperty.set(viewModel.stateMessageLabelProperty().get() + dots);
                    })
                );
                dotsAnimation.setCycleCount(Timeline.INDEFINITE);
                dotsAnimation.play();


            menuButton.setOnAction(event -> viewModel.onMenuButtonClicked());

            playerNameLabel.textProperty().bind(viewModel.playerNameProperty());
            playerCountValue.textProperty().bind(viewModel.playerCountProperty().asString());

            // internalisation
            menuButton.textProperty().bind(viewModel.menuButtonProperty());
            titleLabel.textProperty().bind(viewModel.titleLabelProperty());
            stateMessageLabel.textProperty().bind(animatedMessageProperty);
            playerCountLabel.textProperty().bind(viewModel.playerCountLabelProperty());

            // mise à jour initiale du cercle si des joueurs sont déjà présents
            if (!viewModel.playersProperty().isEmpty()) {
                updatePlayerCircle();
            }

            // écouter les changements dans la liste des joueurs
            viewModel.playersProperty().addListener((obs, oldList, newList) -> {
                Platform.runLater(() -> {
                    updatePlayerCircle();
                });
            });
            
            // les animations sont stoppées quand on passe à l'écran de jeu
            viewModel.getJoinedGameModel().currentStateProperty().addListener((obs, oldState, newState) -> {
                if (newState == JoinedGameModel.ViewState.GAME_STARTED) {
                    stopAnimations();
                }
            });
        }
    }

    public InitScreenViewModel getViewModel() {
        return viewModel;
    }

    private void updatePlayerCircle() {
        circularPane.getChildren().clear();
        for (String player : viewModel.playersProperty()) {
            Label playerLabel = new Label(player);
            playerLabel.getStyleClass().add("player-circle-label");
            // force le label à rester droit en annulant la rotation du parent
            playerLabel.rotateProperty().bind(stackPane.rotateProperty().multiply(-1));
            
            circularPane.getChildren().add(playerLabel);
        }
    }
}
