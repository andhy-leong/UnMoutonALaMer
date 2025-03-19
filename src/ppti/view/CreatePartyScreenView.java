package ppti.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import ppti.model.CreatePartyScreenModel;
import ppti.viewmodel.CreatePartyScreenViewModel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import java.util.Arrays;
import common.reseau.udp.inforecup.PartieInfo;


public class CreatePartyScreenView extends MirrorBaseView {

    private CreatePartyScreenViewModel viewModel;
    private CreatePartyScreenModel model;
    private Stage stage;

    private GridPane upParametersPart;
    private GridPane downParametersPart;

    private String textStyle;
    private String buttonStyle;

    /*----------------------------Partie haute--------------------------------*/
    private Button upCreer;

    private Label upNomPartieLabel;
    private Label upNbJoueurLabel;
    private Label upNbJoueurReelLabel;
    private Label upVitesseDeJeuLabel;
    private Label upIsThereSpyLabel;

    private CheckBox upIsThereSpy;

    private Slider upVitesseDeJeu;

    private RadioButton upThreeRadio;
    private RadioButton upFourRadio;
    private RadioButton upFiveRadio;

    private TextField upNomPartieTextField;

    private final Node[] upNbJoueurReelRadio = new Node[6];
    private ToggleGroup upNbJoueurReel;


    /*----------------------------Partie basse--------------------------------*/
    private Button downCreer;

    private Label downNomPartieLabel;
    private Label downNbJoueurLabel;
    private Label downNbJoueurReelLabel;
    private Label downVitesseDeJeuLabel;
    private Label downIsThereSpyLabel;

    private CheckBox downIsThereSpy;

    private Slider downVitesseDeJeu;

    private RadioButton downThreeRadio;
    private RadioButton downFourRadio;
    private RadioButton downFiveRadio;
    private ToggleGroup downNbJoueur;

    private TextField downNomPartieTextField;

    private final Node[] downNbJoueurReelRadio = new Node[6];
    private ToggleGroup downNbJoueurReel;

    /*----------------------------Binding--------------------------------*/
    private IntegerProperty nbJoueurMax;
    private IntegerProperty nbJoueurReelMax;
    private ObjectProperty<PartieInfo> info;


    public CreatePartyScreenView(Stage stage, CreatePartyScreenViewModel viewModel) {
        super(stage, "Creer une partie", "back");
    	this.stage = stage;
    	this.viewModel = viewModel;
    	customizeScreen();
    	bindToViewModel();
    }
    

    @Override
    protected void customizeContent() {

        createUpPart();
        createDownPart();

        // activer le clavier visuel de JavaFX
        downNomPartieTextField.getProperties().put("vkType", 0);
        upNomPartieTextField.getProperties().put("vkType", 0);

        // activer le clavier virtuel de Windows
        /*
        // Afficher le clavier virtuel
        upNomPartieTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Afficher le clavier virtuel
                try {
                    Runtime.getRuntime().exec("cmd /c osk");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //else {
            //   // Fermer le clavier virtuel
            //   if (Config.DEBUG_MODE) {
            //       System.out.println("Hiding virtual keyboard");
            //   }
            //}
        });
         */

        addContent(upParametersPart, downParametersPart);


        // binding des boutons pour synchroniser les deux parties
        bindingButton();
    }

    private void createUpPart() {
        /* ----------------------Création du gride pane up ---------------------- */
        upParametersPart = new GridPane();

        // un gridpane de 6 lignes avec 2 colonnes, toutes les memes tailles
        for(int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            //row.setPercentHeight((double) 100 /6);
            row.setVgrow(Priority.ALWAYS);
            upParametersPart.getRowConstraints().add(row);
        }
        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setPercentWidth(50);
        ColumnConstraints colForm = new ColumnConstraints();
        colForm.setPercentWidth(50);

        // chaque élément de la colonne 0 est centré à droite et chaque élément de la colonne 1 est centré à gauche
        colLabel.setHalignment(HPos.RIGHT);
        colForm.setHalignment(HPos.LEFT);

        // espace de 20 entre les deux colonnes et de 5 entre les lignes
        upParametersPart.setHgap(20);
        upParametersPart.setVgap(5);

        upParametersPart.getColumnConstraints().addAll(colLabel, colForm);

        upParametersPart.getStyleClass().add("container");

        /*-------------------------------Nom Partie-------------------------------*/
        upNomPartieLabel = new Label();
        upNomPartieTextField = new TextField();
        upNomPartieTextField.setMaxWidth(250);

        /*------------------------Creation du groupe de RadioButton------------------------*/
        upThreeRadio = new RadioButton("3");
        upThreeRadio.setUserData(3);
        upFourRadio = new RadioButton("4");
        upFourRadio.setUserData(4);
        upFiveRadio = new RadioButton("5");
        upFiveRadio.setUserData(5);

        ToggleGroup upNbJoueur = new ToggleGroup();

        upThreeRadio.setSelected(true);
        upThreeRadio.setToggleGroup(upNbJoueur);
        upFourRadio.setToggleGroup(upNbJoueur);
        upFiveRadio.setToggleGroup(upNbJoueur);


        upNbJoueurLabel = new Label();

        HBox upNbJoueurConteneur = new HBox();
        upNbJoueurConteneur.setSpacing(10);
        upNbJoueurConteneur.setAlignment(Pos.CENTER_LEFT);
        upNbJoueurConteneur.getChildren().addAll(upThreeRadio, upFourRadio, upFiveRadio);

        /*-----------------------------Choix nombre joueur virtuel----------------------------*/
        upNbJoueurReelLabel = new Label();
        upNbJoueurReel = new ToggleGroup();
        for(int i = 0; i < upNbJoueurReelRadio.length; i++) {
            upNbJoueurReelRadio[i] = new RadioButton("" + i);
            upNbJoueurReelRadio[i].setUserData(i);
            ((RadioButton)upNbJoueurReelRadio[i]).setToggleGroup(upNbJoueurReel);
        }
        HBox upNbJoueurReelConteneur = new HBox();
        upNbJoueurReelConteneur.setSpacing(10);
        upNbJoueurReelConteneur.setAlignment(Pos.CENTER_LEFT);
        upNbJoueurReelConteneur.getChildren().addAll(Arrays.asList(upNbJoueurReelRadio));

        /*---------------------------------Vitesse de jeu----------------------------------*/
        upVitesseDeJeu = new Slider();
        upVitesseDeJeu.setMin(5);
        upVitesseDeJeu.setMax(100);
        upVitesseDeJeu.setValue(30);
        upVitesseDeJeu.setBlockIncrement(5);
        upVitesseDeJeu.setShowTickLabels(true);
        upVitesseDeJeu.setShowTickMarks(true);
        upVitesseDeJeu.setMajorTickUnit(20);
        upVitesseDeJeu.setMinorTickCount(1);
        upVitesseDeJeu.setSnapToTicks(true);

        upVitesseDeJeu.setMaxWidth(250);
        upVitesseDeJeuLabel = new Label();

        /*--------------------------------------Espion-------------------------------------------*/

        upIsThereSpy = new CheckBox();
        upIsThereSpyLabel = new Label();

        /*---------------------------------Boutton---------------------------------------*/
        upCreer = new Button();

        /* ----------------------Ajout des éléments dans le conteneur---------------------- */

        // regroupement des éléments de parametrage dans le gridPane
        // les label sur la colonne 0 et les champs correspondant sur la colonne 1
        upParametersPart.add(upNomPartieLabel, 0, 0);
        upParametersPart.add(upNomPartieTextField, 1, 0);
        upParametersPart.add(upNbJoueurLabel, 0, 1);
        upParametersPart.add(upNbJoueurConteneur, 1, 1);
        upParametersPart.add(upNbJoueurReelLabel, 0, 2);
        upParametersPart.add(upNbJoueurReelConteneur, 1, 2);
        upParametersPart.add(upVitesseDeJeuLabel, 0, 3);
        upParametersPart.add(upVitesseDeJeu, 1, 3);
        upParametersPart.add(upIsThereSpyLabel, 0, 4);
        upParametersPart.add(upIsThereSpy, 1, 4);
        upParametersPart.add(upCreer, 0, 5, 2, 1); // fusion des 2 colonnes pour le bouton creer

        // centrer le bouton creer et ajouter une marge en haut
        GridPane.setHalignment(upCreer, HPos.CENTER);
        GridPane.setMargin(upCreer, new Insets(10, 0, 0, 0));
    }

    private void createDownPart() {
        /* ----------------------Création du gride pane down ---------------------- */
        downParametersPart = new GridPane();

        // un gridpane de 6 lignes avec 2 colonnes, toutes les memes tailles
        for(int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            //row.setPercentHeight((double) 100 /6);
            row.setVgrow(Priority.ALWAYS);
            downParametersPart.getRowConstraints().add(row);
        }
        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setPercentWidth(50);
        ColumnConstraints colForm = new ColumnConstraints();
        colForm.setPercentWidth(50);

        // chaque élément de la colonne 0 est centré à droite et chaque élément de la colonne 1 est centré à gauche
        colLabel.setHalignment(HPos.RIGHT);
        colForm.setHalignment(HPos.LEFT);

        // espace de 20 entre les deux colonnes et de 5 entre les lignes
        downParametersPart.setHgap(20);
        downParametersPart.setVgap(5);

        downParametersPart.getColumnConstraints().addAll(colLabel, colForm);

        downParametersPart.getStyleClass().add("container");

        /*-------------------------------Nom Partie-------------------------------*/
        downNomPartieLabel = new Label();
        downNomPartieTextField = new TextField();
        downNomPartieTextField.setMaxWidth(250);

        /*------------------------Creation du groupe de RadioButton------------------------*/
        downThreeRadio = new RadioButton("3");
        downThreeRadio.setUserData(3);
        downFourRadio = new RadioButton("4");
        downFourRadio.setUserData(4);
        downFiveRadio = new RadioButton("5");
        downFiveRadio.setUserData(5);

        downNbJoueur = new ToggleGroup();

        downThreeRadio.setSelected(true);
        downThreeRadio.setToggleGroup(downNbJoueur);
        downFourRadio.setToggleGroup(downNbJoueur);
        downFiveRadio.setToggleGroup(downNbJoueur);


        downNbJoueurLabel = new Label();

        HBox downNbJoueurConteneur = new HBox();
        downNbJoueurConteneur.setSpacing(10);
        downNbJoueurConteneur.setAlignment(Pos.CENTER_LEFT);
        downNbJoueurConteneur.getChildren().addAll(downThreeRadio, downFourRadio, downFiveRadio);

        /*-----------------------------Choix joueur virtuel----------------------------*/
        downNbJoueurReelLabel = new Label();
        downNbJoueurReel = new ToggleGroup();
        for(int i = 0; i < downNbJoueurReelRadio.length; i++) {
            downNbJoueurReelRadio[i] = new RadioButton("" + i);
            downNbJoueurReelRadio[i].setUserData(i);
            if(i == 0)
                ((RadioButton)downNbJoueurReelRadio[i]).setSelected(true);
            ((RadioButton)downNbJoueurReelRadio[i]).setToggleGroup(downNbJoueurReel);
        }
        HBox downNbJoueurReelConteneur = new HBox();
        downNbJoueurReelConteneur.setSpacing(10);
        downNbJoueurReelConteneur.setAlignment(Pos.CENTER_LEFT);
        downNbJoueurReelConteneur.getChildren().addAll(Arrays.asList(downNbJoueurReelRadio));

        /*---------------------------------Vitesse de jeu----------------------------------*/
        downVitesseDeJeu = new Slider();
        downVitesseDeJeu.setMaxWidth(250);
        downVitesseDeJeuLabel = new Label();

        /*--------------------------------------Espion-------------------------------------------*/

        downIsThereSpy = new CheckBox();
        downIsThereSpyLabel = new Label();

        /*---------------------------------Boutton---------------------------------------*/
        downCreer = new Button();

        /* ----------------------Ajout des éléments dans le conteneur---------------------- */

        // regroupement des éléments de parametrage dans le gridPane
        // les label sur la colonne 0 et les champs correspondant sur la colonne 1
        downParametersPart.add(downNomPartieLabel, 0, 0);
        downParametersPart.add(downNomPartieTextField, 1, 0);
        downParametersPart.add(downNbJoueurLabel, 0, 1);
        downParametersPart.add(downNbJoueurConteneur, 1, 1);
        downParametersPart.add(downNbJoueurReelLabel, 0, 2);
        downParametersPart.add(downNbJoueurReelConteneur, 1, 2);
        downParametersPart.add(downVitesseDeJeuLabel, 0, 3);
        downParametersPart.add(downVitesseDeJeu, 1, 3);
        downParametersPart.add(downIsThereSpyLabel, 0, 4);
        downParametersPart.add(downIsThereSpy, 1, 4);
        downParametersPart.add(downCreer, 0, 5, 2, 1); // fusion des 2 colonnes pour le bouton creer

        // centrer le bouton creer
        GridPane.setHalignment(downCreer, HPos.CENTER);
        GridPane.setMargin(downCreer, new Insets(10, 0, 0, 0));
    }

    public void bindingButton() {
        // -----------------------
        // à passer dans ViewModel
        // -----------------------

        info = new SimpleObjectProperty<>();

        nbJoueurMax = new SimpleIntegerProperty((int)downNbJoueur.getSelectedToggle().getUserData());
        nbJoueurReelMax = new SimpleIntegerProperty((int)downNbJoueurReel.getSelectedToggle().getUserData());

        upThreeRadio.selectedProperty().bindBidirectional(downThreeRadio.selectedProperty());
        upFourRadio.selectedProperty().bindBidirectional(downFourRadio.selectedProperty());
        upFiveRadio.selectedProperty().bindBidirectional(downFiveRadio.selectedProperty());

        downCreer.disableProperty().bindBidirectional(upCreer.disableProperty());

        upVitesseDeJeu.valueProperty().bindBidirectional(downVitesseDeJeu.valueProperty());
        downVitesseDeJeu.setValue(35);
        downVitesseDeJeu.minProperty().bind(upVitesseDeJeu.minProperty());
        downVitesseDeJeu.maxProperty().bind(upVitesseDeJeu.maxProperty());
        downVitesseDeJeu.showTickLabelsProperty().bind(upVitesseDeJeu.showTickLabelsProperty());
        downVitesseDeJeu.showTickMarksProperty().bind(upVitesseDeJeu.showTickMarksProperty());
        downVitesseDeJeu.blockIncrementProperty().bind(upVitesseDeJeu.blockIncrementProperty());
        downVitesseDeJeu.majorTickUnitProperty().bind(upVitesseDeJeu.majorTickUnitProperty());
        downVitesseDeJeu.minorTickCountProperty().bind(upVitesseDeJeu.minorTickCountProperty());
        downVitesseDeJeu.snapToTicksProperty().bind(upVitesseDeJeu.snapToTicksProperty());
        
        upIsThereSpy.selectedProperty().bindBidirectional(downIsThereSpy.selectedProperty());

        //on bind les radios boutons de joueur reel du haut en bas entre eux
        for(int i = 0; i < upNbJoueurReelRadio.length;i++) {
            ((RadioButton)upNbJoueurReelRadio[i]).selectedProperty().bindBidirectional(((RadioButton)downNbJoueurReelRadio[i]).selectedProperty());
        }

        downNomPartieTextField.textProperty().bindBidirectional(upNomPartieTextField.textProperty());

        upNbJoueurReelRadio[4].disableProperty().bind((upFourRadio.selectedProperty().not().and(upFiveRadio.selectedProperty().not())));
        upNbJoueurReelRadio[5].disableProperty().bind(upFiveRadio.selectedProperty().not());

        downNbJoueurReelRadio[4].disableProperty().bind(upNbJoueurReelRadio[4].disableProperty());
        downNbJoueurReelRadio[5].disableProperty().bind(upNbJoueurReelRadio[5].disableProperty());

        downNomPartieTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.length() > 20) {
                downNomPartieTextField.setText(newValue.substring(0, 20));
            }
        });

        downNbJoueur.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
            	nbJoueurMax.set((int)downNbJoueur.getSelectedToggle().getUserData());
                if((int)newValue.getUserData() < (int)downNbJoueurReel.getSelectedToggle().getUserData()  ) {
                    int val = (int)((RadioButton)newValue).getUserData();
                    ObservableList<Toggle> rb = downNbJoueurReel.getToggles();
                    for(int i = rb.size()-1;i>0;i--) {
                        if((int)rb.get(i).getUserData() <= val) {
                            (downNbJoueurReel.getToggles().get(i)).setSelected(true);
                            break;
                        }
                    }
                }
                
                nbJoueurReelMax.set((int)downNbJoueur.getSelectedToggle().getUserData());
                nbJoueurReelMax.set((int)downNbJoueurReel.getSelectedToggle().getUserData());
            }
        });

        downNbJoueurReel.selectedToggleProperty().addListener((observable,oldValue,newValue) -> {
            if(newValue != null) {
            	nbJoueurReelMax.set((int)downNbJoueurReel.getSelectedToggle().getUserData());
            }
        });
    }

    @Override
    protected void bindToViewModel() {

        //avant de bind toutes les valeurs ont est obligé de les sets car elles ne se mettent à jour que si quelque chose est modifié
    	viewModel.isThereSpyProperty().setValue(upIsThereSpy.isSelected() ? 1 : 0);
        viewModel.vitesseJeuProperty().setValue(downVitesseDeJeu.getValue());
        viewModel.nameProperty().setValue(downNomPartieTextField.getText());
        //viewModel.nbJoueurMaxProperty().setValue((Integer)downNbJoueur.getSelectedToggle().getUserData());
        viewModel.nbJoueurReelMaxProperty().setValue((Integer)downNbJoueurReel.getSelectedToggle().getUserData());


        //en suite on les binds
        viewModel.isThereSpyProperty().bind(
        	    Bindings.when(upIsThereSpy.selectedProperty()).then(1).otherwise(0)
        	);
        viewModel.vitesseJeuProperty().bind(downVitesseDeJeu.valueProperty());
        viewModel.nameProperty().bind(downNomPartieTextField.textProperty());
        viewModel.nbJoueurMaxProperty().bind(nbJoueurMax);
        viewModel.nbJoueurReelMaxProperty().bind(nbJoueurReelMax);

        downCreer.setOnAction(event -> navigateToBotLevelScreen());
        upCreer.setOnAction(event -> navigateToBotLevelScreen());

        Button[] backButtons = super.getBackButtons();
        for(Button backButton : backButtons) {
            backButton.setOnAction(event -> navigateToPreviousScreen());
        }

        /* internationalisation */
        Label[] mirrorTitles = super.getTitleLabels();
        for (Label label : mirrorTitles) {
            label.textProperty().bind(viewModel.getTitleProperty());
        }
        upNomPartieLabel.textProperty().bind(viewModel.getNomPartieNameProperty());
        upNbJoueurLabel.textProperty().bind(viewModel.getNombreJoueurLabelProperty());
        upNbJoueurReelLabel.textProperty().bind(viewModel.getNombreJoueurReelLabelProperty());
        upVitesseDeJeuLabel.textProperty().bind(viewModel.getVitesseJeuLabelProperty());
        upIsThereSpyLabel.textProperty().bind(viewModel.getIsThereSpyLabelProperty());
        upIsThereSpy.textProperty().bind(viewModel.getIsThereSpyCheckBoxProperty());
        upCreer.textProperty().bind(viewModel.getCreatePartyButtonProperty());
        downNomPartieLabel.textProperty().bind(viewModel.getNomPartieNameProperty());
        downNbJoueurLabel.textProperty().bind(viewModel.getNombreJoueurLabelProperty());
        downNbJoueurReelLabel.textProperty().bind(viewModel.getNombreJoueurReelLabelProperty());
        downVitesseDeJeuLabel.textProperty().bind(viewModel.getVitesseJeuLabelProperty());
        downIsThereSpyLabel.textProperty().bind(viewModel.getIsThereSpyLabelProperty());
        downIsThereSpy.textProperty().bind(viewModel.getIsThereSpyCheckBoxProperty());
        downCreer.textProperty().bind(viewModel.getCreatePartyButtonProperty());
    }

    private void navigateToBotLevelScreen() {
        try {
            viewModel.navigateToBotLevelScreen();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void navigateToPreviousScreen() {
        viewModel.navigateToPreviousScreen();
    }

    public PartieInfo getPartieInfo() {
    	return model.getPartieInfo();
    }


	@Override
	public void setViewModel(Object viewModel) {
		this.viewModel = (CreatePartyScreenViewModel) viewModel;
	}
}
