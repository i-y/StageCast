package data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Holds the contents of a file being imported.
 * This class is designed to allow a file containing a mix of organism data to 
 * be loaded in memory and manipulated by the program. Once loaded into an 
 * ImportationObject, the data can be parsed, formatted, and output as a 
 * database that's ready for use with the modeling. Even though it is expected 
 * that a single file may have multiple organisms, these organisms *must* have 
 * the same number of life stages. This class also holds weather data; however, 
 * the operations performed on that are less interesting as it assumes that only 
 * the information from one weather site for one year will be presented.
 * @author Ian Yocum
 * @date 5/30/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @todo Increase file format support.
 */
public class ImportationObject {
    /** 
     * Holds the character is used to separate data columns. 
     * Valid options consist of:
     *  - Space
     *  - tab
     *  - comma 
     */
    public String columnSeparator;
    /** 
     * Stores the organism data. 
     * Hash map where each entry is a string containing all phenology 
     * information from a single row of the import file, indexed by organism 
     * name. It only contains one index if it is a weather data file.
     */
    public HashMap<String, ArrayList<String>> data;
    public String datasetName; /*!< Name that the database will be saved under */
    /**
     * Holds the format used in the date.
     * Valid options are any combination of numeric dates separated by one of 
     * the valid date separator characters or the Julian date value.
     */
    public String dateFormat;
    /**
     * Holds the character that separates elements of a numeric date.
     * Valid options are:
     * - slash (/)
     * - dash (-)
     * - dot (.)
     */
    public String dateSeparator;
    /**
     * Holds the date information.
     * Hash map where each entry is a string containing the date information 
     * from a single row of the import file, indexed by organism name. It only 
     * contains one index if it is a weather data file.
     * @note The dates are currently being separated from the data at load time 
     * as it makes finding the date format easier under the current system. Data 
     * and Dates my be combined into a single table at a later time if it proves 
     * more convenient for further development.
     * @note Dates used in the database must be in Julian form. If the input 
     * file does not use Julian dating, the original dates must be converted to 
     * their Julian equivalents.
     */
    public HashMap<String, ArrayList<String>> dates;
    public String destination; /*!< Directory to save the database in */
    public String source; /*!< Location of data file being imported */
    public Boolean species; /*!< True = organism data file. False = weather data file */
    public int stages; /*!< Number of stages in the organisms being imported. 0 if it is a weather data set. */

    /**
     * Base constructor.
     * This constructor automatically sets the values of the variables to a 
     * default state. It is assumed that the program will replace these defaults 
     * with the information of a real file; however, the defaults would allow 
     * for the construction of a valid, if empty, database.
     */
    public ImportationObject() {
        columnSeparator = "\t";
        dateSeparator = "Julian";
        dateFormat = "Julian";
        datasetName = "default";
        destination = "";
        species = true;
        source = "";
        stages = 0;
    }
}
