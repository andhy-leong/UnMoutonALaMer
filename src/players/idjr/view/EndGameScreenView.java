package players.idjr.view;

import common.ui.BaseScreenView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import players.idjr.model.EndGameScreenModel;
import players.idjr.services.NavigationServiceIDJR;
import players.idjr.viewmodel.EndGameScreenViewModel;

public class EndGameScreenView extends BaseScreenView {
    private EndGameScreenViewModel viewModel;

    /* partie gauche */
    private Label partNameLabel;

    /* conteneur principal */
    private VBox centerVBox;
    private GridPane centerGridPane;

    private Label scoreLabel;
    private Label scoreValueLabel;
    private Label lostBuoysLabel;
    private Label lostBuoysValueLabel;

    /* Texte du haut */
    private Label topLabel;
    
    


    public EndGameScreenView(NavigationServiceIDJR navigationService) {
        this.viewModel = new EndGameScreenViewModel(new EndGameScreenModel(), navigationService);
        this.modelTopBar = 4;

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
        centerVBox = new VBox(20);

        /* partie gauche */
        partNameLabel = new Label();

        /* Texte du haut */
        topLabel = new Label();

        /* Zone centrale */
        centerGridPane = new GridPane();

        scoreLabel = new Label();
        scoreValueLabel = new Label();
        lostBuoysLabel = new Label();
        lostBuoysValueLabel = new Label();
    }

    private void setupComponents() {
        centerVBox.setPadding(new Insets(20, 10, 30, 10));
        centerVBox.maxWidthProperty().bind(this.widthProperty());
        centerVBox.minWidthProperty().bind(this.heightProperty());
        centerVBox.setAlignment(Pos.CENTER);

        /* Texte du haut */
        topLabel.getStyleClass().add("title-idjr");
        topLabel.setAlignment(Pos.CENTER);
        topLabel.prefWidthProperty().bind(centerVBox.widthProperty());

        /* Zone centrale */
        centerGridPane.setHgap(40);
        centerGridPane.setVgap(15);
        centerGridPane.add(scoreLabel, 0, 0);
        centerGridPane.add(scoreValueLabel, 1, 0);
        centerGridPane.add(lostBuoysLabel, 0, 1);
        centerGridPane.add(lostBuoysValueLabel, 1, 1);

        // centrer le gridPane
        centerGridPane.setAlignment(Pos.CENTER);
    }

    private void organizeComponents() {
        /* partie gauche */
        leftVBox.getChildren().add(partNameLabel);

        /* Zone centrale */

        Region vSapce1 = new Region();
        Region vSapce2 = new Region();
        VBox.setVgrow(vSapce1, Priority.ALWAYS);
        VBox.setVgrow(vSapce2, Priority.ALWAYS);
        centerVBox.getChildren().addAll(topLabel, vSapce1, centerGridPane, vSapce2);
    }

    @Override
    protected void bindToViewModel() {
        menuButton.setOnAction(event -> viewModel.onMenuButtonClicked());

        menuButton.textProperty().bind(viewModel.menuButtonProperty());

        playerNameLabel.textProperty().bind(viewModel.playerNameProperty());
        partNameLabel.textProperty().bind(viewModel.partNameLabelProperty());

        topLabel.textProperty().bind(viewModel.topLabelProperty());
        scoreLabel.textProperty().bind(viewModel.scoreLabelProperty());
        lostBuoysLabel.textProperty().bind(viewModel.lostBuoysLabelProperty());

        scoreValueLabel.textProperty().bind(viewModel.scoreValueLabelProperty());
        lostBuoysValueLabel.textProperty().bind(viewModel.lostBuoysValueLabelProperty());
        

        
        titleLabel.textProperty().bind(viewModel.titleLabelProperty());
    }

    public void setViewModel(EndGameScreenViewModel newViewModel) {
        this.viewModel = newViewModel;
        bindToViewModel();
    }
}
