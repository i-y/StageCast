package data;

/**
 * Holds data used in the forecasting operations.
 * @author ian.yocum
 * @date 4/02/2014
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @todo Document class members.
 */
public class ForecastObject {
    public boolean error;
    public boolean fit;
    public boolean fitAlone; /*!< All stages on their own figure if true. */
    public String fitHigh;
    public String fitLow;
    public String heigth;
    public ModelObject model;
    public String outputLoc;
    public boolean predict;
    public String predictHigh;
    public String predictLow;
    public String proportionPercent;
    public String proportionStage;
    public String stageNames[]; 
    public boolean tendencies;
    public String weather;
    public String width;
    
    public ForecastObject() {
        error = false;
        fit = false;
        fitHigh = "";
        fitLow = "";
        fitAlone = false;
        heigth = "";
        model = null;
        outputLoc = "";
        tendencies = false;
        predict = false;
        predictHigh = "";
        predictLow = "";
        proportionPercent = "";
        proportionStage = "";
        stageNames = new String[0];
        weather = "";
        width = "";
    }
}
