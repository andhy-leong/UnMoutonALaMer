package ppti.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import ppti.model.HelpScreenModel;
import ppti.viewmodel.HelpScreenViewModel;

public class HelpScreenView extends PopUpBaseView {
    private HelpScreenViewModel viewModel;

	private VBox centerVBox;
	private HBox bottomHBox;
	private TextFlow helpText;
	private Text helpTextNode;
	private Button bottomLeftArrowButton, bottomRightArrowButton;
    private Circle[] navigationCircles;

	private final int nbCercles = 3;

	public HelpScreenView(HelpScreenViewModel viewModel, Stage stage,Boolean isScreenRotated) {
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
		centerVBox = new VBox();

		/* zone de texte */
		helpText = new TextFlow();
        helpTextNode = new Text();
        helpText.getChildren().add(helpTextNode);

		/* Barre de navigaiton */
		bottomHBox = new HBox();

		bottomLeftArrowButton = new Button("<");
        bottomRightArrowButton = new Button(">");

        navigationCircles = new Circle[nbCercles];
        for (int i = 0; i < nbCercles; i++) {
            navigationCircles[i] = new Circle(5);
        }
	}

	private void setupComponents() {
		/* zone de texte */
		helpText.maxWidthProperty().bind(centerVBox.widthProperty().multiply(0.9));
        helpText.prefHeightProperty().bind(centerVBox.heightProperty());
		VBox.setMargin(helpText, new Insets(20, 20, 20, 20));

		/* Barre de navigaiton */

        for (Circle circle : navigationCircles) {
            circle.setRadius(8);
            circle.setFill(Color.LIGHTGRAY);
        }
        navigationCircles[0].setFill(Color.GRAY);  // Le premier cercle est actif par défaut
	}

	private void organizeComponents() {
		/* Barre de navigaiton */
		bottomHBox.getChildren().add(bottomLeftArrowButton);
        for (Circle circle : navigationCircles) {
            bottomHBox.getChildren().add(circle);
        }
        bottomHBox.getChildren().add(bottomRightArrowButton);
		bottomHBox.setAlignment(Pos.CENTER);
        bottomHBox.setPadding(new Insets(20));
        bottomHBox.setSpacing(20);

		/* ajout des éléments dans leur conteneur */
		centerVBox.getChildren().addAll(helpText, bottomHBox);
	}

	@Override
    protected void bindToViewModel() {
		/* Back button */
		backButton.setOnAction(event -> viewModel.onBackButtonClicked());
        backgroundRegion.setOnMouseClicked(event -> {
            viewModel.onBackButtonClicked();
        });

		/* Text */
		helpTextNode.textProperty().bind(viewModel.helpTextProperty());

		/* Navigation */
		bottomLeftArrowButton.setOnAction(event -> viewModel.onPreviousButtonClicked());
        bottomRightArrowButton.setOnAction(event -> viewModel.onNextButtonClicked());

        // griser les flèches de navigation en fonction de la page courante (début ou fin)
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

        // changement de couleur des cercles de navigation en fonction de la page courante
        viewModel.currentPageProperty().addListener((observable, oldValue, newValue) -> {
            for (int i = 0; i < navigationCircles.length; i++) {
                navigationCircles[i].setFill(i == newValue.intValue() ? Color.GRAY : Color.LIGHTGRAY);
            }
        });

        /* Internalization */
        titleLabel.textProperty().bind(viewModel.titleProperty());
	}

    @Override
    public void setViewModel(Object viewModel) {

    }
}
