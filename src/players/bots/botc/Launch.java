package players.bots.botc;

import java.util.logging.Logger;

public class Launch {
	private static final Logger logger = Logger.getLogger(BotC.class.getName());
    public static void main(String[] args) throws Exception {
    	int verboseLevel = 0;
    	String botName = "";
        // Analyser les arguments de la ligne de commande
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-v":
                case "--verbose":
                    int temp = i + 1;
                    if(temp != args.length && !args[temp].equals("--name"))
                        verboseLevel = Integer.parseInt(args[temp]);
                    break;
                case "--name":
                    String nameNoSpace = "";
                    for(int j = i+1; j< args.length; j++) {
                        if(args[j].equals("-v") || args[j].equals("--verbose")) {
                            botName = nameNoSpace;
                        }
                        if(j+1 != args.length) {
                            nameNoSpace += args[j];
                        }
                        else {
                            nameNoSpace += args[j];
                            botName = nameNoSpace;
                        }
                    }
                    break;
            }
        }

            if(botName.isEmpty()) {
            	//BotB bot = new BotB(verboseLevel,game,nbJoueurs);
                BotC bot = new BotC(verboseLevel);
            }else {
            	BotC bot = new BotC(botName,verboseLevel);
            }


    }
}