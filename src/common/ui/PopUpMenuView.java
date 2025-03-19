package common.ui;

import common.navigation.NavigationService;
import common.viewmodel.PopUpMenuViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/*
 * Popup du menu à afficher par dessus un autre écran
 */
public class PopUpMenuView extends StackPane {
    private PopUpMenuViewModel viewModel;

    private Region backgroundRegion;

    private VBox menuVBox;
    private VBox buttonVBox;

    private Button menuButton;

    private Button optionsButton;
    private Button helpButton;
    private Button quitButton;

    public PopUpMenuView(NavigationService navigationService) {
        this.viewModel = new PopUpMenuViewModel(navigationService);

        initializeBaseComponents();
        setupBaseComponents();
        organizeBaseComponents();
        bindToViewModel();
    }

    private void initializeBaseComponents() {
        backgroundRegion = new Region();

        menuVBox = new VBox(40);
        buttonVBox = new VBox(20);

        menuButton = new Button("[<MENU]");

        optionsButton = new Button("[OPTIONS]");
        helpButton = new Button("[HELP]");
        quitButton = new Button("[QUIT");
    }

    private void setupBaseComponents() {
        backgroundRegion.getStyleClass().add("background-region");

        menuVBox.setAlignment(Pos.TOP_LEFT);
        buttonVBox.setAlignment(Pos.TOP_CENTER);

        menuVBox.setMinWidth(180);
        menuVBox.setMaxWidth(180);
        menuVBox.maxHeightProperty().bind(buttonVBox.heightProperty().add(menuButton.heightProperty()).add(80));
        menuVBox.getStyleClass().add("pop-up-vbox");
        menuVBox.setPadding(new Insets(31, 10, 20, 10));

        menuButton.getStyleClass().addAll("pop-up-button", "menu-button");
        optionsButton.getStyleClass().add("pop-up-button");
        helpButton.getStyleClass().add("pop-up-button");
        quitButton.getStyleClass().add("pop-up-button");

        menuButton.setPrefHeight(30);
        menuButton.setPrefWidth(120);
        optionsButton.prefWidthProperty().bind(buttonVBox.widthProperty());
        helpButton.prefWidthProperty().bind(buttonVBox.widthProperty());
        quitButton.prefWidthProperty().bind(buttonVBox.widthProperty());

        setVisible(false);
    }

    private void organizeBaseComponents() {
        buttonVBox.getChildren().addAll(
                optionsButton,
                helpButton,
                quitButton);

        menuVBox.getChildren().addAll(menuButton, buttonVBox);

        this.getChildren().addAll(backgroundRegion, menuVBox);
        StackPane.setAlignment(menuVBox, Pos.TOP_LEFT);
    }

    protected void bindToViewModel() {
        backgroundRegion.setOnMouseClicked(event -> {
            viewModel.onMenuButtonClicked();
        });

        menuButton.setOnAction(event -> viewModel.onMenuButtonClicked());
        optionsButton.setOnAction(event -> viewModel.onOptionsButtonClicked());
        helpButton.setOnAction(event -> viewModel.onHelpButtonClicked());
        quitButton.setOnAction(event -> viewModel.onQuitButtonClicked());
        
        menuButton.textProperty().bind(viewModel.menuButtonProperty());
        optionsButton.textProperty().bind(viewModel.optionsButtonProperty());
        helpButton.textProperty().bind(viewModel.helpButtonProperty());
        quitButton.textProperty().bind(viewModel.quitButtonProperty());
        
    }

    public void updateVisibility(boolean visible) {
        javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(300));
        VBox vbox = (VBox) this.getChildren().getLast();
        slideIn.setNode(vbox);
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300));
        Region region = (Region) this.getChildren().getFirst();
        fade.setNode(region);

        if (visible) {
            setVisible(true);
            fade.setFromValue(0);
            fade.setToValue(1);
            slideIn.setFromX(-vbox.getWidth());
            slideIn.setToX(0);
            slideIn.play();
            fade.play();
        } else {
            fade.setFromValue(1);
            fade.setToValue(0);
            slideIn.setFromX(0);
            slideIn.setToX(-vbox.getWidth());
            slideIn.play();
            fade.play();
        }

        setDisable(!visible);
    }
}
