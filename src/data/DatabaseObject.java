/**
 * Contains classes which represent the data used by the program.
 * This package corresponds with the "Model" portion of the MVC architecture. 
 * All the data held by the program and all the data passed back and forth 
 * between the scripts and the main program are held in this package. The 
 * classes here exist only to organize information in the form of their
 * publicly-accessible variables. These classes do not contain internal logic 
 * outside of what is used to set their default values in their constructors.
 * @todo Add option to choose between relative and absolute positioning for file 
 * locations.
 */
package data;

/**
 * Holds database information.
 * This object holds information describing the database as a whole. In 
 * particular, it holds the names of all organism or weather files within the 
 * database as well as the absolute location of those files on disk.
 * @author Ian Yocum
 * @date 5/30/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class DatabaseObject {
    public String date; /*!< Date database was created. This will be reset if the database file needs to be reconstructed. */
    public String location; /*!< Location of the *name*.about.txt file on the hard drive. */
    public String memberLocations[]; /*!< Stores the locations of each organisms file in the database. It is empty if the object describes a weather database. */
    public String memberNames[]; /*!< Stores the names of the organisms in the database. It is empty if the object describes a weather database.*/
    public String memberStages[]; /*!< List of the names used for the stages in the organism.*/
    public String name; /*!< Name of the database. */
    public int size; /*!< How many organisms are in the database. This is 0 if the object describes a weather database. */
    public int stages; /*!< Number of stages expressed by the organisms in the database. */
    public String time; /*!< Time the database was created. This will reset if the database file needs to be reconstructed. */
    public String type; /*!< Type of the database. Either organism or weather. */
	
   /**
    * 
    */
    public DatabaseObject() {
        date = "Unknown";
        memberLocations = null;
        memberNames = null;
        name = "Unknown";
        stages = 0;
        size = 0;
        time = "Unknown";
        type = "Unknown";
        location = "Unknown";
    }
}
