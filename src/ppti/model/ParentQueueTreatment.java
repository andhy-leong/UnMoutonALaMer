package ppti.model;

import common.Config;
import javafx.application.Platform;
import ppti.view.singleton.SingletonView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ParentQueueTreatment {

    private BlockingQueue<String> inParentQ;
    private BlockingQueue<String> outParentQ;

    private Thread tMessageTreatment;
    private SingletonView singleton;

    public ParentQueueTreatment(BlockingQueue<String> parentQ,SingletonView singleton) {
        this.inParentQ = parentQ;
        this.outParentQ = new LinkedBlockingQueue<>();
        this.singleton = singleton;
        tMessageTreatment = new Thread(() -> {
            while(true) {
                try {
                    String msg = inParentQ.take();
                    messageTest(msg);
                } catch (InterruptedException e) {

                }
            }
        },"ParentQueueThread ParentQueueTreatement - Equipe3a");
        tMessageTreatment.start();
    }

    public BlockingQueue<String> getBlockingQueue() {
        return outParentQ;
    }

    private void messageTest(String msg) {
        try {
            if(!msg.equals("ADJ"))
                // On utilise une méthode dans le Singleton pour tous les déconnecter
                outParentQ.put(msg);
            else
                Platform.runLater(this::reinit);
        } catch (InterruptedException e) {
            if(Config.DEBUG_MODE)
                System.out.println("impossible d'ajoute le message à la Queue");
        }

    }

    public void reinit() {
        singleton.reinit();
    }

    public void stop() {
        tMessageTreatment.interrupt();
    }
}
