package ppti.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public abstract class MirrorBaseView extends BaseView {
    protected VBox buttonContainerUp;
    protected VBox buttonContainerDown;

    protected Button backButtonUp;
    protected Button backButtonDown;
    protected Button confirmButtonUp;
    protected Button confirmButtonDown;

    private Label titleUpLabel;
    private Label titleDownLabel;

    private HBox upHBox;
    private HBox downHBox;
    private VBox centerVBox;

    protected String title;
    protected String buttonType;

    protected Stage stage;

    /**
     * Constructeur de la classe abstract MirrorBaseView. Sert à initialiser les attributs de la classe.
     * @param title : titre de la page affiché en double au centre de l'écran
     * @param buttonType : "back", "confirm" ou null
     */
    public MirrorBaseView(Stage stage, String title, String buttonType) {
        this.stage = stage;
        this.title = title;
        this.buttonType = Objects.requireNonNullElse(buttonType, "null");
    }


    @Override
    protected void customizeScreen() {
        createComponents();
        setupComponents();
        organizeComponents();

        customizeContent();
    }

    private void createComponents() {
        // Création des labels pour le centre
        titleUpLabel = new Label(title);
        titleUpLabel.setAlignment(Pos.TOP_CENTER);
        titleDownLabel = new Label(title);
        titleDownLabel.setAlignment(Pos.BOTTOM_CENTER);

        // Création des boutons si nécessaire
        if (buttonType.equals("back")) {
            backButtonUp = new Button("↓");
            backButtonDown = new Button("↓");
        } else if (buttonType.equals("confirm")) {
            confirmButtonUp = new Button("[Valider]");
            confirmButtonDown = new Button("[Valider]");
        }

        // Création des conteneurs
        upHBox = new HBox();
        downHBox = new HBox();
        centerVBox = new VBox();
    }

    private void setupComponents() {
        titleUpLabel.getStyleClass().add("title");
        titleDownLabel.getStyleClass().add("title");

        // setup des titres
        titleUpLabel.setRotate(180);

        // setup VBoxCenter
        centerVBox.setAlignment(Pos.CENTER);
        centerVBox.setMaxHeight(170);
        centerVBox.setMinHeight(170);
        centerVBox.getStyleClass().add("mirror-vbox");
        HBox.setHgrow(centerVBox, Priority.ALWAYS); // prendre la place restante

        if (buttonType.equals("back") || buttonType.equals("confirm")) {
            upHBox.setPadding(new Insets(20));
            downHBox.setPadding(new Insets(20));
        } else {
            upHBox.setPadding(new Insets(20, 100, 20, 100));
            downHBox.setPadding(new Insets(20, 100, 20, 100));
        }

        // setup upHBox
        upHBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(upHBox, Priority.ALWAYS);
        HBox.setHgrow(upHBox, Priority.ALWAYS);
        upHBox.prefHeightProperty().bind(stage.heightProperty().divide(2).subtract(170/2));
        upHBox.prefWidthProperty().bind(stage.widthProperty());
        upHBox.setRotate(180);
        upHBox.getStyleClass().add("mirror");

        // setup downHBox
        downHBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(downHBox, Priority.ALWAYS);
        HBox.setHgrow(downHBox, Priority.ALWAYS);
        downHBox.prefHeightProperty().bind(stage.heightProperty().divide(2).subtract(170/2));
        downHBox.prefWidthProperty().bind(stage.widthProperty());
        downHBox.getStyleClass().add("mirror");

        // setup des boutons
        if (buttonType.equals("back")) {
            backButtonUp.getStyleClass().add("back-button");
            backButtonDown.getStyleClass().add("back-button");
        } else if (buttonType.equals("confirm")) {
            confirmButtonDown.getStyleClass().add("confirm-button");
            confirmButtonUp.getStyleClass().add("confirm-button");
            confirmButtonUp.setMinWidth(90);
            confirmButtonDown.setMinWidth(90);
            confirmButtonUp.setAlignment(Pos.CENTER);
            confirmButtonDown.setAlignment(Pos.CENTER);
        }

        if (buttonType.equals("back") || buttonType.equals("confirm")) {
            Region spaceUp = new Region();
            Region spaceDown = new Region();
            VBox.setVgrow(spaceUp, Priority.ALWAYS);
            VBox.setVgrow(spaceDown, Priority.ALWAYS);

            buttonContainerUp = new VBox();
            buttonContainerDown = new VBox();

            // ajouter les espaces et le bouton (en fonction du type de bouton)
            buttonContainerUp.getChildren().addAll(spaceUp, buttonType.equals("back") ? backButtonUp : confirmButtonUp);
            buttonContainerDown.getChildren().addAll(spaceDown, buttonType.equals("back") ? backButtonDown : confirmButtonDown);
        }
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        return separator;
    }

    private void organizeComponents() {
        // ajout des labels dans le VBox
        centerVBox.getChildren().addAll(titleUpLabel, createSeparator(), titleDownLabel);
    }

    protected abstract void customizeContent();

    protected abstract void bindToViewModel();

    protected void setTitle(String title) {
        titleUpLabel.setText(title);
        titleDownLabel.setText(title);
    }

    protected HBox getUpHBox() {
        return upHBox;
    }

    protected HBox getDownHBox() {
        return downHBox;
    }

    protected Button[] getBackButtons() {
        if (buttonType.equals("back")) {
            return new Button[] {backButtonUp, backButtonDown};
        } else {
            return null;
        }
    }

    protected Button[] getConfirmButtons() {
        if (buttonType.equals("confirm")) {
            return new Button[] {confirmButtonUp, confirmButtonDown};
        } else {
            return null;
        }
    }

    protected void setUpHBox(HBox upHBox) {
        this.upHBox = upHBox;
    }

    protected void setDownHBox(HBox downHBox) {
        this.downHBox = downHBox;
    }

    protected Label[] getTitleLabels() {
        return new Label[] {titleUpLabel, titleDownLabel};
    }

    protected void addContent(Node topContent, Node bottomContent) {

        Region downRightSpace = new Region();
        Region upRightSpace = new Region();
        Region downLeftSpace = new Region();
        Region upLeftSpace = new Region();

        if (buttonType.equals("back")) {
            ((Region) topContent).prefWidthProperty().bind(stage.widthProperty().multiply(0.7));
            ((Region) bottomContent).prefWidthProperty().bind(stage.widthProperty().multiply(0.7));
        } else if (buttonType.equals("confirm")) {
            ((Region) topContent).prefWidthProperty().bind(stage.widthProperty().multiply(0.6));
            ((Region) bottomContent).prefWidthProperty().bind(stage.widthProperty().multiply(0.6));
        }

        if (buttonType.equals("back")) {
            // calcul de la taille occupée par les cotés
            backButtonUp.widthProperty().addListener((obs, oldVal, newVal) -> {
                double buttonWidth = newVal.doubleValue();
                upLeftSpace.prefWidthProperty().bind(stage.widthProperty().multiply(0.15).subtract(buttonWidth + 20));
                upRightSpace.prefWidthProperty().bind(stage.widthProperty().multiply(0.15).subtract(20));
                downLeftSpace.prefWidthProperty().bind(stage.widthProperty().multiply(0.15).subtract(buttonWidth + 20));
                downRightSpace.prefWidthProperty().bind(stage.widthProperty().multiply(0.15).subtract(20));
            });

            upHBox.getChildren().addAll(buttonContainerUp, upLeftSpace, topContent, upRightSpace);
            downHBox.getChildren().addAll(buttonContainerDown, downLeftSpace, bottomContent, downRightSpace);
        } else if (buttonType.equals("confirm")) {
            confirmButtonUp.widthProperty().addListener((obs, oldVal, newVal) -> {
                double buttonWidth = newVal.doubleValue();
                upLeftSpace.prefWidthProperty().bind(stage.widthProperty().multiply(0.20).subtract(20));
                upRightSpace.prefWidthProperty().bind(stage.widthProperty().multiply(0.20).subtract(buttonWidth + 20));
                downLeftSpace.prefWidthProperty().bind(stage.widthProperty().multiply(0.20).subtract(20));
                downRightSpace.prefWidthProperty().bind(stage.widthProperty().multiply(0.20).subtract(buttonWidth + 20));
            });

            upHBox.getChildren().addAll(upLeftSpace, topContent, upRightSpace, buttonContainerUp);
            downHBox.getChildren().addAll(downLeftSpace, bottomContent, downRightSpace, buttonContainerDown);
        } else {
            if (topContent != null) {
                upHBox.getChildren().add(topContent);
            }
            if (bottomContent != null) {
                downHBox.getChildren().add(bottomContent);
            }
        }
        addContainerRoot(upHBox, null, centerVBox, null, downHBox);
    }

}
