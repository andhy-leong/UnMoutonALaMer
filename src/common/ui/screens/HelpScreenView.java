package common.ui.screens;

import java.io.InputStream;

import common.ImageHandler;
import common.locales.I18N;
import common.model.HelpScreenModel;
import common.navigation.NavigationService;
import common.ui.BaseScreenView;
import common.viewmodel.HelpScreenViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class HelpScreenView extends BaseScreenView {

    private HBox centerHBox;
    private HBox bottomHBox;
    private ImageView helpImage;
    private TextFlow helpText;
    private Text helpTextNode;
    private Button bottomLeftArrowButton, bottomRightArrowButton;
    private Circle[] navigationCircles;
    private final int nbCercles = 2;
    private HelpScreenViewModel viewModel;
    private boolean isSpyUser;

    public HelpScreenView(NavigationService navigationService, boolean isSpyUser) {
        this.viewModel = new HelpScreenViewModel(new HelpScreenModel(), navigationService, isSpyUser);
        this.modelTopBar = 1;
        this.isSpyUser = isSpyUser;
        
        I18N.getLocaleProperty().addListener((observable, oldValue, newValue) -> refreshUI());

        super.init();
    }

    @Override
    protected void customizeScreen() {
        createComponents();
        setupComponents();
        organizeComponents();

        addContainerRoot(centerHBox, bottomHBox);

        viewModel.updateHelpContent();
    }

    private void createComponents() {
        centerHBox = new HBox(20);
        bottomHBox = new HBox(10);

        helpImage = new ImageView();
        helpText = new TextFlow();
        helpTextNode = new Text();
        helpText.getChildren().add(helpTextNode);

        bottomLeftArrowButton = new Button("<");
        bottomRightArrowButton = new Button(">");

        navigationCircles = new Circle[nbCercles];
        for (int i = 0; i < nbCercles; i++) {
            navigationCircles[i] = new Circle(5);
        }
    }

    private void setupComponents() {
        helpText.getStyleClass().add("help-text");

        centerHBox.setAlignment(Pos.CENTER);
        //System.out.println("centerHBox.prefWidthProperty() = " + centerHBox.prefWidthProperty().getValue());
        //centerHBox.prefWidthProperty().bind(this.widthProperty().multiply(0.8));
        //System.out.println("centerHBox.prefHeightProperty() = " + centerHBox.prefHeightProperty().getValue());
        //centerHBox.prefHeightProperty().bind(this.heightProperty().multiply(0.6));
        //centerHBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        centerHBox.setSpacing(40);

        bottomHBox.setAlignment(Pos.CENTER);
        bottomHBox.setPadding(new Insets(20));
        bottomHBox.setSpacing(20);

        bottomRightArrowButton.getStyleClass().add("arrow-button");
        bottomLeftArrowButton.getStyleClass().add("arrow-button");

        for (Circle circle : navigationCircles) {
            circle.setRadius(8);
            circle.setFill(Color.LIGHTGRAY);
        }
        navigationCircles[0].setFill(Color.GRAY);  // Le premier cercle est actif par défaut
    }

    private void organizeComponents() {
        HBox contentHBox = new HBox();
        contentHBox.setSpacing(20);
        contentHBox.prefWidthProperty().bind(this.widthProperty().multiply(0.9));
        contentHBox.prefHeightProperty().bind(this.heightProperty().multiply(0.9));
        contentHBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        contentHBox.setAlignment(Pos.CENTER);

        helpImage.setPreserveRatio(true);
        helpImage.getStyleClass().add("help-image");
        helpImage.fitWidthProperty().bind(contentHBox.widthProperty().multiply(0.5));

        helpText.maxWidthProperty().bind(contentHBox.widthProperty().multiply(0.5));
        helpText.setTextAlignment(javafx.scene.text.TextAlignment.JUSTIFY);

        VBox helpTextContainer = new VBox(helpText);
        helpTextContainer.setAlignment(Pos.CENTER);
        VBox.setVgrow(helpTextContainer, Priority.ALWAYS);

        contentHBox.getChildren().addAll(helpImage, helpTextContainer);
        centerHBox.getChildren().add(contentHBox);

        bottomHBox.getChildren().add(bottomLeftArrowButton);
        for (Circle circle : navigationCircles) {
            bottomHBox.getChildren().add(circle);
        }
        bottomHBox.getChildren().add(bottomRightArrowButton);

        VBox bottomVBox = new VBox();
        bottomVBox.setAlignment(Pos.BOTTOM_CENTER);
        bottomVBox.getChildren().add(bottomHBox);
    }

    @Override
    protected void bindToViewModel() {
    	titleLabel.textProperty().bind(viewModel.titleLabelProperty());
    	helpTextNode.textProperty().bind(viewModel.helpTextProperty());
        backButton.setOnAction(event -> viewModel.onBackButtonClicked());
        
        bottomLeftArrowButton.setOnAction(event -> viewModel.onPreviousButtonClicked());
        bottomRightArrowButton.setOnAction(event -> viewModel.onNextButtonClicked());

        // grisage des boutons de navigation en fonction de la page courante (début ou fin)
        bottomLeftArrowButton.disableProperty().bind(viewModel.currentPageProperty().isEqualTo(0));
        bottomRightArrowButton.disableProperty().bind(viewModel.currentPageProperty().isEqualTo(viewModel.getTotalPages() - 1));

        // pour chaque cercle de navigation, on associe un événement de clic
        for (int i = 1; i < bottomHBox.getChildren().size() - 1; i++) {
            Circle circle = (Circle) bottomHBox.getChildren().get(i);
            final int index = i - 1;
            circle.setOnMouseClicked(event -> {
                viewModel.onCircleClicked(index);
            });
        }
        
        viewModel.helpImageProperty().addListener((observable, oldValue, newValue) -> {
            loadImage(newValue);
        });
        
        viewModel.currentPageProperty().addListener((observable, oldValue, newValue) -> {
            for (int i = 0; i < navigationCircles.length; i++) {
                navigationCircles[i].setFill(i == newValue.intValue() ? Color.GRAY : Color.LIGHTGRAY);
            }
        });
    }

    private void loadImage(String path) {
        if (path != null) {
                try {
                    InputStream imageStream = ImageHandler.loadImage(path, null);
                    if (imageStream != null) {
                        helpImage.setImage(new Image(imageStream));
                    } else {
                        helpImage.setImage(null);
                    }
                } catch (Exception e) {
                    //System.err.println("Impossible de charger l'image d'aide : " + e.getMessage());
                    helpImage.setImage(null);
                }
            } else {
                helpImage.setImage(null);
            }
    }

    private void refreshUI() {
        helpTextNode.textProperty().bind(viewModel.helpTextProperty());
        viewModel.helpTextProperty().invalidate();
        loadImage(viewModel.helpImageProperty().get());
    }
}

