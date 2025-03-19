package ppti.model;

import common.Config;
import common.enumtype.PlayerType;
import common.enumtype.Placement;

import javafx.beans.property.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class JoueurInfo {
	private SimpleStringProperty nom;
	private String idp;
	private PlayerType pt;
	private String couleurJoueur;
	private Placement placement;
	private SimpleIntegerProperty score;
	private SimpleIntegerProperty maxBouee;
	private SimpleIntegerProperty currentBouee;
	private BooleanProperty aJouer;

	private ArrayList<CarteMeteo> carteMeteos;
	private int nbBouee;
	private ObjectProperty<CarteMeteo> playedCard;
	private ArrayList<CarteMeteo> copyCarteMeteos;
	private ObjectProperty<String> currentCarteMaree;
	private ObjectProperty<CarteMeteo> defausse;
	private boolean estElimine;
	private int scoreManche;
	
	public JoueurInfo(String nom,PlayerType pt) {
		this.nom = new SimpleStringProperty(nom);
		this.pt = pt;
		this.score = new SimpleIntegerProperty(0);
		this.maxBouee = new SimpleIntegerProperty(6);
		this.currentBouee = new SimpleIntegerProperty(3);
		scoreManche = 0;
		estElimine = false;
	}

	public String getNom() {return nom.getValue();}
	public void setNom(String nom) {this.nom = new SimpleStringProperty(nom);}
	public String getIdp() {return idp;}
	public void setIdp(String idp) {this.idp = idp;}
	public PlayerType getPlayerType() {return pt;}
	public Integer getScore() {return score.getValue();}
	public Integer getMaxBouee() {return maxBouee.getValue();}
	public Integer getCurrentBouee() {return currentBouee.getValue();}
	public Integer getScoreManche() {return scoreManche;}

	public void setPlayerType(PlayerType pt) {this.pt = pt;}

	public String getCouleurJoueur() {return couleurJoueur;}
	public void setCouleurJoueur(String c) {couleurJoueur = c;}
	public Placement getPlacement() {return placement;}
	public void setPlacement(Placement p) {placement = p;}
	public void setScore(Integer score) {
		this.score.set(score);
	}
	public void addScore(Integer score) {
		this.score.set(this.score.get() + score);
	}
	public void setMaxBouee(Integer maxBouee) {
		this.maxBouee.set(maxBouee);
	}
	public void setScoreManche(Integer scoreManche) {this.scoreManche = scoreManche;}
	public void setCurrentBouee(Integer currentBouee) {
		this.currentBouee.set(currentBouee);

		if(currentBouee == -1) {
			estElimine = true;
			aJouer.set(true);
		}
	}
	public void setNbBouee(int value) {this.nbBouee = value;}
	public void setCurrentCarteMaree(String carte) {
		currentCarteMaree.setValue(carte);
	}
	public String getCurrentCarteMaree() {return this.currentCarteMaree.getValue();}
	public boolean setPlayedCard(String carteMeteo) {
		if(playedCard.getValue() != null) {
			carteMeteos.add((CarteMeteo) playedCard.getValue());
			playedCard.setValue(null);
		}

		CarteMeteo carte = new CarteMeteo(carteMeteo);
		if(!carteMeteos.contains(carte))
			return false;
		this.playedCard.set(new CarteMeteo(carteMeteo));
		carteMeteos.remove(playedCard.get());
		//System.out.println("Joueur : " + this.idp + "\nJoue : " + carteMeteo);
		aJouer.set(true);
		return true;
	}
	public CarteMeteo getPlayedCard() {
		return playedCard.getValue();
	}
	public void setCartes(ArrayList<CarteMeteo> carteMeteos) {
		this.carteMeteos = carteMeteos;
		// on copie la liste des cartes pour plus tard
		this.copyCarteMeteos = new ArrayList<>(carteMeteos);
	}
	public ArrayList<CarteMeteo> getCartes() {
		return this.carteMeteos;
	}
	public void enleverCarte() {
		carteMeteos.add(playedCard.getValue());
		playedCard.setValue(null);
		aJouer.set(false);
	}
	public void setEstElimine(boolean value) {estElimine = value;}
	public boolean getEstElimine() {return this.estElimine;}

	public SimpleStringProperty nomProperty() {return nom;}
	public SimpleIntegerProperty scoreProperty() {return score;}
	public SimpleIntegerProperty maxBoueeProperty() {return maxBouee;}
	public SimpleIntegerProperty currentBoueeProperty() {return currentBouee;}
	public BooleanProperty aJouerProperty() {
		if(aJouer == null)
			aJouer = new SimpleBooleanProperty();
		return aJouer;
	}
	public ObjectProperty<CarteMeteo> carteJoueeProprety() {return playedCard;}
	public ObjectProperty<String> carteMareeProperty() {return currentCarteMaree;}
	public ObjectProperty<CarteMeteo> defausseProperty() {return defausse;}

	@Override
	public boolean equals(Object o) {
		if(o instanceof JoueurInfo) {
			if(((JoueurInfo)o).getIdp().equals(this.getIdp()))
				return true;
			else
				return false;
		}

		return false;
	}

	public void next() {
		this.defausse.setValue(this.playedCard.getValue());
		this.playedCard.setValue(null);
	}
	public ArrayList<CarteMeteo> nextRound() {
		aJouer.set(false);
		estElimine = false;
		currentCarteMaree.set("00");
		playedCard.set(null);

		return copyCarteMeteos;
	}
	public void setProperty() {
		this.defausse = new SimpleObjectProperty<>();
		this.playedCard = new SimpleObjectProperty<>();
		this.currentCarteMaree = new SimpleObjectProperty<>("00");
	}
	public void replay() {
		this.currentCarteMaree.set("00");
		aJouer.set(false);
		estElimine = false;
	}
	public void unsetJouerProperty() {
		if(!estElimine)
			aJouer.set(false);
	}
}
