package ppti.view.factory;

import java.util.List;

import common.reseau.udp.inforecup.PartieInfo;
import javafx.stage.Stage;
import ppti.view.PlayerConnectionScreenView;
import ppti.view.BaseView;
import ppti.view.BotLevelScreenView;
import ppti.view.CreatePartyScreenView;
import ppti.view.EcranPositionnementView;
import ppti.view.StartingScreenView;
import ppti.viewmodel.CreatePartyScreenViewModel;

public class PPTIViewFactory {

	private Stage stage;
	private List<BaseView> viewList;
	private PartieInfo partieInfo;


    public PPTIViewFactory(Stage stage) {
    	this.stage = stage;
    	this.partieInfo = partieInfo;
    }
    
    public BaseView createView(Integer viewIndex, Object viewModel) throws Exception {
		switch(viewIndex) {
		case 0:
			return createStartingScreenView(stage);
		
		case 1:
			return createPartyScreenView(stage, viewModel);
    	
		case 2:
			return createBotLevelScreenView(stage, viewList, partieInfo);
			
		case 3:
			return createPlayerConnectionScreenView(stage, viewList, partieInfo);
			
		case 4:
			return null;
			
		case 5:
			return createEcranPositionnementView(stage, viewList, partieInfo);
			
		default:
			//System.err.println("la vue précisée n'existe pas");
			return null;
		}
    }
    
    public StartingScreenView createStartingScreenView(Stage stage) {
    	//return new StartingScreenView(stage);
		return null;
    }
    
    public CreatePartyScreenView createPartyScreenView(Stage stage, Object viewModel) {
    	return new CreatePartyScreenView(stage, (CreatePartyScreenViewModel) viewModel);
    }
    
    public BotLevelScreenView createBotLevelScreenView(Stage stage, List<BaseView> viewList, PartieInfo partieInfo) {
    	//return new BotLevelScreenView(stage, viewList);
		return null;
    }
    
    public PlayerConnectionScreenView createPlayerConnectionScreenView(Stage stage, List<BaseView> viewList, PartieInfo partieInfo) throws Exception {
    	//return new PlayerConnectionScreenView(stage, navigationService, viewList, partieInfo);
		return null;
    }
    
    public EcranPositionnementView createEcranPositionnementView(Stage stage, List<BaseView> viewList, PartieInfo partieInfo) throws Exception {
    	//return new EcranPositionnementView();
		return null;
    }

}
