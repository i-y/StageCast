package windows;

import stagecast.ErrorManager;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Displays an error message to the user.
 * @author Ian Yocum
 * @date 6/5/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 * @todo Expand logging functionality.
 * @todo Add warning functionality.
 */
public class ErrorWindow {
    private String details; /*!< The more technical explination for the error. May include the Java-generated error code. */
    private String message; /*!< The human-readable explination for the error. */
    private Stage stage;  /*!< The class which describes an FXML window. This must be non-null before the window is shown. */
    private String title; /*!< The title of the message window. */
	
    public TextArea errArea; /*!< Displays the technical error information to the user. */
    public TextArea msgArea; /*!< Displays the error message to the user. */
	
    /**
     * @brief Constructor for the window.
     * @param title The title that the message window should have. Usually this 
     * is just "Error" but could expand to other alert types later.
     * @param msg The simple, more human readable message.
     * @param details A more detailed explanation that may include more 
     * technical terms as well as stating where in the code it came from.
     * @param err The Java-generated error code itself (can be null).
     */
    public ErrorWindow(String title, String msg, String details, Exception err) {
        try {
            this.title = title;
            message = msg;
            System.out.println("Error Message: " + msg);
            System.out.println("Error Details: " + details);
            if (details != null) {
                this.details = details;
            } else {
                this.details = msg;
            }
            if(err != null) {
                this.details = this.details + "\n\nerror code: " + err;
            }
        } catch(Exception e) {
            ErrorManager.log("Error initializing ErrorWindow", e);
        }
    }
	
    /**
     * Closes the window.
     */
    public void close() {
        stage.close();
    }
	
    /**
     * Initializes the window in perpetration for showing it to the user.
     */
    private void setupWindow() {
        stage = null;
        try {
            FXMLLoader fl = new FXMLLoader(getClass().getResource("ErrorWindow.fxml"));
            fl.setController(this);
            fl.load();
            Parent root = fl.getRoot();
            stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            msgArea.appendText(message);
            errArea.appendText(details);          
        } catch (IOException e) {
            ErrorManager.log("Error setting up ErrorWindow", e);
        }
    }
     
    /**
     * Shows the window to the user.
     */
    public void show() {
        setupWindow();
        if(stage != null) {
            stage.showAndWait();
        }
    }
}
