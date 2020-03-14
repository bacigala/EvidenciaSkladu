package dialog;

/**
 * Creates ands shows dialogs.
 */

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DialogFactory {
    /**
     * The only singleton instance of the class.
     */
    private static final DialogFactory singletonInstance = new DialogFactory();

    /**
     * Empty constructor - singleton class.
     */
    public DialogFactory() {}

    /**
     * Access to the only singleton instance of the class.
     * @return singleton instance of the class
     */
    public DialogFactory getInstance() {
        return singletonInstance;
    }

    /**
     * Displays basic alert.
     * @param alertType Type of the alert window.
     * @param message Message shown in alert window.
     */
    public void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);

        switch (alertType) {
            case INFORMATION:
                alert.setTitle("Info");
                break;
            case WARNING:
                alert.setTitle("Pozor!");
                break;
            case ERROR:
                alert.setTitle("Chyba!");
                break;
        }

        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showCustomSuccessDialog() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLSuccessDialog.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLSuccessDialogController controller = fxmlLoader.<FXMLSuccessDialogController>getController();

        // default db connection details - for test purposes
        controller.initData("hello");
        stage.setTitle("Server");
        stage.showAndWait();
    }

}