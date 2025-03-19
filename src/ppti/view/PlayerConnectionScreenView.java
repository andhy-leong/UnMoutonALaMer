package ppti.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import common.Config;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ppti.model.JoueurInfo;
import ppti.view.singleton.SingletonView;
import ppti.viewmodel.PlayerConnectionScreenViewModel;

/**
 * Classe EcranAccepterJoueur représentant la vue de l'écran d'acceptation ou du
 * refus de connexion d'un joueur. Cette classe utilise JavaFX pour créer
 * l'interface utilisateur et gère l'ajout de joueurs à la liste d'attente de connexion
 * et l'acceptation ou le refus de leur demande.
 */
public class PlayerConnectionScreenView extends MirrorBaseView implements MapChangeListener<JoueurInfo,BlockingQueue<String>>{
    private PlayerConnectionScreenViewModel viewModel;

    private Stage stage;

    private VBox bottomVBoxJoueurs;
    private VBox topInvertedVBoxJoueurs;

    //private VBox bottomConteneurJoueurs;
    //private VBox topINvertedConteneurJoueurs;

    //private Button upContinuer;
    //private Button downContinuer;

    private boolean trigger = true;
    private ObjectProperty<PartieInfo> info;
    private ObjectProperty<ToggleGroup> radioPosition;
    private ArrayList<ToggleGroup> togglesGroups = new ArrayList<>();
    
    public PlayerConnectionScreenView(Stage stage, PlayerConnectionScreenViewModel viewModel) throws Exception{
        super(stage, "Connexion","confirm");
        this.stage = stage;

    	this.viewModel = viewModel;

        // ajouter un listener sur la map des joueurs
        viewModel.getChildQueue().addListener(this);
        
        customizeScreen();
        bindToViewModel();
    }

    @Override
    protected void customizeContent() {

        //upContinuer = new Button("Continuer");
        //downContinuer = new Button("Continuer");

        //upContinuer.setStyle(buttonStyle);
        //downContinuer.setStyle(buttonStyle);


        /* création des conteneurs principaux (contient la liste des joueurs) */
        bottomVBoxJoueurs = new VBox();
        topInvertedVBoxJoueurs = new VBox();

        /* Création des hBox connexion */
        HBox connexionBas = creerHBoxConnexion();
        HBox connexionHaut = creerHBoxConnexion();

        /* Création de la liste bas */
        HBox.setHgrow(bottomVBoxJoueurs, Priority.ALWAYS);
        bottomVBoxJoueurs.getChildren().addAll(connexionBas);
        bottomVBoxJoueurs.setAlignment(Pos.TOP_CENTER);
        bottomVBoxJoueurs.setSpacing(10);

        /* Création de la liste haut */
        HBox.setHgrow(topInvertedVBoxJoueurs, Priority.ALWAYS);
        VBox.setVgrow(topInvertedVBoxJoueurs, Priority.NEVER);
        topInvertedVBoxJoueurs.getChildren().addAll(connexionHaut);
        topInvertedVBoxJoueurs.setAlignment(Pos.TOP_CENTER);
        topInvertedVBoxJoueurs.setSpacing(10);

        //bottomConteneurJoueurs = new VBox();
        //topINvertedConteneurJoueurs = new VBox();

        //bottomConteneurJoueurs.setAlignment(Pos.CENTER);
        //topINvertedConteneurJoueurs.setAlignment(Pos.CENTER);

        //bottomConteneurJoueurs.setSpacing(10);
        //topINvertedConteneurJoueurs.setSpacing(10);

        //bottomConteneurJoueurs.getChildren().addAll(bottomVBoxJoueurs);
        //topINvertedConteneurJoueurs.getChildren().addAll(topInvertedVBoxJoueurs);

        addContent(bottomVBoxJoueurs, topInvertedVBoxJoueurs);
    }

    private HBox createEmptyHBox() {
        HBox emptyBox = new HBox();
        emptyBox.setPrefHeight(47);
        emptyBox.setMaxHeight(47);
        emptyBox.getStyleClass().add("empty-box");
        VBox.setVgrow(emptyBox, Priority.ALWAYS);

        return emptyBox;
    }

    /**
     * Crée une HBox représentant une connexion active avec des informations
     * visuelles.
     *
     * @return Une HBox contenant le label "Connexion(s)".
     */
    private HBox creerHBoxConnexion() {
        Label labelConnexion = new Label("Connexion(s)");

        HBox hBoxConnexion = new HBox();
        hBoxConnexion.setPrefHeight(25);
        hBoxConnexion.setMaxHeight(25);
        VBox.setVgrow(hBoxConnexion, Priority.ALWAYS);
        hBoxConnexion.getChildren().addAll(labelConnexion);
        hBoxConnexion.setAlignment(Pos.CENTER);
        hBoxConnexion.getStyleClass().add("empty-box");

        return hBoxConnexion;
    }

    /**
     * Crée une HBox représentant un joueur en attente de validation (accepter/refuser).
     *
     * @param pseudoJoueur Le nom du joueur.
     * @return Une HBox contenant le pseudo du joueur et les boutons accepter/refuser.
     */
    private HBox createHBoxPlayerWaiting(String pseudoJoueur,String IdJoueur, VBox vBoxParent) {
        Label labelPseudo = new Label(pseudoJoueur);

        Button boutonAccepter = new Button();
        Button boutonRefuser = new Button();
        boutonAccepter.textProperty().bind(viewModel.getAcceptButtonProperty());
        boutonRefuser.textProperty().bind(viewModel.getRejectButtonProperty());

        boutonAccepter.setAlignment(Pos.CENTER);
        boutonRefuser.setAlignment(Pos.CENTER);

        HBox hBoxJoueur = new HBox(10);
        hBoxJoueur.setUserData(IdJoueur);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hBoxJoueur.setPrefHeight(47);
        hBoxJoueur.setMaxHeight(47);
        VBox.setVgrow(hBoxJoueur, Priority.ALWAYS);

        hBoxJoueur.getChildren().addAll(labelPseudo, spacer, boutonAccepter, boutonRefuser);
        HBox.setMargin(labelPseudo, new Insets(0, 0, 0, 10));
        //HBox.setMargin(boutonRefuser, new Insets(0, 10, 0, 0));

        hBoxJoueur.setAlignment(Pos.CENTER);

        hBoxJoueur.getStyleClass().add("connexion-box");

        // Action pour le bouton "Accepter"
        boutonAccepter.setOnAction(e -> {
            // Remplacer la HBox actuelle par une HBox joueur accepté
            HBox hBoxAccepte = createHBoxAcceptedPlayer(pseudoJoueur);
            HBox hBoxAccepte2 = createHBoxAcceptedPlayer(pseudoJoueur);

            int size = togglesGroups.size();
            ToggleGroup top = togglesGroups.get(size -1);
            ToggleGroup bottom = togglesGroups.get(size - 2);


            for(int i = 0;i < top.getToggles().size();i++) {
                top.getToggles().get(i).selectedProperty().bindBidirectional(bottom.getToggles().get(i).selectedProperty());
            }


            // Remplacer dans la VBox parent (qui contient la liste des joueurs)
            int index = vBoxParent.getChildren().indexOf(hBoxJoueur);

            topInvertedVBoxJoueurs.getChildren().set(index, hBoxAccepte);
            bottomVBoxJoueurs.getChildren().set(index, hBoxAccepte2);
            viewModel.accepterJoueur(IdJoueur);
        });

        boutonRefuser.setOnAction(event -> {
        	viewModel.rejeterJoueur(IdJoueur);
        });


        // chercher la première case vide disponible
        for (int i = 0; i < vBoxParent.getChildren().size(); i++) {
            if (vBoxParent.getChildren().get(i) instanceof HBox) {
                HBox hBox = (HBox) vBoxParent.getChildren().get(i);
                if (hBox.getChildren().isEmpty()) {
                    // changer la HBox vide par la HBox joueur
                    vBoxParent.getChildren().set(i, hBoxJoueur);
                    break;
                }
            }
        }

        return hBoxJoueur;
    }

    /**
     * Crée une HBox représentant un joueur déjà accepté.
     *
     * @param pseudoJoueur Le nom du joueur.
     * @return Une HBox contenant le pseudo du joueur et une étiquette "Accepte".
     */
    private HBox createHBoxAcceptedPlayer(String pseudoJoueur) {
        Label labelPseudo = new Label(pseudoJoueur);

        Label labelAccepte = new Label();
        labelAccepte.textProperty().bind(viewModel.getAcceptedLabelProperty());

        HBox hBoxJoueur = new HBox(10);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hBoxJoueur.setPrefHeight(47);
        hBoxJoueur.setMaxHeight(47);
        VBox.setVgrow(hBoxJoueur, Priority.ALWAYS);

        HBox hboxRadio = CreateHBoxRadio(pseudoJoueur);

        hBoxJoueur.getChildren().addAll(labelPseudo, spacer, labelAccepte, hboxRadio);
        HBox.setMargin(labelPseudo, new Insets(0, 0, 0, 10));
        HBox.setMargin(labelAccepte, new Insets(0, 10, 0, 0));

        hBoxJoueur.setAlignment(Pos.CENTER);

        hBoxJoueur.getStyleClass().add("accepted-box");

        return hBoxJoueur;
    }

    private HBox CreateHBoxRadio(String pseudoJoueur) {
        HBox conteneur = new HBox();
        conteneur.setAlignment(Pos.CENTER);
        ToggleGroup radioPos = new ToggleGroup();
        Label position = new Label();
        position.textProperty().bind(viewModel.getPositionLabelProperty());
        conteneur.getChildren().add(position);

        for (int i = 0; i < info.get().getNombreJoueurMax(); i++) {
            RadioButton radio = new RadioButton((i + 1) + "");
            radio.setUserData(pseudoJoueur);
            radio.setToggleGroup(radioPos);
            conteneur.getChildren().add(radio);
        }

        this.radioPosition.set(radioPos);
        this.togglesGroups.add(radioPos);

        return conteneur;
    }

    /**
     * Crée une HBox représentant un bot avec un bouton de création.
     *
     * @param nomBot Le nom du bot.
     * @return Une HBox contenant le nom du bot et un bouton de création.
     */
    private HBox CreateHBoxBot(String nomBot, VBox vBoxParent) {
        Label labelNomBot = new Label(nomBot);

        Button boutonCreer = new Button("Créer");
        boutonCreer.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox hBoxBot = new HBox(10);
        hBoxBot.setPrefHeight(47);
        hBoxBot.setMaxHeight(47);
        VBox.setVgrow(hBoxBot, Priority.ALWAYS);
        hBoxBot.getChildren().addAll(labelNomBot, spacer, boutonCreer);
        HBox.setMargin(labelNomBot, new Insets(0, 0, 0, 10));
        HBox.setMargin(boutonCreer, new Insets(0, 10, 0, 0));

        hBoxBot.setAlignment(Pos.CENTER);

    	// Action pour le bouton "Accepter"
        boutonCreer.setOnAction(e -> {
            // Remplacer la HBox actuelle par une HBox joueur accepté
            HBox hBoxAccepte = createHBoxAcceptedPlayer(nomBot);

            // Remplacer dans la VBox parent (qui contient la liste des joueurs)
            int index = vBoxParent.getChildren().indexOf(hBoxBot);
            vBoxParent.getChildren().set(index, hBoxAccepte); // Remplacer l'ancienne HBox par la nouvelle
        });
        return hBoxBot;
    }


    public void removePlayer(VBox parent, String nomJoueur,String IdJoueur) {
    	HBox joueur = null;
    	for(Node n : parent.getChildren()) {
    		if(n instanceof HBox) {
                /*
    			for(Node label : ((HBox)n).getChildren()) {
    				if(label instanceof Label) {
    					System.out.println(((Label)label).getText());
    					if(((Label)label).getText().equals(nomJoueur)) {
    						joueur = (HBox)n;
    					}
    				}
        		}*/
                if(n.getUserData() != null) {
                    if(n.getUserData().equals(IdJoueur)) {
                        joueur = (HBox)n;
                    }
                }
    		}
    	}

    	parent.getChildren().remove(joueur);
        parent.getChildren().add(createEmptyHBox());
    }


	@Override
    public void onChanged(Change<? extends JoueurInfo, ? extends BlockingQueue<String>> change) {
        if (trigger) {
            if (change.wasAdded()) {
                if (Config.DEBUG_MODE) {
                    System.out.println("Un ajout a ete fait");
                }
                Platform.runLater(() -> {
                    HBox hBoxPlayer = createHBoxPlayerWaiting(change.getKey().getNom(), change.getKey().getIdp(), this.bottomVBoxJoueurs);
                    HBox hBoxPlayerInverted = createHBoxPlayerWaiting(change.getKey().getNom(), change.getKey().getIdp(), this.topInvertedVBoxJoueurs);
                });
            } else if (change.wasRemoved()) {
                if (Config.DEBUG_MODE) {
                    System.out.println("Une suppression a été faites");
                }
                Platform.runLater(() -> {
                    this.removePlayer(this.bottomVBoxJoueurs, change.getKey().getNom(), change.getKey().getIdp());
                    this.removePlayer(this.topInvertedVBoxJoueurs, change.getKey().getNom(), change.getKey().getIdp());
                });
            }
            trigger = false;
        } else {
            trigger = true;
        }
    }

    @Override
    protected void bindToViewModel() {
        radioPosition = new SimpleObjectProperty<>();
        // permet de creer le nombre juste de box pour la connexion
        info = new SimpleObjectProperty<>();
        info.bind(SingletonView.getInfo());
        info.addListener((observable,oldvalue,newValue) -> {
            bottomVBoxJoueurs.getChildren().clear();
            topInvertedVBoxJoueurs.getChildren().clear();
            // Ajouter des HBox vides par défaut
            int nombreJoueurMax = newValue.getNombreJoueurMax();
            if (Config.DEBUG_MODE) {
                System.out.println("Nombre de joueur max : " + nombreJoueurMax);
            }
            for (int i = 0; i < nombreJoueurMax; i++) {
                bottomVBoxJoueurs.getChildren().add(createEmptyHBox());
                topInvertedVBoxJoueurs.getChildren().add(createEmptyHBox());
            }
        });

        viewModel.bindPosition(radioPosition);

        confirmButtonDown.setOnAction(event ->viewModel.navigateToColorSelectionScreen());
        confirmButtonUp.setOnAction(event -> viewModel.navigateToColorSelectionScreen());

        viewModel.getChildQueue().addListener(this);
        Button[] confirmButtons = super.getConfirmButtons();

        if(confirmButtons != null) {
            for(Button button : confirmButtons) {
                button.setOnAction(event -> viewModel.navigateToColorSelectionScreen());
            }
        }

        /* Internationalisation */
        // Title
        Label[] mirrorTitles = super.getTitleLabels();
        for (Label label : mirrorTitles) {
            label.textProperty().bind(viewModel.getTitleProperty());
        }
        // bouton continuer
        Button[] confirmButtonsMirror = super.getConfirmButtons();
        for (Button button : confirmButtonsMirror) {
            button.textProperty().bind(viewModel.getConfirmButtonProperty());
        }
    }

	@Override
	public void setViewModel(Object viewModel) {
		// TODO Auto-generated method stub
		
	}


}
