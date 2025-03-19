package ppti.view;

import javafx.scene.control.Label;
import javafx.scene.layout.*;
import ppti.viewmodel.StartingScreenViewModel;


import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class StartingScreenView extends MirrorBaseView {
	private StartingScreenViewModel viewModel;
	private Stage stage;

    private Button RulesUpButton;
    private Button OptionsUpButton;
    private Button CreateUpButton;
    private Button RulesDownButton;
    private Button OptionsDownButton;
    private Button CreateDownButton;

    private Button QuitUpButton;
    private Button QuitDownButton;

    private HBox upHBox;
    private HBox downHBox;

    
    public StartingScreenView(Stage stage, StartingScreenViewModel viewModel) {
        super(stage, "Un mouton à la mer", null);
        this.stage = stage;
    	this.viewModel = viewModel;
    	customizeScreen();
    	bindToViewModel();
    }

    @Override
    protected void customizeContent() {
        // Etant donné que les boutons sont placés dans les HBox, on récuère les HBox de MirrorBaseView
        this.upHBox = super.getUpHBox();
        this.downHBox = super.getDownHBox();
        
        createComponents();
        setupComponents();
        organizeComponents();
            
        // puis on set les HBox de MirrorBaseView
        super.setUpHBox(this.upHBox);
        super.setDownHBox(this.downHBox);
        
        addContent(null, null); // null car on a utilisé les HBbox de MirrorBaseView
    }

    private void createComponents() {
        HBox up = new HBox();
        HBox down = new HBox();

        // Création des boutons pour le haut
        RulesUpButton = new Button();
        OptionsUpButton = new Button();
        CreateUpButton = new Button();
        QuitUpButton = new Button();

        // Création des boutons pour le bas
        RulesDownButton = new Button();
        OptionsDownButton = new Button();
        CreateDownButton = new Button();
        QuitDownButton = new Button();
    }


    private void setupComponents() {

        Button[] buttons = {RulesUpButton, OptionsUpButton, CreateUpButton, RulesDownButton, OptionsDownButton, CreateDownButton, QuitUpButton, QuitDownButton};

        for (Button button : buttons) {
            button.getStyleClass().add("starting-screen-button");
        }

        QuitDownButton.getStyleClass().add("quit-button");
        QuitUpButton.getStyleClass().add("quit-button");

        CreateDownButton.getStyleClass().add("play-button");
        CreateUpButton.getStyleClass().add("play-button");
    }

    private void organizeComponents() {
        // ajout des boutons dans les HBox
        upHBox.getChildren().addAll(CreateUpButton, createHorizontalSpacer(), OptionsUpButton, createHorizontalSpacer(), RulesUpButton, createHorizontalSpacer(), QuitUpButton);
        downHBox.getChildren().addAll(CreateDownButton, createHorizontalSpacer(), OptionsDownButton, createHorizontalSpacer(), RulesDownButton, createHorizontalSpacer(), QuitDownButton);
    }

    public Region createHorizontalSpacer() {
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        return space;
    }


    @Override
    protected void bindToViewModel() {
        CreateUpButton.setOnAction(event -> viewModel.onCreateButtonClicked());
        CreateDownButton.setOnAction(event -> viewModel.onCreateButtonClicked());

        OptionsUpButton.setOnAction(event -> viewModel.onOptionsButtonClicked(true));
        OptionsDownButton.setOnAction(event -> viewModel.onOptionsButtonClicked(false));

        RulesUpButton.setOnAction(event -> viewModel.onRulesButtonClicked(true));
        RulesDownButton.setOnAction(event -> viewModel.onRulesButtonClicked(false));

        QuitUpButton.setOnAction(event -> viewModel.onQuitButtonClicked());
        QuitDownButton.setOnAction(event -> viewModel.onQuitButtonClicked());

        /* Internationalisation */
        CreateUpButton.textProperty().bind(viewModel.getCreateButtonProperty());
        CreateDownButton.textProperty().bind(viewModel.getCreateButtonProperty());
        OptionsUpButton.textProperty().bind(viewModel.getOptionsButtonProperty());
        OptionsDownButton.textProperty().bind(viewModel.getOptionsButtonProperty());
        RulesUpButton.textProperty().bind(viewModel.getRulesButtonProperty());
        RulesDownButton.textProperty().bind(viewModel.getRulesButtonProperty());
        QuitUpButton.textProperty().bind(viewModel.getQuitButtonProperty());
        QuitDownButton.textProperty().bind(viewModel.getQuitButtonProperty());
        Label[] mirrorTitles = super.getTitleLabels();
        for (Label label : mirrorTitles) {
            label.textProperty().bind(viewModel.getTitleLabelProperty());
        }
    }

	@Override
	public void setViewModel(Object viewModel) {
		this.viewModel = (StartingScreenViewModel) viewModel;		
	}

	

}
