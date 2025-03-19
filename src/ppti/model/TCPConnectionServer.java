package ppti.model;

import common.Config;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.net.Socket;
import java.io.IOException;

import java.util.concurrent.BlockingQueue;


public class TCPConnectionServer {

    private ServerSocket server;
    private ArrayList<ClientHandler> clients;
    private final Thread tAccpetUser;
    private BlockingQueue<String> clientQueue;

    public TCPConnectionServer(int port) throws IOException {
        server = new ServerSocket(port);
        clients = new ArrayList<ClientHandler>();
        tAccpetUser = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Config.DEBUG_MODE) {
                    System.out.println("TCP Server Started");
                }
                try {
                    while(!Thread.interrupted()) {
                    	//on accepte la connexion
                        Socket s = server.accept();
                        if (Config.DEBUG_MODE) {
                            System.out.println("Client : " + s.getRemoteSocketAddress() + " connected");
                        }
                        //on créé un nouveau client handler pour ses futures messages
                        ClientHandler client = new ClientHandler(s,null);
                        //on créé un nouveau thread pour recevoir et envoyer ses messages
                        Thread tClient = new Thread(client,"ThreadClient TCPConnectionServer - Equipe3a");
                        //on l'ajoute à une liste pour ne pas l'oublier
                        clients.add(client);
                        //on lance son thread
                        tClient.start();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        },"ThreadAcceptUser TCPConnectionServer - Equipe3a");
    }

    public void accpetUser() throws IOException {
        tAccpetUser.start();
    }

    public void stop() throws IOException {

        //server.close();
        tAccpetUser.interrupt();
        if(server != null && !server.isClosed())
            server.close();

        if (Config.DEBUG_MODE) {
            System.out.println("TCP Server Stopped");
        }
    }

}
