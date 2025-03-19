package ppti.model;

import common.reseau.udp.inforecup.PartieInfo;

import javafx.collections.ObservableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class GameSaver {

    private static GameSaver INSTANCE = null;
    private Document doc;
    private Element root;

    private GameSaver() {

    }

    public static GameSaver getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GameSaver();
        return INSTANCE;
    }

    public void startFile(PartieInfo partieInfo) {
        try {
            //Creation du dossier de partie
            createDirIfNotExiste();

            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            root = doc.createElement("game");

            doc.appendChild(root);

            Element info = doc.createElement("info");
            info.setAttribute("id",partieInfo.getId());
            info.setAttribute("nom",partieInfo.getNomPartie());
            info.setAttribute("nbj",""+partieInfo.getNombreJoueurMax());
            root.appendChild(info);


        } catch (ParserConfigurationException e ) {
            e.printStackTrace();
        }
    }

    public void playerScore(ObservableMap<String,JoueurInfo> Users, ArrayList<JoueurInfo> joueurOrdre) {
        Element players = doc.createElement("players");
        for(JoueurInfo j : joueurOrdre) {
            Element joueur = doc.createElement("player");
            Element name = doc.createElement("name");
            Element score = doc.createElement("score");

            joueur.setAttribute("idj",Users.get(j.getIdp()).getIdp());

            name.appendChild(doc.createTextNode(Users.get(j.getIdp()).getNom()));
            score.appendChild(doc.createTextNode(""+Users.get(j.getIdp()).getScore()));

            joueur.appendChild(name);
            joueur.appendChild(score);
            players.appendChild(joueur);
        }

        root.appendChild(players);
    }

    public void writeFile(PartieInfo partieInfo) {
        try {
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
            String dateHeure = currentDateTime.format(formatter);
            String name = "gameSaved/" + dateHeure + " - " + partieInfo.getNomPartie()+".xml";

            TransformerFactory transFac = TransformerFactory.newInstance();
            Transformer transformer = transFac.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(name);
            transformer.transform(source,result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

    }

    public void writeSaveFile(PartieInfo info,ObservableMap<String,JoueurInfo> Users) {
        if(createDirIfNotExiste()){
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
            String dateHeure = currentDateTime.format(formatter);
            File fichierSave = new File("gameSaved/" + dateHeure + " - " + info.getNomPartie()+".xml");

            try {
                //fichierSave.createNewFile();
                printInFile(info, Users, fichierSave);
            } catch(IOException e ) {
                //System.err.println("Impossible de créer le fichier.");
                e.printStackTrace();
            }

        }else {
            //System.err.println("Impossible le répertoire parent.");
        }
    }

    private void printInFile(PartieInfo info, ObservableMap<String, JoueurInfo> Users, File fichierSave) throws IOException {
        FileWriter fw = new FileWriter(fichierSave.getAbsoluteFile());
        BufferedWriter writer = new BufferedWriter(fw);

        writer.write("<game>\n");
        writer.write("\t<gameId>" + info.getId() + "</gameId>\n");
        writer.write("\t<players>\n");
        for(JoueurInfo j : Users.values()) {
            writer.write("\t\t<player>\n");
            writer.write("\t\t\t<name>" + j.getNom() + "</name>\n");
            writer.write("\t\t\t<score>" + j.getScore() + "</score>\n");
            writer.write("\t\t</player>\n");
        }
        writer.write("</game>");
        writer.flush();
    }

    private boolean createDirIfNotExiste() {
        File dir = new File("gameSaved");
        if(!dir.exists()) {
            boolean isCreated = dir.mkdir();
            if(!isCreated)
                return false;
        }else if(!dir.isDirectory()) {
            //System.err.println("Répertoire impossible à créer : un fichier porte le même nom.");
            return false;
        }
        return true;
    }
}
