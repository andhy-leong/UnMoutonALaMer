package common.ui;

import common.ImageHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.io.InputStream;

/*
 * Classe abstraite pour les écrans de base avant l'initialisation de la partie
 */
public abstract class BaseScreenView extends BorderPane {
    protected GridPane topGridPane;
    protected ColumnConstraints leftColumn;
    protected ColumnConstraints centerColumn;
    protected ColumnConstraints rightColumn;
    protected Label titleLabel;
    protected VBox leftVBox;
    protected VBox rightVBox;
    protected Button backButton;
    protected ImageView backButtonImage;
    protected Button quitButton;
    protected Button menuButton;
    protected Label playerNameLabel;

    // Choisir quel composant de la barre du haut est affiché
    // -1 : aucun composant
    // 0 : uniquement le titre
    // 1 : back button + titre
    // 2 : titre + quit button
    // 3 : menu button + titre
    // 4 : menu button + titre + player name
    protected int modelTopBar = 0;

    protected void init() {
        getStyleClass().add("base-screen-view-border-pane");

        initializeBaseComponents();
        setupBaseComponents();
        organizeBaseComponents();
        customizeScreen();
        bindToViewModel();
    }

    private void initializeBaseComponents() {
        topGridPane = new GridPane();
        leftColumn = new ColumnConstraints();
        centerColumn = new ColumnConstraints();
        rightColumn = new ColumnConstraints();
        
        leftVBox = new VBox(20);
        rightVBox = new VBox(20);

        if (modelTopBar != -1)
            titleLabel = new Label("[Title]");

        switch (modelTopBar) {
            case 1:
                backButton = new Button();
                InputStream imageStream = ImageHandler.loadImage("/esp/images/backbutton_256.png", "←");
                if (imageStream != null) {
                    backButtonImage = new ImageView(new Image(imageStream));
                    backButtonImage.setFitWidth(80);
                    backButtonImage.setFitHeight(80);
                    backButton.setGraphic(backButtonImage);
                } else {
                    backButton.setText("←");
                    backButton.getStyleClass().add("back-button");
                }
                break;
            case 2:
                quitButton = new Button("[Quit]");
                break;
            case 3:
            case 4:
                menuButton = new Button("[Menu]");
                if (modelTopBar == 4) {
                    playerNameLabel = new Label("[PlayerName]");
                }
                break;
        }
    }

    private void setupBaseComponents() {
        /* Right and left VBoxes */
        leftVBox.setAlignment(Pos.TOP_LEFT);
        leftVBox.setPadding(new Insets(20));
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setPadding(new Insets(20));
        leftVBox.setMinWidth(170);
        leftVBox.setMaxWidth(170);
        rightVBox.setMinWidth(170);
        rightVBox.setMaxWidth(170);

        /* Title */
        if (modelTopBar != -1)
            titleLabel.getStyleClass().add("title-idjr");

        /* Construire les composants d'après modelTopBar */
        switch (modelTopBar) {
            case 1:
                backButton.setGraphic(backButtonImage);
                backButton.getStyleClass().add("back-button");
                break;
            case 2:
                quitButton.getStyleClass().add("quit-button");
                quitButton.setPrefWidth(150);
                break;
            case 3:
            case 4:
                menuButton.setPrefWidth(120);
                menuButton.setPrefHeight(30);
                menuButton.getStyleClass().add("menu-button");

                if (modelTopBar == 4) {
                    playerNameLabel.getStyleClass().add("player-name-label");
                }
                break;
        }

        /* construire le conteneur haut */
        topGridPane.setPadding(new Insets(20, 10, 20, 10));
        topGridPane.setAlignment(Pos.CENTER);
        topGridPane.setMinHeight(100);
        topGridPane.setMaxHeight(100);

        leftColumn.prefWidthProperty().bind(topGridPane.widthProperty().multiply(1/3.0));
        leftColumn.setHalignment(HPos.LEFT);
        centerColumn.prefWidthProperty().bind(topGridPane.widthProperty().multiply(1/3.0));
        centerColumn.setHalignment(HPos.CENTER);
        rightColumn.prefWidthProperty().bind(topGridPane.widthProperty().multiply(1/3.0));
        rightColumn.setHalignment(HPos.RIGHT);
        topGridPane.getColumnConstraints().addAll(leftColumn, centerColumn, rightColumn);
    }

    private void organizeBaseComponents() {
        // ajouter dans le conteneur haut les éléments d'après modelTopBar
        if (modelTopBar != -1) {
            switch (modelTopBar) {
                case 1: // back button + title
                    topGridPane.add(backButton, 0, 0);
                    topGridPane.add(titleLabel, 1, 0);
                    break;
                case 2: // title + quit button
                    topGridPane.add(titleLabel, 1, 0);
                    topGridPane.add(quitButton, 2, 0);
                    break;
                case 3: // menu button + title
                    topGridPane.add(menuButton, 0, 0);
                    topGridPane.add(titleLabel, 1, 0);
                    break;
                case 4: // menu button + title + player name
                    topGridPane.add(menuButton, 0, 0);
                    topGridPane.add(titleLabel, 1, 0);
                    topGridPane.add(playerNameLabel, 2, 0);
                    break;
                default: // uniquement le titre (par défaut)
                    topGridPane.add(titleLabel, 1, 0);
                    break;
            }
        }
    }

    protected abstract void customizeScreen();

    protected void addContainerRoot(Node center, Node bottom) {
        this.setTop(topGridPane);
        this.setLeft(leftVBox);
        this.setRight(rightVBox);
        if (center != null) {
            this.setCenter(center);
        }
        if (bottom != null) {
            this.setBottom(bottom);
        }
    }

    protected abstract void bindToViewModel();

    protected void setTitle(String title) {
        if (modelTopBar != -1)
            titleLabel.setText(title);
    }

    protected void setPlayerName(String playerName) {
        if (modelTopBar == 4) {
            playerNameLabel.setText(playerName);
        }
    }

    public void updateVisibility(boolean visible) {
        setVisible(visible);
        setDisable(!visible);
    }
}
