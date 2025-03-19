package ppti.model;

import common.Config;
import javafx.application.Platform;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.BlockingQueue;

public class ClientHandler implements Runnable {
	
	private Socket client;
	//permet de lire les entrées tant qu'il y en a
	private BufferedReader in;
	//permet d'envoyer des messages tant qu'il y en a à envoyer
	private PrintWriter out;
	private JoueurInfo joueur;
	private BlockingQueue<String> parentQueue;
	private BlockingQueue<String> childQueue;
	private final Thread tParentMessage;
	private final Thread tReceiveMessage;
	private boolean accepted;

	public ClientHandler(Socket s,BlockingQueue<String> parentQueue) {
		if (Config.DEBUG_MODE) {
			System.out.println("Nouveau client");
		}
		client = s;
		this.parentQueue = parentQueue;
		this.childQueue = new LinkedBlockingQueue<String>();
		accepted = false;
		try {
			//nous permettra de lire les informations
	        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	        //nous permettra d'envoyer des informations
	        out = new PrintWriter(client.getOutputStream(),true);
		} catch (IOException e ) {
			//System.err.println("Impossible d'ouvrir une connexion avec l'hôte");
			Thread.currentThread().interrupt();
		}

		tParentMessage = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!Thread.currentThread().isInterrupted()) {
					try {
						//TODO changer le take par un poll permettant de ne pas attendre indefiniement et envoyer une message a l'utilisateur (qu'il en ait un ou non) permettant de savoir s'il est connecter
						//on attend 5 secondes pour recuperer le message du pere, s'il n'y a rien (null) on n'enverra pas de message
						String msg = childQueue.poll(5, TimeUnit.SECONDS);


						if(msg != null) {
							if(msg.matches("<ADP.*"))
								accepted = true;

							//on le renvoie directement au client
							out.println(msg);
						}


                        if (Config.DEBUG_MODE) {
							System.out.println(msg);
						}
					} catch (InterruptedException e) {
						//TODO si l'utilisateur s'est deconnecter envoyer un message au pere qui annulera tout (deconnexion des joueurs)
						//parentQueue.put("STOP");
						if (Config.DEBUG_MODE) {
							System.out.println("ClientHandler : STOP");
						}
					}
				}
            }
		},"ThreadParentAccept ClientHandler - Equipe3a");

		tParentMessage.start();

		tReceiveMessage = new Thread(new Runnable() {
			@Override
			public void run()  {
				try {
					while(!Thread.currentThread().isInterrupted()) {
						//on attend un message de notre client
						if (Config.DEBUG_MODE) {
							System.out.println("En attente d'un message");
						}
						String msg = in.readLine();
						if (Config.DEBUG_MODE) {
							System.out.println("ClientHandler msg : " + msg);
						}
						if(msg != null)
							parentQueue.add(msg.substring(0,msg.length() - 1) + "\" idj=\"" + joueur.getIdp() + "\"/>");
						if (Config.DEBUG_MODE) {
							System.out.println(msg);
						}
						//une fois le messag reçu on le traite directement dans le Decoder
						// TODO envoyer la carte au parent
						//Decoder(msg);
					}
				} catch (IOException e ) {
					try {
						if(accepted)
							parentQueue.put("ADJ");
					} catch(InterruptedException ex) {

					}

					Thread.currentThread().interrupt();
				}
			}
		},"ThreadReceiveMessage ClientHandler - Equipe3a");

		tReceiveMessage.start();
	}

	public BlockingQueue<String> getChildQueue() {return childQueue;}
	public void setJoueurInfo(JoueurInfo joueur) {
		this.joueur = joueur;
	}
	public JoueurInfo getJoueurInfo() {
		return joueur;
	}
	public void stopClientHandler() {
		try {
			this.client.close();
		} catch (IOException e) {
			//System.out.println("ClientHandler " + joueur.getNom() + " à été déconnecté");
		}

		tParentMessage.interrupt();
		tReceiveMessage.interrupt();
	}

	public boolean isStopped() {
		return client.isClosed() && tParentMessage.isInterrupted() && tReceiveMessage.isInterrupted();
	}

	@Override
	public void run() {

	}
}
