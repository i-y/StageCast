package data;

import java.util.ArrayList;

/**
 * Stores settings used to perform further statistical operations on a dataset.
 * This class is used to facilitate two-way communication between the main 
 * program and the R script responsible for performing further statistical 
 * operations. All the information needed by the script must be contained inside 
 * this class and the final values after the calculations have been run must be 
 * returned in this class.
 * @note The variable names in this class are designed to resemble the variable 
 * names used in the targeted script.
 * @note The parameters are divided in to two groups, the `a` group holds all 
 * values but the last in the set with the last parameter labeled `v`.
 * @author Ian Yocum
 * @date 11/19/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @todo Fix the current, ridiculous way model data is stored and passed along 
 * for forecasting. Have the mean and median as well as the high-low ranges 
 * stored instead of recording the entire output of the bootstrapping algorithm.
 */
public class StatsObject {
    public String[] a; /*!< Basic model parameters. (for elements [0,...,n-1]) */
    public String[] aHigh; /*!< High estimates for the model parameters. */
    public String[] aLow; /*!< Low estimates for the model parameters. */
    public String alpha; /*!< The alpha value used in the calculations. */
    public String[][] aStar; /*!< Output as part of the calculations but not currently used by the program. */
    public String gg; /*!< The g-squared value. */
    public String[] ggci; /*!< The confidence interval for the g-squared value. The high value is element 0 and the low value is element 1.  */
    public String ggPval; /*!< The p-value for g-squared. */
    public String[] ggStar; /*!< Output as part of the calculations but not currently used by the program. */
    public String iterations; /*!< Number of iterations used to produce the final output. */
    public String optim;
    public String stages; /*!< Number of stages in the chosen organism dataset. */
    public ArrayList<String> stageMap; /*!< Maps the stages on to each other. */
    public String v; /*!< Basic model parameter. (for element [n]) */
    public String vHigh; /*!< High estimates for the model parameter. */
    public String vLow; /*!< Low estimates for the model parameter. */
    public String[] vStar; /*!< Output as part of the calculations but not currently used by the program. */
    public String xx; /*!< The chi-squared statistic. */
    public String xxHigh; /*!< High estimate for the chi-squared p-value. */
    public String xxLow; /*!< Low estimate for the chi-squared p-value. */
    public String xxPval; /*!< The calculated p-value of the chi-squared statistic. */

    /**
     *
     */
    public StatsObject() {
        a = new String[0];
        aHigh = new String[0];
        aLow = new String[0];
        alpha = "0.05";
        aStar = new String[0][0];
        gg = "";
        ggci = new String[2];
        ggci[0] = "";
        ggci[1] = "";
        ggPval = "";
        ggStar = new String[0];
        iterations = "1000";
        optim = "";
        stages = "0";
        v = "";
        vHigh = "";
        vLow = "";
        vStar = new String[0];
        xx = "";
        xxHigh = "";
        xxLow = "";
        xxPval = "";
        stageMap = new ArrayList<>();
    }
}
