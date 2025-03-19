package ppti.view;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public abstract class PopUpBaseView extends BaseView {

    private StackPane containerStackPane;
    protected Region backgroundRegion;
    private BorderPane borderPane;
    private BorderPane mirrorBorderPane;
	
    protected HBox HBoxTop;
    protected VBox VBoxLeft;
    protected VBox VBoxRight;
    protected HBox mirrorHBoxTop;
    protected VBox mirrorVBoxLeft;
    protected VBox mirrorVBoxRight;

    protected Label titleLabel;
    protected Button backButton;
    protected Label mirrorTitleLabel;
    protected Button mirrorBackButton;

    protected boolean isBackButtonVisible; // bouton retour visible
    protected boolean isScreenRotated; // écran retourné
    protected boolean isMirrorPopup; // popup miroir

    private Stage stage;

    public PopUpBaseView(Boolean isBackButtonVisible, Stage stage, Boolean isScreenRotated, Boolean isMirrorPopup) {
        this.isBackButtonVisible = isBackButtonVisible;
        this.isScreenRotated = isScreenRotated;
        this.isMirrorPopup = isMirrorPopup;
        this.stage = stage;
        customizeScreen();
    }

    public PopUpBaseView(Boolean isBackButtonVisible, Stage stage, Boolean isScreenRotated) {
        this(isBackButtonVisible, stage, isScreenRotated, false);
    }

    public void updateScreenOrientation(boolean isScreenRotated) {
        if (!isMirrorPopup) {
            this.isScreenRotated = isScreenRotated;
            adjustPopupSize();
        }
    }

    @Override
    protected void customizeScreen() {
        createComponents();
        setupComponents();
        organizeComponents();

        customizeCenter();
    }

    private void createComponents() {
        backgroundRegion = new Region();
        containerStackPane = new StackPane();
        borderPane = new BorderPane();

        HBoxTop = new HBox();
        if (isBackButtonVisible) {
            VBoxLeft = new VBox();
            VBoxRight = new VBox();
        }

        titleLabel = new Label("Titre");
        backButton = new Button(" ↓ ");


        if (isMirrorPopup) {
            mirrorBorderPane = new BorderPane();

            mirrorHBoxTop = new HBox();
            if (isBackButtonVisible) {
                mirrorVBoxLeft = new VBox();
                mirrorVBoxRight = new VBox();
            }

            mirrorTitleLabel = new Label("Titre");
            mirrorBackButton = new Button(" ↑ ");
        }
    }

    private void setupComponents() {
        backgroundRegion.getStyleClass().add("background-region");

        /* taille et position du popup */
        stage.widthProperty().addListener((obs, oldVal, newVal) -> adjustPopupSize());
        stage.heightProperty().addListener((obs, oldVal, newVal) -> adjustPopupSize());
        adjustPopupSize();

        // setup des VBox (right et left)
        if (isBackButtonVisible) {
            VBoxLeft.setMinWidth(100);
            VBoxLeft.setMaxWidth(100);
            VBoxLeft.setAlignment(Pos.BOTTOM_CENTER);
            VBoxRight.setMinWidth(100);
            VBoxRight.setMaxWidth(100);
        }

        // setup HBoxTop
        HBoxTop.setPadding(new Insets(10, 20, 10, 20));
        HBoxTop.setAlignment(Pos.CENTER);


        if (isMirrorPopup) {

            if (isBackButtonVisible) {
                mirrorVBoxLeft.setMinWidth(100);
                mirrorVBoxLeft.setMaxWidth(100);
                mirrorVBoxLeft.setAlignment(Pos.BOTTOM_CENTER);
                mirrorVBoxRight.setMinWidth(100);
                mirrorVBoxRight.setMaxWidth(100);
            }

            mirrorHBoxTop.setPadding(new Insets(10, 20, 10, 20));
            mirrorHBoxTop.setAlignment(Pos.CENTER);

        }

        backButton.getStyleClass().add("back-button");

        borderPane.getStyleClass().add("popup-base-view-border-pane");
        if (isMirrorPopup) {
            mirrorBorderPane.getStyleClass().add("popup-base-view-border-pane");
        }
    }

    private void adjustPopupSize() {
        double width = stage.widthProperty().doubleValue();
        double height = stage.heightProperty().doubleValue();

        double popupWidth = Math.min(width * 0.8, 800);
        double popupHeight = Math.min((isMirrorPopup ? height * 0.45 : height * 0.65), 600);

        borderPane.setMinWidth(popupWidth);
        borderPane.setMaxWidth(popupWidth);
        borderPane.setMinHeight(popupHeight);
        borderPane.setMaxHeight(popupHeight);

        // calcul de la position de base
        double baseOffset = height / 2 - (popupHeight /2) - (height * 0.01);

        if (isMirrorPopup) {
            mirrorBorderPane.setMinWidth(popupWidth);
            mirrorBorderPane.setMaxWidth(popupWidth);
            mirrorBorderPane.setMinHeight(popupHeight);
            mirrorBorderPane.setMaxHeight(popupHeight);

            // pop-up bas
            borderPane.setTranslateY(baseOffset);
            borderPane.setRotate(0);

            // pop-up haut
            mirrorBorderPane.setTranslateY(-baseOffset);
            mirrorBorderPane.setRotate(180);
        } else {
            if (isScreenRotated) {
                // position du borderpane en haut collé à la bordure de l'écran
                borderPane.setTranslateY(-baseOffset);
                borderPane.setRotate(180);
            } else {
                // position du borderpane en bas collé à la bordure de l'écran
                borderPane.setTranslateY(baseOffset);
                borderPane.setRotate(0);
            }
        }
    }

    private void organizeComponents() {
        // ajout des éléments dans leur conteneur
        HBoxTop.getChildren().add(titleLabel);
        if (isBackButtonVisible) {
            VBoxLeft.getChildren().add(backButton);
        }

        borderPane.setTop(HBoxTop);

        if (isBackButtonVisible) {
            borderPane.setLeft(VBoxLeft);
            borderPane.setRight(VBoxRight);
        }

        if (isMirrorPopup) {
            mirrorHBoxTop.getChildren().add(mirrorTitleLabel);
            if (isBackButtonVisible) {
                mirrorVBoxLeft.getChildren().add(mirrorBackButton);
            }

            mirrorBorderPane.setTop(mirrorHBoxTop);

            if (isBackButtonVisible) {
                mirrorBorderPane.setLeft(mirrorVBoxLeft);
                mirrorBorderPane.setRight(mirrorVBoxRight);
            }
        }
    }

    protected abstract void customizeCenter();

    protected abstract void bindToViewModel();

    public Button[] getBackButton() {
        if (isMirrorPopup) {
            return new Button[] {backButton, mirrorBackButton};
        } else {
            return new Button[] {backButton};
        }
    }

    public Label[] getTitleLabel() {
        if (isMirrorPopup) {
            return new Label[] {titleLabel, mirrorTitleLabel};
        } else {
            return new Label[] {titleLabel};
        }
    }

    protected void setTitle(String title) {
        titleLabel.setText(title);
        if (isMirrorPopup) mirrorTitleLabel.setText(title);
    }

    protected void addCenterRoot(Node center, Node mirrorCenter) {
        containerStackPane.getChildren().clear();
        containerStackPane.getChildren().add(backgroundRegion);

        if (center != null) {
            borderPane.setCenter(center);
        }

        if (isMirrorPopup && mirrorCenter != null) {
            mirrorBorderPane.setCenter(mirrorCenter);
            containerStackPane.getChildren().addAll(borderPane, mirrorBorderPane);
        } else {
            containerStackPane.getChildren().add(borderPane);
        }

        addContainerRoot(null, null, containerStackPane, null, null);
    }

}
