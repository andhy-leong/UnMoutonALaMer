package common.reseau.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class TCPconnectionSocket {
    private Socket startPoint;
    private BufferedReader in;
    private PrintWriter out;

    public TCPconnectionSocket(String ip, int port) throws IOException {
        //pour que ca marche il faut que de l'autre cote le serveur soit deja mis en place
        //sinon rien ne fonctionnera
        startPoint = new Socket(ip,port);
        //nous permettra de lire les informations
        in = new BufferedReader(new InputStreamReader(startPoint.getInputStream()));
        //nous permettra d'envoyer des informations
        out = new PrintWriter(startPoint.getOutputStream(),true);
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public String receiveMessage() {
        try {
            // verif si le socket est fermé
            if (startPoint.isClosed() || !startPoint.isConnected()) {
                return null;
            }

            // on essaie de lire le message
            String msg = in.readLine();
            
            // null, c'est que la connexion a été fermée par l'autre bout
            if (msg == null) {
                startPoint.close();
                return null;
            }
            
            return msg;
        } catch(SocketException se) {
            // erreur de socket = déconnexion
            try {
                startPoint.close();
            } catch (IOException e) {
                // ignorer l'erreur de fermeture
            }
            return null;
        } catch(IOException e) {

            //System.err.println("Erreur de lecture: " + e.getMessage());
            return null;
        }
    }

    public void stop() throws IOException {
        startPoint.close();
    }
    
    public void setTimeout(int value)  {
    	try {
    		startPoint.setSoTimeout(value);
    	} catch(Exception e ) {
    		//System.err.println("Error TCPConnectionSocket Timeout");
    	}
    	
    }
}
