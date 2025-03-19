package ppti.view;

import common.enumtype.Placement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.awt.event.MouseEvent;
import java.util.*;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ppti.model.JoueurInfo;
import ppti.viewmodel.ChooseColorPositionViewModel;

import java.util.concurrent.Flow;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static common.enumtype.Placement.*;

public class EcranPositionnementView extends BaseView implements ListChangeListener<JoueurInfo> {
	
	private Stage stage;
	private ChooseColorPositionViewModel viewModel;

    // CENTER SECTION
    private StackPane centerContent;
    private HBox tokenContent;

    // TOP, LEFT, RIGHT, BOTTOM SECTIONS
    private HBox topContent;
    private VBox leftContent;
    private VBox rightContent;
    private HBox bottomContent;

    private ObservableList<Button> buttons;
    private ObservableList<GridPane> playerCards;
    private ObservableList<StackPane> tokens;

    private Region space1;
    private Region space2;
    private Region space3;
    private Region space4;

    private double startX;
    private double startY;

    private boolean debug = false;

    private Button startGameButton;

    public EcranPositionnementView(Stage stage, ChooseColorPositionViewModel viewModel) {
    	this.stage = stage;
    	this.viewModel = viewModel;
        customizeScreen();
        bindToViewModel();
    }

    public GridPane PlayerCard(double rotation, Placement userData) {
        GridPane card = new GridPane();

        card.getStyleClass().add("player-card");

        Label playerName = new Label("");

        card.setUserData(userData);
        card.setAlignment(Pos.CENTER);

        card.setPrefHeight(150);
        card.setPrefWidth(150);

        card.add(playerName, 0, 0);

        if (debug) {
            for (String s : card.getStyleClass()) {
                Label StyleClass = new Label("");
                StyleClass.setText(StyleClass.getText() + s + " ");
                card.getChildren().add(StyleClass);
            }

        }

        playerCards.add(card);

        card.setRotate(card.getRotate() + rotation);

        return card;
    }

    public StackPane playerToken() {
        StackPane token = new StackPane();

        token.getStyleClass().add("token");

        token.setUserData(null);

        Label playerName = new Label(null);

        Circle circle = new Circle(50);

        circle.getStyleClass().add("token-circle");

        if (debug) {
            circle.setStyle("-fx-fill: #ff0000;");
            token.setStyle("-fx-background-color: #00ff00;");
        }
        token.setMaxWidth(100);
        token.setMaxHeight(100);
        token.setMinWidth(100);
        token.setMinHeight(100);
        FlowPane colorButtons = new FlowPane();

        List<String> colors = new ArrayList<>(viewModel.getColors().keySet());

        for (String c : viewModel.getColors().keySet()) {
            Button color = new Button();

            color.getStyleClass().addAll("color-button",c);

            color.setUserData(c);

            color.maxWidthProperty().bind(token.maxWidthProperty().divide(5));
            color.maxHeightProperty().bind(token.maxHeightProperty().divide(5));
            color.minWidthProperty().bind(token.maxWidthProperty().divide(5));
            color.maxHeightProperty().bind(token.maxHeightProperty().divide(5));

            colorButtons.getChildren().add(color);
        }

        colorButtons.maxWidthProperty().bind(token.maxWidthProperty());
        colorButtons.maxHeightProperty().bind(token.maxHeightProperty().divide(2));
        colorButtons.minWidthProperty().bind(token.maxWidthProperty());
        colorButtons.minHeightProperty().bind(token.maxHeightProperty().divide(2));

        colorButtons.setVisible(false);

        colorButtons.translateYProperty().bind(token.maxHeightProperty().divide(2));

        circle.accessibleTextProperty().bind(playerName.textProperty());

        token.setOnDragDetected(event -> {
            Dragboard db = token.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(((Label) token.getChildren().getLast()).getText());
            db.setContent(content);
        });

        token.setOnMousePressed(event -> {
            startX = event.getSceneX() - token.getTranslateX();
            startY = event.getSceneY() - token.getTranslateY();
        });

        token.setOnDragDone(event -> {
            boolean canStart = true;

            if (debug) {
                if (event.getTransferMode() == TransferMode.MOVE) {
                    token.getChildren().getFirst().setStyle("-fx-fill: #0022ff;");
                } else {
                    token.getChildren().getFirst().setStyle("-fx-fill: #41f400;");
                }
            }

            for (GridPane playerCard : playerCards) {
                for (StackPane t : tokens) {

                    if (t.getUserData() == null && t.isVisible()) {
                        canStart = false;
                        t.getChildren().get(1).setVisible(false);
                        viewModel.updatePlayer((Placement) t.getUserData(), viewModel.getPlayerByToken(t));
                    }

                    if (t.getUserData() == playerCard.getUserData()) {
                        setCardToPlayer(playerCard, t);

                        break;
                    } else {
                        ((Label) playerCard.getChildren().getFirst()).setText("");
                        playerCard.getStyleClass().clear();
                        playerCard.getStyleClass().add("player-card");
                    }
                }
            }

            if (canStart) {
                for (Button button : buttons) {
                    button.setVisible(true);
                }
            } else {
                for (Button button : buttons) {
                    button.setVisible(false);
                }
            }

            event.consume();
        });
        token.setOnDragOver(event -> {
            token.setTranslateX(event.getSceneX() - startX);
            token.setTranslateY(event.getSceneY() - startY);

            Bounds tokenBounds = token.localToScene(token.getBoundsInLocal());

            boolean intersect = false;
            boolean canBePlaced = true;

            for (GridPane playerCard : playerCards) {
                Bounds playerCardBounds = playerCard.localToScene(playerCard.getBoundsInLocal());
                if (tokenBounds.intersects(playerCardBounds)) {
                    for (StackPane t : tokens) {
                        if (t != token) {
                            canBePlaced = canBePlaced && t.getUserData() != playerCard.getUserData();
                        }
                        if (!canBePlaced) {
                            break;
                        }
                    }
                    if (canBePlaced) {
                        token.setUserData(playerCard.getUserData());
                        intersect = true;
                        if (debug) {
                            token.getChildren().getFirst().setStyle("-fx-fill: #c700f4;");
                        }
                    }
                }
            }

            if (!intersect) {
                token.setUserData(null);
            }

            event.consume();
        });

        token.getChildren().addAll(circle, colorButtons ,playerName);

        token.setVisible(false);

        return token;
    }

    public void bindPlayerToken(StackPane token, JoueurInfo player) {

        ((Label) token.getChildren().getLast()).textProperty().bind(player.nomProperty());

        token.setVisible(true);

        tokenContent.getChildren().add(token);
    }

    public void setCardToPlayer(GridPane card, StackPane token) {
        Label namePlayer = (Label) card.getChildren().getFirst();
        Placement place = (Placement)card.getUserData();

        JoueurInfo player = viewModel.getPlayerByToken(token);

        namePlayer.setText(player.getNom());

        viewModel.updatePlayer(place, player);

        // rotate the token depending on the place
        switch ((Placement)token.getUserData()) {
            case TOP_LEFT:
                token.setRotate(180);
                break;
            case TOP_MIDDLE:
                token.setRotate(180);
                break;
            case TOP_RIGHT:
                token.setRotate(180);
                break;
            case RIGHT_TOP:
                token.setRotate(270);
                break;
            case RIGHT_BOTTOM:
                token.setRotate(270);
                break;
            case BOTTOM_RIGHT:
                token.setRotate(0);
                break;
            case BOTTOM_MIDDLE:
                token.setRotate(0);
                break;
            case BOTTOM_LEFT:
                token.setRotate(0);
                break;
            case LEFT_BOTTOM:
                token.setRotate(90);
                break;
            case LEFT_TOP:
                token.setRotate(90);
                break;
        }

        FlowPane colorButtons = (FlowPane) token.getChildren().get(1);

        for (Node color : colorButtons.getChildren()) {
            ((Button) color).setOnAction(event -> {
                setCardColor(card, player, (String) color.getUserData());
            });
        }

        colorButtons.setVisible(true);

        setCardColor(card, player, player.getCouleurJoueur());
    }

    public void setCardColor(GridPane card, JoueurInfo player, String newColor) {
        String oldColor = player.getCouleurJoueur();

        card.getStyleClass().clear();

        viewModel.updatePlayer(newColor, player);

        card.getStyleClass().addAll(newColor, "player-card");

        // pour chaque token, désactiver les boutons de la nouvelle couleur et réactiver le bouton de la couleur précédente
        for (StackPane token : tokens) {
            FlowPane colorButtons = (FlowPane) token.getChildren().get(1);
            for (Node color : colorButtons.getChildren()) {
                if (color.getUserData() == newColor) {
                    color.setDisable(true);
                } else if (newColor != oldColor && color.getUserData() == oldColor) {
                    color.setDisable(false);
                }
            }
        }

    }

    @Override
    public void onChanged(Change change) {

    }

	@Override
	protected void customizeScreen() {
        // GENERAL
        //list pour que les boutons puissent etre tous change plus tard (si besoin)
        buttons = FXCollections.observableArrayList();
        tokens = FXCollections.observableArrayList();
        playerCards = FXCollections.observableArrayList();

        startGameButton = new Button();
        startGameButton.textProperty().bind(viewModel.startGameButtonProperty());
        startGameButton.setOnAction(event -> {
            viewModel.changeToGame();
        });

        // ajouter quatre instances de startGameButton dans la liste des boutons
        for (int i = 0; i < 4; i++) {
            Button button = new Button();
            button.textProperty().bind(startGameButton.textProperty());
            //button.onActionProperty().bind(startGameButton.onActionProperty());
            button.setOnAction(startGameButton.getOnAction());
            button.setVisible(false);
            button.getStyleClass().add("corner-button");
            buttons.add(button);
        }

        // CENTER SECTION
        centerContent = new StackPane();
        tokenContent = new HBox(40);

        centerContent.getChildren().addAll(tokenContent);

        tokenContent.setAlignment(Pos.CENTER);

        topContent = new HBox(20);
        topContent.setAlignment(Pos.BOTTOM_CENTER);
        leftContent = new VBox(20);
        leftContent.setAlignment(Pos.CENTER);
        rightContent = new VBox(20);
        rightContent.setAlignment(Pos.CENTER);
        bottomContent = new HBox(20);
        bottomContent.setAlignment(Pos.TOP_CENTER);

        space1 = new Region();
        space2 = new Region();
        space3 = new Region();
        space4 = new Region();

        HBox.setHgrow(space1, Priority.ALWAYS);
        HBox.setHgrow(space2, Priority.ALWAYS);
        HBox.setHgrow(space3, Priority.ALWAYS);
        HBox.setHgrow(space4, Priority.ALWAYS);

        if (debug) {

            //set different background color for each content part
            topContent.setStyle("-fx-background-color: #8ec5ff; -fx-padding: 0;");
            bottomContent.setStyle("-fx-background-color: #ff8e8e;-fx-padding: 0;");
            leftContent.setStyle("-fx-background-color: #8eff8e; -fx-padding: 0;");
            rightContent.setStyle("-fx-background-color: #ff8eff; -fx-padding: 0;");
            centerContent.setStyle("-fx-background-color: #ffdf8e; -fx-padding: 0;");
        }

        // créer cinq token vide

        for (int i = 0; i < 5; i++) {
            tokens.add(playerToken());
        }

        topContent.getChildren().add(buttons.get(0));
        bottomContent.getChildren().add(buttons.get(1));
        topContent.getChildren().add(space1);
        bottomContent.getChildren().add(space2);

        for (int i = 0; i < 10; i++) {
            if (i < 3) {
                topContent.getChildren().add(PlayerCard(180, Placement.values()[i]));
            } else if (i < 5) {
                rightContent.getChildren().add(PlayerCard(270, Placement.values()[i]));
            } else if (i < 8) {
                bottomContent.getChildren().add(PlayerCard(0, Placement.values()[i]));
            } else {
                leftContent.getChildren().add(PlayerCard(90, Placement.values()[i]));
            }
        }

        topContent.getChildren().add(space3);
        bottomContent.getChildren().add(space4);
        topContent.getChildren().add(buttons.get(2));
        bottomContent.getChildren().add(buttons.get(3));


        stage.widthProperty().addListener((observable, oldValue, newValue) -> {

            double width = newValue.doubleValue();
            double height = stage.getHeight();

            for (int i = 0; i < 10; i++) {
                GridPane gridPane = playerCards.get(i) != null ? playerCards.get(i) : null;
                if (gridPane != null) {
                    if (i < 3 || (i > 4 && i < 8)) {
                        // top and bottom spots
                        gridPane.setMaxWidth((width / 6));
                        gridPane.setMinWidth((width / 6));
                    } else {
                        // left and right spots
                        gridPane.setMaxHeight((width / 5.5));
                        gridPane.setMinHeight((width / 5.5));
                    }
                }
            }

            leftContent.setMaxWidth(stage.getWidth() / 5.5);
            leftContent.setMinWidth(stage.getWidth() / 5.5);
            rightContent.setMaxWidth(stage.getWidth() / 5.5);
            rightContent.setMinWidth(stage.getWidth() / 5.5);
            leftContent.setAlignment(Pos.CENTER);
            rightContent.setAlignment(Pos.CENTER);
        });

        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            double width = stage.getWidth();
            double height = newValue.doubleValue();

            for (int i = 0; i < 10; i++) {
                GridPane gridPane = playerCards.get(i) != null ? playerCards.get(i) : null;
                if (gridPane != null) {
                    if (i < 3 || (i > 4 && i < 8)) {
                        // top and bottom spots
                        gridPane.setMinHeight((height / 4));
                        gridPane.setMaxHeight((height / 4));

                        if (debug) {
                            gridPane.setStyle("-fx-background-color: #8effea;");
                        }

                    } else {
                        // left and right spots
                        gridPane.setMaxWidth((height / 4));
                        gridPane.setMinWidth((height / 4));
                    }

//                    if (debug) {
//                        Label label = new Label("[" + i + "]");
//
//                        gridPane.add(label, 2, 2);
//                    }
                }
            }

            topContent.setMaxHeight(stage.getHeight() / 4);
            topContent.setMinHeight(stage.getHeight() / 4);
            bottomContent.setMaxHeight(stage.getHeight() / 4);
            bottomContent.setMinHeight(stage.getHeight() / 4);
            centerContent.setMaxHeight(stage.getHeight() / 2);
            centerContent.setMinHeight(stage.getHeight() / 2);
            leftContent.setMaxHeight(stage.getHeight() / 2);
            leftContent.setMinHeight(stage.getHeight() / 2);
            rightContent.setMaxHeight(stage.getHeight() / 2);
            rightContent.setMinHeight(stage.getHeight() / 2);
            topContent.setAlignment(Pos.TOP_CENTER);
            bottomContent.setAlignment(Pos.BOTTOM_CENTER);
        });

        addContainerRoot(topContent, leftContent, centerContent, rightContent, bottomContent);
    }

	@Override
	protected void bindToViewModel() {
        viewModel.getJoueurPlace().addListener(this);
	}

    @Override
    public void setViewModel(Object viewModel) {
    }

    //getTokens
    public ObservableList<StackPane> getTokens() {
        return tokens;
    }
}