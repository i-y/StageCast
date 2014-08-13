package stagecast;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import windows.ErrorWindow;

/**
 * Provides a way for the program to communicate errors to the user graphically 
 * as well as providing logging functionality.
 * <p>This class is used by the main program, when possible, as the way to
 * handle errors. The goal of this class is to prevent the user being frustrated 
 * by errors which appear mysterious, with no explanation of what has happened 
 * or why. To that end, this class will usually produce a pop up window 
 * containing two messages and the technical exception report. The one message 
 * which is mandatory is a non-technical message to the user about what has gone 
 * wrong. This message can be a simple statement of failure such  as "The 
 * program failed to load the settings file." or include a suggestion about what 
 * to do about it such as "The program failed to load the settings file. Please 
 * ensure that the program is set to load the file settings.xml."</p>
 * <p>The second, optional message is a technical report. This report can 
 * contain whatever is appropriate for the particular failure but should always 
 * state the class and method which generated it. This message is intended to be 
 * helpful mostly to technically-oriented users or developers but it should be 
 * clear enough to be useful to an adventurous non-technical user who desires to 
 * fix the code to avoid the error in the future. Related to the second message 
 * is the exception argument. This is the most technical portion of the error as 
 * it records the error thrown by the language itself. This is included in the 
 * output to the user because it may aid in the debugging process by both 
 * technical and non-technical users alike.</p>
 * <p>Lastly, this class adds logging ability to the program. All errors which 
 * pass through this class are logged to `log.txt` in the program's root 
 * directory for potential use later. The log entries contain the date and time 
 * of the event, both messages which were output to the user, and the exception 
 * value. The @ref log function is called directly for errors in window creation 
 * as the failure to open a JavaFX window in one case could indicate that this 
 * class could not successfully open one either.</p>
 * @author Ian Yocum
 * @date 5/31/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @todo Implement system to log stack traces.
 * @todo Possibly implement a priority system.
 */
public class ErrorManager {
    /**
     * Shows an error to the user without including a technical explanation.
     * @param msg The non-technical message about what has gone wrong.
     * @param e The exception which occurred.
     */
    public static void error(String msg, Exception e) {
        ErrorManager.error(msg, "", e);
    }
        
    /**
     * Shows an error to the user which includes a technical and non-technical 
     * explanation.
     * @param msg The non-technical message about what has gone wrong.
     * @param details The technical message about the error.
     * @param e The exception which occurred.
     */
    public static void error(String msg, String details, Exception e) {
        ErrorManager.log(msg + " " + details, e);
        ErrorWindow win = new ErrorWindow("Error", msg, details, e);
        win.show();
    }
        
    /**
     * Prints a message to standard output and logs the error.
     * @param msg The message about the error to be output on the command line.
     * @param err The exception to be logged to disk.
     */
    public static void log(String msg, Exception err) {
        System.out.println(msg + " " + err);
        try {
            try (BufferedWriter outpt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream ("log.txt", true)))) {
                Date date = new Date();
                outpt.append(date.toString() + "\r\n" + msg + "\r\n" + err + "\r\n\r\n");
            }
        } catch (IOException e) {
            System.out.println("Logging function has failed: " + e);
            System.out.println("Message which was not logged: " + msg + " : " + err);
        }
    }
    
    /**
     * Warns the user of some problem which does not need to be logged and does 
     * not cause problems to the program's normal functions.
     * @param msg The non-technical explanation of the warning.
     * @param details The technical explanation of the warning.
     */
    public static void warn(String msg, String details) {
        ErrorWindow win = new ErrorWindow("Warning", msg, details, null);
        win.show();
    }
}
