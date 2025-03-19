package ppti.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ppti.model.JoueurInfo;
import ppti.viewmodel.EndRoundScreenViewModel;
import ppti.viewmodel.GameScreenViewModel;

import java.util.Map;

public class EndRoundScreenView extends PopUpBaseView implements MapChangeListener<String, JoueurInfo> {
    private GameScreenViewModel viewModel;

    private BooleanProperty isEndGame;

    private VBox centerVBox;
    private Label scoreLabel;
    private GridPane playerScoresGridPane;
    private HBox buttonHBox;
    private Button newRoundButton;
    private Button replayButton;
    private Button endGameButton;

    private VBox mirrorCenterVBox;
    private Label mirrorScoreLabel;
    private GridPane mirrorPlayerScoresGridPane;
    private HBox mirrorButtonHBox;
    private Button mirrorNewRoundButton;
    private Button mirrorReplayButton;
    private Button mirrorEndGameButton;

    private int row;

    public EndRoundScreenView(GameScreenViewModel viewModel, Stage stage) {
        super(false, stage, false, true);

        this.viewModel = viewModel;
        isEndGame = new SimpleBooleanProperty();
        customizeCenter();
        bindToViewModel();
    }

    @Override
    protected void customizeCenter() {
        if (isEndGame == null) {
            return;
        }

        createComponents();
        setupComponents();
        organizeComponents();

        addCenterRoot(centerVBox, mirrorCenterVBox);
    }

    private void createComponents() {
        centerVBox = new VBox();
        scoreLabel = new Label("Scores :");
        playerScoresGridPane = new GridPane();
        buttonHBox = new HBox();

        mirrorCenterVBox = new VBox();
        mirrorScoreLabel = new Label();
        mirrorPlayerScoresGridPane = new GridPane();
        mirrorButtonHBox = new HBox();

        replayButton = new Button("Rejouer");
        endGameButton = new Button("Retourner au Menu");
        newRoundButton = new Button("Nouvelle Manche");

        mirrorReplayButton = new Button("Rejouer");
        mirrorEndGameButton = new Button("Retourner au Menu");
        mirrorNewRoundButton = new Button("Nouvelle Manche");

        row = 0;

        replayButton.setVisible(false);
        endGameButton.setVisible(false);
    }

    private void setupComponents() {
        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.setPadding(new Insets(20));
        mirrorCenterVBox.setAlignment(Pos.CENTER);
        mirrorCenterVBox.setPadding(new Insets(20));

        playerScoresGridPane.setAlignment(Pos.CENTER);
        playerScoresGridPane.setHgap(15);
        playerScoresGridPane.setVgap(20);
        mirrorPlayerScoresGridPane.setAlignment(Pos.CENTER);
        mirrorPlayerScoresGridPane.setHgap(15);
        mirrorPlayerScoresGridPane.setVgap(20);

        buttonHBox.setAlignment(Pos.CENTER);
        mirrorButtonHBox.setAlignment(Pos.CENTER);

    }

    private Region createVSpacer() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private Region createHSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void organizeComponents() {
        Region spacer1 = new Region();
        Region spacer2 = new Region();
        VBox.setVgrow(spacer1, Priority.ALWAYS);
        VBox.setVgrow(spacer2, Priority.ALWAYS);

        centerVBox.getChildren().addAll(scoreLabel, createVSpacer(), playerScoresGridPane, createVSpacer(), buttonHBox);
        mirrorCenterVBox.getChildren().addAll(mirrorScoreLabel, createVSpacer(), mirrorPlayerScoresGridPane, createVSpacer(), mirrorButtonHBox);
    }

    private void updatePlayerScoresGridPane(String nom,IntegerProperty score) {
        //SENS NORMAL
        Label playerNameLabel = new Label(nom);
        Label playerScoreLabel = new Label(score.getValue().toString());
        playerScoreLabel.textProperty().bind(score.asString("%d"));
        playerScoresGridPane.add(playerNameLabel, 0, row);
        playerScoresGridPane.add(playerScoreLabel, 1, row);

        // SENS INVERSE
        Label mirrorPlayerNameLabel = new Label(nom);
        Label mirrorPlayerScoreLabel = new Label(score.getValue().toString());
        mirrorPlayerScoreLabel.textProperty().bind(score.asString("%d"));
        mirrorPlayerScoresGridPane.add(mirrorPlayerNameLabel, 0, row);
        mirrorPlayerScoresGridPane.add(mirrorPlayerScoreLabel, 1, row);

        row++;
    }

    @Override
    protected void bindToViewModel() {
        // permet d'ajouter le score de tout les joueurs
        viewModel.getIdUsers().addListener(this);

        isEndGame = new SimpleBooleanProperty();
        isEndGame.bind(viewModel.isEndGameProperty());
        isEndGame.addListener((observable,oldValue,newValue) -> {
            buttonHBox.getChildren().clear();
            mirrorButtonHBox.getChildren().clear();
            if (newValue) {
                Region hSapcer1 = new Region();
                Region hSapcer2 = new Region();
                Region hSapcer3 = new Region();
                HBox.setHgrow(hSapcer1, Priority.ALWAYS);
                HBox.setHgrow(hSapcer2, Priority.ALWAYS);
                HBox.setHgrow(hSapcer3, Priority.ALWAYS);

                Region mirrorHSpacer1 = new Region();
                Region mirrorHSpacer2 = new Region();
                Region mirrorHSpacer3 = new Region();
                HBox.setHgrow(mirrorHSpacer1, Priority.ALWAYS);
                HBox.setHgrow(mirrorHSpacer2, Priority.ALWAYS);
                HBox.setHgrow(mirrorHSpacer3, Priority.ALWAYS);

                buttonHBox.getChildren().addAll(hSapcer1, replayButton, hSapcer2, endGameButton, hSapcer3);
                newRoundButton.setVisible(false);
                replayButton.setVisible(true);
                endGameButton.setVisible(true);

                mirrorButtonHBox.getChildren().addAll(mirrorHSpacer1,mirrorReplayButton,mirrorHSpacer2,mirrorEndGameButton,mirrorHSpacer3);
            } else {
                buttonHBox.getChildren().add(newRoundButton);
                newRoundButton.setVisible(true);
                replayButton.setVisible(false);
                endGameButton.setVisible(false);

                mirrorButtonHBox.getChildren().addAll(mirrorNewRoundButton);
            }
        });

        replayButton.setOnAction(event -> viewModel.onReplayButtonClicked());
        endGameButton.setOnAction(event -> viewModel.onEndGameButtonClicked());
        newRoundButton.setOnAction(event -> viewModel.continuerManche());

        mirrorReplayButton.setOnAction(replayButton.getOnAction());
        mirrorEndGameButton.setOnAction(endGameButton.getOnAction());
        mirrorNewRoundButton.setOnAction(newRoundButton.getOnAction());

        mirrorReplayButton.visibleProperty().bind(replayButton.visibleProperty());
        mirrorEndGameButton.visibleProperty().bind(endGameButton.visibleProperty());
        mirrorNewRoundButton.visibleProperty().bind(newRoundButton.visibleProperty());

        // binding des strings (internationalisation)
        titleLabel.textProperty().bind(viewModel.titleProperty());
        mirrorTitleLabel.textProperty().bind(viewModel.titleProperty());
        scoreLabel.textProperty().bind(viewModel.scoreLabelProperty());
        mirrorScoreLabel.textProperty().bind(viewModel.scoreLabelProperty());
        replayButton.textProperty().bind(viewModel.replayButtonProperty());
        mirrorReplayButton.textProperty().bind(viewModel.replayButtonProperty());
        endGameButton.textProperty().bind(viewModel.endGameButtonProperty());
        mirrorEndGameButton.textProperty().bind(viewModel.endGameButtonProperty());
        newRoundButton.textProperty().bind(viewModel.newRoundButtonProperty());
        mirrorNewRoundButton.textProperty().bind(viewModel.newRoundButtonProperty());
    }

    @Override
    public void setViewModel(Object viewModel) {

    }

    @Override
    public void onChanged(Change<? extends String, ? extends JoueurInfo> change) {
        if(change.wasAdded()) {
            updatePlayerScoresGridPane(change.getValueAdded().getNom(),change.getValueAdded().scoreProperty());
        }
    }
}