/**
 * Contains classes involved with manipulating data and program logic.
 * This package corresponds with the "Control" portion of the MVC 
 * architecture. All the classes which perform significant operations on the 
 * data are held here. The classes in this package can largely be seen as 
 * utility classes which handle the work of loading, manipulating, and storing 
 * information. Through ScriptManager it also handles the work of passing 
 * information back and forth between the main program and the R scripts which 
 * support it. Additionally, the package includes the entry point for the 
 * program.
 * @note The various interfaces offered by these classes are all static.
 * @todo Possibly rename the package to better reflect its purpose.
 * @todo Re-engineer the system to be able to load and work with multi-year 
 * datasets.
 * @todo Write in-program help menus.
 * @todo As part of improving import capabilities, add support for attempting to 
 * auto-detect databases which exist in the default location but are not 
 * recorded in `settings.xml`
 * @todo Add multi-threading support.
 */
package stagecast;

import java.io.File;

import data.SettingsObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import windows.MainWindow;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The entry point for the application.
 * This class performs start up checks on the environment and then launches the 
 * application. At the moment the only environmental checks are on the existence 
 * of local directories but this could be changed to better validate the runtime 
 * environment.
 * @author Ian Yocum
 * @date 5/31/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class StageCast extends Application {
    private SettingsObject settings; /*!< The contents of the settings database loaded by setup() and passed to the application. */
	
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Sets up and validates initial runtime environment.
     * This method is used to establish the existence of certain local 
     * directories that the program uses during the course of its operation, as 
     * well as loads any previously-saved settings. It will create any missing 
     * folder.
     * @note Any future runtime checks should be added to this method.
     */
    private boolean setup() {
        boolean ret = false;
        String directories[] = {"./SavedData/Weather", "./SavedData/Organisms", "./SavedData/Figures", "./Raw/Weather", "./Raw/Organisms", "./Temp/Figures", "./Temp/Figures/Graph", "./Temp/Figures/Forecast"};
        for(String directory : directories) {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        File dir = new File("./Scripts");
        if (!dir.exists()) {
            dir.mkdirs();
            try {
                FileWriter saveFile;
                try (InputStream defStream = this.getClass().getResource("/etc/scripts/graph.r").openStream()) {
                    saveFile = new FileWriter("./Scripts/graph.r");
                    int c;
                    while ((c = defStream.read()) != -1) {
                        saveFile.write(c);
                    }
                }
                saveFile.close();
                try (InputStream defStream = this.getClass().getResource("/etc/scripts/model.r").openStream()) {
                    saveFile = new FileWriter("./Scripts/model.r");
                    int c;
                    while ((c = defStream.read()) != -1) {
                        saveFile.write(c);
                    }
                }
                saveFile.close();
                try (InputStream defStream = this.getClass().getResource("/etc/scripts/stats.r").openStream()) {
                    saveFile = new FileWriter("./Scripts/stats.r");
                    int c;
                    while ((c = defStream.read()) != -1) {
                        saveFile.write(c);
                    }
                }
                saveFile.close();
                try (InputStream defStream = this.getClass().getResource("/etc/scripts/forecast.r").openStream()) {
                    saveFile = new FileWriter("./Scripts/forecast.r");
                    int c;
                    while ((c = defStream.read()) != -1) {
                        saveFile.write(c);
                    }
                }
                saveFile.close();
            } catch (IOException ex) {
                Logger.getLogger(StageCast.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        settings = XmlManager.loadSettings();
        if(settings != null) {
            File file = new File(settings.defaultModelLocation);
            if(!file.exists()) {
                XmlManager.saveModels(settings.defaultModelLocation, null);
                settings.defaultModelLocation = new File(settings.defaultModelLocation).getAbsolutePath();
            }
            ret = true;
        } else {
            ErrorManager.error("Unable to start, failed to load settings","StageCast.setup could not initialize program environment.", null);
        }
        return ret;
    }
	
    /**
     * The true entry point into the program.
     * In addition to starting the program itself, this function will also run
     * any checks which need to be done before anything is shown to the user.
     * @param arg0
     * @throws Exception 
     */
    @Override
    public void start(Stage arg0) throws Exception {
        if(setup()) {
            MainWindow m = new MainWindow(settings);
            m.show();
        }
    }
	
}
