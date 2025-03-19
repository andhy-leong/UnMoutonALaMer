package ppti.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import ppti.viewmodel.OptionsScreenViewModel;
import common.ImageHandler;
import common.ui.ToggleSwitch;

import java.io.InputStream;

public class OptionsScreenView extends PopUpBaseView {
    private OptionsScreenViewModel viewModel;

    private VBox centerVBox;
    private HBox darkModeHBox;
    private VBox themeVBox;
    private HBox themeHBox;
    private VBox languesVBox;
    private HBox languesHBox;

    private Label darkModeLabel;
    private ToggleSwitch themeToggle;

    private Label themeLabel;
    private Button themeButton1;
    private Button themeButton2;
    private Button themeButton3;
    private Region themeRegion1;
    private Region themeRegion2;

    private Label languesLabel;
    private ScrollPane scrallBar;
    private Button langueFrButton;
    private Button langueEnButton;
    private Button langueEsButton;
    private Button langueItButton;
    private Button langueDeButton;
    private Button languePtButton;

    public OptionsScreenView(OptionsScreenViewModel viewModel, Stage stage, Boolean isScreenRotated) {
        super(true, stage, isScreenRotated);
        this.viewModel = viewModel;
        bindToViewModel();
    }

    @Override
    protected void customizeCenter() {
        createComponents();
        setupComponents();
        organizeComponents();

        addCenterRoot(centerVBox, null);
    }

    private void createComponents() {
        centerVBox = new VBox(20);

        darkModeHBox = new HBox(15);
        themeHBox = new HBox();
        themeVBox = new VBox();
        themeRegion1 = new Region();
        themeRegion2 = new Region();

        themeToggle = new ToggleSwitch();

        themeButton1 = new Button();
        themeButton2 = new Button();
        themeButton3 = new Button();

        languesHBox = new HBox();
        languesVBox = new VBox();
        scrallBar = new ScrollPane();

        langueFrButton = new Button();
        langueEnButton = new Button();
        langueEsButton = new Button();
        langueItButton = new Button();
        langueDeButton = new Button();
        languePtButton = new Button();

        themeLabel = new Label();
        darkModeLabel = new Label();
        languesLabel = new Label();
    }

    private void setupComponents() {
        /* setup zone de theme */

        // darkMode
        darkModeHBox.getChildren().addAll(darkModeLabel, themeToggle);

        Button[] themeButtons = {themeButton1, themeButton2, themeButton3};

        for (int i = 0; i < themeButtons.length; i++) {
            themeButtons[i].setPrefSize(140, 100);
            themeButtons[i].getStyleClass().add("theme-button");
            final int themeNumber = i + 1;
            themeButtons[i].setOnAction(event -> viewModel.setThemeNumber(themeNumber));
        }

        HBox.setHgrow(themeRegion1, Priority.ALWAYS);
        HBox.setHgrow(themeRegion2, Priority.ALWAYS);

        themeHBox.setPadding(new Insets(10, 0, 10, 0));

        /* setup zone de langues */

        String[] langues = {"Français", "English", "Español", "Italiano", "Deutsch", "Português"};
        Button[] langueButtons = {langueFrButton, langueEnButton, langueEsButton, langueItButton, langueDeButton, languePtButton};

        for (int i = 0; i < langues.length; i++) {
            VBox content = new VBox(7.5);
            content.setAlignment(Pos.CENTER);

            try {
                String flagsName = switch (langues[i]) {
                    case "Français" -> "france";
                    case "English" -> "amerique";
                    case "Español" -> "espagne";
                    case "Italiano" -> "italie";
                    case "Deutsch" -> "allemagne";
                    case "Português" -> "portugal";
                    default -> throw new IllegalArgumentException("Langue non supportée");
                };
                
                String flagsPath = "/common/images/" + flagsName + ".png";
                InputStream imageStream = ImageHandler.loadImage(flagsPath, "Drapeau non trouvé");
                
                if (imageStream != null) {
                    Image flagImage = new Image(imageStream);
                    ImageView drapeau = new ImageView(flagImage);
                    drapeau.setFitWidth(140);
                    drapeau.setFitHeight(100);
                    drapeau.setPreserveRatio(true);
                    content.getChildren().add(drapeau);
                } else {
                    Rectangle fallback = new Rectangle(140, 100);
                    fallback.setFill(Color.LIGHTGRAY);
                    fallback.setStroke(Color.BLACK);
                    content.getChildren().add(fallback);
                }
            } catch (Exception e) {
                Rectangle fallback = new Rectangle(140, 100);
                fallback.setFill(Color.LIGHTGRAY);
                fallback.setStroke(Color.BLACK);
                content.getChildren().add(fallback);
            }

            Label langueLabel = new Label(langues[i]);
            content.getChildren().add(langueLabel);

            content.getStyleClass().add("language-vbox-content");
            
            langueButtons[i].getStyleClass().add("language-button");
            langueButtons[i].setGraphic(content);
            
            languesHBox.getChildren().add(langueButtons[i]);
        }

        languesHBox.setSpacing(20);
        languesHBox.getStyleClass().add("pop-up-hbox");

        scrallBar.setContent(languesHBox);
        scrallBar.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrallBar.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        scrallBar.getStyleClass().add("pop-up-hbox");
    }

    private void organizeComponents() {
        themeHBox.getChildren().addAll(themeButton1, themeRegion1, themeButton2, themeRegion2, themeButton3);
        themeVBox.getChildren().addAll(themeLabel, themeHBox);

        languesVBox.getChildren().addAll(languesLabel, scrallBar);

        centerVBox.getChildren().addAll(darkModeHBox, themeVBox, languesVBox);
    }

    @Override
    protected void bindToViewModel() {
        /* Back action */
        backButton.setOnAction(event -> viewModel.onBackButtonClicked());
        backgroundRegion.setOnMouseClicked(event -> {
            viewModel.onBackButtonClicked();
        });

        /* Theme */
        themeToggle.selectedProperty().bindBidirectional(viewModel.isDarkModeProperty());

        viewModel.themeNumberProperty().addListener((obs, oldVal, newVal) -> updateThemeButtonStyles());
        updateThemeButtonStyles(); // Appel initial pour mettre à jour les styles des boutons de thème

        /* Langues */
        langueFrButton.setOnAction(e -> viewModel.onLangueButtonClicked("Fr"));
        langueDeButton.setOnAction(e -> viewModel.onLangueButtonClicked("De"));
        langueEnButton.setOnAction(e -> viewModel.onLangueButtonClicked("En"));
        langueItButton.setOnAction(e -> viewModel.onLangueButtonClicked("It"));
        languePtButton.setOnAction(e -> viewModel.onLangueButtonClicked("Pt"));
        langueEsButton.setOnAction(e -> viewModel.onLangueButtonClicked("Es"));

        // Appel de la méthode pour mettre à jour les styles des boutons de langue au démarrage
        updateLangueButtonStyles();
        // Mise à jour des styles des boutons de langue lors du changement de langue
        viewModel.selectedLanguageProperty().addListener((obs, oldVal, newVal) -> updateLangueButtonStyles());

        /* Internationalization */
        titleLabel.textProperty().bind(viewModel.titleLabelProperty());
        darkModeLabel.textProperty().bind(viewModel.darkModeLabelProperty());
        themeLabel.textProperty().bind(viewModel.themesLabelProperty());
        themeButton1.textProperty().bind(viewModel.theme1ButtonProperty());
        themeButton2.textProperty().bind(viewModel.theme2ButtonProperty());
        themeButton3.textProperty().bind(viewModel.theme3ButtonProperty());
        languesLabel.textProperty().bind(viewModel.languesLabelProperty());
    }

    private void updateThemeButtonStyles() {
        Button[] themeButtons = {themeButton1, themeButton2, themeButton3};
        for (int i = 0; i < themeButtons.length; i++) {
            if (viewModel.themeNumberProperty().get() == i + 1) {
                themeButtons[i].getStyleClass().add("selected-button");
            } else {
                themeButtons[i].getStyleClass().remove("selected-button");
            }
        }
    }

    private void updateLangueButtonStyles() {
        Button[] langueButtons = {langueFrButton, langueEnButton, langueEsButton, langueItButton, langueDeButton, languePtButton};
        String selectedLanguage = viewModel.getSelectedLanguage();

        for (Button langueButton : langueButtons) {
            Label langueLabel = (Label) ((VBox) langueButton.getGraphic()).getChildren().get(1);
            if (langueLabel.getText().equals(selectedLanguage)) {
                langueButton.getStyleClass().add("selected-button");
            } else {
                langueButton.getStyleClass().remove("selected-button");
            }
        }
    }

    @Override
    public void setViewModel(Object viewModel) {

    }
}