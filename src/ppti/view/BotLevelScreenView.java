package ppti.view;

import java.util.Arrays;
import java.util.List;

import common.Config;
import common.enumtype.PlayerType;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import ppti.viewmodel.BotLevelScreenViewModel;
import ppti.view.singleton.SingletonView;

public class BotLevelScreenView extends MirrorBaseView {
	private BotLevelScreenViewModel viewModel;
	private Stage stage;
	List<BaseView> viewList;

    private Button boutonCreerPartie;
    private Button invertedBoutonCreerPartie;


    private VBox vBoxBot1;
    private VBox vBoxBot2;
    private VBox vBoxBot3;
    private VBox vBoxBot4;
    private VBox vBoxBot5;

    private VBox invertedVBoxBot1;
    private VBox invertedVBoxBot2;
    private VBox invertedVBoxBot3;
    private VBox invertedVBoxBot4;
    private VBox invertedVBoxBot5;

    private HBox hBoxBots;
    private HBox invertedHBoxBots;

    private List<Node> listevBoxBot;
    private List<Node> invertedListevBoxBot;
    private List<TextField> botName;

    private ObjectProperty<PartieInfo> info;

	public BotLevelScreenView(Stage stage, BotLevelScreenViewModel viewModel) {
        super(stage, "Choix du niveau des bots", "back");
        this.stage = stage;
        this.viewModel = viewModel;
    	customizeScreen();
    	bindToViewModel();
    }


    @Override
    protected void customizeContent() {

        // Vbox contenant chaque bot individuellement
        vBoxBot1 = createBotVBox("");
        vBoxBot2 = createBotVBox("");
        vBoxBot3 = createBotVBox("");
        vBoxBot4 = createBotVBox("");
        vBoxBot5 = createBotVBox("");

        // Vbox contenant chaque bot individuellement
        invertedVBoxBot1 = createBotVBox("");
        invertedVBoxBot2 = createBotVBox("");
        invertedVBoxBot3 = createBotVBox("");
        invertedVBoxBot4 = createBotVBox("");
        invertedVBoxBot5 = createBotVBox("");

        // Hbox contenant les bots
        hBoxBots = new HBox();
        invertedHBoxBots = new HBox();
        hBoxBots.setAlignment(Pos.CENTER);
        invertedHBoxBots.setAlignment(Pos.CENTER);

        this.createBotPartieInfo();

        // Liste contenants toutes les vbox de bots
        listevBoxBot = Arrays.asList(vBoxBot1, vBoxBot2, vBoxBot3, vBoxBot4, vBoxBot5);
        invertedListevBoxBot = Arrays.asList(invertedVBoxBot1, invertedVBoxBot2, invertedVBoxBot3, invertedVBoxBot4, invertedVBoxBot5);

        // Bouton créer la partie
        boutonCreerPartie = new Button();
        invertedBoutonCreerPartie = new Button();

        // réunir les VBox bots et le bouton créer
        VBox vBoxChoixNiveauBot = new VBox(50);
        vBoxChoixNiveauBot.getChildren().addAll(hBoxBots, boutonCreerPartie);
        vBoxChoixNiveauBot.setAlignment(Pos.CENTER);

        VBox invertedVBoxChoixNiveauBot = new VBox(50);
        invertedVBoxChoixNiveauBot.getChildren().addAll(invertedHBoxBots, invertedBoutonCreerPartie);
        invertedVBoxChoixNiveauBot.setAlignment(Pos.CENTER);

        addContent(vBoxChoixNiveauBot, invertedVBoxChoixNiveauBot);

        bindingButtons();
    }

    private VBox createBotVBox(String nomBot) {
        double buttonHeight = 30;
        // -------- provisoir --------
        // Image de bot (selon le niveau)
        ImageView imageBot = new ImageView();
        imageBot.setFitWidth(5);

        Label labelNomBot = new Label(nomBot);

        ToggleGroup playerCountButtonBar = new ToggleGroup();
        viewModel.addToggles(playerCountButtonBar);

        ToggleButton botLevelA = new ToggleButton();
        ToggleButton botLevelB = new ToggleButton();
        ToggleButton botLevelC = new ToggleButton();
        botLevelA.setToggleGroup(playerCountButtonBar);
        botLevelB.setToggleGroup(playerCountButtonBar);
        botLevelC.setToggleGroup(playerCountButtonBar);

        botLevelA.setUserData(PlayerType.BOTF);
        botLevelB.setUserData(PlayerType.BOTM);
        botLevelC.setUserData(PlayerType.BOTE);

        botLevelA.setSelected(true);

        // calculer le maximum de largeur nécessaire par le texte le plus long
        StringBinding maxWidthBinding = new StringBinding() {
            {
                super.bind(botLevelA.textProperty(), botLevelB.textProperty(), botLevelC.textProperty());
            }

            @Override
            protected String computeValue() {
                double maxWidth = Math.max(
                    Math.max(botLevelA.getText().length(), botLevelB.getText().length()),
                    botLevelC.getText().length()
                );
                return String.valueOf(maxWidth * 10);
            }
        };

        botLevelA.minWidthProperty().bind(maxWidthBinding.length());
        botLevelB.minWidthProperty().bind(maxWidthBinding.length());
        botLevelC.minWidthProperty().bind(maxWidthBinding.length());

        botLevelA.getStyleClass().add("selected-button");
        botLevelA.getStyleClass().add("bot-button");
        botLevelB.getStyleClass().add("bot-button");
        botLevelC.getStyleClass().add("bot-button");

        HBox boutonDifficulte = new HBox();
        boutonDifficulte.getChildren().addAll(botLevelA, botLevelB, botLevelC);

        TextField botTextField = new TextField();
        botTextField.setMaxWidth(150);

        CheckBox isLaunchOnPPTI = new CheckBox();
        isLaunchOnPPTI.setSelected(true);

        viewModel.addTextField(botTextField);
        viewModel.addCheckBox(isLaunchOnPPTI);

        VBox vBoxBot = new VBox(10);
        vBoxBot.getChildren().addAll(imageBot, labelNomBot, boutonDifficulte, botTextField, isLaunchOnPPTI);
        vBoxBot.setAlignment(Pos.CENTER);

        vBoxBot.getStyleClass().add("container");

        return vBoxBot;
    }

    private void createBotPartieInfo() {

        //on clear la liste au cas où ce ne serait pas la première fois que l'utilisateur retourne en arrière
        hBoxBots.getChildren().clear();
        invertedHBoxBots.getChildren().clear();

        boolean first = true;

        for (int i = 0; i < viewModel.getNbBots(); i++) {
            if (!first) {
                Region space = new Region();
                HBox.setHgrow(space, Priority.ALWAYS);
                hBoxBots.getChildren().add(space);

                Region invertedSpace = new Region();
                HBox.setHgrow(invertedSpace, Priority.ALWAYS);
                invertedHBoxBots.getChildren().add(invertedSpace);
            }

            hBoxBots.getChildren().add(listevBoxBot.get(i));
            invertedHBoxBots.getChildren().add(invertedListevBoxBot.get(i));

            first = false;
        }
    }

    public void bindingButtons() {
        for (int i = 0; i < listevBoxBot.size(); i++) {
            VBox invertedVBoxBot = (VBox) invertedListevBoxBot.get(i);
            VBox vBoxBot = (VBox) listevBoxBot.get(i);
            HBox invertedHBoxBot = (HBox) invertedVBoxBot.getChildren().get(2);
            HBox hBoxBot = (HBox) vBoxBot.getChildren().get(2);
            for (int j = 0; j < 3; j++) {
                ToggleButton button = (ToggleButton) hBoxBot.getChildren().get(j);
                invertedHBoxBot.getChildren().get(j).styleProperty().bind(button.styleProperty());
                invertedHBoxBot.getChildren().get(j).setOnMouseClicked(e -> button.fire());
            }
            ((TextField)vBoxBot.getChildren().get(3)).textProperty().bindBidirectional(((TextField)invertedVBoxBot.getChildren().get(3)).textProperty());
            ((CheckBox)vBoxBot.getChildren().get(4)).selectedProperty().bindBidirectional(((CheckBox)invertedVBoxBot.getChildren().get(4)).selectedProperty());
        }

    }

    @Override
    protected void bindToViewModel() {
        info = new SimpleObjectProperty<>();
        info.bind(SingletonView.getInfo());

        info.addListener((observable, oldValue, newValue) -> {
            if (Config.DEBUG_MODE) {
                System.out.println("BotLevelView : Essaye de modification");
            }
            this.createBotPartieInfo();
        });

        boutonCreerPartie.setOnAction(e -> navigateToAcceptScreen());
        invertedBoutonCreerPartie.setOnAction(e -> navigateToAcceptScreen());

        for (int j = 0;j < listevBoxBot.size() ; j++) {
            VBox vBox = (VBox) listevBoxBot.get(j);
            ToggleButton[] buttons = new ToggleButton[3];
            ToggleButton[] revertedButtons = new ToggleButton[3];
            for (int i = 0; i < 3; i++) {
                buttons[i] = (ToggleButton) ((HBox) vBox.getChildren().get(2)).getChildren().get(i);
                revertedButtons[i] = (ToggleButton) ((HBox) ((VBox) invertedListevBoxBot.get(j)).getChildren().get(2)).getChildren().get(i);
            }


            for (int i = 0; i < 3; i++) {
                ToggleButton button = buttons[i];
                int finalI = j;
                button.setOnAction(e -> {
                    viewModel.setVirtualPlayerType(button.getUserData().toString(), finalI,((TextField)vBox.getChildren().get(3)).getText());

                    for (ToggleButton b : buttons) {
                        b.getStyleClass().remove("selected-button");
                    }

                    for (ToggleButton b : revertedButtons) {
                        b.getStyleClass().remove("selected-button");
                    }

                    button.getStyleClass().add("selected-button");

                    for (ToggleButton b : revertedButtons) {
                        if (b.getUserData().toString().equals(button.getUserData().toString())) {
                            b.getStyleClass().add("selected-button");
                        }
                    }
                });
            }
        }

        Button[] backButtons = getBackButtons();
        for(Button button : backButtons) {
            button.setOnAction(e -> navigateToPreviousScreen());
        }

        /* Internationalization */
        // titre
        Label[] mirrorTitles = super.getTitleLabels();
        for (Label label : mirrorTitles) {
            label.textProperty().bind(viewModel.getTitleLabelProperty());
        }
        // bouton créer partie
        boutonCreerPartie.textProperty().bind(viewModel.getCreatePartieButtonProperty());
        invertedBoutonCreerPartie.textProperty().bind(viewModel.getCreatePartieButtonProperty());
        // Nom des bots et difficultés
        for (int i = 0; i < 5; i++) {
            // Nom des bots
            ((Label) ((VBox) listevBoxBot.get(i)).getChildren().get(1)).textProperty().bind(viewModel.getBotNameProperty(i));
            ((Label) ((VBox) invertedListevBoxBot.get(i)).getChildren().get(1)).textProperty().bind(viewModel.getBotNameProperty(i));

            // boutons difficultés
            for (int j = 0; j < 3; j++) {
                ((ToggleButton) ((HBox) ((VBox) listevBoxBot.get(i)).getChildren().get(2)).getChildren().get(j)).textProperty().bind(viewModel.getBotLevelButtonProperty(j));
                ((ToggleButton) ((HBox) ((VBox) invertedListevBoxBot.get(i)).getChildren().get(2)).getChildren().get(j)).textProperty().bind(viewModel.getBotLevelButtonProperty(j));
            }

            // CheckBox
            ((CheckBox) ((VBox) listevBoxBot.get(i)).getChildren().get(4)).textProperty().bind(viewModel.getIsLaunchOnPPTILabelProperty());
            ((CheckBox) ((VBox) invertedListevBoxBot.get(i)).getChildren().get(4)).textProperty().bind(viewModel.getIsLaunchOnPPTILabelProperty());
        }
    }

    private void navigateToAcceptScreen() {
        try {
            viewModel.navigateToAcceptScreen();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void navigateToPreviousScreen() {
        viewModel.navigateToPreviousScreen();
    }


	@Override
	public void setViewModel(Object viewModel) {
		// TODO Auto-generated method stub
	}

}
