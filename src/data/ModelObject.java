package data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Holds information relevant to the output of a statistical model.
 * This class is used to enumerate all information about a generated model. In 
 * addition to the model elements themselves, it also retains pertinent 
 * information about the model and which data sets were used to make it.
 * @author Ian Yocum
 * @date 5/30/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @todo Expand modeling to take multiple data sets.
 */
public class ModelObject {
    public String aic; /*!< The model's Akaike Information Criterion. */
    public String date; /*!< Date of model's creation. */
    /**
     * @brief List of the elements in the model.
     * @details These are the values used to produce predictive graphs or run 
     * statistical operation.
     * @warning The ordering of the elements in the model must not be changed 
     * after the fact. If the elements are stored out of order the statistics 
     * will output garbage.
     */
    public String params[];
    public String logLikelihood; /*!< Stores the computed log likelihood of the model. */
    public String optim; /*!< The method of optimization. */
    public String OrganismDatabaseLocation; /*!< Location of the organism database used. */
    public String OrganismDatabaseName; /*!< Name of the organism database used. */
    public String OrganismFiles[]; /*!< List of organism files used. @note This does not have to include all possible files in the dataset. */
    public int paramCount; /*!< Number of parameters in the model (includes V as well as the A values). */
    public int organismCount; /*!< Number of organisms used in the model. */
    public String name; /*!< The model's name.*/
    public ArrayList<String> stageMap; /*!< Maps the stages on to each other. */
    public StatsObject stats; /*!< Holds the models statistical information if it  has been calculated. */
    public String time; /*!< Time of the model's creation. */
    public String weatherDatabaseLocation; /*!< Location of the weather database used.*/
    public String weatherDatabaseName; /*!< Name of weather database used. */ 

    /**
     * @brief Basic constructor to set all values to their defaults.
     */
    public ModelObject() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date d = new Date();
        name = "Default";
        params = null;
        OrganismFiles = null;
        date = dateFormat.format(d);
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        time = dateFormat.format(d);
        weatherDatabaseName = "Unknown";
        weatherDatabaseLocation= "Unknown";
        OrganismDatabaseName = "Unknown";
        OrganismDatabaseLocation ="Unknown";
        optim = "Nelder-Mead";
        paramCount = 0;
        organismCount = 0;
        logLikelihood = "";
        stageMap = new ArrayList<>();
        aic = "";
        stats = new StatsObject();
    }
}
