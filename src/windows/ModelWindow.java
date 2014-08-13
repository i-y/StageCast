package windows;

import data.cell.DisplayCell;
import data.cell.DisplayInfo;
import data.cell.DisplayItem;
import stagecast.ScriptManager;
import data.DatabaseObject;
import data.ModelObject;
import data.SettingsObject;
import stagecast.ErrorManager;
import stagecast.XmlManager;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Shows the model creation dialog to the user.
 * This class shows the user a window which displays the various options 
 * involved in creating a new model. Unlike with data importing, model creation
 * does not involve much automated support so the dialog options must be more
 * extensive.
 * @author Ian Yocum
 * @date 6/5/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class ModelWindow {
    private String defaultName; /*!< The pre-determined default name of the model to be created. Based on current date and time. */
    private ModelObject finalModel; /*!< The model which is returned to the caller.*/
    private DatabaseObject organism; /*!< Holds the organism data set to be used to make the model. */
    private SettingsObject settings; /*!< Holds the information relating to the default load/save directories. */
    private Stage stage; /*!< The class which describes an FXML window. This must be non-null before the window is shown. */
    private DatabaseObject weather; /*!< Holds the weather data set to be used to make the model. */
    
    public Button modelBtn; /*!< Create the model. */
    public TextField modelNameTxt; /*!< Name of the resultant model. */
    public CheckBox paramChckBx; /*!< Supply your own initial parameters or let them be estimated automatically. */
    public ListView<TextFieldListCell> paramList; /*!< List of custom input parameters. */
    public RadioButton optim1RBtn; /*!< Use Nelder-Mead optimization function. */
    public RadioButton optim2RBtn; /*!< Use BFGS optimization function. */
    public RadioButton optim3RBtn; /*!< Use CG optimization function. */
    public RadioButton optim4RBtn; /*!< Use L-BFGS-B optimization function. */
    public RadioButton optim5RBtn; /*!< Use SANN optimization function. */
    public ScrollPane organismScroll; /*!< Lists all organisms included in the currently loaded dataset and allows the user to choose which ones to include in the model. */
    public ComboBox<String> organismListBx; /*!< Lists all available organism datasets. */
    public ListView<DisplayItem> stagesList; /*!< A list of the organism's life stages such that the user can combine and rearrange them as desired. */
    public TextField stagesTxt; /*!< Shows the number of life stages for the currently loaded organism dataset. */
    public ComboBox<String> weatherListBx; /*!< Lists all available weather datasets. */
    
	
    /**
     * Constructor for the window.
     * @param so SettingsObject to use while creating the model.
     */
    public ModelWindow(SettingsObject so) {
        try {
            settings = so;
            finalModel = null;
            if(!settings.loadedOrganism.equals("")) {
                organism = XmlManager.readDatabaseFile(settings.loadedOrganism);
            }
            if(!settings.loadedWeather.equals("")) {
                weather = XmlManager.readDatabaseFile(settings.loadedWeather);
            }
            DateFormat dateFormat = new SimpleDateFormat("MM.dd.HH.mm.ss");
            Date date = new Date();
            defaultName = "Model-" + dateFormat.format(date);
        } catch(Exception e) {
            System.out.println("Error initializing ModelWindow: " + e);
        }
    }
    
    /**
     * Loads an organism dataset.
     * This method allows the user to choose a new organism database to use for 
     * model creation.
     * @note It is not necessary to use all members of a particular database in
     * model creation.
     */
    public void loadOrganism() {
        if ((organismListBx != null) && (organismListBx.getItems().size() > 0)&&(organism != null) && (!organismListBx.getValue().equals(organism.name))) {
            int index = organismListBx.getSelectionModel().getSelectedIndex();
            DatabaseObject temp = XmlManager.readDatabaseFile(settings.organismLocations.get(index));
            if(temp != null) {
                organism = temp;
                settings.loadedOrganism = temp.location;
                populate();
                setupStageList();
            }
        }
    }
	
    /**
     * Loads a weather dataset.
     * This method allows the user to choose a new weather database to use for 
     * model creation.
     */
    public void loadWeather() {
        if ((weatherListBx != null) && (weatherListBx.getItems().size() > 0)&&(weather != null) && (!weatherListBx.getValue().equals(weather.name))) {
            int index = weatherListBx.getSelectionModel().getSelectedIndex();
            DatabaseObject temp = XmlManager.readDatabaseFile(settings.weatherLocations.get(index));
            if(temp != null) {
                weather = temp;
                settings.loadedWeather = temp.location;
                populate();
                setupStageList();
            }
        }
    }
	
    /**
     * Creates the model.
     * This method takes the currently selected options and databases and calls 
     * the R script which produces the model. It then constructs a ModelObject 
     * which describes the created model.
     */
    public void model() {
        VBox box = (VBox) organismScroll.getContent();
        ObservableList<Node> items = box.getChildren();
        ArrayList<String> orgList = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) {
            CheckBox c = (CheckBox) items.get(i);
            if (c.isSelected()) {
                orgList.add(c.getId());
            }
        }
        String o[] = new String[orgList.size()];
        o = orgList.toArray(o);
        
        ArrayList<String> stages = new ArrayList<>(Arrays.asList(organism.memberStages));
        
        try {
            ModelObject model = new ModelObject();
            model.organismCount = o.length;
            model.OrganismFiles = o;
            model.weatherDatabaseLocation = weather.memberLocations[0];
            for(DisplayItem item : stagesList.getItems()) {
                List<DisplayInfo> content = item.getContent();
                for(DisplayInfo di : content) {
                    model.stageMap.add(stages.indexOf(di.name) + ":" + stages.indexOf(content.get(0).name));
                }
            }
            if(optim1RBtn.isSelected()) { 
                model.optim = "Nelder-Mead";
            } else if(optim2RBtn.isSelected()) { 
                model.optim = "BFGS";
            } else if(optim3RBtn.isSelected()) { 
                model.optim = "CG";
            } else if(optim4RBtn.isSelected()) { 
                model.optim = "L-BFGS-B";
            } else if(optim5RBtn.isSelected()) { 
                model.optim = "SANN";
            }
            if(paramChckBx.isSelected()) {
                model.params = new String[paramList.getItems().size()];
                int i = 0;
                for(TextFieldListCell t : paramList.getItems()) {
                    model.params[i++] = t.getText();
                }
            }
            ModelObject outputModel = ScriptManager.model(model);
            model.params = Arrays.copyOf(outputModel.params, outputModel.params.length);
            model.logLikelihood = outputModel.logLikelihood;
            model.aic = outputModel.aic;
            model.paramCount = model.params.length;
            model.OrganismDatabaseName = organism.name;
            model.OrganismDatabaseLocation = organism.location;
            model.weatherDatabaseName = weather.name;
            if(modelNameTxt.getText().isEmpty()) {
                modelNameTxt.setText(defaultName);
                model.name = defaultName;
            } else {
                model.name = modelNameTxt.getText();
            }
            Date d = new Date();
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            model.date = dateFormat.format(d);
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            model.time = dateFormat.format(d);
            finalModel = model;
        } catch (Exception e) {
            ErrorManager.log("ModelWindow.model() encountered an error as it tried to add the new model to the existing database of models.", e);
        }
        stage.close();
    }

    /**
     * Populates the lists of organism and weather datasets with their members 
     * as well as filling in organismScroll with the organisms for the currently 
     * loaded organism dataset.
     */
    private void populate() {
        
        ObservableList<String> elements =  FXCollections.observableArrayList();
        
        elements.setAll(settings.organismNames);
        organismListBx.getItems().setAll(elements);
        if(!settings.loadedOrganism.equals("")) {
            if (!new File(settings.loadedOrganism).exists()) {
                settings.loadedOrganism = "";
            } else {
                int index = settings.organismLocations.indexOf(settings.loadedOrganism);
                if(index > -1) {
                    organismListBx.getSelectionModel().select(index);
                }
            }
            stagesTxt.setText(String.valueOf(organism.stages));
        }
        elements.setAll(settings.weatherNames);
        weatherListBx.getItems().setAll(elements);
        if(!settings.loadedWeather.equals("")) {
            if (!(new File(settings.loadedWeather).exists())) {
                settings.loadedWeather = "";
            } else {
                int index = settings.weatherLocations.indexOf(settings.loadedWeather);
                if(index > -1) {
                    weatherListBx.getSelectionModel().select(index);
                }
            }
        }
        if (organism != null) {
            ObservableList<CheckBox> options =  FXCollections.observableArrayList();
            for(int i = 0; i < organism.size; i++) {
                CheckBox c = new CheckBox(organism.memberNames[i]);
                c.setId(organism.memberLocations[i]);
                c.setSelected(true);
                c.getStyleClass().add("model-checkbox");
                options.add(c);
            }
            VBox box = new  VBox();
            box.getChildren().addAll(options);
            organismScroll.setContent(box);
        }
    }
    
    private void setupCellFactories() {
        try {
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
                        ErrorManager.log("Exception was raised in the toString method of the callback created in ModelWindow.setupCellFactories", e);
                    }
                    return ret;
                }
            });
            paramList.setCellFactory(onCommit);
        } catch (Exception e) {
            ErrorManager.log("Error in ModelWindow.setupCellFactories", e);
        }
    }
    
    /**
     * Creates and populates the drag-and-drop control used to let the user 
     * reorder and combine stages.
     */
    private void setupStageList() {
        if (organism != null) {
            List<DisplayItem> stageOptions =  new CopyOnWriteArrayList<>();
            for (String memberStage : organism.memberStages) {
                stageOptions.add(new DisplayItem(new DisplayInfo(memberStage)));
            }
            ObservableList<DisplayItem> stageFinal = FXCollections.observableArrayList();
            stageFinal.addAll(stageOptions);
            stagesList.setItems(stageFinal);
            stagesList.setCellFactory(new Callback<ListView<DisplayItem>, ListCell<DisplayItem>>() {
                @Override 
                public DisplayCell call(ListView<DisplayItem> list) {
                    return new DisplayCell();
                }
            });
        }
    }
	
    /**
     * Adds tooltips to GUI elements.
     */
    private void setupTooltips() {
        modelBtn.setTooltip(new Tooltip("Create model."));
        modelNameTxt.setTooltip(new Tooltip("Set the name for this model."));
        optim1RBtn.setTooltip(new Tooltip("Derivative-free hill-climbing optimization algorithm that creates a simplex \"amoeba\" that crawls up a surface, changing its shape to conform to different curvatures.  It is slow, but experience suggests that it is robust to some problematic surfaces such as ridge-shaped likelihoods."));
        optim2RBtn.setTooltip(new Tooltip("Quasi-Newton method for unconstrained hill-climbing optimization that avoids the need for evaluating second derivatives.  It is robust and fast for well behaved likelihood functions."));
        optim3RBtn.setTooltip(new Tooltip("Similar to BFGS but sometimes performs better on bigger problems with many parameters."));
        optim4RBtn.setTooltip(new Tooltip("The BFGS method using upper and lower bounds."));
        optim5RBtn.setTooltip(new Tooltip("Simulated annealing."));
        organismListBx.setTooltip(new Tooltip("Choose an organism dataset to use."));
        organismScroll.setTooltip(new Tooltip("Choose which dataset members to include in the model."));
        stagesTxt.setTooltip(new Tooltip("Number of life stages in the organism dataset."));
        weatherListBx.setTooltip(new Tooltip("Choose a weather dataset to use."));
        stagesList.setTooltip(new Tooltip("Drag and drop stages on to one another to combine them in the output."));
    }
    
    /**
     * Initializes the window in perpetration for showing it to the user.
     */
    private void setupWindow() {
        stage = null;
        try {
            FXMLLoader fl = new FXMLLoader(getClass().getResource("ModelWindow.fxml"));
            fl.setController(this);
            fl.load();
            Parent root = fl.getRoot();
            stage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(ModelWindow.class.getResource("/etc/css/modelSkin.css").toExternalForm());
            stage.setTitle("New Model");
            modelNameTxt.setText(defaultName);
            stage.setScene(scene);
        } catch (IOException e) {
            ErrorManager.log("Error setting up ImportWindow", e);
        }
    }
    
    /**
     * Shows the window to the user.
     * @return ModelObject holding the calculated model information.
     */
    public ModelObject show() {
        setupWindow();
        setupStageList();
        setupCellFactories();
        toggleParams();
        if(settings.tooltips) {
            setupTooltips();
        }
        if(stage != null) {
            populate();
            stage.showAndWait();
        }
        return finalModel;
    }
    
    /**
     * Allows the user to supply starting parameters.
     */
    public void toggleParams() {
        if(paramChckBx.isSelected()) {
           paramList.setDisable(false);
           ObservableList<DisplayItem> items = stagesList.getItems();
           int stageCount = 0;
           for(DisplayItem item : items) {
               if(item.getContent().size() > 0) {
                   stageCount++;
               }
           }
           ObservableList<TextFieldListCell> paramItems = FXCollections.observableArrayList();
           for(int i = 0; i < stageCount; i++) {
               TextFieldListCell t = new TextFieldListCell();
               t.setText("0");
               paramItems.add(t);
           }
           paramList.setItems(paramItems);
        } else {
            paramList.getItems().clear();
            paramList.setDisable(true);
        }
    }
}
