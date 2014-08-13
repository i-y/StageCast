package windows;

import data.ImportationObject;
import data.SettingsObject;
import stagecast.ErrorManager;

import java.io.File;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * Displays the import window to the user.
 * @details This class shows the user the import window. From this window, the
 * user is able to manage the process of importing a data file into the program.
 * The program has a fairly reliable method of automatically importing the files
 * but it is not error-proof. Therefore, this window provides an opportunity to
 * manually adjust the options used before the final importing process is
 * carried out.
 * @author Ian Yocum
 * @date 6/5/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class ImportWindow {
    private ImportationObject importer; /*!< The ImportationObject used to managed the process of importing a data file. */
    private SettingsObject settings; /*!< The SettingsObject containing information which informs the program of default file locations (load and save). */
    private Stage stage; /*!< The class which describes an FXML window. This must be non-null before the window is shown. */
    private Boolean submitFlag; /*!< Flag used to indicate if the user has made changes to the information. If so, a manual request for reloading will be sent. */
    
    public ComboBox<String> columnSep; /*!< Lists the allowed column separation characters.  */
    public ComboBox<String> dateSep; /*!< Lists the allowed date separation characters. */
    public ComboBox<String> dateScheme; /*!< Lists the valid date scheme options. */
    public TextField locationTxt; /*!< Location to save the resultant dataset. */
    public TextField nameTxt; /*!< Name of the resultant dataset. */
	
    /**
     * Constructor for the window.
     * @param io ImportationObject which is the result from the attempt to 
     * automatically load the input file.
     * @param so SettingsObject which contains information on what the default 
     * directories to load or save from are.
     */
    public ImportWindow(ImportationObject io, SettingsObject so) {
        try {
            importer = io;
            settings = so;
            submitFlag = false;
        } catch(Exception e) {
            ErrorManager.log("Error initializing ImportWindow", e);
        }
    }
	
    /**
     * Closes the window.
     */
    public void close() {
        stage.close();
    }
	
    /**
     * Chooses a non-default directory to save the database in.
     */
    public void newDest() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Directory to Save in");
        if(importer.species) {
            dc.setInitialDirectory(new File(settings.defaultOrganismSave));
        } else {
            dc.setInitialDirectory(new File(settings.defaultWeatherSave));
        }
        File outpt = dc.showDialog(stage);
        if(outpt != null) {
            locationTxt.setText(outpt.getAbsolutePath());
        }
    }
	
    /**
     * Updates the data stored in the ImportationObject after the user has 
     * modified it from its automatically-generated state.
     */
    private void save() {
        String newColSep = columnSep.getValue();
        String newDateSep = dateSep.getValue();
        switch (newColSep) {
            case "space":
                importer.columnSeparator = " ";
                break;
            case "comma (,)":
                importer.columnSeparator = ",";
                break;
            case "tab":
                importer.columnSeparator = "\t";
                break;
        }
        switch (newDateSep) {
            case "period (.)":
                importer.dateSeparator = "\\.";
                break;
            case "dash (-)":
                importer.dateSeparator = "-";
                break;
            case "slash (/)":
                importer.dateSeparator = "/";
                break;
            case "Julian":
                importer.dateSeparator = "Julian";
                break;
        }
        importer.dateFormat = dateScheme.getValue();
        importer.datasetName = nameTxt.getText();
        importer.destination = locationTxt.getText();
    }
	
    /**
     * Reads the automatically generated settings from the ImportationObject and 
     * populates the GUI to reflect those settings.
     */
    private void setup() {
        nameTxt.setText(importer.datasetName);
        if(importer.species) {
            locationTxt.setText(settings.defaultOrganismSave);
            importer.destination = settings.defaultOrganismSave;
        } else {
            locationTxt.setText(settings.defaultWeatherSave);
            importer.destination = settings.defaultWeatherSave;
        }
        switch (importer.columnSeparator) {
            case " ":
                columnSep.setValue("space");
                break;
            case ",":
                columnSep.setValue("comma (,)");
                break;
            case "\t":
                columnSep.setValue("tab");
                break;
        }
        switch (importer.dateSeparator) {
            case "\\.":
                dateSep.setValue("period (.)");
                break;
            case "-":
                dateSep.setValue("dash (-)");
                break;
            case "/":
                dateSep.setValue("slash (/)");
                break;
            case "Julian":
                dateSep.setValue("Julian");
                break;
        }
        dateScheme.setValue(importer.dateFormat);
        submitFlag = false;
    }
    
    /**
     * Adds tooltips to GUI elements.
     */
    private void setupTooltips() {
        columnSep.setTooltip(new Tooltip("Character used to separate the columns in the data file."));
        dateScheme.setTooltip(new Tooltip("The format used by the date."));
        dateSep.setTooltip(new Tooltip("Character used to separate the elements of the date."));
        locationTxt.setTooltip(new Tooltip("Directory to save the created dataset."));
        nameTxt.setTooltip(new Tooltip("Name to be given to the created dataset."));
    }
	
    /**
     * Initializes the window in perpetration for showing it to the user.
     */
    private void setupWindow() {
        stage = null;
        try {
            FXMLLoader fl = new FXMLLoader(getClass().getResource("ImportWindow.fxml"));
            fl.setController(this);
            fl.load();
            Parent root = fl.getRoot();
            stage = new Stage();
            columnSep.setItems(FXCollections.observableArrayList("comma (,)", "space","tab"));
            dateSep.setItems(FXCollections.observableArrayList("slash (/)", "period (.)","dash (-)", "Julian"));
            dateScheme.setItems(FXCollections.observableArrayList("dd/mm/yyyy","mm/dd/yyyy","yyyy/dd/mm","yyyy/mm/dd","Julian"));
            if (importer.species) {
                stage.setTitle("Organism Data Importer");
            } else {
                stage.setTitle("Weather Data Importer");
            }
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            ErrorManager.log("Error setting up ImportWindow", e);
        }
    }
     
    /**
     * Shows the window to the user.
     * @return ImportationObject holding the data imported by the user.
     */
    public ImportationObject show() {
        ImportationObject ret = null;
        setupWindow();
        if(settings.tooltips) {
            setupTooltips();
        }
        if((importer != null)&&(stage != null)) {
            setup();
            stage.showAndWait();
            if(submitFlag){
                save();
                ret = importer;
            }
        }
        return ret;
    }
	
    /**
     * Indicates that the the user wishes to go ahead and import the data file 
     * using the currently selected options.
     */
    public void submit() {
        submitFlag = true;
        close();
    }
}
