package ppti;	

import common.ShutdownManager;

public class Launch {

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ShutdownManager.getInstance().shutdown();
        },"ShutDownManager Launch - Equipe3a"));


        // Lancer l'application 
        App.lancement(args);
    }
}

