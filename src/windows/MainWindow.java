/**
 * Contains classes involved in the creation and presentation of GUI elements to 
 * the user.
 * This package corresponds with the "View" portion of the MVC architecture. As 
 * the program implements its GUI in JavaFX, the only classes in this package 
 * are those which control `.fxml` files. From the user's perspective, the 
 * classes in this package form the core of the program. Data is pulled in from 
 * the @ref data package and is manipulated using classes from the @ref main 
 * package. The logic contained within these classes is limited to what is 
 * necessary to open the window, provide the current information to the user, 
 * and respond appropriately to user input.
 * @note Both the Java controller code and the FXML code are contained within 
 * the package but only the Java code is documented here.
 */
package windows;

import stagecast.Importer;
import stagecast.ScriptManager;
import data.DatabaseObject;
import data.GraphObject;
import data.ImportationObject;
import data.ModelObject;
import data.SettingsObject;
import data.StatsObject;
import data.ForecastObject;
import stagecast.ErrorManager;
import stagecast.XmlManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javax.imageio.ImageIO;

/**
 * Provides the primary screen of the program's GUI.
 * <p>This class creates and manages the main GUI screen and serves as the 
 * central point for user control of the program. The main screen provides 
 * access to all primary functions of the program through a tabbed interface. 
 * This system was adopted to expose as much functionality to the user as 
 * possible within one to two mouse-clicks. All other screens in the program are 
 * windows designed to interact with the user and help them manage the data 
 * which is displayed on the main screen. This class is the controller class for 
 * the GUI file MainWindow.fxml.</p>
 * <p>Because it is an fxml controller class it is driven almost entirely by 
 * inputs made by the user in the main screen. The functions setupWindow(), 
 * setupCellFactories(), and setupListeners() are called once at the start of 
 * the program to initialize the window and its components. The collections of 
 * functions inside updateWindow() and populateStageNames() serve to update the 
 * state of the GUI elements to reflect the current state of the stored or 
 * loaded data. These functions are called many times, first at start up and 
 * then each time a change is made to the underlying data which would require a 
 * refresh of the GUI.</p>
 * <p>The rest of the functions in the program are publicly exposed so they can 
 * be called from the fxml window this class manages. These public functions are 
 * not called from within the program itself, only through the external window. 
 * The functions will update the private data members as appropriate and in this 
 * way communicate with the rest of the program. Although large amounts of logic 
 * pertaining to the access and display of data are included in this file, the 
 * rest of the work necessary is accomplished through direct user input and the 
 * capabilities provided by @ref main "main".</p>
 * @author Ian Yocum
 * @date 6/5/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @todo Output some kind of statistics report, maybe in .pdf form.
 * @todo Option to save all graphs at once.
 * @todo Add some sort of pop-up to show the user that the program is working on 
 * things and hasn't frozen. Maybe with a spinning ball or some other 
 * meaningless icon that gives the illusion of progress.
 */
public class MainWindow {
    /**
     * @brief Class used to populate the parameter table in the statistics 
     * window.
     * @details This class represents the data to be stored and displayed by 
     * statsParamTbl. It holds high, low, and standard parameter values. The 
     * standard parameter value is found during model creation and the high and 
     * low values are estimated during the process of calculating statistics. 
     * This class is adapted directly from the canonical example provided by 
     * Oracle.
     */
    protected class ParameterTable {
        private StringProperty param;
        public void setParam(String value) { paramProperty().set(value); }
        public String getParam() { return paramProperty().get(); }
        public StringProperty paramProperty() {
            if (param == null) {
                param = new SimpleStringProperty(this, "param");
            }
            return param;
        }
        private StringProperty paramLow;
        public void setParamLow(String value) { paramLowProperty().set(value); }
        public String getParamLow() { return paramLowProperty().get(); }
        public StringProperty paramLowProperty() {
            if (paramLow == null) {
                paramLow = new SimpleStringProperty(this, "param");
            }
            return paramLow;
        }
        private StringProperty paramHigh;
        public void setParamHigh(String value) { paramHighProperty().set(value); }
        public String getParamHigh() { return paramHighProperty().get(); }
        public StringProperty paramHighProperty() {
            if (paramHigh == null) {
                paramHigh = new SimpleStringProperty(this, "param");
            }
            return paramHigh;
        }
    }
    
    private int currentFigure; /*!< Index to be used with figures to show the currently selected figure. */
    private int figureCount; /*!< Current number of figures loaded by the program. */
    private Image figures[]; /*!< Array of created images. */
    private int forecastCurrentFigure; /*!< Index to be used with forecasting figures to show the currently selected forecasting figure. */
    private int forecastFiguresCount; /*!< Current number of forecasting figures loaded by the program. */
    private Image forecastFigures[]; /*!< Array of created forecasting images. */
    private WebHistory history; /*!< Tracks the user's history while browsing the help tab. */
    private int loadedModel; /*!< Index value of the currently loaded model. */
    private ArrayList<ModelObject> models; /*!< Array that stores all previously created models. */
    private List<ParameterTable> paramTblList; /*!< Constructs the list which holds the actual data inside of tableParams. */
    private SettingsObject settings; /*!< Record of the current program settings. */
    private Stage stage; /*!< The class which describes an FXML window. This must be non-null before the window is shown. */
    private ObservableList<ParameterTable> tableParams; /*!< Holds the data to be displayed by statsParamTbl. */
    
    public TextField aicTxt; /*!< Holds the Akaike Information Criterion for the current model. */
    public RadioButton allGraphsrbtn; /*!< Whether the per-stage comparative graphs should all be in one figure or their own independent figures. */
    public TextField alphaTxt; /*!< The alpha value to use when calculating statistics. */
    public CheckBox combChartCheckBx; /*!< Whether to produce a graph of the predicted model lines with the actual data points charted on them. */
    public TextField combChartHighTxt; /*!< The high range of the predicted model line graph. */
    public TextField combChartLowTxt; /*!< The low range of the predicted model line graph. */
    public CheckBox compRawCheckBx;  /*!< Whether to produce a graph of the predicted model lines separated by stage with the actual data points charted on them. */
    public TextField compRawHighTxt; /*!< The high range of the comparative graph. */
    public TextField compRawLowTxt; /*!< The low range of the comparative graph. */
    public CheckBox expPropCheckBx; /*!< Whether to graph the predicted model lines. */
    public TextField expPropHighTxt; /*!< The high range for the prediction graph. */
    public TextField expPropLowTxt; /*!< The low range for the prediction graph. */
    public TextField figureSaveTxt; /*!< The default place to save generated figures. */
    public ImageView figureView; /*!< Displays the generated figures. */
    public CheckBox forecastErrorCheckBx; /*!< Show error bars for the prediction figure. */
    public CheckBox forecastFitCheckBx; /*!< Goodness-Of-Fit tests. */
    public TextField forecastFitHighTxtBx; /*!< High range for fitness test. */
    public TextField forecastFitLowTxtBx; /*!< Low range for fitness graph. */
    public TextField forecastHeightTxt; /*!< Height of generated forecast figures. */
    public ComboBox<String> forecastModelCmbBx; /*!< Model to use for forecasting. */
    public RadioButton forecastOneFigureStageBtn; /*!< To use one figure per stage in the fitness test. */
    public TextField forecastPercentTxt; /*!< Percent to use as the prediction target. */
    public TextField forecastPredictHigh; /*!< High range for prediction figure. */
    public TextField forecastPredictLow; /*!< Low range for prediction figure. */
    public CheckBox forecastPredictionCheckBx; /*!< Predict when the population will be in a certain stage. */
    public TextField forecastStageTxt; /*!< Stage to use in prediction figure. */
    public CheckBox forecastTendenciesCheckBx; /*!< Measures Of Central Tendency */
    public ImageView forecastView; /*!< Shows forecast figures. */
    public ComboBox<String> forecastWeatherCmbBx; /*!< Weather dataset to use with forecasting. */
    public TextField forecastWidthTxt; /*!< Width of generated forecast figures. */
    public TextField ggTxt; /*!< The G-squared value of the statistical operation. */
    public TextField ggciHighTxt; /*!< The high value for the G-squared value. */
    public TextField ggciLowTxt; /*!< The low value for the G-squared value. */
    public TextField ggpTxt; /*!< The p-value of the G-squared value of the statistical operation. */
    public Button graphBtn; /*!< Generate selected figures. */
    public Button helpBackBtn; /*!< Load the last page in helpWebView (if any). */
    public Button helpFwdBtn; /*!< Load the next page in helpWebView (if any). */
    public WebView helpWebView; /*!< The program's help menu content. Stored as html. */
    public TextField imageHeightTxt; /*!< The height for the output images. */
    public TextField imageWidthTxt; /*!< The width for the output images. */
    public TextField iterTxt; /*!< Number of iterations to use when calculating statistics. */
    public Button loadOrgBtn; /*!< Load a previously created organism dataset. */
    public Button loadWeatherBtn; /*!< Load a previously created weather dataset. */
    public CheckBox log2dCheckBx; /*!< Whether to produce a 2d logarithmic graph. */
    public TextField log2dIntervalTxt; /*!< How far each sample should be from each other in degree days. */
    public TextField log2dOffsetTxt; /*!< Where the first sample should be taken. */
    public TextField log2dSampleTxt; /*!< Number of samples (peaks) to graph. */
    public CheckBox log3dCheckBx; /*!< Whether to produce a 3d logarithmic graph. */
    public TextField log3dSHighTxt; /*!< The high range of the s-axis on the 3d logarithmic graph. */
    public TextField log3dSLowTxt; /*!< The low range of the s-axis on the 3d logarithmic graph. */ 
    public TextField log3dTHighTxt; /*!< The high range of the t-axis on the 3d logarithmic graph. */
    public TextField log3dTLowTxt; /*!< The low range of the t-axis on the 3d logarithmic graph. */
    public TextField logTxt; /*!< The log likelihood of the model.*/
    public TextField membersTxt; /*!< States the number of different organisms which are part of the currently loaded organism dataset. */
    public ComboBox<String> modelGraphCombBx; /*!< Lists all models available for graphing. */
    public ComboBox<String> modelListBx; /*!< Lists all loaded models. */
    public ListView<String> modelOrganismList; /*!< Lists the organisms used to create the currently loaded model. */
    public TextField modelOrganismTxt; /*!< The organism dataset which was used to create the current model. */
    public ListView<String> modelParamList; /*!< Lists the parameter values of the currently loaded model. */
    public TextField modelTxt; /*!< File to save the model database to. */
    public TextField modelMethodTxt; /*!< Which method was used in the optimization function. */
    public TextField modelWeatherTxt; /*!< The weather dataset which was used to create the current model. */
    public Button newModelBtn; /*!< Button to create a new model. */
    public Button newOrgBtn; /*!< Button to create a new organism dataset from a raw input file. */
    public Button newWeatherBtn; /*!< Button to create new weather dataset from a raw input file. */
    public Button nextBtn; /*!< Select the image after the currently selected image. */
    public RadioButton oneGraphPerBtn; /*!< Not used by the program. Only given a label so that it can be assigned a tooltip. */
    public ComboBox<String> organismGraphCombBx; /*!< Lists all organism datasets available for graphing. */
    public TextField organismImportTxt; /*!< The default directory to look for organism datasets to import. */
    public ListView<String> organismList; /*!< Lists the organisms which are part of the currently loaded organism dataset. */
    public ComboBox<String> organismListBx; /*!< Holds the list of all loaded organism datasets. */
    public TextField organismSaveTxt; /*!< The default directory to save organism datasets. */
    public TableColumn<ParameterTable,String> paramCol; /*!< Holds the model parameters. */
    public TableColumn<ParameterTable,String> paramHighCol; /*!< Holds the high estimates for the model parameters. */
    public TableColumn<ParameterTable,String> paramLowCol; /*!< Holds the low estimates for the model parameters. */
    public Button prevBtn; /*!< Select the image before the currently selected image. */
    public Button saveBtn; /*!< Save the currently selected image. */
    public ListView<TextFieldListCell> stageNameList; /*!< An editable list of the stage names for the currently loaded organism dataset. */
    public TextField stagesTxt; /*!< The number of stages in the currently loaded organism dataset. */
    public Button statsCalcBtn; /*!< The button to run the statistical calculations. */
    public TextField statsMethodTxt; /*!< Which method was used in the statistics function. */
    public ComboBox<String> statsModelCombBx; /*!< Lists all models available to run statistical operations on. */
    public ComboBox<String> statsOrganismCombBx; /*!< Lists all organism datasets available to run statistical operations on. */
    public TableView<ParameterTable> statsParamTbl; /*!< Displays the range of parameter values. */
    public ComboBox<String> statsWeatherCombBox; /*!< Lists all weather datasets available to run statistical operations on. */
    public CheckBox tooltipCheckBx; /*!< Should tooltips be displayed. */
    public ComboBox<String> weatherGraphCombBx; /*!< Lists all weather datasets available for graphing. */
    public TextField weatherImportTxt; /*!< The default directory to look for weather datasets to import. */
    public ComboBox<String> weatherListBx; /*!< Holds the list of all loaded weather datasets. */
    public TextField weatherSaveTxt; /*!< The default directory to save weather datasets. */
    public TextField xxTxt; /*!< The chi-squared value of a model. */
    public TextField xxHighTxt; /*!< High estimate for the chi-squared p-value. */
    public TextField xxLowTxt; /*!< Low estimate for the chi-squared p-value. */
    public TextField xxpTxt; /*!< The p-value for the chi-squared test. */
        
    /**
     * Basic constructor which is passed a SettingsObject and tries to load the 
     * models.
     * @param so SettingsObject passed by the caller. Must be created at an 
     * earlier stage in the program.
     */
    public MainWindow(SettingsObject so) {
        try {
            settings = so;
            models = new ArrayList<>();
            models.addAll(XmlManager.loadModels(settings.defaultModelLocation));
            paramTblList = new ArrayList<>();
            tableParams = FXCollections.observableList(paramTblList);
            figureCount = 0;
            forecastFiguresCount = 0;
            loadedModel = -1;
        } catch(Exception e) {
            ErrorManager.log("Error while making MainWindow.", e);
        }
    }
	
    /**
     * Allows the user to move backwards through their interactions with the 
     * help tab.
     */
    public void back() {
        if(history.getCurrentIndex() > 0) {
            history.go(-1);
        }
    }
    
    /**
     * Allows the user to pick a new default location to save generated figures.
     */
    public void changeFigureSave() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Default Location To Save Figures");
        dc.setInitialDirectory(new File(settings.defaultFigureLocation));
        File saveDirectory = dc.showDialog(stage);
        if(saveDirectory != null) {
            settings.defaultFigureLocation = saveDirectory.getAbsolutePath();
            figureSaveTxt.setText(saveDirectory.getAbsolutePath());
        }
    }
    
    /**
     * Changes which file the model database is stored in.
     * This method allows the user to choose a new model database file.
     * This file can either be pre-existing or created by the program. 
     * @note The model database must be unified within one file. If a file is 
     * chosen that already exists, the old contents of the file will be 
     * completely overwritten.
     */
    public void changeModelFile() {
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fc.getExtensionFilters().add(extFilter);
        fc.setTitle("Choose New Model Database File");
        File file = new File(settings.defaultModelLocation);
        if(file.exists()) {
            fc.setInitialDirectory(new File(new File(new File(settings.defaultModelLocation).getAbsolutePath()).getParent()));
        }
        File model = fc.showSaveDialog(stage);
        if(model != null) {
            settings.defaultModelLocation = model.getAbsolutePath();
            modelTxt.setText(settings.defaultModelLocation);
            XmlManager.saveModels(settings.defaultModelLocation, models);
        }
        populateStageNames();
    }
	
    /**
     * Changes the default organism dataset import directory.
     * This method allows the user to choose which directory the program should 
     * look at first when asked to load an organism database. This does not 
     * prevent databases from other locations being loaded, it just means that 
     * the file chooser will start in the default import directory.
     */
    public void changeOrganismImport() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Default Location To Look For Organism Data");
        dc.setInitialDirectory(new File(settings.defaultOrganismLoad));
        File importDirectory = dc.showDialog(stage);
        if(importDirectory != null) {
            settings.defaultOrganismLoad = importDirectory.getAbsolutePath();
            organismImportTxt.setText(importDirectory.getAbsolutePath());
        }
    }
	
    /**
     * Changes the default organism dataset save directory.
     * This method allows the user to choose which directory the program should 
     * suggest first when the user desires to save an organism data file. The 
     * user can save the data file in any location, but the directory chooser 
     * will start in the default save directory.
     */
    public void changeOrganismSave() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Default Location To Save Organism Datasets");
        dc.setInitialDirectory(new File(settings.defaultOrganismSave));
        File saveDirectory = dc.showDialog(stage);
        if(saveDirectory != null) {
            settings.defaultOrganismSave = saveDirectory.getAbsolutePath();
            organismSaveTxt.setText(saveDirectory.getAbsolutePath());
        }
    }
    
    /**
     * Changes whether tooltips are displayed on the GUI or not.
     */
    public void changeTooltips() {
        settings.tooltips = tooltipCheckBx.isSelected();
        if(settings.tooltips) {
            setupTooltips();
        } else {
            clearTooltips();
        }
    }
	
    /**
     * Changes the default weather dataset import location.
     * This method allows the user to choose the default directory the program 
     * will look when told to import a weather data set. The program can still 
     * load weather data from any location but the file chooser will start in 
     * the default import directory.
     */
    public void changeWeatherImport() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Default Location To Look For Weather Data");
        dc.setInitialDirectory(new File(settings.defaultWeatherLoad));
        File importDirectory = dc.showDialog(stage);
        if(importDirectory != null) {
            settings.defaultWeatherLoad = importDirectory.getAbsolutePath();
            weatherImportTxt.setText(importDirectory.getAbsolutePath());
        }
    }
	
    /**
     * Changes the default weather dataset save location.
     * This method allows the user to choose a new default directory for the 
     * program to save weather databases to. The user can save weather database 
     * in any desired location, but the directory chooser will start in the 
     * default save directory.
     */
    public void changeWeatherSave() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Default Location To Save Weather Datasets");
        dc.setInitialDirectory(new File(settings.defaultWeatherSave));
        File saveDirectory = dc.showDialog(stage);
        if(saveDirectory != null) {
            settings.defaultWeatherSave = saveDirectory.getAbsolutePath();
            weatherSaveTxt.setText(saveDirectory.getAbsolutePath());
        }
    }
    
    /**
     * Removes tooltips from the GUI if the user chooses that option.
     */
    private void clearTooltips() {
        aicTxt.setTooltip(null);
        allGraphsrbtn.setTooltip(null);
        alphaTxt.setTooltip(null);
        combChartCheckBx.setTooltip(null);
        combChartHighTxt.setTooltip(null);
        combChartLowTxt.setTooltip(null);
        compRawCheckBx.setTooltip(null);
        compRawHighTxt.setTooltip(null);
        compRawLowTxt.setTooltip(null);
        expPropCheckBx.setTooltip(null);
        expPropHighTxt.setTooltip(null);
        expPropLowTxt.setTooltip(null);
        ggTxt.setTooltip(null);
        ggciHighTxt.setTooltip(null);
        ggciLowTxt.setTooltip(null);
        ggpTxt.setTooltip(null);
        graphBtn.setTooltip(null);
        imageHeightTxt.setTooltip(null);
        imageWidthTxt.setTooltip(null);
        iterTxt.setTooltip(null);
        loadOrgBtn.setTooltip(null);
        loadWeatherBtn.setTooltip(null);
        log2dCheckBx.setTooltip(null);
        log2dSampleTxt.setTooltip(null);
        log2dIntervalTxt.setTooltip(null);
        log2dOffsetTxt.setTooltip(null);
        log3dCheckBx.setTooltip(null);
        log3dSHighTxt.setTooltip(null);
        log3dSLowTxt.setTooltip(null);
        log3dTHighTxt.setTooltip(null);
        log3dTLowTxt.setTooltip(null);
        logTxt.setTooltip(null);
        membersTxt.setTooltip(null);
        modelGraphCombBx.setTooltip(null);
        modelListBx.setTooltip(null);
        modelOrganismList.setTooltip(null);
        modelOrganismTxt.setTooltip(null);
        modelParamList.setTooltip(null);
        modelWeatherTxt.setTooltip(null);
        newModelBtn.setTooltip(null);
        newOrgBtn.setTooltip(null);
        newWeatherBtn.setTooltip(null);
        oneGraphPerBtn.setTooltip(null);
        organismGraphCombBx.setTooltip(null);
        organismList.setTooltip(null);
        organismListBx.setTooltip(null);
        saveBtn.setTooltip(null);
        stageNameList.setTooltip(null);
        stagesTxt.setTooltip(null);
        statsCalcBtn.setTooltip(null);
        statsModelCombBx.setTooltip(null);
        statsOrganismCombBx.setTooltip(null);
        statsParamTbl.setTooltip(null);
        statsWeatherCombBox.setTooltip(null);
        weatherGraphCombBx.setTooltip(null);
        weatherListBx.setTooltip(null);
        xxHighTxt.setTooltip(null);
        xxLowTxt.setTooltip(null);
        xxTxt.setTooltip(null);
        xxpTxt.setTooltip(null);
    }
	
    /**
     * Creates forecast figures for the current model and dataset.
     * This method creates forecast figures and loads them for display to the 
     * user. Unlike other methods, forecasting only requires a model and a 
     * weather dataset. The figures show both the forecast itself as well as the 
     * goodness-of-fit and the central tendencies for the particular model. The 
     * implementation of this function is nearly identical to how graph() is
     * implemented.
     * @see graph()
     */
    public void forecast() {
        File dir = new File(settings.defaultTempForecastOutput);
        if(dir.exists()) {
            if(dir.isFile()) {
                ErrorManager.log("MainWindow.forecast tried to open a file as a directory", null);
            } else {
                File[] files = dir.listFiles(); 
                for(File file : files) {
                    file.delete();
                }
            }
        }
        ForecastObject fo = new ForecastObject();
        fo.outputLoc = settings.defaultTempForecastOutput;
        fo.fit = forecastFitCheckBx.isSelected();
        fo.fitLow = forecastFitLowTxtBx.getText();
        fo.fitHigh = forecastFitHighTxtBx.getText();
        fo.fitAlone = forecastOneFigureStageBtn.isSelected();
        fo.heigth = forecastHeightTxt.getText();
        fo.tendencies = forecastTendenciesCheckBx.isSelected();
        fo.predict = forecastPredictionCheckBx.isSelected();
        fo.error = forecastErrorCheckBx.isSelected();
        fo.proportionStage = forecastStageTxt.getText();
        fo.predictHigh = forecastPredictHigh.getText();
        fo.predictLow = forecastPredictLow.getText();
        String tmp = forecastPercentTxt.getText();
        double tmpVal = Double.parseDouble(tmp);
        if (tmpVal != 0) {
            tmpVal = tmpVal/100.0;
        }
        fo.proportionPercent = String.valueOf(tmpVal);
        String name = forecastWeatherCmbBx.getValue();
        int index = settings.weatherNames.indexOf(name); 
        if(index > -1) {
           DatabaseObject file = XmlManager.readDatabaseFile(settings.weatherLocations.get(index));
           fo.weather = file.memberLocations[0];
        }
        fo.width = forecastWidthTxt.getText();
        for (ModelObject model : models) {
            if (model.name.equals(forecastModelCmbBx.getValue())) {
                fo.model = model;
            }
        }
        int ind = settings.organismNames.indexOf(fo.model.OrganismDatabaseName);
        if(ind > -1) {
            DatabaseObject file = XmlManager.readDatabaseFile(settings.organismLocations.get(ind));
            fo.stageNames = new String[file.memberStages.length];
            System.arraycopy(file.memberStages, 0, fo.stageNames, 0, file.memberStages.length);
        }
        if(ScriptManager.forecast(fo)) {
            File folder = new File(settings.defaultTempForecastOutput);
            if(folder.exists()) {
                File[] listOfFigures = folder.listFiles(); 
                forecastFiguresCount = listOfFigures.length;
                if(forecastFiguresCount > 0) {
                    forecastFigures = new Image[forecastFiguresCount];
                    for(int i = 0; i < forecastFiguresCount; i++) {
                        forecastFigures[i] = new Image("file:" + listOfFigures[i]);
                    }
                    forecastCurrentFigure = 0;
                    if(forecastFiguresCount > 0) {
                        forecastView.setImage(forecastFigures[forecastCurrentFigure]);
                    }
                }
            }
        }
    }
    
    /**
     * Iterates through the forecast figures in the positive direction. 
     */
    public void forecastNext() {
        if(forecastFigures != null) {
            forecastCurrentFigure++;
            if(forecastCurrentFigure >= forecastFiguresCount) {
                forecastCurrentFigure = 0;
            }
            forecastView.setImage(forecastFigures[forecastCurrentFigure]);
        }
    }
    
    /**
     * Iterates through the forecast figures in the negative direction. 
     */
    public void forecastPrev() {
        if(forecastFigures != null) {
            forecastCurrentFigure--;
            if(forecastCurrentFigure < 0) {
                forecastCurrentFigure = forecastFiguresCount - 1;
            }
            forecastView.setImage(forecastFigures[forecastCurrentFigure]);
        }
    }
    
    /**
     * Saves the currently loaded forecast figure.
     */
    public void forecastSave() {
        if(forecastFiguresCount > 0) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Figure");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            fc.getExtensionFilters().add(extFilter);
            fc.setInitialDirectory(new File(settings.defaultFigureLocation));
            File file = fc.showSaveDialog(stage);
            if(file != null) {
                try {
                    if(file.getName().indexOf(".png") < 0) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                    ImageIO.write(SwingFXUtils.fromFXImage(forecastFigures[forecastCurrentFigure], null), "png", file);
                } catch (IOException e) {
                    ErrorManager.error("Could not save image.", "MainWindow.forecastSave has encountered an error.", e);
                }
            }
        }
    }
    
    /**
     * Allows the user to move forwards through their interactions with the help 
     * tab.
     */
    public void forward() {
        if((history.getCurrentIndex()) < (history.getEntries().size() - 1)) {
            history.go(1);
        }
    }
    
    /**
     * Creates graphs of the current model and datasets.
     * This method both creates the figures and loads them for display to the 
     * user. If a model is loaded and an organism and weather database are 
     * chosen it calls ScriptManager to create the figures. It then initializes 
     * an array of figures and begins the process of showing them to the user.
     */
    public void graph() {
        if(loadedModel >= 0) {
            File dir = new File(settings.defaultTempGraphOutput);
            if(dir.exists()) {
                if(dir.isFile()) {
                    ErrorManager.log("MainWindow.graph tried to open a file as a directory", null);
                } else {
                    File[] files = dir.listFiles(); 
                    for(File file : files) {
                        file.delete();
                    }
                }
            }
            GraphObject graphSettings = new GraphObject();
            graphSettings.compRaw = compRawCheckBx.isSelected();
            graphSettings.expProp = expPropCheckBx.isSelected();
            graphSettings.log2d = log2dCheckBx.isSelected();
            graphSettings.log3d = log3dCheckBx.isSelected();
            graphSettings.log2dinterval = log2dIntervalTxt.getText();
            graphSettings.log2doffset = log2dOffsetTxt.getText();
            graphSettings.log2dsample = log2dSampleTxt.getText();
            graphSettings.logPdf3DShigh = log3dSHighTxt.getText();
            graphSettings.logPdf3DSlow = log3dSLowTxt.getText();
            graphSettings.logPdf3DThigh = log3dTHighTxt.getText();
            graphSettings.logPdf3DTlow = log3dTLowTxt.getText();
            graphSettings.expProphigh = expPropHighTxt.getText();
            graphSettings.expProplow = expPropLowTxt.getText();
            graphSettings.compRawhigh = compRawHighTxt.getText();
            graphSettings.compRawlow = compRawLowTxt.getText();
            graphSettings.outputLoc = settings.defaultTempGraphOutput;
            String name = organismGraphCombBx.getValue();
            String[] organisms = null;
            String tWeather = "";
            int index = settings.organismNames.indexOf(name);
            if(index > -1){
                DatabaseObject file = XmlManager.readDatabaseFile(settings.organismLocations.get(index));
                organisms = file.memberLocations;
                graphSettings.organismStageNames = file.memberStages;
            }
            name = weatherGraphCombBx.getValue();
            index = settings.weatherNames.indexOf(name);
            if(index > -1) {
                DatabaseObject file = XmlManager.readDatabaseFile(settings.weatherLocations.get(index));
                tWeather = file.memberLocations[0];
            }
            ModelObject tModel = null;
            for (ModelObject model : models) {
                if (model.name.equals(modelGraphCombBx.getValue())) {
                    tModel = model;
                }
            }
            graphSettings.allTogether = allGraphsrbtn.isSelected();
            graphSettings.imgHeight = imageHeightTxt.getText();
            graphSettings.imgWidth = imageWidthTxt.getText();
            graphSettings.combined = combChartCheckBx.isSelected();
            graphSettings.combHigh = combChartHighTxt.getText();
            graphSettings.combLow = combChartLowTxt.getText();
            if(ScriptManager.graph(tWeather,organisms,tModel, graphSettings)) {
                File folder = new File(settings.defaultTempGraphOutput);
                if(folder.exists()) {
                    File[] listOfFigures = folder.listFiles(); 
                    figureCount = listOfFigures.length;
                    figures = new Image[figureCount];
                    for(int i = 0; i < figureCount; i++) {
                        figures[i] = new Image("file:" + listOfFigures[i]);
                    }
                    currentFigure = 0;
                    if(figureCount > 0) {
                        figureView.setImage(figures[currentFigure]);
                    }
                }
            }
        }
    }
    
    /**
     * Saves the current figure.
     * This method allows the user to save the current figure to a location of 
     * their choosing. The figures will be stored as .png files and this is 
     * enforced on the user.
     * @warning Saving anywhere within the “./Temp” directory will risk 
     * accidental loss due to the fact that the program empties that directory 
     * without prior warning under some conditions.
     */
    public void graphSave() {
        if(figureCount > 0) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Figure");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            fc.getExtensionFilters().add(extFilter);
            fc.setInitialDirectory(new File(settings.defaultFigureLocation));
            File file = fc.showSaveDialog(stage);
            if(file != null) {
                try {
                    if(file.getName().indexOf(".png") < 0) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                    ImageIO.write(SwingFXUtils.fromFXImage(figures[currentFigure], null), "png", file);
                } catch (IOException e) {
                    ErrorManager.error("Could not save image.", "MainWindow.graphSave has encountered an error.", e);
                }
            }
        }
    }
	
    /**
     * Loads a previously created organism database.
     */
    public void loadOrganism() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Organism Dataset to Load");
        dc.setInitialDirectory(new File(settings.defaultOrganismSave));
        File inpt = dc.showDialog(stage);
        if(inpt != null) {
            DatabaseObject temp = XmlManager.readDatabaseFile(inpt.getAbsolutePath() + "/" + inpt.getName() + ".about.xml");
            if (temp != null) {
                settings.organismCount++;
                settings.organismLocations.add(temp.location);
                settings.organismNames.add(temp.name);
                settings.loadedOrganism = temp.location;
                updateOrganismInfo();
            }
        }
    }

    /**
     * Loads a previously created weather database.
     */
    public void loadWeather() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Weather Dataset to Load");
        dc.setInitialDirectory(new File(settings.defaultWeatherSave));
        File inpt = dc.showDialog(stage);
        if(inpt != null) {
            DatabaseObject temp = XmlManager.readDatabaseFile(inpt.getAbsolutePath() + "/" + inpt.getName() + ".about.xml");
            if (temp != null) {
                settings.weatherCount++;
                settings.weatherLocations.add(temp.location);
                settings.weatherNames.add(temp.name);
                settings.loadedWeather = temp.location;
                updateWeatherInfo();
            }
        }
    }

    /**
     * Starts the creation of a new model.
     */
    public void newModel() {
        File f = new File("Temp/modelOutput.xml");
        if(f.exists()) {
            f.delete();
        }
        ModelWindow m = new ModelWindow(settings);
        ModelObject temp = m.show();
        if(temp != null) {
            settings.loadedModel = temp.name;
            temp.stats.optim = temp.optim;
            models.add(temp);
            loadedModel = models.size() - 1;
        }
        updateModelWindow();
        updateStats();
    }
    
    /**
     * Starts the creation of a new organism database.
     * This method allows the user to choose the file to be imported and then 
     * runs it through the importation process provided by Importer. If 
     * successful, it updates the list of organism databases displayed by the 
     * GUI.
     */
    public void newOrganism() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Organism Data File To Import");
        fc.setInitialDirectory(new File(settings.defaultOrganismLoad));
        File importFile = fc.showOpenDialog(stage);
        if(importFile != null) {
            ImportationObject io = Importer.autoLoad(importFile.getAbsolutePath(), true);
            if(io != null) {
                ImportWindow iw = new ImportWindow(io, settings);
                io = iw.show();
                if(io != null) {
                    Importer.save(io);
                    DatabaseObject loadedOrganismInfo = XmlManager.readDatabaseFile(io.destination + "/" + io.datasetName + ".about.xml");
                    settings.organismCount++;
                    settings.organismLocations.add(loadedOrganismInfo.location);
                    settings.organismNames.add(loadedOrganismInfo.name);
                    settings.loadedOrganism = loadedOrganismInfo.location;
                    updateOrganismInfo();
                }
            }
        }
    }
    
    /**
     * Starts the creation of a new weather database.
     * This method allows the user to choose the file to be imported and then 
     * runs it through the importation process provided by Importer. If 
     * successful, it updates the list of weather databases displayed by the 
     * GUI.
     */
    public void newWeather() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Weather Data File To Import");
        fc.setInitialDirectory(new File(settings.defaultWeatherLoad));
        File importFile = fc.showOpenDialog(stage);
        if(importFile != null) {
            ImportationObject io = Importer.autoLoad(importFile.getAbsolutePath(), false);
            if(io != null) {
                ImportWindow iw = new ImportWindow(io, settings);
                io = iw.show();
                if(io != null) {
                    Importer.save(io);
                    DatabaseObject loadedWeatherInfo = XmlManager.readDatabaseFile(io.destination + "/" + io.datasetName + ".about.xml");
                    settings.weatherCount++;
                    settings.weatherLocations.add(loadedWeatherInfo.location);
                    settings.weatherNames.add(loadedWeatherInfo.name);
                    settings.loadedWeather = loadedWeatherInfo.location;
                    updateWeatherInfo();
                }
            }
        }
    }
	
    /**
     * Iterates in the positive direction through the displayed image by one, 
     * wrapping around if necessary.
     */
    public void next() {
        if(figures != null) {
            currentFigure++;
            if(currentFigure >= figureCount) {
                currentFigure = 0;
            }
            figureView.setImage(figures[currentFigure]);
        }
    }
	
    /**
     * Adds the current names of each stage for the current organism dataset 
     * into stageNameList.
     */
    private void populateStageNames() {
        if(!settings.loadedOrganism.isEmpty()) {
            DatabaseObject loadedOrganismInfo = XmlManager.readDatabaseFile(settings.loadedOrganism);
            if(loadedOrganismInfo.stages > 0) {
                ObservableList<TextFieldListCell> names = FXCollections.observableArrayList();
                for(String stageName : loadedOrganismInfo.memberStages) {
                    TextFieldListCell t = new TextFieldListCell();
                    t.setText(stageName);
                    names.add(t);
                }
                stageNameList.setItems(names);
            }
        }
    }
	
    /**
     * Iterates in the negative direction through the displayed image by one, 
     * wrapping around if necessary.
     */
    public void prev() {
        if(figures != null) {
            currentFigure--;
            if(currentFigure < 0) {
                currentFigure = figureCount - 1;
            }
            figureView.setImage(figures[currentFigure]);
        }
    }
	
    /**
     * Creates a new SettingsObject using the default values and repopulates the 
     * GUI with them.
     */
    public void resetSettings() {
        SettingsObject tempSettings = new SettingsObject();
        settings.defaultFigureLocation = tempSettings.defaultFigureLocation;
        settings.defaultModelLocation = tempSettings.defaultModelLocation;
        settings.defaultOrganismLoad = tempSettings.defaultOrganismLoad;
        settings.defaultOrganismSave = tempSettings.defaultOrganismSave;
        settings.defaultTempGraphOutput = tempSettings.defaultTempGraphOutput;
        settings.defaultWeatherLoad = tempSettings.defaultWeatherLoad;
        settings.defaultWeatherSave = tempSettings.defaultWeatherSave;
        updateSettingsWindow();
    }
    
    /**
     * Contains all cell factory declarations in the program.
     */
    private void setupCellFactories() {
        try {
            paramCol.setCellValueFactory(new Callback<CellDataFeatures<ParameterTable, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(CellDataFeatures<ParameterTable, String> p) {
                    return p.getValue().paramProperty();
                }
            });
            paramLowCol.setCellValueFactory(new Callback<CellDataFeatures<ParameterTable, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(CellDataFeatures<ParameterTable, String> p) {
                    return p.getValue().paramLowProperty();
                }
            });
            paramHighCol.setCellValueFactory(new Callback<CellDataFeatures<ParameterTable, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(CellDataFeatures<ParameterTable, String> p) {
                    return p.getValue().paramHighProperty();
                }
            });
            Callback<ListView<TextFieldListCell>, ListCell<TextFieldListCell>> onCommit = TextFieldListCell.forListView(new StringConverter<TextFieldListCell>() {
                @Override 
                public TextFieldListCell fromString(String input) {
                    TextFieldListCell ret = new TextFieldListCell();
                    ret.setText(input);
                    return ret;
                }
                @Override 
                public String toString(TextFieldListCell t) {
                    String ret = "";
                    try {
                        ret = t.getText();
                    } catch (Exception e) {
                        ErrorManager.log("Exception was raised in the toString method of the callback created in MainWindow.setupCellFactories", e);
                    }
                    return ret;
                }
            });
            stageNameList.setCellFactory(onCommit);
        } catch (Exception e) {
            ErrorManager.log("Error in MainWindow.setupCellFactories", e);
        }
    }
    
    
    /**
     * Contains all listener declarations in the program.
     */
    private void setupListeners() {
        try {
            organismListBx.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String> () {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {
                        if(new_val != null) {
                            int index = settings.organismNames.indexOf(organismListBx.getValue());
                            if((index > -1)&&(!settings.loadedOrganism.equals(settings.organismLocations.get(index)))) {
                                settings.loadedOrganism = settings.organismLocations.get(index);
                                updateOrganismInfo();
                            }
                        }
                    } 
                }
            );
            weatherListBx.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String> () {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {
                        if(new_val != null) {
                            int index = settings.weatherNames.indexOf(weatherListBx.getValue());
                            if((index > -1)&&(!settings.loadedWeather.equals(settings.weatherLocations.get(index)))) {
                                settings.loadedWeather = settings.weatherLocations.get(index);
                                updateWeatherInfo();
                            }
                        }
                    } 
                }
            );
            modelListBx.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String> () {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {
                        if((modelListBx != null)&&(modelListBx.getItems().size() > 0)&&(modelListBx.getValue() != null)&&(!modelListBx.getValue().equals(settings.loadedModel))) {
                            String selected = modelListBx.getValue();
                            if ((models != null)&&(models.size() > 0)) {
                                int count = 0;
                                for (ModelObject model : models) {
                                    if((model.name.equals(selected))&&(count < models.size())) {
                                        loadedModel = count;
                                        settings.loadedModel = model.name;
                                        statsModelCombBx.setValue(settings.loadedModel);
                                        updateModelWindow();
                                        updateStats();
                                    }
                                    count++;
                                }
                            }
                        }
                    }
                }
            );
            statsModelCombBx.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String> () {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {
                        if((statsModelCombBx != null)&&(statsModelCombBx.getItems().size() > 0)&&(statsModelCombBx.getValue() != null)&&(!statsModelCombBx.getValue().equals(settings.loadedModel))) {
                            String selected = statsModelCombBx.getValue();
                            if ((models != null)&&(models.size() > 0)) {
                                int count = 0;
                                for (ModelObject model : models) {
                                    if((model.name.equals(selected))&&(count < models.size())) {
                                        loadedModel = count;
                                        settings.loadedModel = model.name;
                                        modelListBx.setValue(settings.loadedModel);
                                        updateStats();
                                        updateModelWindow();
                                    }
                                    count++;
                                }
                            }
                        }
                    }
                }
            );
            stageNameList.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<TextFieldListCell>() {
                    @Override
                    public void changed (ObservableValue<? extends TextFieldListCell> ov, TextFieldListCell old_cell, TextFieldListCell new_cell) {
                        if(!settings.loadedOrganism.isEmpty()) {
                            DatabaseObject loadedOrganismInfo = XmlManager.readDatabaseFile(settings.loadedOrganism);
                            ObservableList<TextFieldListCell> names = stageNameList.getItems();
                            if((names != null)&&(loadedOrganismInfo != null)) {
                                for(int i = 0; (i < names.size())&&(i < loadedOrganismInfo.memberStages.length); i++) {
                                    loadedOrganismInfo.memberStages[i] = names.get(i).getText();
                                }
                                XmlManager.updateDatabaseFile(loadedOrganismInfo);
                            }
                        }
                    }
                }
            );
        } catch(Exception e) {
            ErrorManager.log("Error in MainWindow.setupListeners", e);
        }
    }
    
    /**
     * Creates and sets tooltips for GUI elements.
     */
    private void setupTooltips() {
        aicTxt.setTooltip(new Tooltip("The Akaike Information Criterion for the loaded model."));
        allGraphsrbtn.setTooltip(new Tooltip("All graphs are contained on a single image."));
        alphaTxt.setTooltip(new Tooltip("Alpha value to use during the statistical tests."));
        combChartCheckBx.setTooltip(new Tooltip("Create a graph plotting the expected proportions and the observed data with a key."));
        combChartHighTxt.setTooltip(new Tooltip("The high end of the x-axis for the graph (0 = all)."));
        combChartLowTxt.setTooltip(new Tooltip("The low end of the x-axis for the graph."));
        compRawCheckBx.setTooltip(new Tooltip("Create a graph comparing the expected proportions to the observed data."));
        compRawHighTxt.setTooltip(new Tooltip("The high end of the x-axis for the comparison graph (0 = all)."));
        compRawLowTxt.setTooltip(new Tooltip("The low end of the x-axis for the comparison graph."));
        expPropCheckBx.setTooltip(new Tooltip("Create a graph of the predicted proportions of the life stages over time."));
        expPropHighTxt.setTooltip(new Tooltip("The high end of the x-axis for the predicted proportion graph (0 = all)."));
        expPropLowTxt.setTooltip(new Tooltip("The low end of the x-axis for the predicted proportion graph."));
        ggTxt.setTooltip(new Tooltip("The estimated G-Squared value."));
        ggciHighTxt.setTooltip(new Tooltip("The high end of the estimated confidence interval for the G-Squared value."));
        ggciLowTxt.setTooltip(new Tooltip("The low end of the estimated confidence interval for the G-Squared value."));
        ggpTxt.setTooltip(new Tooltip("The estimated P-value of the G-Squared value."));
        graphBtn.setTooltip(new Tooltip("Create the currently selected graphs."));
        imageHeightTxt.setTooltip(new Tooltip("The height of all created images."));
        imageWidthTxt.setTooltip(new Tooltip("The width of all created images."));
        iterTxt.setTooltip(new Tooltip("Number of times to run statistical tests."));
        loadOrgBtn.setTooltip(new Tooltip("Load a previously created dataset in to the program."));
        loadWeatherBtn.setTooltip(new Tooltip("Load a previously created dataset in to the program"));
        log2dCheckBx.setTooltip(new Tooltip("Create a 2D graph of the logistic probability density function"));
        log2dSampleTxt.setTooltip(new Tooltip("How many samples to be taken of the PDF"));
        log2dIntervalTxt.setTooltip(new Tooltip("Distance between samples."));
        log2dOffsetTxt.setTooltip(new Tooltip("The location of the first sample."));
        log3dCheckBx.setTooltip(new Tooltip("Create a 3D graph of the logistic probability density function"));
        log3dSHighTxt.setTooltip(new Tooltip("The high end of the S-axis (0 = all)."));
        log3dSLowTxt.setTooltip(new Tooltip("The low end of the S-axis."));
        log3dTHighTxt.setTooltip(new Tooltip("The high end of the T-axis (0 = all)."));
        log3dTLowTxt.setTooltip(new Tooltip("The low end of the T-axis."));
        logTxt.setTooltip(new Tooltip("The log likelihood of the model."));
        membersTxt.setTooltip(new Tooltip("Number of organisms in current dataset."));
        modelGraphCombBx.setTooltip(new Tooltip("Choose a model to use."));
        modelListBx.setTooltip(new Tooltip("Choose a model to load."));
        modelOrganismList.setTooltip(new Tooltip("The members of the organism dataset used to create the current model."));
        modelOrganismTxt.setTooltip(new Tooltip("The organism dataset used to create the current model."));
        modelParamList.setTooltip(new Tooltip("The model parameters."));
        modelWeatherTxt.setTooltip(new Tooltip("The weather dataset used to create the current model."));
        newModelBtn.setTooltip(new Tooltip("Create a new model."));
        newOrgBtn.setTooltip(new Tooltip("Create new organism dataset from input data."));
        newWeatherBtn.setTooltip(new Tooltip("Create a new weather dataset from input data."));
        oneGraphPerBtn.setTooltip(new Tooltip("Each graph is put on its own image."));
        organismGraphCombBx.setTooltip(new Tooltip("Choose an organism dataset to use."));
        organismList.setTooltip(new Tooltip("All the organisms included in the current dataset."));
        organismListBx.setTooltip(new Tooltip("Choose an organism dataset to load."));
        saveBtn.setTooltip(new Tooltip("Save the current figure."));
        stageNameList.setTooltip(new Tooltip("Lists the names of the life stages for the current organism dataset. Double click to edit."));
        stagesTxt.setTooltip(new Tooltip("Number of life stages in the loaded organism dataset"));
        statsCalcBtn.setTooltip(new Tooltip("Run the calculations."));
        statsModelCombBx.setTooltip(new Tooltip("Choose a model to use."));
        statsOrganismCombBx.setTooltip(new Tooltip("Choose an organism dataset to use."));
        statsParamTbl.setTooltip(new Tooltip("Table which holds the model parameters as well as the high and low values for the estimated confidence interval for these parameters."));
        statsWeatherCombBox.setTooltip(new Tooltip("Choose a weather dataset to use."));
        weatherGraphCombBx.setTooltip(new Tooltip("Choose a weather dataset to use."));
        weatherListBx.setTooltip(new Tooltip("Choose a weather dataset to load."));
        xxHighTxt.setTooltip(new Tooltip("The high end of the estimated confidence interval for the chi-squared statistic."));
        xxLowTxt.setTooltip(new Tooltip("The low end of the estimated confidence interval for the chi-squared statistic."));
        xxTxt.setTooltip(new Tooltip("The value of Pearson's chi-squared statistic."));
        xxpTxt.setTooltip(new Tooltip("The p-value for the chi-squared statistic."));
        
    }
    
    private void setupHelpWindow() {
        WebEngine webEngine = helpWebView.getEngine();
        webEngine.load(this.getClass().getResource("/etc/documentation/html/doc.html").toExternalForm());
        history = webEngine.getHistory();
    }
	
    /**
     * Initializes the window in preparation for showing it to the user.
     */
    private void setupWindow() {
        try {
            stage = null;
            FXMLLoader fl = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
            fl.setController(this);
            fl.load();
            Parent root = fl.getRoot();
            stage = new Stage();
            stage.setTitle("StageCast");
            stage.setScene(new Scene(root));
            setupCellFactories();
            setupListeners();
            if(settings.tooltips) {
                setupTooltips();
            }
            setupHelpWindow();
            statsParamTbl.setItems(tableParams);
        } catch (IOException e) {
            ErrorManager.log("Error in MainWindow.setupWindow", e);
        }
    }
        
    /**
     * Shows the window to the user.
     */
    public void show() {
        setupWindow();
        if(stage != null) {
            updateWindow();
            stage.showAndWait();
            XmlManager.saveSettings(settings);
            if(models != null) {
                XmlManager.saveModels(settings.defaultModelLocation, models);
            }
        }
    }
    
    /**
     * Runs the currently selected model, organism, and weather datasets through 
     * the further statistical operations provided by ScriptManager.
     * @note This is a two part process where, after calling ScriptManager the 
     * program expects the R script to create a new file which contains the
     * output. The output file expected from the R script is an xml file 
     * representing a StatsObject.
     */
    public void stats() {
        try {
            if(!iterTxt.getText().isEmpty()) {
                File f = new File("Temp/statOutput.xml");
                if(f.exists()) {
                    f.delete();
                }
                StatsObject stat = new StatsObject();
                stat.iterations = iterTxt.getText(); 
                stat.alpha = alphaTxt.getText();
                String name = statsOrganismCombBx.getValue();
                DatabaseObject organism = null;
                int index = settings.organismNames.indexOf(name);
                if(index > -1) {
                    organism = XmlManager.readDatabaseFile(settings.organismLocations.get(index));
                }
                name = statsWeatherCombBox.getValue();
                DatabaseObject weather = null;
                index = settings.weatherNames.indexOf(name);
                if(index > -1) {
                    weather = XmlManager.readDatabaseFile(settings.weatherLocations.get(index));
                }
                stat.a = models.get(loadedModel).params;
                stat.stageMap.addAll(models.get(loadedModel).stageMap);
                stat.optim = models.get(loadedModel).optim;
                if((organism != null)&&(organism.stages == stat.stageMap.size())) {
                    ScriptManager.stats(organism, weather, stat);
                    models.get(loadedModel).stats = XmlManager.loadStats("Temp/statOutput.xml");
                    tableParams.clear();
                    updateStats();
                } else {
                   if(organism == null) {
                       ErrorManager.warn("Could not run statistics.", "Program did not successfully load the organism dataset: " + statsOrganismCombBx.getValue() + ".");
                   } else if (organism.stages != stat.stageMap.size()) {
                       ErrorManager.warn("Could not run statistics.", "The number of stages in the chosen organism dataset does not match the number of parameters in the chosen model.");
                   } else {
                       ErrorManager.warn("Could not run statistics.", "An unknown configuration error has occurred.");
                   }
                }
            }
        } catch (Exception e) {
            ErrorManager.log("MainWindow.stats failed to load the output statistics file.", e);
        }
    }
    
    public void updateForecastWindow() {
        ObservableList<String> options =  FXCollections.observableArrayList();
        options.setAll(settings.weatherNames);
        forecastWeatherCmbBx.getItems().setAll(options);
        if(!settings.loadedWeather.isEmpty()) {
            int index = settings.weatherLocations.indexOf(settings.loadedWeather);
            if(index > -1) {
                forecastWeatherCmbBx.setValue(settings.weatherNames.get(index));
            }
        }
        options.clear();
        if(models != null) {
            for (ModelObject model : models) {
                options.add(model.name);
            }
        }
        forecastModelCmbBx.getItems().setAll(options);
        if(!settings.loadedModel.isEmpty()) {
            forecastModelCmbBx.setValue(settings.loadedModel);
        }
    }
    
    /**
     * Sets the content of the various GUI elements in the Graph tab to reflect 
     * their current state. 
     */
    public void updateGraphWindow() {
        ObservableList<String> options =  FXCollections.observableArrayList();
        options.setAll(settings.organismNames);
        organismGraphCombBx.getItems().setAll(options);
        if(!settings.loadedOrganism.isEmpty()) {
            int index = settings.organismLocations.indexOf(settings.loadedOrganism);
            if(index > -1) {
                organismGraphCombBx.setValue(settings.organismNames.get(index));
            }
        }
        options.setAll(settings.weatherNames);
        weatherGraphCombBx.getItems().setAll(options);
        if(!settings.loadedWeather.isEmpty()) {
            int index = settings.weatherLocations.indexOf(settings.loadedWeather);
            if(index > -1) {
                weatherGraphCombBx.setValue(settings.weatherNames.get(index));
            }
        }
        options.clear();
        if(models != null) {
            for (ModelObject model : models) {
                options.add(model.name);
            }
        }
        modelGraphCombBx.getItems().setAll(options);
        if(!settings.loadedModel.isEmpty()) {
            modelGraphCombBx.setValue(settings.loadedModel);
        }
    }


    /**
     * Sets the content of the GUI elements under the Model tab pertaining to 
     * the model to reflect the appropriate current value.
     */
    public void updateModelWindow() {
        try {
            ObservableList<String> options = FXCollections.observableArrayList();
            if(models.size() > 0) {
                for (ModelObject model : models) {
                    options.add(model.name);
                }
            }
            modelListBx.getItems().setAll(options);
        } catch (Exception e) {
            ErrorManager.log("MainWindow.updateModelWindow encountered an error while populating model list.", e);
        }
        if(!settings.loadedModel.isEmpty()) {
            if(loadedModel >= 0) {
                modelListBx.setValue(settings.loadedModel);
                modelOrganismTxt.setText(models.get(loadedModel).OrganismDatabaseName);
                modelWeatherTxt.setText(models.get(loadedModel).weatherDatabaseName);
                modelMethodTxt.setText(models.get(loadedModel).optim);
                ObservableList<String> organisms =  FXCollections.observableArrayList();
                if(models.get(loadedModel).OrganismFiles != null) {
                    for(String fileName : models.get(loadedModel).OrganismFiles) {
                        String holder[] = fileName.split("\\.");
                        organisms.add(holder[holder.length - 2]);
                    }
                }
                modelOrganismList.setItems(organisms);
                aicTxt.setText(models.get(loadedModel).aic);
                logTxt.setText(models.get(loadedModel).logLikelihood);
                ObservableList<String> options =  FXCollections.observableArrayList();
                options.addAll(Arrays.asList(models.get(loadedModel).params));
                modelParamList.setItems(options);
            } else {
                try {
                    if(models != null) {
                        int count = 0;
                        for (ModelObject model : models) {
                            if (model.name.equals(settings.loadedModel)) {
                                loadedModel = count;
                                updateModelWindow();
                            }
                            count++;
                        }
                    }
                    if(loadedModel < 0) {
                        settings.loadedModel = "";
                    }
                } catch (Exception e) {
                    ErrorManager.error("Failed to update model tab UI elements.","MainWindow.updateModelWindow ran in to an error.", e);
                }
            }
        }
        updateStats();
    }
    
    /**
     * Sets the content of the GUI elements under the Files tab pertaining to 
     * the organism dataset to reflect the appropriate current value.
     */
    private void updateOrganismInfo() {
        ObservableList<String> options =  FXCollections.observableArrayList();
        options.addAll(settings.organismNames);
        organismListBx.getItems().setAll(options);
        if(!settings.loadedOrganism.isEmpty()) {
            int index = settings.organismLocations.indexOf(settings.loadedOrganism);
            if((index > -1)&&(index < organismListBx.getItems().size())) {
                organismListBx.getSelectionModel().select(index);
            }
        }
        if(!settings.loadedOrganism.isEmpty()) {
            DatabaseObject loadedOrganismInfo = XmlManager.readDatabaseFile(settings.loadedOrganism);
            stagesTxt.setText(String.valueOf(loadedOrganismInfo.stages));
            membersTxt.setText(String.valueOf(loadedOrganismInfo.memberLocations.length));
            ObservableList<String> organisms = FXCollections.observableArrayList();
            organisms.addAll(Arrays.asList(loadedOrganismInfo.memberNames));
            organismList.setItems(organisms);
        }
        populateStageNames();
    }

    /**
     * Loads the current settings information to the GUI. 
     */
    private void updateSettingsWindow() {
        organismImportTxt.setText(settings.defaultOrganismLoad);
        weatherImportTxt.setText(settings.defaultWeatherLoad);
        organismSaveTxt.setText(settings.defaultOrganismSave);
        weatherSaveTxt.setText(settings.defaultWeatherSave);
        figureSaveTxt.setText(settings.defaultFigureLocation);
        modelTxt.setText(settings.defaultModelLocation);
        tooltipCheckBx.setSelected(settings.tooltips);
    }
    
    /**
     * Sets the content of the GUI elements under the Model tab pertaining to 
     * the statistical calculation to reflect the appropriate current value.
     */
    private void updateStats() {
        try {
            tableParams.clear();
            StatsObject stats = null;
            if(loadedModel >= 0) {
                stats = models.get(loadedModel).stats;
            }
            if(stats != null) {
                if(stats.a != null) {
                    for(int i = 0; i < stats.a.length; i++) {
                        ParameterTable table;
                        table = new ParameterTable();
                        table.setParam(stats.a[i]);
                        if(stats.aHigh != null) {
                            table.setParamHigh(stats.aHigh[i]);
                        }
                        if(stats.aLow != null) {
                            table.setParamLow(stats.aLow[i]);
                        }
                        tableParams.add(table);
                    }
                    statsParamTbl.setItems(tableParams);
                }
                iterTxt.setText(stats.iterations);
                alphaTxt.setText(stats.alpha);
                ggTxt.setText(stats.gg);
                ggpTxt.setText(stats.ggPval);
                ggciHighTxt.setText(stats.ggci[0]);
                ggciLowTxt.setText(stats.ggci[1]);
                xxTxt.setText(stats.xx);
                xxpTxt.setText(stats.xxPval);
                xxHighTxt.setText(stats.xxHigh);
                xxLowTxt.setText(stats.xxLow);
                statsMethodTxt.setText(stats.optim);
                ObservableList<String> options =  FXCollections.observableArrayList();
                options.addAll(settings.organismNames);
                statsOrganismCombBx.getItems().setAll(options);
                if(loadedModel >= 0) {
                    statsOrganismCombBx.setValue(models.get(loadedModel).OrganismDatabaseName);
                }
                options.setAll(settings.weatherNames);
                statsWeatherCombBox.getItems().setAll(options);
                if(loadedModel >= 0) {
                    statsWeatherCombBox.setValue(models.get(loadedModel).weatherDatabaseName);
                }
                options.clear();
                if(models != null) {
                    for(ModelObject model : models) {
                        options.add(model.name);
                    }
                }
                statsModelCombBx.getItems().setAll(options);
                if(loadedModel >= 0) {
                    statsModelCombBx.setValue(models.get(loadedModel).name);
                }
            }
        } catch (Exception e) {
            ErrorManager.error("Could not update statistical information.","MainWindow.updateStats encountered an error while refreshing the statistical information.", e);
        }
    }
    
    /**
     * Sets the content of the GUI elements under the Files tab pertaining to 
     * the weather dataset to reflect the appropriate current value.
     */
    private void updateWeatherInfo() {
        ObservableList<String> options =  FXCollections.observableArrayList();
        options.addAll(settings.weatherNames);
        weatherListBx.getItems().setAll(options);
        if(!settings.loadedWeather.isEmpty()) {
            int index = settings.weatherLocations.indexOf(settings.loadedWeather);
            if((index > -1)&&(index < weatherListBx.getItems().size())) {
                weatherListBx.getSelectionModel().select(index);
            }
        }
    }
    
    /**
     * Serves as a centralized collection of the different steps necessary to 
     * update the main window of the GUI.
     */
    private void updateWindow() {
        updateOrganismInfo();
        updateWeatherInfo();
        updateModelWindow();
        updateGraphWindow();
        updateForecastWindow();
        updateSettingsWindow();
        populateStageNames();
        updateStats();
    }
}