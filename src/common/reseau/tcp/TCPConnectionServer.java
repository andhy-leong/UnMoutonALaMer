package common.reseau.tcp;

import common.Config;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class TCPConnectionServer {

    private ServerSocket server;
    private ArrayList<Socket> clients;
    private final Thread tAccpetUser;

    public TCPConnectionServer(int port) throws IOException {
        server = new ServerSocket(port);
        clients = new ArrayList<Socket>();
        tAccpetUser = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Config.DEBUG_MODE) {
                    System.out.println("TCP Server Started");
                }
                try {
                    while(!Thread.interrupted()) {
                        Socket s = server.accept();
                        if (Config.DEBUG_MODE) {
                            System.out.println("    Client : " + s.getRemoteSocketAddress() + " connected");
                        }
                        clients.add(s);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void accpetUser() throws IOException {
        tAccpetUser.start();

    }

    public void stop() throws IOException {

        //server.close();
        if (Config.DEBUG_MODE) {
            System.out.println("TCP Server Stopped");
        }
        tAccpetUser.interrupt();
        //server.close();

    }

}
