package data.cell;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the content of a single row.
 * This class is used to hold the DisplayInfo class and it is the class 
 * which forms the ObservableList passed to the ListView. Each instance of this 
 * class represents a single row of the ListView. The data held by the list is 
 * contained in the content List. This list may hold an arbitrary number of 
 * elements greater than or equal to zero. The list is modified by the 
 * DisplayCell associated with the DisplayItem to control the movement of stage 
 * information from one DisplayItem to another.
 * @author Ian Yocum
 * @date 3/7/2014
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class DisplayItem {
    private final List<DisplayInfo> content; /*!< All the stages in this row. */
    
    /**
     * Constructs a DisplayItem from the given value.
     * Each instance of the DisplayItem class represents one row of the table.
     * Each row can have an arbitrary number of stages within it, but it is
     * assumed that every row will start with exactly one stage.
     * @param first The stage for this DisplayItem to show on startup.
     */
    public DisplayItem(DisplayInfo first) {
        content = new ArrayList<>();
        content.add(first);
    }
    
    /**
     * Returns the stages being held by this DisplayItem.
     * @return The list of stages displayed on this row.
     */
    public List<DisplayInfo> getContent() {
        return content;
    }
    
    /**
     * Returns the index of a given stage.
     * This method takes a stage name and returns the index if it is the
     * content list.
     * @param target Name of the stage to search for.
     * @return The index of the stage or -1 if the stage is not present.
     */
    public int has(String target) {
        int index = 0;
        int ret = -1;
        for(DisplayInfo item : content) {
            if(item.name.equals(target)) {
                ret = index;
            }
            index++;
        }
        return ret;
    }
    
    /**
     * Removes an item from the content list.
     * @param target Index of the item to remove.
     * @return A copy of the item which has been removed from the list.
     */
    public DisplayInfo pop(int target) {
        DisplayInfo ret = content.get(target);
        content.remove(content.get(target));
        return ret;
    }
    
    /**
     * Adds an item to the content list.
     * @param inpt Item to add to the content list.
     */
    public void push(DisplayInfo inpt) {
        content.add(inpt);
    }
}
