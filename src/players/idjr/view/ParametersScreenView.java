package players.idjr.view;

import common.ui.BaseScreenView;
import esp.services.NavigationServiceESP;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import players.idjr.viewmodel.ParametersScreenViewModel;
import players.idjr.model.ParametersScreenModel;
import players.idjr.services.NavigationServiceIDJR;
import common.navigation.NavigationService;

public class ParametersScreenView extends BaseScreenView {
    private VBox centerVBox;
    private TextField playerNameInput;
    private Label playerNameInfosLabel;
    private ToggleGroup playerCountButtonBar;
    private HBox playerCountButtons; // Changé de ButtonBar à HBox car plus adapté pour le visuel de la maquette
    private Button searchGameButton;
    private ParametersScreenViewModel viewModel;
    private Label playerCountLabel;

    public ParametersScreenView(NavigationServiceIDJR navigationServiceIdjr) {
        this.viewModel = new ParametersScreenViewModel(new ParametersScreenModel(), navigationServiceIdjr);
        this.modelTopBar = 1;

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
        centerVBox = new VBox(40);
        playerNameInput = new TextField();
        playerNameInfosLabel = new Label();
        playerCountButtonBar = new ToggleGroup();
        playerCountButtons = new HBox(0);
        searchGameButton = new Button();
    }

    private void setupComponents() {
        centerVBox.setAlignment(Pos.TOP_CENTER);
        centerVBox.setPadding(new Insets(80, 0, 0, 0));

        playerNameInput.getStyleClass().add("player-name-input");

        playerNameInfosLabel.getStyleClass().add("player-name-infos-label");
        playerNameInfosLabel.setVisible(false);

        ToggleButton threePlayersButton = new ToggleButton("3");
        ToggleButton fourPlayersButton = new ToggleButton("4");
        ToggleButton fivePlayersButton = new ToggleButton("5");
        threePlayersButton.setToggleGroup(playerCountButtonBar);
        fourPlayersButton.setToggleGroup(playerCountButtonBar);
        fivePlayersButton.setToggleGroup(playerCountButtonBar);
        threePlayersButton.setSelected(true);

        threePlayersButton.getStyleClass().add("bot-button");
        fourPlayersButton.getStyleClass().add("bot-button");
        fivePlayersButton.getStyleClass().add("bot-button");

        double buttonHeight = 40;
        double buttonWidth = buttonHeight * 4;

        threePlayersButton.getStyleClass().add("selected-button");

        HBox buttonBox = new HBox(0);
        buttonBox.getChildren().addAll(threePlayersButton, fourPlayersButton, fivePlayersButton);
        buttonBox.setAlignment(Pos.CENTER);
        playerCountButtons = buttonBox;

        searchGameButton.getStyleClass().add("search-button");
        searchGameButton.setDisable(true);

        // activer le clavier visuel de JavaFX
        playerNameInput.getProperties().put("vkType", 0);

        // activer le clavier virtuel de Windows
        /*
        // Afficher le clavier virtuel
        playerNameInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Afficher le clavier virtuel
                try {
                    Runtime.getRuntime().exec("cmd /c osk");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //else {
            //    // Fermer le clavier virtuel
            //    System.out.println("Hiding virtual keyboard");
            //}
        });
         */

        // validation du nom du joueur quand on appuie sur Entrée
        playerNameInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !searchGameButton.isDisabled()) {
                searchGameButton.fire(); // simule un clic sur le bouton
            }
        });

    }

    private void organizeComponents() {
        VBox playerNameBox = new VBox(5);
        playerNameLabel = new Label();
        HBox playerNameInputBox = new HBox(5);
        playerNameInputBox.setSpacing(20);
        playerNameInputBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(playerNameInput, Priority.ALWAYS); // étirer l'input pour s'aligner avec le reste
        playerNameInputBox.getChildren().addAll(playerNameLabel, playerNameInput);
        playerNameBox.getChildren().addAll(playerNameInputBox, playerNameInfosLabel);
        playerNameBox.setAlignment(Pos.CENTER_LEFT);

        VBox playerCountBox = new VBox(10);
        playerCountLabel = new Label();

        playerCountBox.getChildren().addAll(playerCountLabel, playerCountButtons);

        centerVBox.getChildren().addAll(
            playerNameBox,
            playerCountBox,
            searchGameButton
        );

        centerVBox.setMaxWidth(500);
    }

    @Override
    protected void bindToViewModel() {
        playerNameInput.textProperty().bindBidirectional(viewModel.playerNameProperty());
        
        for (javafx.scene.Node node : playerCountButtons.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton button = (ToggleButton) node;
                button.setOnAction(event -> {
                    int maxPlayers = Integer.parseInt(button.getText());
                    viewModel.maxPlayersProperty().set(maxPlayers);
                    
                    for (javafx.scene.Node otherNode : playerCountButtons.getChildren()) {
                        if (otherNode instanceof ToggleButton) {
                            ToggleButton otherButton = (ToggleButton) otherNode;
                            if (otherButton == button) {
                                otherButton.getStyleClass().add("selected-button");
                            } else {
                                otherButton.getStyleClass().remove("selected-button");
                            }
                        }
                    }
                });
            }
        }

        searchGameButton.setOnAction(event -> viewModel.onSearchGameButtonClicked());
        backButton.setOnAction(event -> viewModel.onBackButtonClicked());

        playerNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = viewModel.isValidPlayerName(newValue);
            searchGameButton.setDisable(newValue.isEmpty() || !isValid);
            playerNameInfosLabel.setVisible(!isValid && !newValue.isEmpty());
        });
        
        titleLabel.textProperty().bind(viewModel.titleLabelProperty());   
        playerNameInfosLabel.textProperty().bind(viewModel.playerNameInfosLabelProperty());
        searchGameButton.textProperty().bind(viewModel.searchGameButtonProperty());
        playerNameLabel.textProperty().bind(viewModel.playerNameLabelProperty());
        playerCountLabel.textProperty().bind(viewModel.playerCountLabelProperty());
        
    
    }
}
