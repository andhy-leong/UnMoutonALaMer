package players.idjr.view;

import java.io.InputStream;

import common.ImageHandler;
import common.ui.BaseScreenView;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import players.idjr.services.NavigationServiceIDJR;
import players.idjr.viewmodel.GameScreenViewModel;

public class GameScreenView extends BaseScreenView {
    private GameScreenViewModel viewModel;

    /* partie gauche */
    private Label partNameLabel;
    private Label roundLabel;

    /* conteneur principal */
    private VBox centerVBox;

    /* Texte du haut */
    private Label topLabel;

    /* Zone centrale - informations de partie */
    private HBox centerHBox;

    // bouées
    private VBox buoysVBox;
    private Label buoysLabel;
    private StackPane buoysStackPane;
    private ImageView buoy;
    private Label buoyValueLabel;

    // score
    private VBox scoreVBox;
    private Label scoreLabel;
    private Label scoreValueLabel;
    private StackPane scoreStackPane;

    // carte marée
    private VBox tideCardVBox;
    private Label tideCardLabel;
    private ImageView tideCard;
    private StackPane tideStackPane;
    private Rectangle tidePlaceholder;

    /* Zone centrale - fin de manche */
    private GridPane centerGridPane;

    private Label endRoundScoreLabel;
    private Label endRoundScoreValueLabel;
    private Label endRoundLostBuoysLabel;
    private Label endRoundLostBuoysValueLabel;
    private Label endRoundOldScoreLabel;
    private Label endRoundOldScoreValueLabel;

    /* Cartes en main */
    private StackPane handStackPane;

    /* Zone de tri */
    private HBox sortingHBox;
    private Label sortingLabel;
    private ToggleGroup sortingGroup;
    private RadioButton crescentRadioButton;
    private RadioButton decrescentRadioButton;


    public GameScreenView(NavigationServiceIDJR navigationService) {
        this.modelTopBar = 4;
        super.init();
    }

    @Override
    protected void customizeScreen() {
        createComponents();
        setupComponents();
        organizeComponents();

        addContainerRoot(centerVBox, null);

        // Par défaut, afficher la section d'information de partie (bouées, score, carte marée)
        createInfoSection();
    }

    private void createComponents() {
        centerVBox = new VBox(20);

        /* partie gauche */
        partNameLabel = new Label();
        roundLabel = new Label();

        /* Texte du haut */
        topLabel = new Label();

        /* Cartes en main */
        handStackPane = new StackPane();

        /* Zone de tri */
        sortingHBox = new HBox(30);
        sortingLabel = new Label("[Trie]");
        sortingGroup = new ToggleGroup();
        crescentRadioButton = new RadioButton("[croissant]");
        decrescentRadioButton = new RadioButton("[décroissant]");
    }

    private void setupComponents() {
        centerVBox.setPadding(new Insets(20, 10, 30, 10));
        centerVBox.maxWidthProperty().bind(this.widthProperty());
        centerVBox.minWidthProperty().bind(this.heightProperty());
        centerVBox.setAlignment(Pos.CENTER);

        /* Texte du haut */
        topLabel.getStyleClass().add("title-idjr");
        topLabel.setAlignment(Pos.CENTER);
        topLabel.prefWidthProperty().bind(centerVBox.widthProperty());

        /* Cartes en main */
        handStackPane.getStyleClass().add("hand-stack-pane");
        handStackPane.setPrefHeight(300);
        handStackPane.setMinHeight(250);
        handStackPane.minWidthProperty().bind(centerVBox.widthProperty());
        handStackPane.maxWidthProperty().bind(centerVBox.widthProperty());
        handStackPane.setPadding(new Insets(10));
        handStackPane.setAlignment(Pos.BOTTOM_LEFT);


        /* Zone de tri */
        sortingHBox.setAlignment(Pos.CENTER_LEFT);
        sortingGroup.selectToggle(crescentRadioButton);
        crescentRadioButton.setSelected(true);

        sortingHBox.getStyleClass().add("sorting-hbox");

    }

    private void organizeComponents() {
        /* partie gauche */
        leftVBox.getChildren().addAll(partNameLabel, roundLabel);

        /* Zone de tri */
        sortingGroup.getToggles().addAll(crescentRadioButton, decrescentRadioButton);
        sortingHBox.getChildren().addAll(sortingLabel, crescentRadioButton, decrescentRadioButton);

        Region vSapce1 = new Region();
        Region vSapce2 = new Region();
        VBox.setVgrow(vSapce1, Priority.ALWAYS);
        VBox.setVgrow(vSapce2, Priority.ALWAYS);
        centerVBox.getChildren().addAll(topLabel, vSapce1, vSapce2, handStackPane, sortingHBox);
    }

    private void createInfoSection() {
        /* --- Create components --- */
        centerHBox = new HBox(20);

        // bouées
        buoysVBox = new VBox(20);
        buoysLabel = new Label("[Nb bouées]");
        buoysStackPane = new StackPane();
        InputStream buoyImageStream = ImageHandler.loadImage("/common/images/images/carteBouee.png", "carte bouée");
        buoy = (buoyImageStream != null) ? new ImageView(new Image(buoyImageStream)) : new ImageView();
        buoyValueLabel = new Label();
        
        // applique les effets aux cartes 
        if (buoy != null && buoy.getImage() != null) {
            applyCardEffects(buoy);
        }
        
        // Ajout du rectangle pour l'emplacement de la carte bouée
        Rectangle buoyPlaceholder = new Rectangle(95, 152);
        buoyPlaceholder.getStyleClass().add("buoy-placeholder");

        // score
        scoreVBox = new VBox(20);
        scoreLabel = new Label("Score :");
        scoreStackPane = new StackPane();
        scoreValueLabel = new Label();

        // carte marée
        tideCardVBox = new VBox(20);
        tideCardLabel = new Label("Carte marée reçue :");
        InputStream tideCardImageStream = ImageHandler.loadImage("/common/images/images/carteMareeRetournee.png", "carte marée emplacement");
        tideCard = (tideCardImageStream != null) ? new ImageView(new Image(tideCardImageStream)) : new ImageView();
        
        // applique les effets aux cartes 
        if (tideCard != null && tideCard.getImage() != null) {
            applyCardEffects(tideCard);
        }
        
        // Ajout du rectangle pour l'emplacement de la carte marée
        Rectangle tidePlaceholder = new Rectangle(95, 152);
        tidePlaceholder.getStyleClass().add("buoy-placeholder");

        /* --- Setup components --- */
        // bouées
        buoysVBox.setAlignment(Pos.TOP_CENTER);
        buoyValueLabel.getStyleClass().add("buoy-value-label");
        buoysStackPane.setAlignment(Pos.CENTER);
        buoysStackPane.setMinSize(95, 152);
        buoysStackPane.setMaxSize(95, 152);
        buoy.fitWidthProperty().bind(buoysStackPane.widthProperty());
        buoy.fitHeightProperty().bind(buoysStackPane.heightProperty());
        
        // effet d'ombre pour les cartes
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0.4, 0.4, 0.4));
        
        buoyPlaceholder.setEffect(dropShadow);
        tidePlaceholder.setEffect(dropShadow);
        
        // empiler les éléments dans le bon ordre
        buoysStackPane.getChildren().addAll(buoyPlaceholder, buoy, buoyValueLabel);

        // score
        scoreVBox.setAlignment(Pos.TOP_CENTER);
        scoreValueLabel.getStyleClass().add("title-idjr");
        scoreStackPane.setAlignment(Pos.CENTER);
        scoreStackPane.setMinSize(95, 152);
        scoreStackPane.setMaxSize(95, 152);

        // carte marée
        tideCardVBox.setAlignment(Pos.TOP_CENTER);
        tideCard.setFitWidth(95);
        tideCard.setFitHeight(152);
        
        // Créer un StackPane pour les cartes marée avec effet d'empilement
        tideStackPane = new StackPane();
        tideStackPane.setMinSize(95, 152);
        tideStackPane.setMaxSize(95, 152);
        
        // Ajouter le placeholder et la carte
        tideStackPane.getChildren().addAll(tidePlaceholder, tideCard);
        
        // Effet de décalage pour simuler l'empilement
        tideCard.translateXProperty().set(2);
        tideCard.translateYProperty().set(2);

        /* --- Organize components --- */
        // bouées
        buoysVBox.getChildren().addAll(buoysLabel, buoysStackPane);

        // score
        scoreStackPane.getChildren().add(scoreValueLabel);
        scoreVBox.getChildren().addAll(scoreLabel, scoreStackPane);

        // carte marée
        tideCardVBox.getChildren().addAll(tideCardLabel, tideStackPane);

        Region hSpace1 = new Region();
        Region hSpace2 = new Region();
        HBox.setHgrow(hSpace1, Priority.ALWAYS);
        HBox.setHgrow(hSpace2, Priority.ALWAYS);
        centerHBox.getChildren().addAll(buoysVBox, hSpace1, scoreVBox, hSpace2, tideCardVBox);

        // ajouter la section à la page
        addTopSectionToCenterVBox(centerHBox);
    }

    private void addTopSectionToCenterVBox(Node topSection) {
        if (centerVBox.getChildren().size() >= 6) {
            // si centerVBox contient 6 éléments ou plus, ajouter la nouvelle section après le titre
            centerVBox.getChildren().add(2, topSection);
        } else {
            // sinon ajouter en 3e position
            centerVBox.getChildren().add(2, topSection);
        }
    }

    private void createEndRoundSection() {
        /* --- Create components --- */
        centerGridPane = new GridPane();

        endRoundScoreLabel = new Label("[score manche :]");
        endRoundScoreValueLabel = new Label();
        endRoundLostBuoysLabel = new Label("[Nb bouées perdu :]");
        endRoundLostBuoysValueLabel = new Label();
        endRoundOldScoreLabel = new Label("[score précédente manche:]");
        endRoundOldScoreValueLabel = new Label();

        /* --- Setup components --- */
        centerGridPane.setHgap(40);
        centerGridPane.setVgap(15);
        centerGridPane.add(endRoundScoreLabel, 0, 0);
        centerGridPane.add(endRoundScoreValueLabel, 1, 0);
        centerGridPane.add(endRoundLostBuoysLabel, 0, 1);
        centerGridPane.add(endRoundLostBuoysValueLabel, 1, 1);
        centerGridPane.add(endRoundOldScoreLabel, 0, 2);
        centerGridPane.add(endRoundOldScoreValueLabel, 1, 2);

        centerGridPane.setAlignment(Pos.CENTER);

        /* --- Organize components --- */
        // Ajouter la section sans supprimer les autres
        centerVBox.getChildren().add(2, centerGridPane);
    }

    @Override
    protected void bindToViewModel() {
        // Ne rien faire ici car le viewModel n'est pas encore défini
    }

    public void setViewModel(GameScreenViewModel viewModel) {
        this.viewModel = viewModel;
        // binding maintenant que le viewModel est défini
        if (viewModel != null) {
            menuButton.setOnAction(event -> viewModel.onMenuButtonClicked());

            titleLabel.textProperty().bind(viewModel.titleProperty());
            playerNameLabel.textProperty().bind(viewModel.playerNameProperty());
            partNameLabel.textProperty().bind(viewModel.partNameLabelProperty());
            roundLabel.textProperty().bind(viewModel.roundLabelProperty());
            topLabel.textProperty().bind(viewModel.topLabelProperty());

            // intenationalisation
            menuButton.textProperty().bind(viewModel.menuButtonProperty());
            buoysLabel.textProperty().bind(viewModel.buoysLabelProperty());
            scoreLabel.textProperty().bind(viewModel.scoreLabelProperty());
            tideCardLabel.textProperty().bind(viewModel.tideCardLabelProperty());
            sortingLabel.textProperty().bind(viewModel.sortingLabelProperty());
            crescentRadioButton.textProperty().bind(viewModel.crescentRadioButtonProperty());
            decrescentRadioButton.textProperty().bind(viewModel.decrescentRadioButtonProperty());

            // observer les changements de consignes pour détecter la fin de manche et le début d'une nouvelle manche
            viewModel.topLabelProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    if (newVal != null) {
                        if (newVal.equals("La manche est terminée !")) {
                            // afficher la section de fin de manche
                            createEndRoundSection();
                            // bind les valeurs après la création de la section
                            if (endRoundScoreLabel != null) {
                                endRoundScoreLabel.textProperty().bind(viewModel.endRoundScoreLabelProperty());
                                endRoundLostBuoysLabel.textProperty().bind(viewModel.endRoundLostBuoysLabelProperty());
                                endRoundOldScoreLabel.textProperty().bind(viewModel.endRoundOldScoreLabelProperty());
                            }
                            if (endRoundScoreValueLabel != null) {
                                endRoundScoreValueLabel.textProperty().bind(viewModel.endRoundScoreValueProperty());
                                endRoundLostBuoysValueLabel.textProperty().bind(viewModel.endRoundBuoysValueProperty());
                                endRoundOldScoreValueLabel.textProperty().bind(viewModel.endRoundOldScoreValueProperty());
                            }
                        } else if (newVal.equals("Début du pli")) {
                            // suppression de la section de fin de manche si elle existe
                            if (centerGridPane != null && centerVBox.getChildren().contains(centerGridPane)) {
                                centerVBox.getChildren().remove(centerGridPane);
                                centerGridPane = null;
                                endRoundScoreValueLabel = null;
                                endRoundLostBuoysValueLabel = null;
                                endRoundOldScoreValueLabel = null;
                            }
                            
                            if (tideStackPane != null) {
                                Platform.runLater(() -> {
                                    try {
                                        Node placeholder = null;
                                        Node baseCard = null;
                                        if (tideStackPane.getChildren().size() > 0) {
                                            placeholder = tideStackPane.getChildren().get(0);
                                        }
                                        if (tideStackPane.getChildren().size() > 1) {
                                            baseCard = tideStackPane.getChildren().get(1);
                                        }
                                        
                                        tideStackPane.getChildren().clear();
                                        
                                        if (placeholder != null) {
                                            tideStackPane.getChildren().add(placeholder);
                                        }
                                        if (baseCard != null) {
                                            tideStackPane.getChildren().add(baseCard);
                                            baseCard.setTranslateX(2);
                                            baseCard.setTranslateY(2);
                                        }
                                    } catch (Exception e) {
                                        //System.err.println("Erreur lors de la réinitialisation des cartes marée: " + e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                });
            });

            if (buoyValueLabel != null && scoreValueLabel != null) {
                buoyValueLabel.textProperty().bind(viewModel.buoysValueProperty());
                scoreValueLabel.textProperty().bind(viewModel.scoreValueProperty());

                // Modifier le listener existant pour ajouter l'animation
                viewModel.buoysValueProperty().addListener((obs, oldValue, newValue) -> {
                    Platform.runLater(() -> {
                        if (newValue != null) {
                            try {
                                int nbBouees = Integer.parseInt(newValue);
                                String imagePath = (nbBouees == 0) ?
                                    "/common/images/carteBoueeRetournee.png" :
                                    "/common/images/carteBouee.png";

                                InputStream buoyImageStream = ImageHandler.loadImage(imagePath, "carte bouée");
                                if (buoyImageStream != null) {
                                    buoy.setImage(new Image(buoyImageStream));
                                    // afficher le nombre seulement si > 0
                                    buoyValueLabel.setVisible(nbBouees > 0);
                                    
                                    // animation de texte si c'est une perte de bouée
                                    if (oldValue != null && !oldValue.equals(newValue)) {
                                        try {
                                            int oldBouees = Integer.parseInt(oldValue);
                                            if (nbBouees < oldBouees) {
                                                animateBuoyValueChange(newValue);
                                            }
                                        } catch (NumberFormatException e) {
                                            //System.err.println("Format invalide pour l'ancien nombre de bouées: " + oldValue);
                                        }
                                    }
                                }
                            } catch (NumberFormatException e) {
                                //System.err.println("Format invalide pour le nombre de bouées: " + newValue);
                            }
                        }
                    });
                });
            }

            if (endRoundScoreValueLabel != null && endRoundLostBuoysValueLabel != null && endRoundOldScoreValueLabel != null) {
                endRoundScoreValueLabel.textProperty().bind(viewModel.endRoundScoreValueProperty());
                endRoundLostBuoysValueLabel.textProperty().bind(viewModel.endRoundBuoysValueProperty());
                endRoundOldScoreValueLabel.textProperty().bind(viewModel.endRoundOldScoreValueProperty());
            }

            // Mise à jour de la carte marée quand on en reçoit une
            viewModel.carteMareeRecueProperty().addListener((obs, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    if (tideStackPane == null) return; 
                    
                    if (newValue != null && !newValue.equals("00")) {
                        String cardNumber;
                        ImageView newTideCard = null;
                        try {
                            cardNumber = String.format("%02d", Integer.parseInt(newValue));
                            InputStream tideCardImageStream = ImageHandler.loadImage("/common/images/carteMaree" + cardNumber + ".png", null);
                            
                            // Si le format à 2 chiffres échoue, essayer le format à 1 chiffre
                            if (tideCardImageStream == null) {
                                cardNumber = String.valueOf(Integer.parseInt(newValue));
                                tideCardImageStream = ImageHandler.loadImage("/common/images/carteMaree" + cardNumber + ".png", "carte marée " + cardNumber);
                            }

                            if (tideCardImageStream != null) {
                                newTideCard = new ImageView(new Image(tideCardImageStream));
                                newTideCard.setFitWidth(95);
                                newTideCard.setFitHeight(152);
                                applyCardEffects(newTideCard);
                            }
                        } catch (NumberFormatException e) {
                            //System.err.println("Format de carte marée invalide: " + newValue);
                            return;
                        }

                        if (newTideCard != null) {
                            final ImageView finalNewTideCard = newTideCard;
                            // calculer le décalage en fonction du nombre de cartes déjà présentes
                            int cardCount = tideStackPane.getChildren().size() - 1; // -1 pour le placeholder
                            finalNewTideCard.setTranslateX(2 * cardCount);
                            finalNewTideCard.setTranslateY(2 * cardCount);
                            
                            // nouvelle carte au-dessus des autres
                            tideStackPane.getChildren().add(finalNewTideCard);
                            
                            // animation d'arrivée de la carte
                            animateNewTideCard(finalNewTideCard);
                            
                            // on limite le nombre de cartes empilées visibles
                            if (tideStackPane.getChildren().size() > 4) { // placeholder + 3 cartes au maximum
                                tideStackPane.getChildren().remove(2); // supprimer la plus ancienne carte
                            }
                        }
                    } else {
                        // Afficher la carte retournée si pas de carte
                        InputStream tideCardImageStream = ImageHandler.loadImage("/common/images/carteMareeRetournee.png", "carte marée retournée");
                        if (tideCardImageStream != null && tideCard != null) {
                            tideCard.setImage(new Image(tideCardImageStream));
                            // reset du StackPane en préservant les éléments non-null
                            tideStackPane.getChildren().clear();
                            if (tidePlaceholder != null) {
                                tideStackPane.getChildren().add(tidePlaceholder);
                            }
                            tideStackPane.getChildren().add(tideCard);
                            tideCard.setTranslateX(2);
                            tideCard.setTranslateY(2);
                        }
                    }
                });
            });

            crescentRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    viewModel.onSortWeatherCards(false);
                }
            });
            decrescentRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    viewModel.onSortWeatherCards(true);
                }
            });

            // déteceter un changement dans la liste des cartes
            viewModel.weatherCardsProperty().addListener((ListChangeListener<String>) change -> {
                Platform.runLater(() -> {
                    updateHandStackPane(viewModel.weatherCardsProperty());
                });
            });

            // Initialiser l'affichage des cartes en main
            Platform.runLater(() -> {
                createWeatherCards(viewModel.weatherCardsProperty());
            });
        }
    }

    private void detectWeatherCardClicked() {
        // détecter un clique sur une carte
        for (int i = 0; i < handStackPane.getChildren().size(); i++) {
            int finalI = i;
            handStackPane.getChildren().get(i).setOnMouseClicked(event -> {
                // récupérer le user data de la carte
                String cardValue = (String) handStackPane.getChildren().get(finalI).getUserData();
                if (cardValue != null) {
                    // informer le viewModel
                    viewModel.onWeatherCardClicked(cardValue);
                }
            });
        }
    }

    private void createWeatherCards(ListProperty<String> weatherCardsProperty) {
        handStackPane.getChildren().clear();

        double cardWidth = 100;
        double cardHeight = 160;
        int numberOfCards = weatherCardsProperty.size();
        double availableWidth = handStackPane.getWidth() - cardWidth - 20;

        for (String s : weatherCardsProperty) {
            StackPane card = createSingleWeatherCard(s, cardWidth, cardHeight);
            handStackPane.getChildren().add(card);
        }

        // positionner les cartes
        updateCardPositions(availableWidth, numberOfCards);

        // Animer la distribution si c'est une nouvelle main
        if (isNewHand(weatherCardsProperty)) {
            animateCardDealing();
        }

        updateHandStackPane(weatherCardsProperty);
        detectWeatherCardClicked();
    }

    private boolean isNewHand(ListProperty<String> weatherCardsProperty) {
        return weatherCardsProperty.stream().allMatch(card -> card.endsWith("N"));
    }

    private StackPane createSingleWeatherCard(String cardData, double width, double height) {
        StackPane card = new StackPane();
        card.setMaxSize(width, height);
        card.setMinSize(width, height);

        String cardName = cardData.substring(0, 3);
        String cardImagePath = "/common/images/carteMeteo" + cardName + ".png";
        InputStream imageStream = ImageHandler.loadImage(cardImagePath, "carte météo " + cardName);

        ImageView cardImage = null;
        if (imageStream != null) {
            cardImage = new ImageView(new Image(imageStream));
            cardImage.setFitWidth(width);
            cardImage.setFitHeight(height);
            cardImage.setPreserveRatio(true);
            
            // Appliquer le clip pour les coins arrondis
            Rectangle clip = new Rectangle(width, height);
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            card.setClip(clip);
            
            card.getChildren().add(cardImage);
        } else {
            //System.err.println("Image non trouvée pour la carte: " + cardImagePath);
            Label cardLabel = new Label(cardName);
            cardLabel.getStyleClass().add("card-label");
            card.getChildren().add(cardLabel);
            card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
            card.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1))));
        }

        // appliquer initialement l'effet selon l'état de la carte
        if (cardData.endsWith("G")) {
            applyGrayEffect(card);
        } else {
            card.setEffect(createCardShadow());
        }

        card.setUserData(cardData);
        return card;
    }

    private void applyGrayEffect(StackPane card) {
        ColorAdjust grayscale = new ColorAdjust();
        grayscale.setSaturation(-1.0);
        grayscale.setBrightness(-0.3);
        card.setEffect(grayscale);
        card.setOpacity(0.7);
    }

    private void updateCardPositions(double availableWidth, int numberOfCards) {
        if (numberOfCards <= 1) return;
        
        double overlap = availableWidth / (numberOfCards - 1);
        for (int i = 0; i < numberOfCards; i++) {
            Node card = handStackPane.getChildren().get(i);
            StackPane.setMargin(card, new Insets(0, 0, 0, i * overlap));
        }
    }

    private void animateCardDealing() {
        ParallelTransition dealingAnimation = new ParallelTransition();
        
        for (int i = 0; i < handStackPane.getChildren().size(); i++) {
            Node card = handStackPane.getChildren().get(i);
            

            card.setTranslateY(-handStackPane.getHeight());
            card.setRotate(180);
            card.setOpacity(0);
            
            TranslateTransition translate = new TranslateTransition(Duration.millis(600), card);
            translate.setToY(0);
            translate.setDelay(Duration.millis(i * 100));
            
            RotateTransition rotate = new RotateTransition(Duration.millis(600), card);
            rotate.setToAngle(0);
            rotate.setDelay(Duration.millis(i * 100));
            
            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(i * 100));
            
            Timeline bounce = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(card.translateYProperty(), -handStackPane.getHeight())),
                new KeyFrame(Duration.millis(500 + i * 100), new KeyValue(card.translateYProperty(), 10, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000))),
                new KeyFrame(Duration.millis(600 + i * 100), new KeyValue(card.translateYProperty(), 0, Interpolator.SPLINE(0.215, 0.610, 0.355, 1.000)))
            );
            
            ParallelTransition cardAnimation = new ParallelTransition(card, translate, rotate, fade, bounce);
            dealingAnimation.getChildren().add(cardAnimation);
        }
        
        dealingAnimation.play();
    }

    private void updateHandStackPane(ListProperty<String> weatherCardsProperty) {
        // Si le nombre de cartes dans le StackPane ne correspond pas au nombre de cartes dans la liste
        // il faut recréer toutes les cartes pour éviter les erreurs
        if (handStackPane.getChildren().size() != weatherCardsProperty.size()) {
            createWeatherCards(weatherCardsProperty);
            return;
        }

        // modifier l'état des cartes (normal, grisée, levée)
        for (int i = 0; i < weatherCardsProperty.size(); i++) {
            StackPane card = (StackPane) handStackPane.getChildren().get(i);
            String cardValue = weatherCardsProperty.get(i);

            // si l'ordre des cartes à changé (tri), recréer les cartes
            if (card.getUserData() != null && !cardValue.substring(0, 3).equals(((String) card.getUserData()).substring(0, 3))) {
                createWeatherCards(weatherCardsProperty);
                return;
            }

            // détecter un changement d'état
            if (!cardValue.equals(card.getUserData())) {
                switch (cardValue.charAt(cardValue.length() - 1)) {
                    case 'N': // normal
                        card.translateYProperty().set(0);
                        card.setDisable(false);
                        card.setEffect(createCardShadow());
                        card.setOpacity(1.0);
                        break;
                    case 'G': // grisée
                        card.translateYProperty().set(0);
                        card.setDisable(true);
                        applyGrayEffect(card);
                        break;
                    case 'S': // levée (selectionnée)
                        card.translateYProperty().set(-60);
                        card.setEffect(createCardShadow());
                        card.setOpacity(1.0);
                        break;
                    default:
                        card.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                        break;
                }
                card.setUserData(cardValue);
            }
        }
    }

    // Méthode pour créer l'effet d'ombre standard
    private DropShadow createCardShadow() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0.4, 0.4, 0.4));
        return dropShadow;
    }

    // Méthode pour appliquer tous les effets visuels à une carte
    private void applyCardEffects(ImageView cardImage) {
        // effet d'ombre
        cardImage.setEffect(createCardShadow());
        
        // contour arrondi
        Rectangle clip = new Rectangle(
            cardImage.getFitWidth(),
            cardImage.getFitHeight()
        );
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        cardImage.setClip(clip);
        
        // ajuster le clip si la taille change
        cardImage.fitWidthProperty().addListener((obs, oldVal, newVal) -> {
            Rectangle clipRect = (Rectangle) cardImage.getClip();
            clipRect.setWidth(newVal.doubleValue());
        });
        cardImage.fitHeightProperty().addListener((obs, oldVal, newVal) -> {
            Rectangle clipRect = (Rectangle) cardImage.getClip();
            clipRect.setHeight(newVal.doubleValue());
        });
    }

    private void animateNewTideCard(ImageView newTideCard) {
        // on sauvegarde la position finale
        double finalX = newTideCard.getTranslateX();
        double finalY = newTideCard.getTranslateY();

        // position de départ de la carte marée (hors écran à droite)
        newTideCard.setTranslateX(tideStackPane.getWidth() + 200);
        newTideCard.setTranslateY(-50);
        newTideCard.setRotate(15);
        newTideCard.setScaleX(0.8);
        newTideCard.setScaleY(0.8);

        // les transitions
        TranslateTransition translate = new TranslateTransition(Duration.millis(800), newTideCard);
        translate.setToX(finalX);
        translate.setToY(finalY);

        RotateTransition rotate = new RotateTransition(Duration.millis(800), newTideCard);
        rotate.setToAngle(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(800), newTideCard);
        scale.setToX(1);
        scale.setToY(1);

        // combinaison des animations
        ParallelTransition parallelTransition = new ParallelTransition(translate, rotate, scale);
        parallelTransition.setInterpolator(Interpolator.EASE_OUT);
        parallelTransition.play();
    }

    private void animateBuoyValueChange(String newValue) {
        // transition de scale pour l'effet bounce
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), buoyValueLabel);
        scaleDown.setToX(0.7);
        scaleDown.setToY(0.7);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), buoyValueLabel);
        scaleUp.setToX(1.2);
        scaleUp.setToY(1.2);

        ScaleTransition scaleNormal = new ScaleTransition(Duration.millis(100), buoyValueLabel);
        scaleNormal.setToX(1.0);
        scaleNormal.setToY(1.0);

        // animation de couleur en utilisant un Timeline au lieu de FillTransition
        Timeline colorAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(
                buoyValueLabel.textFillProperty(), 
                Color.RED
            )),
            new KeyFrame(Duration.millis(400), new KeyValue(
                buoyValueLabel.textFillProperty(), 
                Color.BLACK
            ))
        );

        // séquencer les animations
        SequentialTransition sequentialTransition = new SequentialTransition(
            scaleDown,
            scaleUp,
            scaleNormal
        );

        // affichage des animations en parallèle
        ParallelTransition parallelTransition = new ParallelTransition(
            sequentialTransition,
            colorAnimation
        );

        parallelTransition.play();
    }

    public GameScreenViewModel getViewModel() {
        return viewModel;
    }
}

