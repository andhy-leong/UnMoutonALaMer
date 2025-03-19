package ppti.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ppti.viewmodel.PauseScreenViewModel;

public class PauseScreenView extends PopUpBaseView {
    private PauseScreenViewModel viewModel;

    private VBox centerVBox;
    private VBox mirrorCenterVBox;

    private Button settingsButton;
    private Button resumeButton;
    private Button quitButton;
    private Button mirrorSettingsButton;
    private Button mirrorResumeButton;
    private Button mirrorQuitButton;

    public PauseScreenView(PauseScreenViewModel viewModel, Stage stage) {
        super(false, stage, false, true);
        this.viewModel = viewModel;
        customizeCenter();
        bindToViewModel();
    }

    @Override
    protected void customizeCenter() {
        setTitle("Jeu en pause");
        createComponents();
        setupComponents();
        organizeComponents();

        addCenterRoot(centerVBox, mirrorCenterVBox);
    }

     private void createComponents() {
        centerVBox = new VBox();
        settingsButton = new Button();
        resumeButton = new Button();
        quitButton = new Button();

        mirrorCenterVBox = new VBox();
        mirrorSettingsButton = new Button();
        mirrorResumeButton = new Button();
        mirrorQuitButton = new Button();
     }

     private void setupComponents() {
        centerVBox.setSpacing(20);
        mirrorCenterVBox.setSpacing(20);
        centerVBox.setAlignment(Pos.CENTER);
        mirrorCenterVBox.setAlignment(Pos.CENTER);
     }

     private void organizeComponents() {
        centerVBox.getChildren().addAll(settingsButton, resumeButton, quitButton);
        mirrorCenterVBox.getChildren().addAll(mirrorSettingsButton, mirrorResumeButton, mirrorQuitButton);
     }

    @Override
    protected void bindToViewModel() {
        // biding des strings (internationalisation)
        for (int i = 0; i < super.getTitleLabel().length; i++) {
            super.getTitleLabel()[i].textProperty().bind(viewModel.titleProperty());
        }
        settingsButton.textProperty().bind(viewModel.settingsButtonProperty());
        mirrorSettingsButton.textProperty().bind(viewModel.settingsButtonProperty());
        resumeButton.textProperty().bind(viewModel.resumeButtonProperty());
        mirrorResumeButton.textProperty().bind(viewModel.resumeButtonProperty());
        quitButton.textProperty().bind(viewModel.quitButtonProperty());
        mirrorQuitButton.textProperty().bind(viewModel.quitButtonProperty());

        // biding des actions
        settingsButton.setOnAction(event -> viewModel.onSettingsButtonClicked());
        mirrorSettingsButton.setOnAction(event -> viewModel.onSettingsButtonClicked());
        resumeButton.setOnAction(event -> viewModel.onResumeButtonClicked());
        mirrorResumeButton.setOnAction(event -> viewModel.onResumeButtonClicked());
        quitButton.setOnAction(event -> viewModel.onQuitButtonClicked());
        mirrorQuitButton.setOnAction(event -> viewModel.onQuitButtonClicked());

        backgroundRegion.setOnMouseClicked(event -> {
            viewModel.onResumeButtonClicked();
        });
    }

    @Override
    public void setViewModel(Object viewModel) {

    }
}
