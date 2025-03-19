package esp.view;

import common.Config;
import common.navigation.NavigationService;
import common.ui.BaseScreenView;
import common.ui.pane.CircularPane;
import esp.model.JoinedGameModelESP;
import esp.viewmodel.EspConnectionScreenViewModel;
import esp.viewmodel.GameScreenViewModel;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

/**
 * Vue de l'écran de suivie de la partie.
 */
public class GameScreenViewEsp extends BaseScreenView implements MapChangeListener<String, ArrayList<String>> {
    private EspConnectionScreenViewModel viewModel;
    private final Stage stage;

    private StackPane stackPaneContainer;

    /* top-left corner */
    private VBox topLeftCorner;
    private Button menuButton;
    private Label infoLabel;
    private Label breakLabel;

    /* bottom-left corner */
    private VBox bottomLeftCornerCards;
    private Label discardedLabel;
    private FlowPane discardedCardsFlowPane;

    /* top-right corner */
    private VBox topRightCorner;
    private Label roundLabel;
    private Label foldLabel;

    /* bottom-right corner */
    private VBox bottomLeftCornerClassificator;
    private Label classificatorLabel;
    private GridPane classificatorPane;

    /* center */
    private StackPane center;
    private CircularPane playerCircularPane;
    private CircularPane cardCircularPane;

    // zone pioche
    private VBox centerContentVBox;
    private VBox selectedCardsVBox;
    private HBox selectedCardsHBox;
    private Label selectedCardsLabel;
    private Label selectedEmptyLabel;
    private VBox deckVBox;
    private Label deckLabel;
    private Label deckEmptyLabel;
    private HBox deckHBox;

    private int currentRotationIndex = 0;  // garde en mémoire la position actuelle des zones de cartes

    private ArrayList<String> ordreJoueur;
    private int indexUtilisateur;
    private int distribue;
    private int indexRotate;
    private int compteur;
    private String[][] scoreAssoc;
    private boolean firstRound;
    private HashMap<String,String[]> carteJoueur;

    /**
     * Crée une vue de l'écran de suivi de la partie.
     * @param viewModel ViewModel de la vue (GameScreenViewModel).
     * @param stage Scène de l'application.
     */
    public GameScreenViewEsp(EspConnectionScreenViewModel viewModel, Stage stage, NavigationService navigationService) {

        this.viewModel = viewModel;
        this.stage = stage;

        createComponents();
        setupComponents();
        organizeComponents();

        bindToViewModel();
    }


    @Override
    protected void customizeScreen() {
        addContainerRoot(this,null);
    }

    /**
     * Lie les éléments de la vue au ViewModel pour la mise à jour des données par binding.
     */
    protected void bindToViewModel() {
        // TODO : action boutons, binding valeurs et internationalisation
        viewModel.getMessage().addListener(this);
        indexUtilisateur = 0;
        distribue = 0;
        indexRotate = 0;
        compteur = 0;
        firstRound = true;

        infoLabel.textProperty().bind(viewModel.nameProperty());

        /* -------- provisoir --------

        // ajout de 5 joueurs pr test
        for (int i = 0; i < 5; i++) {
            // ajout de 5 joueurs avec des id "pl1", "pl2", ...
            playerCircularPane.getChildren().add(createPlayerPane("Player " + i, "pl" + i));
            // set les scores des joueurs
            updatePlayerScore("pl" + i, i * 2);

            // ajout des 5 plateau de cartes avec des id "ca1", "ca2", ...
            cardCircularPane.getChildren().add(createPlayerCardPane("ca" + i));
            // ajout des cartes météo des joueurs (12) G = Grisé, N = normal, S = séléctionnée
            updatePlayerWeatherCards("ca" + i, new String[][] {{"Y01G", "Y02N", "G14N", "G15S", "G16N", "G17N", "B25G", "B26N", "R53N", "R54N", "R55G", "R56N"}});
            // set les cartes marées des joueurs
            updatePlayerTideCards("ca" + i, String.valueOf((int) (Math.random() * 12) + 1));
            // set le nombre de bouées des joueurs
            updatePlayerBuoyCards("ca" + i, (int) (Math.random() * 9));
            // set le status des joueurs (éliminé ou non)
            if (i == 3) updatePlayerStatus("pl" + i, "ca" + i, true);
        }

        // ajout des carte de pioche pr test
        String[] deck = new String[] {"11", "04", "05", "06", "03", "09", "02", "10", "07", "12"};
        updateDeck(deck);

        // ajout des cartes piochées pr test
        String[] selectedCards = new String[] {"01", "08"};
        updateSelectedCards(selectedCards);

        // Affichage du classement pr test
        updateClassification(new String[][] {{"Player 1", "10"}, {"Player 2", "8"}, {"Player 3", "6"}, {"Player 4", "4"}, {"Player 5", "2"}});

        // ajout des cartes météo écartées pr test
        updateDiscardedCards(new String[] {"Y01", "Y02", "Y03", "Y04", "Y05", "Y06", "G13", "G14", "G15", "G16", "G17", "G18", "B25", "B26", "B27", "B28", "B29", "B30", "P37", "P38", "P39", "P40", "P41", "P42", "R49", "R50", "R51", "R52", "R53", "R54"});
        //updateDiscardedCards(new String[] {}); // test sans carte

        // tester rotateCardsToPlayer() toutes les 2 secondes
        new Thread(() -> {
            String players = "pl0";
            String[] cards = new String[] {"ca0", "ca1", "ca2", "ca3", "ca4"};
            int i = 0;
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                rotateCardsToPlayer(cards[i], players);
                i = (i + 1) % cards.length;
            }
        }).start();
        */
    }


    /**
     * Crée les composants de la vue.
     */
    private void createComponents() {
        stackPaneContainer = new StackPane();

        /* top-left corner */
        topLeftCorner = new VBox();
        menuButton = new Button("[Menu]");
        infoLabel = new Label("[Game info Lorem ipsum dolor sit amet, consectetur]");
        breakLabel = new Label("[On break]");

        /* bottom-left corner */
        bottomLeftCornerCards = new VBox();
        discardedLabel = new Label("[Discarded cards]");
        discardedCardsFlowPane = new FlowPane(7, 5);

        /* top-right corner */
        topRightCorner = new VBox();
        roundLabel = new Label("[Round]");
        foldLabel = new Label("[Fold]");

        /* bottom-right corner */
        bottomLeftCornerClassificator = new VBox();
        classificatorLabel = new Label("[Classement]");
        classificatorPane = new GridPane();

        /* center */
        center = new StackPane();
        playerCircularPane = new CircularPane();
        cardCircularPane = new CircularPane();

        centerContentVBox = new VBox(10);
        selectedCardsVBox = new VBox(10);
        selectedCardsLabel = new Label("[cartes piochées]");
        selectedEmptyLabel = new Label("[Empty]");
        selectedCardsHBox = new HBox(10);
        deckVBox = new VBox(10);
        deckLabel = new Label("[Pioche]");
        deckEmptyLabel = new Label("[Empty]");
        deckHBox = new HBox(10);
    }


    /**
     * Configure les composants de la vue.
     */
    private void setupComponents() {
        /* top-left corner */
        topLeftCorner.setPadding(new Insets(10));
        topLeftCorner.setSpacing(10);
        if (Config.DEBUG_MODE) {
            topLeftCorner.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        topLeftCorner.setPrefSize(400, 300);
        topLeftCorner.setMaxSize(400, 300);

        infoLabel.setWrapText(true);
        infoLabel.setTextAlignment(TextAlignment.JUSTIFY);
        infoLabel.setPrefWidth(300);
        infoLabel.setMaxWidth(300);

        menuButton.setStyle("-fx-font-size: 20px; -fx-background-color: white; -fx-border-color: black; -fx-border-width: 2px;");
        menuButton.setPrefSize(150, 50);
        infoLabel.setStyle("-fx-font-size: 20px;");
        breakLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: red; -fx-font-weight: bold;");

        /* bottom-left corner */
        bottomLeftCornerCards.setPadding(new Insets(10));
        bottomLeftCornerCards.setSpacing(10);
        if (Config.DEBUG_MODE) {
            bottomLeftCornerCards.setBackground(new Background(new BackgroundFill(Color.LIGHTPINK, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        bottomLeftCornerCards.setPrefSize(220, 150);
        bottomLeftCornerCards.setMaxSize(220, 150);

        discardedLabel.setStyle("-fx-font-size: 20px;");

        /* top-right corner */
        topRightCorner.setPadding(new Insets(10));
        topRightCorner.setSpacing(10);
        topRightCorner.setAlignment(Pos.TOP_RIGHT);
        if (Config.DEBUG_MODE) {
            topRightCorner.setBackground(new Background(new BackgroundFill(Color.LIGHTCORAL, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        topRightCorner.setPrefSize(200, 150);
        topRightCorner.setMaxSize(200, 150);

        roundLabel.setStyle("-fx-font-size: 20px;");
        foldLabel.setStyle("-fx-font-size: 20px;");

        /* bottom-right corner */
        bottomLeftCornerClassificator.setPadding(new Insets(10));
        bottomLeftCornerClassificator.setSpacing(10);
        bottomLeftCornerClassificator.setAlignment(Pos.BOTTOM_RIGHT);
        if (Config.DEBUG_MODE) {
            bottomLeftCornerClassificator.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        bottomLeftCornerClassificator.setPrefSize(200, 150);
        bottomLeftCornerClassificator.setMaxSize(200, 150);

        classificatorLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        classificatorPane.setHgap(10);

        /* center */
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            double width = (double) newVal;
            double height = stage.getHeight();
            double size = Math.min(width, height) - 80;

            center.setPrefSize(size, size);
            center.setMaxSize(size, size);

            center.setBackground(new Background(new BackgroundFill(Color.LIGHTYELLOW, new CornerRadii(size/2), Insets.EMPTY)));
            center.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(size/2), BorderWidths.DEFAULT)));

            playerCircularPane.setRadius((int) (size / 2 + 25));
            cardCircularPane.setRadius((int) (size / 2 - 150));
        });

        // Deck and selected cards
        centerContentVBox.setPadding(new Insets(10));
        centerContentVBox.setSpacing(10);
        centerContentVBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(113), Insets.EMPTY)));
        centerContentVBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(113), BorderWidths.DEFAULT)));
        centerContentVBox.setPrefSize(225, 225);
        centerContentVBox.setMaxSize(225, 225);

        centerContentVBox.setAlignment(Pos.CENTER);
        selectedCardsVBox.setAlignment(Pos.CENTER);
        selectedCardsHBox.setAlignment(Pos.CENTER);
        deckVBox.setAlignment(Pos.CENTER);
        deckHBox.setAlignment(Pos.CENTER);
        deckHBox.setTranslateX(-15);
    }


    /**
     * Organise les composants de la vue dans la fenêtre.
     */
    private void organizeComponents() {
        /* top-left corner */
        topLeftCorner.getChildren().addAll(menuButton, infoLabel, breakLabel);
        StackPane.setAlignment(topLeftCorner, Pos.TOP_LEFT);

        /* bottom-left corner */
        bottomLeftCornerCards.getChildren().addAll(discardedLabel, discardedCardsFlowPane);
        StackPane.setAlignment(bottomLeftCornerCards, Pos.BOTTOM_LEFT);

        /* top-right corner */
        topRightCorner.getChildren().addAll(roundLabel, foldLabel);
        StackPane.setAlignment(topRightCorner, Pos.TOP_RIGHT);

        /* bottom-right corner */
        bottomLeftCornerClassificator.getChildren().addAll(classificatorLabel, classificatorPane);
        StackPane.setAlignment(bottomLeftCornerClassificator, Pos.BOTTOM_RIGHT);

        /* center */
        selectedCardsHBox.getChildren().add(selectedEmptyLabel);
        selectedCardsVBox.getChildren().addAll(selectedCardsLabel, selectedCardsHBox);
        deckHBox.getChildren().add(deckEmptyLabel);
        deckVBox.getChildren().addAll(deckLabel, deckHBox);
        centerContentVBox.getChildren().addAll(selectedCardsVBox, deckVBox);
        center.getChildren().addAll(playerCircularPane, cardCircularPane, centerContentVBox);

        stackPaneContainer.getChildren().addAll(topLeftCorner, bottomLeftCornerCards, bottomLeftCornerClassificator, topRightCorner, center);

        this.setCenter(stackPaneContainer);
    }


    /**
     * Crée un panneau de joueur avec son nom et son score.
     * @param name Nom du joueur.
     * @param idVBox ID du panneau de joueur.
     * @return Panneau de joueur (VBox).
     */
    private VBox createPlayerPane(String name, String idVBox) {
        // vérifier si l'idVBox est déjà utilisé
        if (playerCircularPane.lookup("#" + idVBox) != null) {
            if (Config.DEBUG_MODE) {
                System.out.println("idVBox " + idVBox + " already used, createPlayerPane()");
            }
            return null;
        }

        VBox playerPane = new VBox();
        Label playerName = new Label(name);
        Label PLayerScore = new Label("[Score : 0]");

        playerPane.setAlignment(Pos.CENTER);

        playerPane.getChildren().addAll(playerName, PLayerScore);

        playerPane.setId(idVBox);
        return playerPane;
    }


    /**
     * Crée un panneau de cartes pour un joueur.
     * @param idVBox ID du panneau de cartes.
     * @return Panneau de cartes (VBox).
     */
    private VBox createPlayerCardPane(String idVBox) {
        // vérifier si l'idVBox est déjà utilisé
        if (cardCircularPane.lookup("#" + idVBox) != null) {
            if (Config.DEBUG_MODE) {
                System.out.println("idVBox " + idVBox + " already used, createPlayerCardPane()");
            }
            return null;
        }


        VBox cardPane = new VBox();
        FlowPane cards = new FlowPane(5, 5);
        HBox infoSectionHBox = new HBox();

        cardPane.setPadding(new Insets(10));
        cardPane.setSpacing(10);
        cardPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        cardPane.setPrefSize(200, 150);
        cardPane.setMaxSize(200, 150);
        cardPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        cards.setPrefSize(200, 100);

        infoSectionHBox.setSpacing(10);
        infoSectionHBox.setAlignment(Pos.CENTER);

        if (Config.DEBUG_MODE) {
            Label id = new Label("(" + idVBox + ")");
            id.setStyle("-fx-text-fill: gray;");
            infoSectionHBox.getChildren().add(id);
        }

        cardPane.getChildren().addAll(cards, infoSectionHBox);

        cardPane.setId(idVBox);
        return cardPane;
    }


    /**
     * Crée une carte avec sa valeur et potentiellement sa couleur. Si isBuoyCard et isTideCard sont faux, il s'agit d'une carte météo.
     * @param cardValue Valeur de la carte.
     * @param cardColor Couleur de la carte.
     * @param isBuoyCard Indique si la carte est une bouée.
     * @param isTideCard Indique si la carte est une carte de marée.
     * @return Panneau de carte (StackPane).
     */
    public StackPane createCardPane(String cardValue, String cardColor, boolean isBuoyCard, boolean isTideCard) {
        StackPane cardPane = new StackPane();
        Label cardLabel = new Label(cardValue);

        // ajouter une couleur de fond à la carte s'il s'agit d'une carte météo
        if (cardColor != null && !isBuoyCard && !isTideCard) {
            switch (cardColor) {
                case "Y" ->
                        cardPane.setBackground(new Background(new BackgroundFill(Color.GOLD, CornerRadii.EMPTY, Insets.EMPTY)));
                case "G" ->
                        cardPane.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                case "B" ->
                        cardPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
                case "P" ->
                        cardPane.setBackground(new Background(new BackgroundFill(Color.PURPLE, CornerRadii.EMPTY, Insets.EMPTY)));
                case "R" ->
                        cardPane.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                default ->
                        cardPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }

        // si la carte est une bouée, la rendre ronde
        if (isBuoyCard) {
            cardPane.setPrefSize(33, 33);
            cardPane.setMaxSize(33, 33);
            cardPane.setBackground(new Background(new BackgroundFill(Color.LIGHTCORAL, new CornerRadii(999), Insets.EMPTY)));
            cardLabel.setStyle("-fx-font-size: 15px;");
        } else if (isTideCard) {
            cardPane.setPrefSize(25, 33);
            cardPane.setMaxSize(25, 33);

            double value = Integer.parseInt(cardValue);
            double ratio = value / 12.0;
            double filledHeight = 33 * ratio;

            Rectangle filledPart = new Rectangle(25, filledHeight, Color.rgb(0, 0, 255, 0.5));
            Rectangle emptyPart = new Rectangle(25, 33, Color.rgb(0, 0, 255, 0.3));

            filledPart.setTranslateY((33 - filledHeight) / 2.0);
            filledPart.setStroke(Color.TRANSPARENT);

            StackPane tideCard = new StackPane();
            tideCard.setPrefSize(25, 33);
            tideCard.setMaxSize(25, 33);
            tideCard.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            tideCard.getChildren().addAll(emptyPart, filledPart);
            cardPane.getChildren().add(tideCard);

            cardLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: black; -fx-font-weight: bold;");
        } else {
            cardPane.setPrefSize(25, 33);
            cardPane.setMaxSize(25, 33);
            cardPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            cardLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: black;");
        }

        cardPane.getChildren().add(cardLabel);

        return cardPane;
    }


    /**
     * Met à jour les cartes piochées au centre de l'écran.
     * @param cards Cartes sélectionnées.
     */
    public void updateSelectedCards(String[] cards) {
        // si la hbox contient déjà des cartes, les supprimer
        selectedCardsHBox.getChildren().clear();

        // ajouter les nouvelles cartes
        for (String card : cards) {
            // si la carte commence par "0", l'enlever
            if (card.startsWith("0")) {
                card = card.substring(1);
            }
            selectedCardsHBox.getChildren().add(createCardPane(card, null, false, true));
        }
    }


    /**
     * Met à jour les cartes de la pioche. Affiche les 4 dernières cartes de la pioche en agrandissant la dernière.
     * @param deck Cartes de la pioche.
     */
    public void updateDeck(String[] deck) {
        // si la vbox contient déjà des cartes, les supprimer
        deckHBox.getChildren().clear();

        // ajouter les nouvelles cartes
        // afficher seulement mes 4 dernières cartes
        // la dernière carte est plus grande que les autres
        for (int i = deck.length - 1; i >= deck.length - 4; i--) {
            if (deck[i].startsWith("0")) {
                deck[i] = deck[i].substring(1);
            }

            deckHBox.getChildren().addFirst(createCardPane(deck[i], null, false, true));

            if (i == deck.length - 1) { // dernière
                // zoomer
                deckHBox.getChildren().getFirst().setScaleX(1.5);
                deckHBox.getChildren().getFirst().setScaleY(1.5);
            }

            if (i == deck.length - 4) { // premier élément de la liste
                // ajouter une carte grise avant la première carte
                StackPane grayCardPane = createCardPane("", null, false, false);
                grayCardPane.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                grayCardPane.setBorder(null);
                deckHBox.getChildren().addFirst(grayCardPane);
                // cacher la carte grise légèrement derrière la première carte
                grayCardPane.setTranslateX(20);
            }
        }
    }


    /**
     * Met à jour les cartes météo des joueurs. Le format d'une carte est "Y01N" (couleur, valeur, état).
     * @param idVBox ID du panneau de cartes du joueur.
    * @param playerWeatherCards Cartes météo du joueur.
    */
    public void updatePlayerWeatherCards(String idVBox, String[][] playerWeatherCards) {
        // playerWeatherCards est de type : {"Y01N", "B02N", "G03S", "P04G", "R05N"}
        // N : normal, G : grisé, S : sélectionné

        if (cardCircularPane.lookup("#" + idVBox) == null) {
            if (Config.DEBUG_MODE) {
                System.out.println("Player " + idVBox + " not found in cardCircularPane, updatePlayerWeatherCards()");
            }
            return;
        }

        VBox playerCardPane = (VBox) cardCircularPane.lookup("#" + idVBox);
        FlowPane cards = (FlowPane) playerCardPane.getChildren().getFirst();

        // si le flowpane contient déjà des cartes, les supprimer
        cards.getChildren().clear();

        for (String card : playerWeatherCards[0]) {
            if (card.length() >= 3) {
                String cardColor = card.substring(0, 1);
                String cardValue = card.substring(1, 3);
                String cardState = card.substring(3);

                // si la valeur de la carte commence par "0", l'enlever
                if (cardValue.startsWith("0")) {
                    cardValue = cardValue.substring(1);
                }

                StackPane cardStackPane = createCardPane(cardValue, cardColor, false, false);

                // griser ou zoomer la carte selon son état
                switch (cardState) {
                    case "G" -> {
                        cardStackPane.setDisable(true);
                        cardStackPane.setOpacity(0.5);
                    }
                    case "S" -> {
                        cardStackPane.setScaleX(1.2);
                        cardStackPane.setScaleY(1.2);
                    }
                }

                cards.getChildren().add(cardStackPane);
            } else {
                if (Config.DEBUG_MODE) {
                    System.out.println("Invalid card format: " + card + ", updatePlayerWeatherCards()");
                }
            }
        }
    }


    /**
     * Met à jour le score d'un joueur.
     * @param idVBox ID du panneau de joueur.
     * @param score Score du joueur.
     */
    public void updatePlayerScore(String idVBox, int score) {
        if (playerCircularPane.lookup("#" + idVBox) == null) {
            if (Config.DEBUG_MODE) {
                System.out.println("Player " + idVBox + " not found in playerCircularPane, updatePlayerScore()");
            }
            return;
        }

        Label playerScore = (Label) ((VBox) playerCircularPane.lookup("#" + idVBox)).getChildren().get(1);
        playerScore.setText("[Score : " + score + "]");
    }


    /**
     * Met à jour la carte marée reçue pour un joueur.
     * @param idVBox ID du panneau de cartes du joueur.
     * @param tideCards Carte marée du joueur.
     */
    public void updatePlayerTideCards(String idVBox, String tideCards) {
        // Vérifier si le joueur existe dans le circularPane
        VBox playerCardPane = (VBox) cardCircularPane.lookup("#" + idVBox);
        if (playerCardPane == null) {
            if (Config.DEBUG_MODE) {
                System.out.println("Player " + idVBox + " not found in cardCircularPane, updatePlayerTideCards()");
            }
            return;
        }

        // Vérifier si la section info existe
        if (playerCardPane.getChildren().size() <= 1 || !(playerCardPane.getChildren().get(1) instanceof HBox)) {
            if (Config.DEBUG_MODE) {
                System.out.println("Info section not found for player " + idVBox + " in updatePlayerTideCards()");
            }
            return;
        }

        HBox infoSection = (HBox) playerCardPane.getChildren().get(1);

        // Chercher la carte marée existante
        StackPane existingTideCard = null;
        for (Node child : infoSection.getChildren()) {
            if (child instanceof StackPane && "tideCard".equals(child.getId())) {
                existingTideCard = (StackPane) child;
                break;
            }
        }

        if (existingTideCard != null) {
            // Mettre à jour les rectangles représentant la hauteur de marée
            StackPane tideCardContent = (StackPane) existingTideCard.getChildren().get(0);
            Rectangle filledPart = (Rectangle) tideCardContent.getChildren().get(1);
            ((Label)existingTideCard.getChildren().get(1)).setText(tideCards);

            double value = Integer.parseInt(tideCards);
            double ratio = value / 12.0;
            double filledHeight = 33 * ratio;

            filledPart.setHeight(filledHeight);
            filledPart.setTranslateY((33 - filledHeight) / 2.0);

            if (Config.DEBUG_MODE) {
                System.out.println("Updated tide card for player " + idVBox + " with value: " + tideCards);
            }
        } else {
            // Ajouter une nouvelle carte marée si elle n'existe pas
            StackPane tideCardCreated = createCardPane(tideCards, null, false, true);
            tideCardCreated.setId("tideCard");
            infoSection.getChildren().addFirst(tideCardCreated);

            if (Config.DEBUG_MODE) {
                System.out.println("Added new tide card for player " + idVBox + " with value: " + tideCards);
            }
        }
    }



    /**
     * Met à jour le nombre de bouées pour un joueur.
     * @param idVBox ID du panneau de cartes du joueur.
     * @param buoyCards Nombre de bouées du joueur.
     */
    public void updatePlayerBuoyCards(String idVBox, int buoyCards) {
        if (cardCircularPane.lookup("#" + idVBox) == null) {
            if (Config.DEBUG_MODE) {
                System.out.println("Player " + idVBox + " not found in cardCircularPane, updatePlayerBuoyCards()");
            }
            return;
        }

        VBox playerCardPane = (VBox) cardCircularPane.lookup("#" + idVBox);

        // Chercher la carte existante
        StackPane existingBuoyCard = (StackPane) playerCardPane.lookup("#buoyCard");

        if (existingBuoyCard != null) {
            // Mettre à jour la carte existante
            Label cardLabel = (Label) existingBuoyCard.getChildren().get(0);
            cardLabel.setText(String.valueOf(buoyCards));

            if (Config.DEBUG_MODE) {
                System.out.println("Updated buoy card for player " + idVBox + " with value: " + buoyCards);
            }
        } else {
            // Ajouter une nouvelle carte si elle n'existe pas
            HBox infoSection = (HBox) playerCardPane.getChildren().get(1);
            StackPane buoyCardCreated = createCardPane(String.valueOf(buoyCards), null, true, false);
            buoyCardCreated.setId("buoyCard");
            infoSection.getChildren().add(buoyCardCreated);

            if (Config.DEBUG_MODE) {
                System.out.println("Added new buoy card for player " + idVBox + " with value: " + buoyCards);
            }
        }
    }



    /**
     * Met à jour le statut d'un joueur (éliminé ou non).
     * @param idPlayerVBox ID du panneau de joueur.
     * @param idCardVBox ID du panneau de cartes du joueur.
     * @param isEliminated Indique si le joueur est éliminé.
     */
    public void updatePlayerStatus(String idPlayerVBox, String idCardVBox, Boolean isEliminated) {
        if (playerCircularPane.lookup("#" + idPlayerVBox) == null || cardCircularPane.lookup("#" + idCardVBox) == null) {
            if (Config.DEBUG_MODE) {
                System.out.println("Player " + idPlayerVBox + " not found in playerCircularPane or " + idCardVBox + " not found in cardCircularPane, updatePlayerStatus()");
            }
            return;
        }

        // passer le nom et le score en gris + grisé son playerCardPane
        VBox playerPane = (VBox) playerCircularPane.lookup("#" + idPlayerVBox);
        VBox cardPane = (VBox) cardCircularPane.lookup("#" + idCardVBox);

        if (isEliminated) {
            playerPane.setDisable(true);
            cardPane.setDisable(true);
            cardPane.setOpacity(0.5);
        } else {
            playerPane.setDisable(false);
            cardPane.setDisable(false);
            cardPane.setOpacity(1);
        }
    }


    /**
     * Met à jour le classement des joueurs situé en bas à droite de l'écran.
     * @param classification Classement des joueurs.
     */
    public void updateClassification(String[][] classification) {
        // si le gridpane contient déjà des scores, les supprimer
        classificatorPane.getChildren().clear();

        // ajouter les noms de joueurs et scores avec une taille de police décroissante
        for (int i = 0; i < classification.length; i++) {
            Label playerName = new Label(classification[i][0]);
            Label playerScore = new Label(classification[i][1]);

            playerName.setStyle("-fx-font-size: " + (18 - (i/1.5)) + "px;");
            playerScore.setStyle("-fx-font-size: " + (18 - (i/1.5)) + "px;");

            // si premier joueur, mettre en gras
            if (i == 0) {
                playerName.setStyle(playerName.getStyle() + " -fx-font-weight: bold;");
                playerScore.setStyle(playerScore.getStyle() + " -fx-font-weight: bold;");
            }

            classificatorPane.add(playerName, 0, i);
            classificatorPane.add(playerScore, 1, i);
        }
    }


    /**
     * Met à jour les cartes météo écartées du jeu (quand nb joueurs < 5).
     * @param discardedCards Cartes météo écartées.
     */
    public void updateDiscardedCards(String[] discardedCards) {
        // si le flowpane contient déjà des cartes, les supprimer
        discardedCardsFlowPane.getChildren().clear();

        // ajouter les nouvelles cartes
        for (String card : discardedCards) {
            String cardValue = card.substring(1);
            String cardColor = card.substring(0, 1);

            discardedCardsFlowPane.getChildren().add(createCardPane(cardValue, cardColor, false, false));
        }

        // si pas de carte, ajouter un message
        if (discardedCards.length == 0) {
            Label noCardLabel = new Label("[No card discarded]");
            noCardLabel.setStyle("-fx-font-size: 18px;");
            discardedCardsFlowPane.getChildren().add(noCardLabel);
        }
    }


    /**
     * Fait pivoter les zones de cartes pour aligner la carte avec l'ID donné face au joueur spécifié.
     * @param cardId ID de la zone de cartes à placer face au joueur.
     * @param playerId ID du joueur de référence.
     */
    public void rotateCardsToPlayer(String cardId, String playerId) {
        Platform.runLater(() -> {
            VBox targetCardPane = (VBox) cardCircularPane.lookup("#" + cardId);
            VBox targetPlayerPane = (VBox) playerCircularPane.lookup("#" + playerId);

            if (targetCardPane == null || targetPlayerPane == null) {
                if (Config.DEBUG_MODE) {
                    System.out.println("ID invalide : carte (" + cardId + ") ou joueur (" + playerId + ")");
                }
                return;
            }

            int totalCards = cardCircularPane.getChildren().size();
            int targetIndex = cardCircularPane.getChildren().indexOf(targetCardPane);

            if (targetIndex == -1) return;

            // calculer l'offset de rotation
            int offset = (targetIndex - currentRotationIndex + totalCards) % totalCards;

            if (Config.DEBUG_MODE) {
                System.out.println("Rotation offset : " + offset);
            }

            if (offset == 0) return;  // Pas besoin d'animer si tout est déjà aligné

            // debug
            if (Config.DEBUG_MODE) {
                System.out.println("Rotation des cartes : " + cardId + " aligné avec " + playerId + ", currentRotationIndex : " + currentRotationIndex);
            }

            // ⚡ Animation de rotation
            animateCardRotation(offset);
            currentRotationIndex = targetIndex;
        });
    }


    /**
     * Anime la rotation des cartes avec compensation pour que les zones restent droites.
     * @param offset Nombre de positions à décaler.
     */
    private void animateCardRotation(int offset) {
        double anglePerCard = 360.0 / cardCircularPane.getChildren().size();
        double rotationAngle = anglePerCard * offset;

        // rotation de l'ensemble du circularPane
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1), cardCircularPane);
        rotateTransition.setByAngle(rotationAngle);
        rotateTransition.setCycleCount(1);
        rotateTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        double[] initialRotations = new double[cardCircularPane.getChildren().size()];
        for (Node card : cardCircularPane.getChildren()) {
            // pour chaque carte, récupérer la rotation initiale et la stocker
            double initialRotation = card.getRotate();
            initialRotations[cardCircularPane.getChildren().indexOf(card)] = initialRotation;
        }

        // compensation de la rotation sur chaque zone de carte
        rotateTransition.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            double progress = newTime.toMillis() / rotateTransition.getDuration().toMillis();
            double currentAngle = rotationAngle * progress;

            for (Node card : cardCircularPane.getChildren()) {
                card.setRotate(initialRotations[cardCircularPane.getChildren().indexOf(card)] - currentAngle);  // inversion de la rotation en tenant compte de la rotation initiale
            }
        });

        // si la rotation de cardCircularPane dépasse 360°, la remettre à 0
        // éviter l'accumulation de la rotation
        rotateTransition.setOnFinished(event -> {
            if (cardCircularPane.getRotate() >= 360) {
                cardCircularPane.setRotate(cardCircularPane.getRotate() % 360);
                for (Node card : cardCircularPane.getChildren()) {
                    card.setRotate(0);
                }
            }
        });

        rotateTransition.play();
    }

    @Override
    public void onChanged(Change<? extends String, ? extends ArrayList<String>> change) {
        if(change.wasAdded()){
            String header = change.getKey();
            ArrayList<String> param = change.getValueAdded();
            switch(header) {
                case "IP" :
                    ordreJoueur = new ArrayList<>(List.of(param.get(0).split(",")));
                    scoreAssoc = new String[ordreJoueur.size()][2];
                    for(int i = 0; i < ordreJoueur.size(); i++) {
                        scoreAssoc[i][0] = ordreJoueur.get(i);
                        scoreAssoc[i][1] = "0";
                        playerCircularPane.getChildren().add(createPlayerPane(ordreJoueur.get(i) , "pl" + i));
                        updatePlayerScore("pl"+i,0);
                        cardCircularPane.getChildren().add(createPlayerCardPane("ca" + i));
                    }

                    updateClassification(scoreAssoc);

                    break;
                case "IPME" :
                    String[] carteMeteo = param.get(0).split(",");
                    break;
                case "IM" :
                    for(int i = 0; i < ordreJoueur.size(); i++) {
                        updatePlayerStatus("pl" + i, "ca" + i, false);
                    }
                    roundLabel.setText(param.get(0));
                    break;
                case "IPMA":
                    String[] carteMareePioche = param.get(0).split(",");
                    updateDeck(carteMareePioche);
                    break;
                case "DCJ":
                    if(compteur > 0 && !firstRound) {
                        compteur+=1;
                        //System.out.println("rotate");
                        rotateCardsToPlayer("ca0","pl"+(ordreJoueur.size() - 1));
                        //rotateCardsToPlayer("ca0","pl0");
                    }else
                        compteur += 1;



                    if(indexUtilisateur == ordreJoueur.size() - 1)
                        indexUtilisateur = 0;
                    else
                        indexUtilisateur++;

                    //System.out.println("index : " + indexUtilisateur);
                    // ajoute des N a la fin de chaque carte
                    String[][] cartes = new String[][] {param.get(0).split(",")};

                    // Parcourir et modifier les éléments pour ajouter "N"
                    for (int i = 0; i < cartes.length; i++) {
                        for (int j = 0; j < cartes[i].length; j++) {
                            cartes[i][j] = cartes[i][j] + "N";
                        }
                    }
                    //System.out.println("Manche");
                    //System.out.println("DCJ : ca" + indexUtilisateur + "\ncartes : " + param.get(0));
                    updatePlayerWeatherCards("ca"+indexUtilisateur,cartes);
                    break;
                case "ICMR":
                    updateDiscardedCards(param.get(0).split(","));
                    break;
                case "ITP":
                    String[] nbbouee = param.get(0).split(",");
                    String[] carteMareeJoueur = param.get(1).split(",");
                    String[] carteMareeSortie = param.get(3).split(",");

                    foldLabel.setText(param.get(2));

                    updateSelectedCards(carteMareeSortie);

                    for(int i = 0; i < ordreJoueur.size(); i++) {
                        // set les cartes marées des joueurs
                        updatePlayerTideCards("ca" + i, String.valueOf(Integer.parseInt(carteMareeJoueur[i])));
                        // set le nombre de bouées des joueurs
                        updatePlayerBuoyCards("ca" + i, Integer.parseInt(nbbouee[i]));
                    }
                    break;
                case "RCPB":

                    String[] nomj = param.get(0).split(",");
                    String[] listeeffet = param.get(1).split(",");

                    for(int i = 0; i < nomj.length; i++) {
                        if(listeeffet[i].equals("E")) {
                            int index = ordreJoueur.indexOf(nomj[i]);
                            updatePlayerStatus("pl" + index, "ca" + index, true);
                        }
                    }

                    break;
                case "IFP":
                    distribue = 0;
                    indexRotate = 0;
                    compteur = 0;
                    firstRound = true;
                    break;
                case "IFM":
                    // message de fin de manche
                    String[] score = param.get(0).split(",");
                    for(int i = 0; i < score.length; i++) {
                        // ajout des scores dans le tableau
                        scoreAssoc[i][1] = "" + (Integer.parseInt(scoreAssoc[i][1]) + Integer.parseInt(score[i]));
                        updatePlayerScore("pl"+i,Integer.parseInt(scoreAssoc[i][1]));
                    }

                    // creation du tableau dupliquer
                    String[][] sortedPlayers = Arrays.stream(scoreAssoc).map(row -> row.clone()).toArray(String[][]::new);

                    // tri du nouveau tableau
                    Arrays.sort(sortedPlayers, (a,b) -> Integer.parseInt(b[1]) - Integer.parseInt(a[1]));

                    updateClassification(sortedPlayers);
                    firstRound = false;
                    compteur = 0;
                    break;
                default:
                    break;
            }
        }
    }
}
