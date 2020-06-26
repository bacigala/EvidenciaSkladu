package dialog;

import databaseAccess.ConnectionFactory;
import databaseAccess.Login;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Creates and shows dialogs.
 * Singleton.
 */

public class DialogFactory {
    // singleton
    private static final DialogFactory singletonInstance = new DialogFactory();
    private DialogFactory() {}
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLConnectionDetailsDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Server");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ConnectionFactory.getInstance().hasValidConnectionDetails();
    }

    /**
     * Shows user login dialog requiring username and password.
     * @return true on successful login
     */
    public boolean showUserLoginDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLUserLoginDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Prihlásenie");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Login.getInstance().hasUser();
    }

}
