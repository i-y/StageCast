package data;

import java.io.File;
import java.util.ArrayList;

/**
 * Stores settings information.
 * This class enumerates all user-configurable options presently offered and is 
 * used in conjunction with an XML file to keep user preferences constant from 
 * session to session. In addition to user options, this class is used by the 
 * program to track internally relevant information, such as loaded datasets and 
 * their names. This is done partly as a convenience to the user but also to 
 * speed the program's access to these datasets.
 * @author Ian Yocum
 * @date 5/30/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class SettingsObject {
    public String defaultFigureLocation; /*!< Default location for the program to suggest saving figures. */
    public String defaultModelLocation; /*!< Location of the XML database listed all previously created models. */
    public String defaultOrganismLoad; /*!< Default location to look for organism databases to load. */
    public String defaultOrganismSave; /*!< Default location to try to save organism databases. */
    public String defaultTempForecastOutput;
    public String defaultTempGraphOutput; /*!< Default place for the R script to output its generated figures. Will be over-written on each subsequent graphing call. */
    public String defaultWeatherLoad; /*!< Default location to look for weather databases to load. */
    public String defaultWeatherSave; /*!< Default location to try to save weather databases. */
    public String loadedModel; /*!< Name of the currently loaded model. */
    public String loadedOrganism; /*!< Absolute address of currently loaded organism data set. */
    public String loadedWeather; /*!< Absolute address of currently loaded weather data set. */
    public ArrayList<String> organismLocations; /*!< List of all previously loaded organism data set locations. */
    public ArrayList<String> organismNames; /*!< Names of all previously loaded organism data sets. */
    public int organismCount; /*!< Number of all previously loaded organism data sets*/
    public boolean tooltips; /*!< Whether the program should show tooltips when the user hovers their mouse over a GUI element. */
    public ArrayList<String> weatherLocations;/*!< List of all previously loaded weather data set locations. */
    public ArrayList<String> weatherNames; /*!< Names of all previously loaded weather data sets. */
    public int weatherCount; /*!< Number of all previously loaded weather data sets*/
	
    /**
    * Default constructor.
    * Sets all values to their defaults. The default directories are guaranteed 
    * to exist by the program.
    * @note The default locations are relative whereas the rest of the program 
    * currently uses exact addresses. This might cause unexpected behavior under 
    * certain circumstances.
    */
    public SettingsObject() {
        File file = new File("SavedData/Figures");
        if(file.exists()) {
            defaultFigureLocation = file.getAbsolutePath();
        } else {
            defaultFigureLocation = "SavedData/Figures";
        }
        file = new File("Raw/Weather");
        if(file.exists()) {
            defaultWeatherLoad = file.getAbsolutePath();
        } else {
            defaultWeatherLoad = "Raw/Weather";
        }
        file = new File("Raw/Organisms");
        if(file.exists()) {
            defaultOrganismLoad = file.getAbsolutePath();
        } else {
            defaultOrganismLoad = "Raw/Organisms";
        }
        file = new File("SavedData/Organisms");
        if(file.exists()) {
            defaultOrganismSave = file.getAbsolutePath();
        } else {
            defaultOrganismSave = "SavedData/Organisms";
        }
        file = new File("SavedData/Weather");
        if(file.exists()) {
            defaultWeatherSave = file.getAbsolutePath();
        } else {
            defaultWeatherSave = "SavedData/Weather";
        }
        file = new File("Temp/Figures/Graph");
        if(file.exists()) {
            defaultTempGraphOutput = file.getAbsolutePath();
        } else {
            defaultTempGraphOutput = "Temp/Figures/Graph";
        }
        file = new File("Temp/Figures/Forecast");
        if(file.exists()) {
            defaultTempForecastOutput = file.getAbsolutePath();
        } else {
            defaultTempForecastOutput = "Temp/Figures/Forecast";
        }
        file = new File("models.xml");
        if(file.exists()) {
            defaultModelLocation = file.getAbsolutePath();
        } else {
            defaultModelLocation = "models.xml";
        }
        loadedOrganism = "";
        loadedWeather = "";
        loadedModel = "";
        organismCount = 0;
        weatherCount = 0;
        organismLocations = new ArrayList<>();
        organismNames = new ArrayList<>();
        tooltips = true;
        weatherLocations = new ArrayList<>();
        weatherNames = new ArrayList<>();
    }
}
