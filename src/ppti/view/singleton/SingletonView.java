package ppti.view.singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;


import common.ThemeManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import common.reseau.udp.inforecup.PartieInfo;

import ppti.model.*;

import ppti.view.*;

import ppti.viewmodel.*;

public class SingletonView extends StackPane{
	private static volatile SingletonView instance;
	
	private PartieInfo partieInfo;
    // Ensemble des vues
	private List<BaseView> viewList;
	// Ensemble des models
	private List<Object> viewModelList;

	// Objet permettant au vue en ayant besoin de se mettre à jour en conséquence de la modification de la partie info
	private static ObjectProperty<PartieInfo> info = new SimpleObjectProperty<>();
	// Object permettant au vue en ayant besoin de se mettre à jour par rapport au joueur qui se connecte
	private static ObjectProperty<Map<JoueurInfo, BlockingQueue<String>>> joueurConnecte = new SimpleObjectProperty<>();
	// Object permettant à l'application de récupérer les messages d'idjr envoyé au client handler vers le ppti
	private static ObjectProperty<ParentQueueTreatment> parentQueue = new SimpleObjectProperty<>();
	// Object permettant de savoir l'ordre des joueurs
	private static ObjectProperty<ArrayList<JoueurInfo>> joueurOrdre = new SimpleObjectProperty<>();
	// Object permettant de savoir l'emplacement des joueurs
	private static ObjectProperty<ArrayList<JoueurInfo>> joueurPlace = new SimpleObjectProperty<>();
	// Object permettant d'avoir les espions
	private static ObjectProperty<EspionQueue> espionQueue = new SimpleObjectProperty<>();
	//permet de savoir si le réseau est lancé pour pouvoir l'arrêter si besoin
	private BooleanProperty networkLaunch;
	// Ici on créer toutes les vues, puis tous les models,
	// puis on ajoute les models aux vues
    public SingletonView(Stage stage) throws Exception {
		networkLaunch = new SimpleBooleanProperty(false);
		// Creation des liste
		viewModelList = new ArrayList<>();
		viewList = new ArrayList<>();

		// Creation des viewModels
		viewModelList.add(new StartingScreenViewModel(this)); // 0
		
		CreatePartyScreenModel CreatePartyScreenModel = new CreatePartyScreenModel();
		viewModelList.add(new CreatePartyScreenViewModel(new CreatePartyScreenModel(), this)); // 1
		CreatePartyScreenModel.creerInfoPartie();
		this.partieInfo = CreatePartyScreenModel.getPartieInfo();
		
		viewModelList.add(new BotLevelScreenViewModel(new BotLevelScreenModel(partieInfo), this)); // 2

		viewModelList.add(new PlayerConnectionScreenViewModel(new PlayerConnectionModel(partieInfo,this), this)); // 3
		((PlayerConnectionScreenViewModel)viewModelList.get(3)).bindToInfo(info);
		networkLaunch.bindBidirectional(((PlayerConnectionScreenViewModel)viewModelList.get(3)).networkStartProperty());

		viewModelList.add(new ChooseColorPositionViewModel(this)); // 4
		
		// Creation des popups viewModels
		
		viewModelList.add(new OptionsScreenViewModel(this)); // 5
		viewModelList.add(new HelpScreenViewModel(new HelpScreenModel(), this)); // 6
		viewModelList.add(null); // 7 TODO: A supprimer, anciennement écran de fin de manche
		viewModelList.add(null); // 8 TODO: A supprimer, anciennement écran de fin de partie
		viewModelList.add(new PauseScreenViewModel(new PauseScreenModel(), this)); // 9
		viewModelList.add(new GameScreenViewModel(this)); // 10
		
		// Creation des views
		viewList.add(new StartingScreenView(stage, (StartingScreenViewModel) viewModelList.get(0))); // 0
		
		viewList.add(new CreatePartyScreenView(stage, (CreatePartyScreenViewModel) viewModelList.get(1))); // 1
				
		viewList.add(new BotLevelScreenView(stage, (BotLevelScreenViewModel) viewModelList.get(2))); // 2

		viewList.add(new PlayerConnectionScreenView(stage, (PlayerConnectionScreenViewModel) viewModelList.get(3)));

		viewList.add(null); // 4 TODO: A supprimer, anciennement écran de choix de couleur
		viewList.add(new EcranPositionnementView(stage, (ChooseColorPositionViewModel) viewModelList.get(4))); // 5
		viewList.add(null); // 6 TODO: A supprimer, anciennement écran de validation

		//Creation des popups views

		viewList.add(new OptionsScreenView((OptionsScreenViewModel) viewModelList.get(5), stage, false)); // 7
		viewList.add(new HelpScreenView((HelpScreenViewModel) viewModelList.get(6), stage, false)); // 8
		viewList.add(new EndRoundScreenView((GameScreenViewModel) viewModelList.get(10), stage)); // 9
		viewList.add(new EndRoundScreenView((GameScreenViewModel) viewModelList.get(10), stage)); // 10
		viewList.add(new PauseScreenView((PauseScreenViewModel) viewModelList.get(9), stage)); // 11

		//Ecran de jeu
		viewList.add(new GameScreenView(stage, (GameScreenViewModel) viewModelList.get(10))); // 12

		//on ajoute le viewModel des vues de directement ici pour qu'il puisse être bind avec le reste des vues
		/*
		viewList.get(4).setViewModel(viewModelList.get(4));
		viewList.get(5).setViewModel(viewModelList.get(4));
		viewList.get(6).setViewModel(viewModelList.get(4));
		*/
		
		setViewVisible(0);

		for (BaseView view : viewList) {
			if (view != null) {
				this.getChildren().add(view);
			}
		}
	}


	public static SingletonView getInstance(Stage stage) throws Exception {
		if(instance == null) {
			instance = new SingletonView(stage);
		}
		return instance;
	}
	

	// Affiche la vue mise en argument
	public void setViewVisible(Integer viewToDisplay) {
		for(int i = 0; i < viewList.size(); i++) {
			if(i == viewToDisplay && viewList.get(viewToDisplay) != null) {
				viewList.get(viewToDisplay).setVisible(true);
				viewList.get(i).toFront();
			} else {
				// TODO: trouver une solution pour garder les popups supperposés sans le bug de GameScreenView
				if (!(viewList.get(viewToDisplay) instanceof PopUpBaseView)) {
					if(viewList.get(i) != null)
						viewList.get(i).setVisible(false);
				}
			}
		}

		// on regarde pour arrêter le réseau si besoin
		if(networkLaunch.get() && (viewToDisplay == 1)){
			((PlayerConnectionScreenViewModel)this.viewModelList.get(3)).cancel();
			((PlayerConnectionScreenViewModel)this.viewModelList.get(3)).suspend(); 
		}
	}

	public void updatePopUpScreenOrientation(boolean isScreenRotated) {
        for (BaseView view : viewList) {
            if (view instanceof PopUpBaseView) {
                ((PopUpBaseView) view).updateScreenOrientation(isScreenRotated);
            }
        }
    }

	public void applyThemeToAllViews(int themeNumber, boolean isDarkMode) {
		ThemeManager.setTheme(this.getScene(), themeNumber, isDarkMode);
    }

	public void reinit() {
		((PlayerConnectionScreenViewModel)viewModelList.get(3)).deconnexion();
		this.setViewVisible(0);
	}


	// methodes permettant de récupérer les valeurs dont on a besoin tout au long du déroulement du programme
	public static ObjectProperty<PartieInfo> getInfo() {return info;}
	public static void setInfo(PartieInfo partie) {info.set(partie);}
	public static ObjectProperty<Map<JoueurInfo, BlockingQueue<String>>> getJoueur() {return joueurConnecte;}
	public static void setJoueur(Map<JoueurInfo, BlockingQueue<String>> joueur) {
		joueurConnecte.set(joueur);
	}
	public static void setJoueurOrdre(ArrayList<JoueurInfo> ordre) {joueurOrdre.set(ordre);}
	public static ObjectProperty<ArrayList<JoueurInfo>> getJoueurOrdre() {return joueurOrdre;}
	public static void setJoueurPlace(ArrayList<JoueurInfo> place) {joueurPlace.set(place);}
	public static ObjectProperty<ArrayList<JoueurInfo>> getJoueurPlace() {return joueurPlace;}
	public static List<BaseView> getViewList() {return instance.viewList;}
	public static void setParentQueue(ParentQueueTreatment parent) {parentQueue.set(parent);}
	public static ObjectProperty<ParentQueueTreatment> getParentQueue() {return parentQueue;}
	public static ObjectProperty<EspionQueue> getEspionQueue() {return espionQueue;}
	public static void setEspionQueue(EspionQueue queue) {espionQueue.set(queue);}
	// pas sûr de ce que c'est
	public static List<Object> getViewModelList() {return instance.viewModelList;}

	public void quitGame() {
		System.exit(0);
	}
}
