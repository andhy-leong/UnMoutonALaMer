package common.reseau.udp;

import common.Config;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collections;

public interface MulticastNetwork {

    static String getIpAddressSocket() {
        ArrayList<NetworkInterface> interfacesValables = new ArrayList<>();
        NetworkInterface interfaceReseau = null;
        InetAddress AddressIP = null;
        try {
            ArrayList<NetworkInterface> ToutesNetworkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            // Remplacer le premier stream par une boucle
            for(NetworkInterface i : ToutesNetworkInterfaces) {
                if(i.isUp() && !i.isLoopback() && !i.isVirtual()) {
                    if(!i.getInterfaceAddresses().isEmpty()) {
                        // Vérifier si l'interface a une adresse IPv4
                        boolean hasIPv4 = false;
                        for(InterfaceAddress addr : i.getInterfaceAddresses()) {
                            if(addr.getAddress() instanceof Inet4Address) {
                                hasIPv4 = true;
                                break;
                            }
                        }
                        if(hasIPv4 && i.supportsMulticast()) {
                            interfacesValables.add(i);
                        }
                    }
                }
            }

            if(!interfacesValables.isEmpty()) {
                // Recherche d'une interface non virtuelle
                for(NetworkInterface i : interfacesValables) {
                    if(!i.getDisplayName().contains("Virtual")) {
                        interfaceReseau = i;
                        break;
                    }
                }
                if(interfaceReseau == null) {
                    interfaceReseau = interfacesValables.get(0);
                }

                // Remplacer le stream pour trouver la première adresse IPv4
                AddressIP = null;
                for(InterfaceAddress interfaceAddress : interfaceReseau.getInterfaceAddresses()) {
                    InetAddress addr = interfaceAddress.getAddress();
                    if(addr instanceof Inet4Address) {
                        AddressIP = addr;
                        break;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(Config.DEBUG_MODE)
            System.out.println("Address : " + AddressIP);

        return AddressIP != null ? AddressIP.getHostAddress() : null;
    }
}