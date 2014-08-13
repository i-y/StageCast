package stagecast;

import data.DatabaseObject;
import data.ImportationObject;
import data.ModelObject;
import data.SettingsObject;
import data.StatsObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Handles loading and saving XML files.
 * This class is used to manipulate XML files used by the program to store or 
 * transfer data. The specific types of files covered are the `[x].about.xml` 
 * files which describe an organism or weather database, the model database, and 
 * information being passed back to the program from called R scripts. The 
 * program does not use a third-party library to manage XML because one of its 
 * design goals is to use only the standard libraries so as to increase the odds 
 * of it running without issue on any computer.
 * @author Ian Yocum
 * @date 7/17/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @note Database: This does not load the data in the database, but rather the 
 * `[x].about.txt` file which describes the database named `[x]`.
 * @todo Database: Investigate trimming some of the loops in the readXML caller 
 * graph.
 * @todo Add checks to the `saveModels()` method so that a null value doesn't 
 * cause a crash.
 * @todo Fix crash on trying to load empty xml file.
 */
public class XmlManager {    
    /**
     * Requests individual values from an XML document.
     * @param n Individual node to request the data from.
     * @param target The name of the XML element who's value is being requested.
     * @param index In the case that there are multiple values under the same 
     * target name, this specifies which one will be returned.
     * @return Value of the tag as a string.
     */
    private static String getValue(Node n, String target, int index) {
        String ret = "";
        if(n != null) {
            try {
                Element elem = (Element) n;
                NodeList elemList = elem.getElementsByTagName(target);
                if ((elemList != null) && (elemList.getLength() > 0)) {
                    Element namedElem = (Element) elemList.item(index);
                    if (namedElem != null) {
                        NodeList value = namedElem.getChildNodes();
                        if ((value != null)&&(value.item(0) != null)) {
                            ret = value.item(0).getNodeValue();
                        }
                    }
                }
            } catch(DOMException e) {
                ErrorManager.error("Error while reading XML file.", "XmlManager.getValue failed to load a requested XML value.", e);
            }
        }
        return ret;
    }
    
    /**
     * Loads a model database.
     * @param target The model database to be loaded.
     * @return an array of all existing models at targeted location.
     * @note This method is also used to retrieve data output from the script
     * `model.r`.
     * @note The data stored by model is redundant as such and so this function
     * is liable to be split into something to load the models and something to
     * load the statistical output.
     */
    public static ArrayList<ModelObject> loadModels(String target) {
        ArrayList<ModelObject> ret = new ArrayList<>();
        File file = new File(target);
        if(file.exists()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom;
                try {
                    dom = db.parse(target);
                } catch (SAXException | IOException e) {
                    ErrorManager.log("XmlManager.loadModels failed to load an empty file.", e);
                    XmlManager.saveModels(target, null);
                    dom = db.parse(target);
                }
                Element doc = dom.getDocumentElement();
                if(!dom.getDocumentElement().getNodeName().equals("models")) {
                    ErrorManager.error("Could not load model database.", "XmlManager.loadModels has found that the XML file does not contain valid model data.", null);
                } else {
                    NodeList models = doc.getElementsByTagName("model");
                    if(models.getLength() > 0) {
                        for(int i = 0; i < models.getLength(); i++) {
                            String temp;
                            ModelObject tempModel = new ModelObject();
                            tempModel.name = getValue(models.item(i), "name", 0);
                            temp = getValue(models.item(i), "date", 0);
                            if (!temp.isEmpty()){
                                tempModel.date = temp;
                            } else {
                                tempModel.date = "Unknown";
                            }
                            temp = getValue(models.item(i), "time", 0);
                            if (!temp.isEmpty()){
                                tempModel.time = temp;
                            } else {
                                tempModel.time = "Unknown";
                            }
                            tempModel.OrganismDatabaseName = getValue(models.item(i), "organismName", 0);
                            tempModel.OrganismDatabaseLocation = getValue(models.item(i), "organismLocation", 0);
                            tempModel.weatherDatabaseName = getValue(models.item(i), "weatherName", 0);
                            tempModel.weatherDatabaseLocation = getValue(models.item(i), "weatherLocation", 0);
                            temp = getValue(models.item(i), "organismCount", 0);
                            if (!temp.isEmpty()){
                                tempModel.organismCount = Integer.parseInt(temp);
                            }
                            temp = getValue(models.item(i), "paramCount", 0);
                            if (!temp.isEmpty()){
                                tempModel.paramCount = Integer.parseInt(temp);
                            }
                            tempModel.optim = getValue(models.item(i), "optim", 0);
                            if((tempModel.organismCount > 0)&&(doc.getElementsByTagName("organismCount").getLength() > 0)){ 
                                int len = ((Element)(models.item(i))).getElementsByTagName("organismFile").getLength();
                                if(len == 0) {
                                    len = tempModel.organismCount;
                                }
                                if(len != tempModel.organismCount) {
                                    tempModel.organismCount = len;
                                }
                                if(len > 0) {
                                    tempModel.OrganismFiles = new String[len];
                                    for(int j = 0; j < len; j++) {
                                        tempModel.OrganismFiles[j] = getValue(models.item(i), "organismFile", j);
                                    }
                                }
                            }
                            if((tempModel.paramCount > 0)&&(doc.getElementsByTagName("param").getLength() > 0)){ 
                                NodeList parameters = ((Element)(models.item(i))).getElementsByTagName("param");
                                int len = parameters.getLength();
                                tempModel.paramCount = len;
                                if(len > 0) {
                                    tempModel.params = new String[len];
                                    for(int j = 0; j < len; j++) {
                                        tempModel.params[j] = getValue(parameters.item(j), "val", 0);
                                    }
                                }
                            }
                            if(doc.getElementsByTagName("stageMap").getLength() > 0){ 
                                int len = ((Element)(models.item(i))).getElementsByTagName("stageMap").getLength();
                                for(int j = 0; j < len; j++) {
                                    tempModel.stageMap.add(getValue(models.item(i),"stageMap",j));
                                }
                            }
                            tempModel.logLikelihood = getValue(models.item(i), "log", 0);
                            tempModel.aic = getValue(models.item(i), "aic", 0);
                            tempModel.stats = readStatsElements((Element) models.item(i));
                            ret.add(tempModel);
                        }
                    }
                }
            } catch(ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
                ErrorManager.error("Could not load model database", "Failed to read database. XmlManager.loadModels encountered an error.", e);
            }
        }
        return ret;
    }
    
    /**
     * Loads an XML file describing the current settings.
     * @return A SettingsObject holding the loaded settings.
     */
    public static SettingsObject loadSettings() {
        File file = new File("settings.xml");
        SettingsObject ret = new SettingsObject();
        if(!file.exists()) {
            XmlManager.saveSettings(new SettingsObject());
        }
        try {	
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse("settings.xml");
            Element doc = dom.getDocumentElement();
            if(!dom.getDocumentElement().getNodeName().equals("settings")) {
                ErrorManager.error("Could not load settings file.","XmlManager.loadSettings found that the XML file does not contain settings information.", null);
            } else {
                ret.loadedOrganism = getValue(doc,"lastOrganism", 0);
                ret.loadedWeather = getValue(doc,"lastWeather", 0);
                ret.loadedModel = getValue(doc,"lastModel", 0);
                String temp = getValue(doc,"organismImport", 0);
                if(!temp.isEmpty()){
                    ret.defaultOrganismLoad = temp;
                }
                temp = getValue(doc,"weatherImport", 0);
                if(!temp.isEmpty()){
                    ret.defaultWeatherLoad = temp;
                }
                temp = getValue(doc,"organismSave", 0);
                if(!temp.isEmpty()){
                    ret.defaultOrganismSave = temp;
                }
                temp = getValue(doc,"weatherSave", 0);
                if(!temp.isEmpty()){
                    ret.defaultWeatherSave = temp;
                }
                temp = getValue(doc, "modelLocation", 0);
                if(!temp.isEmpty()){
                    ret.defaultModelLocation = getValue(doc, "modelLocation", 0);
                }
                temp = getValue(doc, "figureLocation", 0);
                if(!temp.isEmpty()){
                    ret.defaultFigureLocation = getValue(doc, "figureLocation", 0);
                }
                temp = getValue(doc, "tempFigureLocation", 0);
                if(!temp.isEmpty()){
                    ret.defaultTempGraphOutput = getValue(doc, "tempFigureLocation", 0);
                }
                temp = getValue(doc, "tooltips",0);
                if(!temp.isEmpty()){
                    ret.tooltips = Boolean.valueOf(temp);
                }
                NodeList nodes = doc.getElementsByTagName("organismList");
                if(nodes.getLength() > 0) {
                    ret.organismCount = ((Element) nodes.item(0)).getElementsByTagName("organism").getLength();
                    for(int i = 0; i < ret.organismCount; i++) {
                        NodeList tn = ((Element) nodes.item(0)).getElementsByTagName("organism");
                        String location = getValue(tn.item(i), "location", 0);
                        String name = getValue(tn.item(i), "name", 0);
                        if(new File(location).exists()) {
                            ret.organismLocations.add(location);
                            ret.organismNames.add(name);
                        } else {
                            ret.organismCount--;
                        }
                    }
                }
                nodes = doc.getElementsByTagName("weatherList");
                if(nodes.getLength() > 0) {
                    ret.weatherCount = ((Element) nodes.item(0)).getElementsByTagName("weather").getLength();
                    for(int i = 0; i < ret.weatherCount; i++) {
                        NodeList tn = ((Element) nodes.item(0)).getElementsByTagName("weather");
                        String location = getValue(tn.item(i), "location", 0);
                        String name = getValue(tn.item(i), "name", 0);
                        if(new File(location).exists()) {
                            ret.weatherLocations.add(location);
                            ret.weatherNames.add(name);
                        } else {
                            ret.weatherCount--;
                        }
                    }
                } 
            }
        } catch(ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
            ErrorManager.error("Failed to load settings file.", "XmlManager.loadSettings encountered an error while attempting to read settings file.", e);
        }
        return ret;
    }
    
    /**
     * Loads an XML file containing statistical data.
     * This method is used to retrieve information output by the script 
     * `stats.r` and feed it back into the main program. The script outputs its 
     * information as an XML file for easy parsing. See ScriptManager for an 
     * example of the file produced by the main program to be used as input for 
     * the script.
     * @param target String representing the location to attempt to read 
     * statistics data.
     * @return A StatsObject containing the results of the calculations by the 
     * script `stats.r`.
     */
    public static StatsObject loadStats(String target) {

        StatsObject ret = new StatsObject();
        File file = new File(target);
        if(!file.exists()) {
            System.out.println("no file");
            return null;
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(target);
            Element doc = dom.getDocumentElement();
            if(!dom.getDocumentElement().getNodeName().equals("stats")) {
                ErrorManager.error("Could not load statistical output.", "XmlManager.loadStats has found that the XML file does not contain valid statistical data. <stats> tag expected as root element.", null);
            } else {
                ret = readStatsElements(doc);
            }
        } catch (ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
            ErrorManager.error("Could not load statistical output.", "XmlManager.loadStats has found that an error occurred while attempting to load the XML file " + target, e);
        }
        return ret;
    }
    
    /**
     * Loads the XML file describing a database.
     * This Method attempts to read a given XML file and store it as a 
     * DatabaseObject. Should this fail, it will attempt to create a valid file 
     * from the contents of the directory its target is in. Only when the
     * reconstruction returns a failure will the method return a failed load to
     * the caller.
     * @param target The file describing a database. File is expected to be in 
     * the form "[x].about.txt" where "[x]" is the name of the database.
     * @return DatabaseObject containing the contents of the targeted database.
     */
    public static DatabaseObject readDatabaseFile(String target) {
        File file = new File(target);
        if(!file.exists()) {
            return null;
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(target);
            Element doc = dom.getDocumentElement();
            if(!dom.getDocumentElement().getNodeName().equals("about")) {
                ErrorManager.error("The data file, " + target + ", is invalid and is being reconstructed.", "The XML file loaded by the program at XmlManager.readDatabaseFile, " + target + ", did not describe a valid database file and so could not be loaded. The program will now attempt to reconstruct a valid file.", null);
                return reconstruct(new File(target).getParent());
            }
            DatabaseObject database = new DatabaseObject();
            database.name = getValue(doc,"name", 0);
            database.date = getValue(doc,"date", 0);
            database.time = getValue(doc,"time", 0);
            database.location = getValue(doc,"location", 0);
            database.type = getValue(doc,"type", 0);
            String temp = getValue(doc,"size", 0);
            if(!temp.isEmpty()) {
                database.size = Integer.parseInt(temp);
            }
            temp = getValue(doc,"stages", 0);
            if(!temp.isEmpty()) {
                database.stages = Integer.parseInt(temp);
                database.memberStages = new String[database.stages];
                NodeList nodes = doc.getElementsByTagName("stageNames");
                for(int i = 0; i < database.stages; i++) {
                    database.memberStages[i] = getValue(nodes.item(0), "nameVal", i );
                }
            }
            NodeList nodes = doc.getElementsByTagName("members");
            if((database.size > 0) && (nodes.getLength() > 0)) {
                List<String> locationHolder = new ArrayList<>();
                List<String> nameHolder = new ArrayList<>();
                int modelsLen = ((Element) nodes.item(0)).getElementsByTagName("member").getLength();
                for(int i = 0; i < modelsLen; i++) {
                    locationHolder.add(getValue( nodes.item(0), "location", i ));
                    nameHolder.add(getValue( nodes.item(0), "name", i ));
                }
                database.memberLocations = new String[locationHolder.size()];
                database.memberLocations = locationHolder.toArray(database.memberLocations);
                database.memberNames = new String[nameHolder.size()];
                database.memberNames = nameHolder.toArray(database.memberNames);
            } else {
                if(!(nodes.getLength() > 0)) {
                    ErrorManager.error("Tags in the file, " + target + ", are invalid and the file is being reconstructed.","XmlManager.readDatabaseFile has found that the file, " + target + ", does not have any members that the program can identify. File is expected to have the tags \"<Members></Members>\" around its members and these tags may be missing or improperly set. The program will now attempt to reconstruct a valid file.", null);
                    return reconstruct(new File(target).getParent());
                }
            }
            return database;
        } catch(ParserConfigurationException | SAXException | IOException | NumberFormatException e) {
            ErrorManager.error("Could not load database file.","XmlManager.readDatabaseFile has encountered an error. Please review log.txt for details.", e);
        } 
        return reconstruct(new File(target).getParent());
    }
    
    /**
     * Loads statistical output.
     * @param doc Element to parse.
     * @return Loaded statistics as a StatsObject.
     */
    private static StatsObject readStatsElements(Element doc) {
        StatsObject ret = new StatsObject();
        try {
            String temp;
            ret.iterations = getValue(doc,"iter", 0);
            ret.alpha = getValue(doc,"alpha", 0);
            ret.gg = getValue(doc,"gg", 0);
            ret.ggPval = getValue(doc,"ggPval", 0);
            NodeList nodes = doc.getElementsByTagName("ggci");
            if(nodes.getLength() > 0) {
                ret.ggci[0] = getValue(nodes.item(0),"low",0);
                ret.ggci[1] = getValue(nodes.item(0),"high",0);
            }
            temp = getValue(doc,"stages", 0);
            int stages;
            int iter = 0;
            if(!ret.iterations.isEmpty()) {
                iter = Integer.parseInt(ret.iterations);
            }
            nodes = doc.getElementsByTagName("xx");
            if(nodes.getLength() > 0) {
                ret.xx = getValue(nodes.item(0),"stat",0);
                ret.xxPval = getValue(nodes.item(0),"p",0);
                ret.xxHigh = getValue(nodes.item(0),"high",0);
                ret.xxLow = getValue(nodes.item(0),"low",0);
            }
            ret.optim = getValue(doc,"optim",0);
            if ((!temp.isEmpty())&&(iter > 0)){
                ret.stages = temp;
                stages = Integer.parseInt(temp);
                ret.aStar = new String[iter][stages];
                ret.a = new String[stages];
                ret.aHigh = new String[stages];
                ret.aLow = new String[stages];
                nodes = doc.getElementsByTagName("aparam");
                int len = nodes.getLength();
                if(nodes.getLength() > 0) {
                    for(int i = 0; i < nodes.getLength(); i++) {
                        ret.a[i] = getValue(nodes.item(i),"val",0);
                        ret.aHigh[i] = getValue(nodes.item(i),"high",0);
                        ret.aLow[i] = getValue(nodes.item(i),"low",0);
                    }
                }
                nodes = doc.getElementsByTagName("aStar");
                if(nodes.getLength() > 0) {
                    for(int i = 0; i < nodes.getLength(); i++) {
                        for(int j = 0; j < stages; j++) {
                            ret.aStar[i][j] = getValue(nodes.item(i),"val",j);
                        }
                    }
                }
            }
            nodes = doc.getElementsByTagName("vparam");
            if(nodes.getLength() > 0) {
                ret.v = getValue(nodes.item(0),"val",0);
                ret.vHigh = getValue(nodes.item(0),"high",0);
                ret.vLow = getValue(nodes.item(0),"low",0);
            }
            nodes = doc.getElementsByTagName("vStar");
            if(nodes.getLength() > 0) {
                int len = nodes.getLength();
                ret.vStar = new String[len];
                for(int i = 0; i < len; i++) {
                    ret.vStar[i] = getValue(doc,"vStar",i);
                }
            }
            nodes = doc.getElementsByTagName("ggStar");
            if(nodes.getLength() > 0) {
                int len = nodes.getLength();
                ret.ggStar = new String[len];
                for(int i = 0; i < len; i++) {
                    ret.ggStar[i] = getValue(doc,"ggStar",i);
                }
            }
        } catch (NumberFormatException e) {
            ErrorManager.error("XmlManager.readStatsElements has encountered an error.",e);
        }
        return ret;
    }
    
    /**
     * Attempts to automatically construct a database file.
     * This method is a way for the program to recover from some user or program 
     * errors. If the database file is in some way unreadable, it is frequently 
     * possible to reconstruct what it should look like given the contents of 
     * the directory.
     * @param directory The directory to be parsed.
     * @warning It is expected that the database directory will only contain 
     * files which are part of that data set. Improper files will cause the 
     * reconstruction method to fail.
     */
    private static DatabaseObject reconstruct(String directory) {
        DatabaseObject ret = null;
        try {
            String name = new File(directory).getName();
            File dir = new File(directory);
            if((!dir.isDirectory()) || (name.equals("")) ) {
                ErrorManager.error("Program failed to reconstruct database file.", "The function XmlManager.reconstruct was passed incorrect parameters.", null);
                return null;
            }
            DatabaseObject database = new DatabaseObject();
            database.name = name;
            File[] files = dir.listFiles(); 
            if(files.length < 2) {
                ErrorManager.error("Program failed to reconstruct database file.", "The function XmlManager.reconstruct was not given the location of a database.", null);
                return null;
            }
            ArrayList<String> fileNames = new ArrayList<>();
            ArrayList<String> fileLocations = new ArrayList<>();
            for (File file : files) {
                String[] filePath = file.getAbsolutePath().split("\\\\");
                String holder[] =  filePath[filePath.length - 1].split("\\.");
                if((holder.length < 3) || (!holder[holder.length - 3].equals(name))) {
                    ErrorManager.error("Program failed to reconstruct database file.", "The function XmlManager.reconstruct was given a folder that contains non-database files. Remove extra files from the directory " + directory + ".", null);
                    return null;
                }
                if (holder[holder.length - 1].equals("txt")) {
                    if (holder[holder.length - 2].equals("weather")) {
                        database.type = "Weather";
                        fileNames.add("weather");
                        fileLocations.add(file.getAbsolutePath());
                    } else {
                        fileNames.add(holder[holder.length - 2]);
                        fileLocations.add(file.getAbsolutePath());
                    }
                }
            }
            database.size = fileNames.size();
            if(database.type.equals("Unknown")) {
                database.type = "Organism";
            }
            database.location = dir.getAbsolutePath() + "/" + name + ".about.xml";
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date d = new Date();
            database.date = dateFormat.format(d) + " (R)";
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            database.time = dateFormat.format(d) + " (R)";
            if(!database.type.equals("Weather")) {
                String line;
                try (BufferedReader inputStream = new BufferedReader(new FileReader(fileLocations.get(0)))) {
                    line = inputStream.readLine();
                }
                database.stages = line.split("\t").length - 1;
            }
            database.memberLocations = new String[database.size];
            database.memberLocations = fileLocations.toArray(database.memberLocations);
            database.memberNames = new String[database.size];
            database.memberNames = fileNames.toArray(database.memberNames);
            if (writeReconstruction(database)) {
                ret = database;
            } else {
                ErrorManager.error("Program failed to reconstruct database file.", "The function XmlManager.reconstruct was able to reconstruct a file but the file could not be saved.", null);
            }
        } catch(IOException e){
            ErrorManager.error("Attempted Database reconstruction failed.", "XmlManager.reconstruct ran into the following error, which caused it to abort operations. See log.txt for details.",e);
        }
        return ret;
    }
    
    /**
     * Saves a collection of models to a database.
     * This method saves models to a database. Each model is its own object and 
     * so this method takes a collection of these objects and saves them to a 
     * single database.
     * @param target File to save to.
     * @param models Collection of ModelObjects which hold the constituent parts 
     * of the database to be saved.
     */
    public static void saveModels(String target, ArrayList<ModelObject> models/*ModelObject[] models*/) {
        try {
            Document dom;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();
            Element rootEle = dom.createElement("models");
            if(((models != null)&&(new File(target).exists()))) {	
                for (ModelObject model1 : models) {
                    if (model1 != null) {
                        Element model = dom.createElement("model");
                        Element e = dom.createElement("name");
                        e.appendChild(dom.createTextNode(model1.name));
                        model.appendChild(e);
                        e = dom.createElement("date");
                        e.appendChild(dom.createTextNode(model1.date));
                        model.appendChild(e);
                        e = dom.createElement("time");
                        e.appendChild(dom.createTextNode(model1.time));
                        model.appendChild(e);
                        e = dom.createElement("weatherName");
                        e.appendChild(dom.createTextNode(model1.weatherDatabaseName));
                        model.appendChild(e);
                        e = dom.createElement("weatherLocation");
                        e.appendChild(dom.createTextNode(model1.weatherDatabaseLocation));
                        model.appendChild(e);
                        e = dom.createElement("organismName");
                        e.appendChild(dom.createTextNode(model1.OrganismDatabaseName));
                        model.appendChild(e);
                        e = dom.createElement("organismLocation");
                        e.appendChild(dom.createTextNode(model1.OrganismDatabaseLocation));
                        model.appendChild(e);
                        e = dom.createElement("paramCount");
                        e.appendChild(dom.createTextNode(Integer.toString(model1.paramCount)));
                        model.appendChild(e);
                        e = dom.createElement("organismCount");
                        e.appendChild(dom.createTextNode(Integer.toString(model1.organismCount)));
                        model.appendChild(e);
                        for (String OrganismFile : model1.OrganismFiles) {
                            e = dom.createElement("organismFile");
                            e.appendChild(dom.createTextNode(OrganismFile));
                            model.appendChild(e);
                        }
                        if(model1.params != null) {
                            for (String param : model1.params) {
                                e = dom.createElement("param");
                                Element f = dom.createElement("val");
                                f.appendChild(dom.createTextNode(param));
                                e.appendChild(f);
                                model.appendChild(e);
                            }
                        }
                        if(model1.stageMap.size() > 0) {
                            for(String map : model1.stageMap) {
                                e = dom.createElement("stageMap");
                                e.appendChild(dom.createTextNode(map));
                                model.appendChild(e);
                            }
                        }   
                        e = dom.createElement("log");
                        e.appendChild(dom.createTextNode(model1.logLikelihood));
                        model.appendChild(e);
                        e = dom.createElement("aic");
                        e.appendChild(dom.createTextNode(model1.aic));
                        model.appendChild(e);
                        e = dom.createElement("optim");
                        e.appendChild(dom.createTextNode(model1.optim));
                        model.appendChild(e);
                        e = writeStatsElements(dom, model1.stats);
                        if(e != null) {
                            model.appendChild(e);
                        }
                        rootEle.appendChild(model);
                    }
                }
            }
            dom.appendChild(rootEle);
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(target)));
        } catch(ParserConfigurationException | DOMException | TransformerFactoryConfigurationError | IllegalArgumentException | FileNotFoundException | TransformerException e) {
            ErrorManager.error("Could not save model database", "Failed to write database. XmlManager.saveModels encountered an error.", (Exception) e);
        }
    }
    
    /**
     * Saves the current settings to an XML file.
     * @param settings The current settings as stored in a SettingsObject.
     */
    public static void saveSettings(SettingsObject settings) {
        try {
            Document dom;
            Element e;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();
            Element rootEle = dom.createElement("settings");
            e = dom.createElement("lastOrganism");
            e.appendChild(dom.createTextNode(settings.loadedOrganism));
            rootEle.appendChild(e);
            e = dom.createElement("lastWeather");
            e.appendChild(dom.createTextNode(settings.loadedWeather));
            rootEle.appendChild(e);
            e = dom.createElement("lastModel");
            e.appendChild(dom.createTextNode(settings.loadedModel));
            rootEle.appendChild(e);
            e = dom.createElement("organismImport");
            e.appendChild(dom.createTextNode(settings.defaultOrganismLoad));
            rootEle.appendChild(e);
            e = dom.createElement("weatherImport");
            e.appendChild(dom.createTextNode(settings.defaultWeatherLoad));
            rootEle.appendChild(e);
            e = dom.createElement("organismSave");
            e.appendChild(dom.createTextNode(settings.defaultOrganismSave));
            rootEle.appendChild(e);
            e = dom.createElement("weatherSave");
            e.appendChild(dom.createTextNode(settings.defaultWeatherSave));
            rootEle.appendChild(e);
            e = dom.createElement("modelLocation");
            e.appendChild(dom.createTextNode(settings.defaultModelLocation));
            rootEle.appendChild(e);
            e = dom.createElement("figureLocation");
            e.appendChild(dom.createTextNode(settings.defaultFigureLocation));
            rootEle.appendChild(e);
            e = dom.createElement("tempFigureLocation");
            e.appendChild(dom.createTextNode(settings.defaultTempGraphOutput));
            rootEle.appendChild(e);
            e = dom.createElement("tooltips");
            e.appendChild(dom.createTextNode(String.valueOf(settings.tooltips)));
            rootEle.appendChild(e);
            e = dom.createElement("organismList");
            if (settings.organismLocations.size() > 0) {
                for(int i = 0; i < settings.organismLocations.size(); i++) {
                    Element f = dom.createElement("organism"); 
                    Element g = dom.createElement("location");
                    g.appendChild(dom.createTextNode(settings.organismLocations.get(i)));
                    f.appendChild(g);
                    g = dom.createElement("name");
                    g.appendChild(dom.createTextNode(settings.organismNames.get(i)));
                    f.appendChild(g);
                    e.appendChild(f);
                }
            }
            rootEle.appendChild(e);
            e = dom.createElement("weatherList");
            if(settings.weatherLocations.size() > 0) {
                for(int i = 0; i < settings.weatherLocations.size(); i++) {
                    Element f = dom.createElement("weather"); 
                    Element g = dom.createElement("location");
                    g.appendChild(dom.createTextNode(settings.weatherLocations.get(i)));
                    f.appendChild(g);
                    g = dom.createElement("name");
                    g.appendChild(dom.createTextNode(settings.weatherNames.get(i)));
                    f.appendChild(g);
                    e.appendChild(f);
                    
                }
            }
            rootEle.appendChild(e);
            dom.appendChild(rootEle);
	        
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream("settings.xml")));
        } catch(ParserConfigurationException | DOMException | TransformerFactoryConfigurationError | IllegalArgumentException | FileNotFoundException | TransformerException e) {
            ErrorManager.error("Could not save settings.","XmlManager.saveSettings encountered an error while trying to write to the XML file.", (Exception) e);
        }
    }
    
    /**
     * Updates a database.
     * This updates the .about.xml file which describes a database.
     * @param data The DatabaseObject which describes the .about.xml file for 
     * the database.
     */
    public static void updateDatabaseFile(DatabaseObject data) {
        try {
            Document dom;
            Element e;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();
            Element rootEle = dom.createElement("about");
            e = dom.createElement("name");
            e.appendChild(dom.createTextNode(data.name));
            rootEle.appendChild(e);
            e = dom.createElement("date");
            e.appendChild(dom.createTextNode(data.date));
            rootEle.appendChild(e);
            e = dom.createElement("time");
            e.appendChild(dom.createTextNode(data.time));
            rootEle.appendChild(e);
            e = dom.createElement("location");
            e.appendChild(dom.createTextNode(data.location));
            rootEle.appendChild(e);
            e = dom.createElement("type");
            e.appendChild(dom.createTextNode(data.type));
            rootEle.appendChild(e);
            e = dom.createElement("stages");
            e.appendChild(dom.createTextNode(String.valueOf(data.stages)));
            rootEle.appendChild(e);
            e = dom.createElement("stageNames");
            for(int i = 0; i < data.stages; i++) {
                Element f = dom.createElement("nameVal");
                f.appendChild(dom.createTextNode(data.memberStages[i]));
                e.appendChild(f);
            }
            rootEle.appendChild(e);
            e = dom.createElement("size");
            e.appendChild(dom.createTextNode(String.valueOf(data.size)));
            rootEle.appendChild(e);
            e = dom.createElement("members");
            for(int i = 0; i < data.size; i++) {
                Element f = dom.createElement("member"); 
                Element g = dom.createElement("location");
                g.appendChild(dom.createTextNode(data.memberLocations[i]));
                f.appendChild(g);
                g = dom.createElement("name");
                g.appendChild(dom.createTextNode(data.memberNames[i]));
                f.appendChild(g);
                e.appendChild(f);
            }
            rootEle.appendChild(e);
            dom.appendChild(rootEle);
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(data.location)));
        } catch (ParserConfigurationException | DOMException | TransformerFactoryConfigurationError | IllegalArgumentException | FileNotFoundException | TransformerException e) {
            ErrorManager.error("The program ran in to an error trying to update a database.", "XmlManager.updateDatabaseFile encountered an error." ,(Exception) e);
        }
    }
    
    /**
     * Records a database.
     * This method creates a database from an imported data set.
     * @param target The directory the database should be created in.
     * @param io The ImportationObject which holds all the necessary data to 
     * save the database.
     */
    public static void writeDatabaseFile(String target, ImportationObject io) {
        try {
            Document dom;
            Element e;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();
            Element rootEle = dom.createElement("about");
            e = dom.createElement("name");
            e.appendChild(dom.createTextNode(io.datasetName));
            rootEle.appendChild(e);
            e = dom.createElement("date");
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date = new Date();
            e.appendChild(dom.createTextNode(dateFormat.format(date)));
            rootEle.appendChild(e);
            e = dom.createElement("time");
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            e.appendChild(dom.createTextNode(dateFormat.format(date)));
            rootEle.appendChild(e);
            e = dom.createElement("location");
            e.appendChild(dom.createTextNode(io.destination + "/" + io.datasetName + ".about.xml"));
            rootEle.appendChild(e);
            e = dom.createElement("type");
            if(io.species) {
                e.appendChild(dom.createTextNode("Organism"));
            } else {
                e.appendChild(dom.createTextNode("Weather"));
            }
            rootEle.appendChild(e);
            e = dom.createElement("stages");
            e.appendChild(dom.createTextNode(String.valueOf(io.stages)));
            rootEle.appendChild(e);
            e = dom.createElement("stageNames");
            for(int i = 0; i < io.stages; i++) {
                Element f = dom.createElement("nameVal");
                f.appendChild(dom.createTextNode("Stage " + i));
                e.appendChild(f);
            }
            rootEle.appendChild(e);
            e = dom.createElement("size");
            e.appendChild(dom.createTextNode(String.valueOf(io.data.keySet().size())));
            rootEle.appendChild(e);
            e = dom.createElement("members");
            String keys[] = new String[io.dates.keySet().size()];
            io.dates.keySet().toArray(keys);
            for (String key : keys) {
                Element f = dom.createElement("member"); 
                Element g = dom.createElement("location");
                g.appendChild(dom.createTextNode(String.valueOf(io.destination + "/" + io.datasetName + "." + key + ".txt")));
                f.appendChild(g);
                g = dom.createElement("name");
                g.appendChild(dom.createTextNode(key));
                f.appendChild(g);
                e.appendChild(f);
            }
            rootEle.appendChild(e);
            dom.appendChild(rootEle);
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(target)));
        } catch(ParserConfigurationException | DOMException | TransformerFactoryConfigurationError | IllegalArgumentException | FileNotFoundException | TransformerException e) {
            ErrorManager.error("Could not save data file " + target, "XmlManager.writeDatabaseFile has found that an error occurred while attempting write to the XML file " + target, (Exception)e);
        }
    }
    
    /**
     * Writes reconstructed database file to its proper location.
     * @param database The DatabaseOjbect created by reconstruct().
     */
    private static boolean writeReconstruction(DatabaseObject database) {
        try {
            Document dom;
            Element e;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();
            Element rootEle = dom.createElement("about");
            e = dom.createElement("name");
            e.appendChild(dom.createTextNode(database.name));
            rootEle.appendChild(e);
            e = dom.createElement("date");
            e.appendChild(dom.createTextNode(database.date));
            rootEle.appendChild(e);
            e = dom.createElement("time");
            e.appendChild(dom.createTextNode(database.time));
            rootEle.appendChild(e);
            e = dom.createElement("location");
            e.appendChild(dom.createTextNode(database.location));
            rootEle.appendChild(e);
            e = dom.createElement("type");
            e.appendChild(dom.createTextNode(database.type));
            rootEle.appendChild(e);
            e = dom.createElement("stages");
            e.appendChild(dom.createTextNode(String.valueOf(database.stages)));
            rootEle.appendChild(e);
            e = dom.createElement("size");
            e.appendChild(dom.createTextNode(String.valueOf(database.size)));
            rootEle.appendChild(e);
            e = dom.createElement("members");
            for(int i = 0; i < database.size; i++) {
                Element f = dom.createElement("member"); 
                Element g = dom.createElement("location");
                g.appendChild(dom.createTextNode(database.memberLocations[i]));
                f.appendChild(g);
                g = dom.createElement("name");
                g.appendChild(dom.createTextNode(database.memberNames[i]));
                f.appendChild(g);
                e.appendChild(f);
            }
            rootEle.appendChild(e);
            dom.appendChild(rootEle);
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(database.location)));
            return true;
        } catch(ParserConfigurationException | DOMException | TransformerFactoryConfigurationError | IllegalArgumentException | FileNotFoundException | TransformerException e) {
             ErrorManager.error("Failed to save the reconstructed database.", "XmlManager.writeReconstruction has found that an error occurred while attempting write to the XML file.", (Exception)e);
        }
        return false;
    }
    
    /**
     * Write statistics to an element.
     * @param dom Parent document.
     * @param stat Statistics to save.
     * @return Element ready for saving as an XML file.
     */
    private static Element writeStatsElements(Document dom, StatsObject stat) {
        Element root = null;
        try {
            root = dom.createElement("stats");
            Element f;
            Element e;
            e = dom.createElement("iter");
            e.appendChild(dom.createTextNode(stat.iterations));
            root.appendChild(e);
            e = dom.createElement("alpha");
            e.appendChild(dom.createTextNode(stat.alpha));
            root.appendChild(e);
             e = dom.createElement("stages");
            e.appendChild(dom.createTextNode(stat.stages));
            root.appendChild(e);
            e = dom.createElement("gg");
            e.appendChild(dom.createTextNode(stat.gg));
            root.appendChild(e);
            e = dom.createElement("ggPval");
            e.appendChild(dom.createTextNode(stat.ggPval));
            root.appendChild(e);
            e = dom.createElement("ggci");
            f = dom.createElement("low");
            f.appendChild(dom.createTextNode(stat.ggci[0]));
            e.appendChild(f);
            f = dom.createElement("high");
            f.appendChild(dom.createTextNode(stat.ggci[1]));
            e.appendChild(f);
            root.appendChild(e);
            e = dom.createElement("optim");
            e.appendChild(dom.createTextNode(stat.optim));
            root.appendChild(e);
            e = dom.createElement("xx");
            f = dom.createElement("stat");
            f.appendChild(dom.createTextNode(stat.xx));
            e.appendChild(f);
            f = dom.createElement("p");
            f.appendChild(dom.createTextNode(stat.xxPval));
            e.appendChild(f);
            f = dom.createElement("low");
            f.appendChild(dom.createTextNode(stat.xxLow));
            e.appendChild(f);
            f = dom.createElement("high");
            f.appendChild(dom.createTextNode(stat.xxHigh));
            e.appendChild(f);
            root.appendChild(e);
            for(int i = 0; i < stat.a.length; i++) {
                e = dom.createElement("aparam");
                f = dom.createElement("val");
                f.appendChild(dom.createTextNode(stat.a[i]));
                e.appendChild(f);
                if(stat.aLow.length >= i) {
                    f = dom.createElement("low");
                    f.appendChild(dom.createTextNode(stat.aLow[i]));
                    e.appendChild(f);
                }
                if(stat.aHigh.length >= i) {
                    f = dom.createElement("high");
                    f.appendChild(dom.createTextNode(stat.aHigh[i]));
                    e.appendChild(f);
                }
                root.appendChild(e);
            }
            e = dom.createElement("vparam");
            f = dom.createElement("val");
            f.appendChild(dom.createTextNode(stat.v));
            e.appendChild(f);
            f = dom.createElement("low");
            f.appendChild(dom.createTextNode(stat.vLow));
            e.appendChild(f);
            f = dom.createElement("high");
            f.appendChild(dom.createTextNode(stat.vHigh));
            e.appendChild(f);
            root.appendChild(e);
            for (String[] aStar : stat.aStar) {
                e = dom.createElement("aStar");
                for (String aStar1 : aStar) {
                    f = dom.createElement("val");
                    f.appendChild(dom.createTextNode(aStar1));
                    e.appendChild(f);
                }
                root.appendChild(e);
            }
            for (String star : stat.vStar) {
                e = dom.createElement("vStar");
                e.appendChild(dom.createTextNode(star));
                root.appendChild(e);
            }
            for (String star : stat.ggStar) {
                e = dom.createElement("ggStar");
                e.appendChild(dom.createTextNode(star));
                root.appendChild(e);
            }

        } catch(DOMException e) {
           ErrorManager.log("XmlManager.writeStatsElements has encountered an error.", e);
        }
        return root;
    }
}
