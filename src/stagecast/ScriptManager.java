package stagecast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.ProcessBuilder.Redirect;

import data.DatabaseObject;
import data.GraphObject;
import data.ModelObject;
import data.StatsObject;
import data.ForecastObject;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Runs the R scripts.
 * This class is how the Java application is able to request that a model is 
 * created or graphed. The model data is output to the ./Temp directory and the 
 * graphs to the ./Temp/Figures directory. Further statistical operations will 
 * be added here as they are developed.
 * @author Ian Yocum
 * @date 5/31/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @todo Compile local copy of R to avoid depending on R being installed on the 
 * computer.
 * @todo Add configurable settings for R. Maybe the choice between using a 
 * pre-compiled binary or a locally installed version.
 * @todo Linux functionality needs to be checked.
 */
public class ScriptManager {	
    
    /**
     * Requests a forecast.
     * This method is largely still awaiting final specification of the forecast
     * tab.
     * @param settings All information needed by forecast.r
     * @return True if successful, false otherwise.
     */
    public static Boolean forecast(ForecastObject settings) {
        boolean ret = false;
        if(settings.model.stats.stages.equals("0")) {
            ErrorManager.warn("Could not make a forecast.", "The model does not seem to have statistics yet. Go to the \"Model\" tab and run statistics for this model and then try again.");
        } else {
            try {
                try (BufferedWriter outpt = new BufferedWriter(new FileWriter("Temp/forecastInput.txt"))) {
                    outpt.write("tag\tdata\r\n");
                    outpt.write("weather\t" + settings.weather + "\r\n");

                    for(int i = 0; i < settings.model.paramCount; i++) {
                        outpt.write("params\t" + settings.model.params[i] + "\r\n");
                    }
                    if(settings.stageNames != null) {
                        for (String stageName : settings.stageNames) {
                            outpt.write("stage\t" + stageName + "\r\n");
                        }
                    }
                    for(String map : settings.model.stageMap) {
                        outpt.write("stageMap\t" + map + "\r\n");
                    }
                    if(settings.error) {
                        outpt.write("error\t1\r\n");
                    } else {
                        outpt.write("error\t0\r\n");
                    }
                    if(settings.fit) {
                        outpt.write("fit\t1\r\n");
                    } else {
                        outpt.write("fit\t0\r\n");
                    }
                    if(settings.fitAlone) {
                        outpt.write("fitAlone\t1\r\n");
                    } else {
                        outpt.write("fitAlone\t0\r\n");
                    }
                    outpt.write("fitHigh\t" + settings.fitHigh + "\r\n");
                    outpt.write("fitLow\t" + settings.fitLow + "\r\n");
                    outpt.write("height\t" + settings.heigth + "\r\n");
                    outpt.write("saveLoc\t" + (new File(settings.outputLoc).getAbsolutePath()) + "\r\n");
                    if(settings.predict) {
                        outpt.write("predict\t1\r\n");
                    } else {
                        outpt.write("predict\t0\r\n");
                    }
                    outpt.write("predictHigh\t" + settings.predictHigh + "\r\n");
                    outpt.write("predictLow\t" + settings.predictLow + "\r\n");
                    outpt.write("proportionPercent\t" + settings.proportionPercent + "\r\n");
                    outpt.write("proportionStage\t" + settings.proportionStage + "\r\n");
                    if(settings.tendencies) {
                        outpt.write("tendencies\t1\r\n");
                    } else {
                        outpt.write("tendencies\t0\r\n");
                    }
                    outpt.write("width\t" + settings.width + "\r\n");
                    StatsObject stat = settings.model.stats;
                    if(stat != null) {
                        if(stat.aStar != null) {
                            for(String[] s : stat.aStar) {
                                for(String t : s) {
                                    outpt.write("astar\t" + t + "\r\n");
                                }
                            }
                            for(String s : stat.vStar) {
                                outpt.write("vstar\t" + s + "\r\n");
                            }
                        }
                    }
                }
                String path = (new File("Scripts/forecast.r")).getAbsolutePath();
                String path2 = (new File("Temp/forecastInput.txt")).getAbsolutePath();
                String[] cmd = {"","",""};
                if(System.getProperty("os.name").startsWith("Linux")) {
                    String command = "R --vanilla <" + path + " >/dev/null --args " + path2;
                    cmd[0] = "bash";
                    cmd[1] = "-c";
                    cmd[2] = command;
                } else if(System.getProperty("os.name").startsWith("Windows"))  {
                    String command = "R --vanilla <" + path + " > nul 2> nul  --args " + path2;
                    cmd[0] = "cmd";
                    cmd[1] = "/c";
                    cmd[2] = command;
                }
                Process p = new ProcessBuilder(cmd).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
                p.waitFor();
                ret = true;
            } catch(IOException | InterruptedException e) {
                ErrorManager.error("Forecast creation failed.","ScriptManager.forecast has encountered an error.", e);
            }
        }
        return ret;
    }
    
    /**
     * Requests a graph of a model.
     * This method is the interface point between graphing requests from the GUI 
     * and the graphing scripts. Only takes a model argument as a ModelObject 
     * has all the information needed to produce a graph.
     * @param weather String containing the absolute path to the weather file.
     * @param organisms String array holding the locations of the organism files.
     * @param model ModelObject containing the model and related information 
     * necessary for graphing.
     * @param settings GraphObject which holds the graphing options chosen by 
     * the user.
     * @return False if there was an error in the Java code while calling the 
     * script, true otherwise.
     */
    public static Boolean graph(String weather, String[] organisms, ModelObject model, GraphObject settings) {
        boolean ret = false;
        try {
            try (BufferedWriter outpt = new BufferedWriter(new FileWriter("Temp/graphInput.txt"))) {
                outpt.write("tag\tdata\r\n");
                outpt.write("weather\t" + weather + "\r\n");
                for (String OrganismFile : organisms) {
                    outpt.write("species\t" + OrganismFile + "\r\n");
                }
                if(settings.organismStageNames != null) {
                    for (String OrganismStageName : settings.organismStageNames) {
                        outpt.write("stage\t" + OrganismStageName + "\r\n");
                    }
                }
                outpt.write("saveLoc\t" + (new File(settings.outputLoc).getAbsolutePath()) + "\r\n");
                for(int i = 0; i < model.paramCount; i++) {
                    outpt.write("params\t" + model.params[i] + "\r\n");
                }
                outpt.write("logLike\t" + model.logLikelihood + "\r\n");
                for (String map : model.stageMap) {
                    outpt.write("stageMap\t" + map + "\r\n");
                }
                if(settings.log3d) {
                    outpt.write("log3d\t1\r\n");
                } else {
                    outpt.write("log3d\t0\r\n");
                }
                if(settings.log2d) {
                    outpt.write("log2d\t1\r\n");
                } else {
                    outpt.write("log2d\t0\r\n");
                }
                if(settings.expProp) {
                    outpt.write("expProp\t1\r\n");
                } else {
                    outpt.write("expProp\t0\r\n");
                }
                if(settings.compRaw) {
                    outpt.write("compRaw\t1\r\n");
                } else {
                    outpt.write("compRaw\t0\r\n");
                }
                if(settings.allTogether) {
                    outpt.write("together\t1\r\n");
                } else {
                    outpt.write("together\t0\r\n");
                }
                if(settings.combined) {
                    outpt.write("combined\t1\r\n");
                } else {
                    outpt.write("combined\t0\r\n");
                }
                outpt.write("log2dinterval\t" + settings.log2dinterval + "\r\n");
                outpt.write("log2doffset\t" + settings.log2doffset + "\r\n");
                outpt.write("log2dsample\t" + settings.log2dsample + "\r\n");
                outpt.write("logPdf3DShigh\t" + settings.logPdf3DShigh + "\r\n");
                outpt.write("logPdf3DSlow\t" + settings.logPdf3DSlow + "\r\n");
                outpt.write("logPdf3DThigh\t" + settings.logPdf3DThigh + "\r\n");
                outpt.write("logPdf3DTlow\t" + settings.logPdf3DTlow + "\r\n");
                outpt.write("expProphigh\t" + settings.expProphigh + "\r\n");
                outpt.write("expProplow\t" + settings.expProplow + "\r\n");
                outpt.write("compRawhigh\t" + settings.compRawhigh + "\r\n");
                outpt.write("compRawlow\t" + settings.compRawlow + "\r\n");
                outpt.write("combHigh\t" + settings.combHigh + "\r\n");
                outpt.write("combLow\t" + settings.combLow + "\r\n");
                outpt.write("width\t" + settings.imgWidth + "\r\n");
                outpt.write("height\t" + settings.imgHeight + "\r\n");
            }
            String path = (new File("Scripts/graph.r")).getAbsolutePath();
            String path2 = (new File("Temp/graphInput.txt")).getAbsolutePath();
            String[] cmd = {"","",""};
            if(System.getProperty("os.name").startsWith("Linux")) {
                String command = "R --vanilla <" + path + " >/dev/null --args " + path2;
                cmd[0] = "bash";
                cmd[1] = "-c";
                cmd[2] = command;
            } else if(System.getProperty("os.name").startsWith("Windows"))  {
                String command = "R --vanilla <" + path + " > nul 2> nul  --args " + path2;
                cmd[0] = "cmd";
                cmd[1] = "/c";
                cmd[2] = command;
            }
            Process p = new ProcessBuilder(cmd).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
            p.waitFor();
            ret = true;
        } catch(IOException | InterruptedException e) {
            ErrorManager.error("Graph creation failed.","ScriptManager.graph has encountered an error.", e);
        }
        return ret;
    }
    
    /**
     * Calls the R scripts involved in producing a model.
     * This method is the interface point between requests for model creation 
     * coming from the GUI and the external scripts that do the actual 
     * calculations. The method communicates with the R script by passing it a 
     * labeled, two-column table. The first column states what kind of 
     * information is stored by the line (weather, species, save target) and the 
     * second column stores the actual values. The R script outputs the 
     * calculated values as an XML file that can be read in directly through 
     * XmlManager.loadModels. Makes use of the ./Temp directory to store 
     * information in a format that R can understand.
     * @param inpt ModelObject containing the model and related information 
     * necessary for modeling.
     * @return The created model, if any.
     */
    public static ModelObject model(ModelObject inpt) {
        ModelObject ret = null;
        try {
            try (BufferedWriter outpt = new BufferedWriter(new FileWriter("Temp/modelInput.txt"))) {
                outpt.write("tag\tdata\r\n");
                outpt.write("weather\t" + inpt.weatherDatabaseLocation + "\r\n");
                for (String organismFile : inpt.OrganismFiles) {
                    outpt.write("species\t" + organismFile + "\r\n");
                }
                for (String map : inpt.stageMap) {
                    outpt.write("stageMap\t" + map + "\r\n");
                }
                outpt.write("saveLoc\t" + (new File("Temp").getAbsolutePath()) + "\r\n");
                outpt.write("optim\t" + inpt.optim + "\r\n");
                if(inpt.params != null) {
                    for(String s : inpt.params) {
                        outpt.write("par\t" + s + "\r\n");
                    }
                }
            }
            String path = (new File("Scripts/model.r")).getAbsolutePath();
            String path2 = (new File("Temp/modelInput.txt")).getAbsolutePath();
            String path3 = (new File("Temp/model.txt")).getAbsolutePath();
            String[] cmd = {"","",""};
            if(System.getProperty("os.name").startsWith("Linux")) {
                String command = "R --vanilla <" + path + " >/dev/null --args " + path2 + " " + path3; 
                cmd[0] = "bash";
                cmd[1] = "-c";
                cmd[2] = command;
            } else if(System.getProperty("os.name").startsWith("Windows"))  {
                String command = "R --vanilla <" + path + " > nul 2> nul --args " + path2 + " " + path3;
                cmd[0] = "cmd";
                cmd[1] = "/c";
                cmd[2] = command;
            }
            Process p = new ProcessBuilder(cmd).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
            p.waitFor();
            File modelFile = new File("Temp/modelOutput.xml");
            if(!modelFile.exists()) {
                ErrorManager.error("Model creation failed", "The script to create the model was run but no output could be found even though no error messages were recieved.", null);
            } else {
                ArrayList<ModelObject> mo = XmlManager.loadModels("Temp/modelOutput.xml");
                if(mo.size() < 1) {
                    ErrorManager.error("Model creation failed", "The script to create the model was run by ScriptManager.model but no model data was found in the output file.", null);
                } else {
                    ret = mo.get(0);
                }
            }
        } catch(IOException | InterruptedException | NumberFormatException e) {
            ErrorManager.error("Model creation failed.","ScriptManager.model has encounered an error.", e);
        }
        return ret;
    }
    
    /**
     * Calls the R script which performs further statistical operations on the 
     * dataset and model.
     * This method is the interface point between the GUI and the statistics 
     * script. This method calls a script, `stats.r` which takes in an organism 
     * dataset, a weather dataset, and a model and performs a variety of 
     * statistical calculations on them. The script outputs the results using an 
     * XML file described by XmlManager.loadStats.
     * @param organism Organism dataset to use in the calculations.
     * @param weather Weather dataset to use in the calculations.
     * @param params StatsObject containing the information necessary for 
     * calculations, including the model parameters and user settings.
     */
    public static void stats(DatabaseObject organism, DatabaseObject weather, StatsObject params) {
        try {
            try (BufferedWriter outpt = new BufferedWriter(new FileWriter("Temp/statsInput.txt"))) {
                outpt.write("tag\tdata\r\n");
                outpt.write("weather\t" + weather.memberLocations[0] + "\r\n");
                for (String OrganismFile : organism.memberLocations) {
                    outpt.write("species\t" + OrganismFile + "\r\n");
                }
                for (String map : params.stageMap) {
                    outpt.write("stageMap\t" + map + "\r\n");
                }
                outpt.write("saveLoc\t" + (new File("Temp").getAbsolutePath()) + "\r\n");
                outpt.write("iter\t" + params.iterations + "\r\n");
                outpt.write("alpha\t" + params.alpha + "\r\n");
                for(String param : params.a) {
                    outpt.write("params\t" + param + "\r\n");
                }
                outpt.write("AvalMat\t" + 1 + "\r\n");
                outpt.write("Astar\t" + 1 + "\r\n");
                outpt.write("Vstar\t" + 1 + "\r\n");
                outpt.write("GGstar\t" + 1 + "\r\n");
                outpt.write("optim\t" + params.optim + "\r\n");
            }
            String path = (new File("Scripts/stats.r")).getAbsolutePath();
            String path2 = (new File("Temp/statsInput.txt")).getAbsolutePath();
            String[] cmd = {"","",""};
            if(System.getProperty("os.name").startsWith("Linux")) {
                String command = "R --vanilla <" + path + " >/dev/null --args " + path2; 
                cmd[0] = "bash";
                cmd[1] = "-c";
                cmd[2] = command;
            } else if(System.getProperty("os.name").startsWith("Windows"))  {
                String command = "R --vanilla <" + path + " > nul 2> nul --args " + path2;
                cmd[0] = "cmd";
                cmd[1] = "/c";
                cmd[2] = command;
            }
            Process p = new ProcessBuilder(cmd).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start();
            p.waitFor();
       } catch (IOException | InterruptedException e) {
            ErrorManager.error("Statistical creation failed.","ScriptManager.stats has encounered an error.", e);
        }
    }
}
