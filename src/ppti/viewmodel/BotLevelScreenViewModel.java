package ppti.viewmodel;

import common.Config;
import common.enumtype.GameType;
import common.enumtype.PlayerType;
import common.locales.I18N;
import common.reseau.udp.inforecup.PartieInfo;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import ppti.model.BotLevelScreenModel;
import ppti.view.singleton.SingletonView;

import java.io.File;
import java.util.ArrayList;


public class BotLevelScreenViewModel {

	private ObjectProperty<PartieInfo> partieInfo;
	private BotLevelScreenModel model;
	private PartieInfo info;
	private SingletonView singleton;

	private ArrayList<ToggleGroup> toggles;
	private ArrayList<TextField> BotName;
	private ArrayList<Process> botProcesses;
	private ArrayList<BooleanProperty> launch;

	private StringBinding createPartieButtonProperty;
	private StringBinding titleLabelProperty;
	private StringBinding botPreLabelProperty;
	private StringBinding fBotLevelButtonProperty;
	private StringBinding mBotLevelButtonProperty;
	private StringBinding eBotLevelButtonProperty;
	private StringBinding isLaunchOnPPTILabelProperty;


	public BotLevelScreenViewModel(BotLevelScreenModel model, SingletonView singleton) {
		this.model = model;
		this.singleton = singleton;
		this.info = model.getPartieInfo();
		this.botProcesses = new ArrayList<>();

		this.partieInfo = new SimpleObjectProperty<>();
		this.partieInfo.bind(SingletonView.getInfo());
		partieInfo.addListener((observable, oldValue, newValue) -> {
			if (Config.DEBUG_MODE) {
				System.out.println("BotLevelViewModel : Essaye de modification");
			}

			if(newValue.getNombreJoueurMax() == newValue.getNombreJoueurReelMax())
				newValue.setGameType(GameType.JR);
			else if(newValue.getNombreJoueurVirtuelMax() == newValue.getNombreJoueurMax())
				newValue.setGameType(GameType.BOT);
			else
				newValue.setGameType(GameType.MIX);

			this.info = newValue;
			this.model.setPartieInfo(info);
		});

		toggles = new ArrayList<>();
		BotName = new ArrayList<>();
		launch = new ArrayList<>();

		/* Internationalization */
		createPartieButtonProperty = I18N.createStringBinding("createPartyButtonBotLevelScreenPPTI");
		titleLabelProperty = I18N.createStringBinding("titleLabelBotLevelScreenPPTI");
		botPreLabelProperty = I18N.createStringBinding("botPreLabelBotLevelScreenPPTI");
		fBotLevelButtonProperty = I18N.createStringBinding("fBotLevelButtonBotLevelScreenPPTI");
		mBotLevelButtonProperty = I18N.createStringBinding("mBotLevelButtonBotLevelScreenPPTI");
		eBotLevelButtonProperty = I18N.createStringBinding("eBotLevelButtonBotLevelScreenPPTI");
		isLaunchOnPPTILabelProperty = I18N.createStringBinding("isLaunchOnPPTILabelBotLevelScreenPPTI");
	}

	public void navigateToAcceptScreen() throws Exception {
		for(int i = 0;i < model.getNbBots();i++)
			this.setVirtualPlayerType(((ToggleButton)toggles.get(i).getSelectedToggle()).getUserData().toString(),i,BotName.get(i).getText());

		for(int i = 0;i < model.getNbBots();i++) {
			String path = "";

			if(Config.IS_DEV_MODE) {
				path = System.getProperty("user.dir");
			}else {
				path = System.getProperty("user.dir");
			}

			if(!path.endsWith("\\dist") && Config.IS_DEV_MODE) {
				path = path + "\\dist";
			}

			ProcessBuilder bot = null;
			Process p = null;
			if(launch.get(i).get()) {
				switch(model.getVirtualPlayerType()[i].toString()) {
					case "BOTF":
						bot = new ProcessBuilder("cmd","/c","start","/MIN","java","-jar","BOTA_Etape4_Equipe3a.jar","-v", ""+1,"--name",model.getPartieInfo().getVirtualPlayersName()[i]);
						bot.directory(new File(path));
						p = bot.start();
						this.botProcesses.add(p);
						break;
					case "BOTM":
						bot = new ProcessBuilder("cmd","/c","start","/MIN","java","-jar","BOTC_Etape4_Equipe3a.jar","-v", ""+1,"--name",model.getPartieInfo().getVirtualPlayersName()[i]);
						bot.directory(new File(path));
						p = bot.start();
						this.botProcesses.add(p);
						break;
					case "BOTE":
						bot = new ProcessBuilder("cmd","/c","start","/MIN","java","-jar","BOTB_Etape4_Equipe3a.jar","-v", ""+1,"--name",model.getPartieInfo().getVirtualPlayersName()[i]);
						bot.directory(new File(path));
						p = bot.start();
						this.botProcesses.add(p);
						break;
				}
			}
		}

		singleton.setViewVisible(3);
	}

	public void navigateToPreviousScreen() {
		singleton.setViewVisible(1);
	}

	public int getNbBots() {
		return model.getNbBots();
	}

	public void setVirtualPlayerType(String type, int i,String name) {

		PlayerType playerType;

		// TODO changer pour les différents niveaux de bot
		switch(type) {
			case "BOTF":
				playerType = PlayerType.BOTF;
				break;
			case "BOTM":
				playerType = PlayerType.BOTM;
				break;
			case "BOTE":
				playerType = PlayerType.BOTE;
				break;
			default:
				playerType = PlayerType.BOTF;
				break;
		}

		model.setVirtualPlayerType(playerType, i,name);
	}

	public ObjectProperty<PartieInfo> getPartieInfo() {return this.partieInfo;}
	public void addToggles(ToggleGroup group){
		if (toggles.size() < 5) {
			toggles.add(group);
		}
	}

	public void addTextField(TextField tf) {
		if(BotName.size() < 5) {
			BotName.add(tf);
		}
	}

	public void addCheckBox(CheckBox cb) {
		if(launch.size() < 5) {
			launch.add(new SimpleBooleanProperty());
			launch.get(launch.size() - 1).bind(cb.selectedProperty());
		}
	}


	/* Internationalization */

	public StringBinding getCreatePartieButtonProperty() {
		return createPartieButtonProperty;
	}

	public StringBinding getTitleLabelProperty() {
		return titleLabelProperty;
	}

	public StringBinding getBotNameProperty(int botIndex) {
		return new StringBinding() {
			{
				super.bind(botPreLabelProperty);
			}

			@Override
			protected String computeValue() {
				return botPreLabelProperty.get() + " " + (botIndex + 1);
			}
		};
	}

	public StringBinding getBotLevelButtonProperty(int level) {
        return switch (level) {
            case 1 -> mBotLevelButtonProperty;
            case 2 -> eBotLevelButtonProperty;
            default -> fBotLevelButtonProperty;
        };
	}

	public StringBinding getIsLaunchOnPPTILabelProperty() {
		return isLaunchOnPPTILabelProperty;
	}
}
