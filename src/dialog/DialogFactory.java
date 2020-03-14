package dialog;

import databaseAccess.QueryHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Creates and shows dialogs.
 */

public class DialogFactory {
    /**
     * The only singleton instance of the class.
     */
    private static final DialogFactory singletonInstance = new DialogFactory();

    /**
     * Empty private constructor - singleton class.
     */
    private DialogFactory() {}

    /**
     * Access to the only singleton instance of the class.
     * @return singleton instance of the class
     */
    public static DialogFactory getInstance() {
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

    /**
     * Shows connection details dialog for ip and port change.
     * @return true on valid connection details provided
     */
    public boolean showConnectionDetailsDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLConnectionDetailsDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Server");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return QueryHandler.getInstance().hasConnectionDetails();
    }

    /**
     * Shows user login dialog requiring username and password.
     * @return true on successful login
     */
    public boolean showUserLoginDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLUserLoginDialog.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Prihlásenie");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return QueryHandler.getInstance().hasUser();
    }

}
