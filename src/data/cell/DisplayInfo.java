package data.cell;

/**
 * Holds the data about a single stage.
 * This class represents the data being displayed and modified by the custom 
 * drag-and-drop control. Each item is a single stage of a given organism's 
 * developmental cycle. The other two classes in this package exist only to 
 * display the data held by this class.
 * @author Ian Yocum
 * @date 3/7/2014
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class DisplayInfo {
    public String name; /*!< The name of this stage. */
    
    /**
     * Sets the information to be stored by this object.
     * @param inpt The name of this stage.
     */
    public DisplayInfo(String inpt) {
        name = inpt;
    }
}
