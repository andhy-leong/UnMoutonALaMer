package ppti.model;

import common.enumtype.PlayerType;
import common.reseau.udp.inforecup.PartieInfo;

public class BotLevelScreenModel {

	private PartieInfo info;
	
	public BotLevelScreenModel(PartieInfo info) {
		this.info = info;
	}

	public PartieInfo getPartieInfo() {
		return this.info;
	}

	public void setPartieInfo(PartieInfo info) {this.info = info;}

	public int getNbBots() {
		return info.getNombreJoueurVirtuelMax();
	}

	public void setVirtualPlayerType(PlayerType type, int i,String name) {
		info.setVirtualPlayerType(type, i);
		info.setVirtualPlayerName(name,i);
	}
	public PlayerType[] getVirtualPlayerType() {return info.getVirtualPlayersTypes();}

}
