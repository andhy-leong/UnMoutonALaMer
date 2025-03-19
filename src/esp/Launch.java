package esp;

import common.ShutdownManager;

public class Launch {

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ShutdownManager.getInstance().shutdown();
        }));

        // Lancer l'application
        App.lancement(args);
    }
}

