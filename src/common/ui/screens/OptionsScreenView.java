package common.ui.screens;

import common.Config;
import common.ImageHandler;
import common.navigation.NavigationService;
import common.ui.BaseScreenView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import players.idjr.services.NavigationServiceIDJR;
import javafx.scene.paint.Color;
import java.io.InputStream;

import common.ui.ToggleSwitch;
import common.viewmodel.OptionsScreenViewModel;
import esp.services.NavigationServiceESP;

public class OptionsScreenView extends BaseScreenView {
    private OptionsScreenViewModel viewModel;
    private VBox centerVBox;
    private GridPane languesGridPane;
    private HBox themeBox;
    private ToggleSwitch themeToggle;
    private VBox couleursBox;
    
    //internationalisation
    private Label languesLabel;
    private Label themeLabel;
    private Label couleursLabel;
    private Button langueFrButton;
    private Button langueEnButton;
    private Button langueEsButton;
    private Button langueItButton;
    private Button langueDeButton;
    private Button languePtButton;


    public OptionsScreenView(NavigationService navigationService, NavigationServiceIDJR navigationServiceIdjr, NavigationServiceESP navigationServiceEsp ,  boolean isSpyUser) {
        this.viewModel = new OptionsScreenViewModel( navigationService, navigationServiceIdjr,  navigationServiceEsp ,   isSpyUser);
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
        centerVBox = new VBox(20);
        languesGridPane = new GridPane();
        themeBox = new HBox(15);
        couleursBox = new VBox(15);
        
        //internationalisation
        langueFrButton = new Button();
        langueEnButton = new Button();
        langueEsButton = new Button();
        langueItButton = new Button();
        langueDeButton = new Button();
        languePtButton = new Button();
        
        languesLabel = new Label();
        themeLabel = new Label();
    }

    private void setupComponents() {
        centerVBox.setAlignment(Pos.CENTER_LEFT);
        centerVBox.setPadding(new Insets(10, 0, 0, 0));
        centerVBox.setMaxWidth(Region.USE_PREF_SIZE);

        languesGridPane.setHgap(7.5);
        languesGridPane.setVgap(5);
        languesGridPane.setAlignment(Pos.CENTER_LEFT);

        themeBox.setAlignment(Pos.CENTER_LEFT);
        themeBox.prefWidthProperty().bind(centerVBox.widthProperty());

        couleursBox.setAlignment(Pos.CENTER_LEFT);

    }

    private void organizeComponents() {
        languesLabel = new Label();

        organizeLanguesGridPane();
        organizeThemeBox();
        organizeCouleursBox();

        VBox.setMargin(languesGridPane, new Insets(0, 0, 5, 0));
        VBox.setMargin(themeBox, new Insets(0, 0, 5, 0));

        centerVBox.getChildren().addAll(languesLabel, languesGridPane, themeBox, couleursBox);
    }

    private void organizeLanguesGridPane() {
        Button[] langueButtons = {langueFrButton, langueEnButton, langueEsButton, langueItButton, langueDeButton, languePtButton};
        // Chaque language est écrit dans sa langue donc pas internationalisé
        String[] langues = {"Français", "English", "Español", "Italiano", "Deutsch", "Português"};
        
        int row = 0, col = 0;
        for (int i = 0; i < langueButtons.length; i++) {
            Button styledButton = createLangueButton(langues[i]);
            
            langueButtons[i].setGraphic(styledButton.getGraphic());
            langueButtons[i].setStyle(styledButton.getStyle());
            langueButtons[i].setPrefSize(styledButton.getPrefWidth(), styledButton.getPrefHeight());

            languesGridPane.add(langueButtons[i], col, row);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private Button createLangueButton(String langue) {
        Button button = new Button();
        VBox content = new VBox(7.5);
        content.setAlignment(Pos.CENTER);
        
        try {
            String flagsName = switch (langue) {
                case "Français" -> "france";
                case "English" -> "amerique";
                case "Español" -> "espagne";
                case "Italiano" -> "italie";
                case "Deutsch" -> "allemagne";
                case "Português" -> "portugal";
                default -> throw new IllegalArgumentException("Langue non supportée: " + langue);
            };
            
            String flagsPath = "/common/images/" + flagsName + ".png";
            InputStream imageStream = ImageHandler.loadImage(flagsPath, langue);
            
            if (imageStream != null) {
                Image flagImage = new Image(imageStream);
                ImageView drapeau = new ImageView(flagImage);
                drapeau.setFitWidth(150);
                drapeau.setFitHeight(100);
                drapeau.setPreserveRatio(true);
                content.getChildren().add(drapeau);
            } else {
                if (Config.DEBUG_MODE) {
                    System.out.println("Utilisation d'un rectangle de remplacement pour " + langue);
                }
                Rectangle fallback = new Rectangle(150, 100);
                fallback.setFill(Color.LIGHTGRAY);
                fallback.setStroke(Color.BLACK);
                content.getChildren().add(fallback);
            }
            
        } catch (Exception e) {
            if (Config.DEBUG_MODE) {
                e.printStackTrace();
            }
            Rectangle fallback = new Rectangle(150, 100);
            fallback.setFill(Color.LIGHTGRAY);
            fallback.setStroke(Color.BLACK);
            content.getChildren().add(fallback);
        }
        
        Label langueLabel = new Label(langue);
        langueLabel.setStyle("-fx-font-size: 18px;");
        content.getChildren().add(langueLabel);
        
        button.setGraphic(content);
        button.getStyleClass().add("language-button");
        return button;
    }

    private void organizeThemeBox() {
        themeLabel = new Label("Thème sombre");
        themeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        themeToggle = new ToggleSwitch();
        HBox themeContainer = new HBox(15, themeLabel, themeToggle);
        themeContainer.setAlignment(Pos.CENTER_LEFT);
        themeBox.getChildren().add(themeContainer);
    }

    private void organizeCouleursBox() {
        couleursLabel = new Label();
        couleursLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        HBox themesBox = new HBox(15);
        themesBox.setAlignment(Pos.CENTER_LEFT);

        for (int i = 1; i <= 3; i++) {
            Button themeButton = new Button("[Thème " + i + "]");
            themesBox.getChildren().add(themeButton);
        }

        couleursBox.getChildren().addAll(couleursLabel, themesBox);
    }

    private void updateLangueButtonStyles() {
        Button[] langueButtons = {langueFrButton, langueEnButton, langueEsButton, langueItButton, langueDeButton, languePtButton};
        String selectedLanguage = viewModel.getSelectedLanguage();

        if (Config.DEBUG_MODE) {
            System.out.println("Selected language: " + selectedLanguage);
        }

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
    protected void bindToViewModel() {
        for (int i = 0; i < languesGridPane.getChildren().size(); i++) {
            Button langueButton = (Button) languesGridPane.getChildren().get(i);
            langueButton.setOnAction(event -> {
                viewModel.setSelectedLanguage(((Label)((VBox)langueButton.getGraphic()).getChildren().get(1)).getText());
                updateLangueButtonStyles();
            });
        }

        themeToggle.selectedProperty().bindBidirectional(viewModel.isDarkModeProperty());
        viewModel.themeNumberProperty().addListener((obs, oldVal, newVal) -> updateThemeButtonStyles());
        updateThemeButtonStyles();

        HBox themesBox = (HBox) couleursBox.getChildren().get(1);
        for (int i = 0; i < themesBox.getChildren().size(); i++) {
            final int index = i;
            Button themeButton = (Button) themesBox.getChildren().get(i);
            themeButton.setOnAction(event -> this.viewModel.setThemeNumber(index + 1));
        }

        backButton.setOnAction(event -> this.viewModel.onBackButtonClicked());
        
        langueFrButton.setOnAction(e -> viewModel.onLangueButtonClicked("Fr"));
        langueDeButton.setOnAction(e -> viewModel.onLangueButtonClicked("De"));
        langueEnButton.setOnAction(e -> viewModel.onLangueButtonClicked("En"));
        langueItButton.setOnAction(e -> viewModel.onLangueButtonClicked("It"));
        languePtButton.setOnAction(e -> viewModel.onLangueButtonClicked("Pt"));
        langueEsButton.setOnAction(e -> viewModel.onLangueButtonClicked("Es"));
        
        languesLabel.textProperty().bind(viewModel.languesLabelProperty());
        themeLabel.textProperty().bind(viewModel.themeLabelProperty());
        couleursLabel.textProperty().bind(viewModel.couleurLabelProperty());
        titleLabel.textProperty().bind(viewModel.titleLabelProperty());

        // Appel de la méthode pour mettre à jour les styles des boutons de langue au démarrage
        updateLangueButtonStyles();
        // Mise à jour des styles des boutons de langue lors du changement de langue
        viewModel.selectedLanguageProperty().addListener((obs, oldVal, newVal) -> updateLangueButtonStyles());
    }

    private void updateThemeButtonStyles() {
        HBox themesBox = (HBox) couleursBox.getChildren().get(1);
        for (int i = 0; i < themesBox.getChildren().size(); i++) {
            Button themeButton = (Button) themesBox.getChildren().get(i);
            if (viewModel.themeNumberProperty().get() == i + 1) {
                themeButton.getStyleClass().add("selected-button");
            } else {
                themeButton.getStyleClass().remove("selected-button");
            }
        }
    }
}