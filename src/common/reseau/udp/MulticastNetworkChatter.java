package common.reseau.udp;

//import multicast.finalclass.mutlicast.exception.NoNetworkInterfaceFound;

import common.Config;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MulticastNetworkChatter {
    private final InetSocketAddress group;
    private final MulticastSocket mcs;
    private final NetworkInterface networkInt;
    private final String ip;

    public MulticastNetworkChatter() throws IOException,Exception {
        //Adresse multicast sur laquelle on enverra nos informations
        String address = "224.7.7.7";
        //Port sur lequel on doit recevoir ces informations
        int port = 7777;

        //on recupere l'adresse de notre group dans un objet pour en suite mieux la maniupler
        InetAddress addr = InetAddress.getByName(address);
        group = new InetSocketAddress(addr, port);

        //on cree notre socket qui servira de point de depart et d'arriver pour les messages
        mcs = new MulticastSocket(port);
        //on recupere notre interface qui nous permettra d'envoyer nos messages
        ip = MulticastNetwork.getIpAddressSocket();
        networkInt = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));

        if (Config.DEBUG_MODE) {
            System.out.println(networkInt.getDisplayName());
        }

        //on regarde si une interface a ete trouvee
        if(networkInt != null) {
            mcs.joinGroup(group,networkInt);
        } else {
            // si ce n'est pas le cas on renvoie une exception
            throw new Exception("Aucune interface reseau fonctionnelle trouvee");
        }
    }

    public boolean sendMessage(String msg) {
        try {
            DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), group);
            mcs.send(packet);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String receiveMessage() {
        try {
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            mcs.receive(packet);
            if (Config.DEBUG_MODE) {
                System.out.println("Message reçu");
            }
            // le string est renvoyer au model pour etre traiter
            // une fois traiter le ModelView renverra les informations a notre view
            // pour etre affichee
            String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            if (Config.DEBUG_MODE) {
                System.out.println("Contenu du message: " + message);
            }
            return message;
        } catch (IOException e) {
            return null;
        }
    }

    public String getIp() {return ip;}

    public void close() {
        try {
            if(!mcs.isClosed()) {
                mcs.leaveGroup(this.group,this.networkInt);
                mcs.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isUp() {
        return mcs.isConnected();
    }
    public void setTimeUp(int milsecond) throws SocketException {
        mcs.setSoTimeout(milsecond);
    }
}
