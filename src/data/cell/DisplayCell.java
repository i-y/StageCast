/**
 * Contains classes involved in the creation and management of a custom 
 * drag-drop control for stage data.
 * <p>This package can be considered part of the "View" portion of the 
 * MVC architecture as it provides a way to display and interact with data. The 
 * specific control implemented by this package is a drag-and-drop control which 
 * lets the user arrange stage data to their liking. Notably, this control 
 * allows multiple stages to share a single line in the list, and to be freely 
 * dragged in or out of this line without modifying the other cells on it. 
 * Stages sharing a single line are then collapsed into each other at model 
 * creation.</p>
 * <p>To accomplish this, the control is split up into three different portions. 
 * DisplayCell is the class which is fed directly to the ListView via a cell 
 * factory. Each DisplayCell holds one DisplayItem object. Each cell only 
 * contains a single DisplayItem, and these DisplayItems may hold an arbitrary 
 * number of DisplayInfo objects. The DisplayInfo object represents an 
 * individual stage and is moved from one DisplayInfo object to another one to 
 * represent being moved from one line to another.</p>
 * @note This implementation is probably not the correct way to do it using 
 * idiomatic Java/JavaFX. However, it was hacked together under a somewhat tight 
 * time constraint.
 */
package data.cell;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Provides a custom ListCell for use with a custom control.
 * This class extends the ListCell class to provide an interface between 
 * DisplayItem objects and a standard ListView. All of the event handling for 
 * the drag-drop control is implemented in this class. It should be noted, 
 * however, that the majority of the event handling is done on the individual 
 * stage items, of which each DisplayCell may hold an arbitrary number.
 * @author Ian Yocum
 * @date 3/7/2014
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class DisplayCell extends ListCell<DisplayItem> {
    private final int cellHeight = 27; /*!< The height in pixels of each cell. */
    
    /**
     * Sets some event handling and css properties.
     * This constructor defines the event handling needed to allow a cell to 
     * accept a stage being dropped on to it. It is here that the element is 
     * removed from the content list in the source DisplayItem object and added 
     * to the content list of the DisplayItem associated with this cell.
     */
    public DisplayCell() {
        getStyleClass().add("drag-display-cell");
        setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {
                Dragboard db = dragEvent.getDragboard();
                boolean success = false;
                if(db.hasString()) {
                    ObservableList<DisplayItem> inpt = getListView().getItems();
                    List<DisplayItem> list =  new CopyOnWriteArrayList<>();
                    list.addAll(inpt);
                    int indexCount = 0;
                    int sourceMajor = -1;
                    int sourceMinor = -1;
                    int targetMajor = list.indexOf(getItem());
                    for(DisplayItem item : list) {
                        int temp = item.has(db.getString());
                        if(temp >= 0) {
                            sourceMajor = indexCount;
                            sourceMinor = temp;
                        }
                        indexCount++;
                    }
                    // Remove and then reinsert items from the parent list to trigger the ListView to update.
                    DisplayItem sourceItem = list.get(sourceMajor);
                    getListView().getItems().remove(sourceMajor);
                    DisplayInfo sourceInfo = sourceItem.pop(sourceMinor);
                    getListView().getItems().add(sourceMajor, sourceItem);
                    DisplayItem targetItem = list.get(targetMajor);
                    getListView().getItems().remove(targetMajor);
                    targetItem.push(sourceInfo);
                    getListView().getItems().add(targetMajor, targetItem);
                    success = true;
                }
                dragEvent.setDropCompleted(success);
                dragEvent.consume();
            }
        });
    }
    
    /**
     * Creates and sets the graphic for display to the user.
     * This method iterates through the content list of the associated 
     * DisplayItem. For each DisplayInfo item found, a label is created with the 
     * appropriate name, assigned event handles, and added to an HBox. Once all 
     * DisplayInfo items have been added to the HBox, the HBox is set as the 
     * graphics for this cell. If there are no DisplayInfo items held by the 
     * associated DisplayItem, an empty HBox is created and displayed instead. 
     * By defining drag-and-drop event handling on labels it allows the user to 
     * choose and drag a single stage, no matter how many may be in either the 
     * source line or destination line.
     */
    private void build() {
        DisplayItem target = getItem();
        HBox graphic = new HBox();
        graphic.getStyleClass().add("drag-display-hbox");
        if(target.getContent().size() > 0) {
            for(DisplayInfo info : target.getContent()) {
                final Label label = new Label(info.name);
                label.setMaxWidth(Double.MAX_VALUE);
                label.setPrefHeight(cellHeight);
                HBox.setHgrow(label, Priority.ALWAYS);
                label.getStyleClass().add("drag-display-node");
                label.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(label.getText());
                        dragBoard.setContent(content);
                        event.consume();
                    }
                });
                label.setOnDragOver(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                        dragEvent.acceptTransferModes(TransferMode.MOVE);
                    }
                });
                label.setOnDragExited(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                        dragEvent.consume();
                    }
                });
                graphic.getChildren().add(label);
            }
        } else {
            Label label = new Label();
            label.setMaxWidth(Double.MAX_VALUE);
            label.setPrefHeight(cellHeight);
            HBox.setHgrow(label, Priority.ALWAYS);
            label.getStyleClass().add("drag-display-node-empty");
            label.setOnDragOver(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent dragEvent) {
                    dragEvent.acceptTransferModes(TransferMode.MOVE);
                    dragEvent.consume();
                }
            });
            label.setOnDragExited(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent dragEvent) {
                    dragEvent.acceptTransferModes(TransferMode.NONE);
                    dragEvent.consume();
                }
            });
            graphic.getChildren().add(label);
        }
        setGraphic(graphic);
    }
    
    /**
     * Implementation of the required update method.
     * @param item The DisplayItem associated with this cell.
     * @param empty Whether the cell is empty or not.
     */
    @Override
    protected void updateItem(DisplayItem item, boolean empty) {
        super.updateItem(item, empty);
        if(!empty) {
            setItem(item);
            build();
        } else {
            setItem(null);
            setGraphic(null);
        }
    }
    
}
