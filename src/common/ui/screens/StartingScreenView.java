package common.ui.screens;

import common.ui.BaseScreenView;
import common.viewmodel.StartingScreenViewModel;
import esp.services.NavigationServiceESP;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import players.idjr.services.NavigationServiceIDJR;
import common.navigation.NavigationService;

public class StartingScreenView extends BaseScreenView {
    private StartingScreenViewModel viewModel;
    private Button playButton;
    private Button optionsButton;
    private Button helpButton;
    private VBox centerVBox;
    private VBox buttonsVBox;
    private Label subtitleLabel;
    private boolean isSpyUser;

    public StartingScreenView(NavigationService navigationService, NavigationServiceIDJR navigationServiceIdjr, NavigationServiceESP navigationServiceEsp ,  boolean isSpyUser) {
        this.viewModel = new StartingScreenViewModel(navigationService,navigationServiceIdjr,navigationServiceEsp,isSpyUser);
        this.modelTopBar = 2;
        this.isSpyUser = isSpyUser;

        super.init();
    }
    
    

    @Override
    protected void customizeScreen() {
        createComponents();
        setupComponents();
        organizeComponents();

        addContainerRoot(centerVBox, null);
    }

    private void createComponents() {
        playButton = new Button();
        optionsButton = new Button();
        helpButton = new Button();
        centerVBox = new VBox(20);
        buttonsVBox = new VBox(10);
        if (isSpyUser) {
            subtitleLabel = new Label();
        }
    }

    private void setupComponents() {
        playButton.getStyleClass().addAll("idjr-starting-screen-button", "play-button");
        optionsButton.getStyleClass().add("idjr-starting-screen-button");
        helpButton.getStyleClass().add("idjr-starting-screen-button");
        quitButton.getStyleClass().add("idjr-starting-screen-button");

        if (isSpyUser) {
            subtitleLabel.getStyleClass().add("subtitle-label");
        }

        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.setSpacing(50);

        buttonsVBox.setAlignment(Pos.CENTER);
        buttonsVBox.setSpacing(20);
    }

    private void organizeComponents() {
        buttonsVBox.getChildren().addAll(playButton, optionsButton, helpButton);
        if (isSpyUser) {
            centerVBox.getChildren().addAll(subtitleLabel, buttonsVBox);
        } else {
            centerVBox.getChildren().addAll(buttonsVBox);
        }
    }

    @Override
    protected void bindToViewModel() {
        playButton.setOnAction(event -> viewModel.onPlayButtonClicked());
        optionsButton.setOnAction(event -> viewModel.onOptionsButtonClicked());
        helpButton.setOnAction(event -> viewModel.onHelpButtonClicked());
        quitButton.setOnAction(event -> viewModel.onQuitButtonClicked());
    
        titleLabel.textProperty().bind(viewModel.titleLabelProperty());
        playButton.textProperty().bind(viewModel.playButtonProperty());
        optionsButton.textProperty().bind(viewModel.optionsButtonProperty());
        helpButton.textProperty().bind(viewModel.helpButtonProperty());
        quitButton.textProperty().bind(viewModel.quitButtonProperty());
        
        if (isSpyUser) {
            subtitleLabel.textProperty().bind(viewModel.subtitleLabelProperty());
        }
    }
} 