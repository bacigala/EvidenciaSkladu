
package dialog.controller;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

import databaseAccess.ConnectionFactory;
import databaseAccess.QueryHandler;
import dialog.DialogFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class FXMLConnectionDetailsDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField ipAddressTextField;
    @FXML private javafx.scene.control.TextField portTextField;
    @FXML private javafx.scene.control.Button connectButton;

    @FXML
    private void connectButtonAction(ActionEvent e) {
        ConnectionFactory cf = ConnectionFactory.getInstance();
        if (cf.hasValidConnectionDetails()) {

        } else {
            String ip = ipAddressTextField.getText();
            String port = portTextField.getText();
            if (ConnectionFactory.getInstance().setConnectionDetails(ip, port)) {
                disableInput();
                DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Pripojené.");
                Stage stage = (Stage) connectButton.getScene().getWindow();
                stage.close();
            } else {
                DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Nepodarlo sa poripojiť.");
            }
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ConnectionFactory cf = ConnectionFactory.getInstance();
        if (cf.hasValidConnectionDetails()) {
            ipAddressTextField.setText(cf.getDatabaseIp());
            portTextField.setText(cf.getDatabasePort());
            disableInput();
        } else {
            enableInput();
        }
    }
    
    private void enableInput() {
        ipAddressTextField.setDisable(false);
        portTextField.setDisable(false);
        connectButton.setDisable(false);
    }   
    
    private void disableInput() {
        ipAddressTextField.setDisable(true);
        portTextField.setDisable(true);
        connectButton.setDisable(true);
    }
}
