package ppti.model;

public class CarteMeteo {

    private String nom;
    private int valeur;
    private double nbBouee;

    CarteMeteo(String couleur, int valeur) {
        nom = couleur + (valeur < 10 ? "0"+valeur : valeur);
        this.valeur = valeur;

        setNbBouee();
    }

    CarteMeteo(String nom) {
        this.nom = nom;
        this.valeur = Integer.parseInt(nom.replaceAll("[A-z]+",""));

        setNbBouee();
    }

    private void setNbBouee(){
        if(valeur <= 12) {
            nbBouee = 0.;
        }else if(valeur <= 24) {
            nbBouee = 0.5;
        }else if(valeur <= 36) {
            nbBouee = 1.;
        }else if(valeur <= 48) {
            nbBouee = 0.5;
        }else {
            nbBouee = 0.;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CarteMeteo) {
            CarteMeteo carte = (CarteMeteo) obj;
            return carte.getNom().equals(this.nom);
        }

        return false;
    }

    public String getNom() {
        return this.nom;
    }
    public int getValeur() {
        return this.valeur;
    }
    public double getNbBouee() {
        return this.nbBouee;
    }

}
