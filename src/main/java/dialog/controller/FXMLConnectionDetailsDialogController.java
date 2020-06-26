
package dialog.controller;

import databaseAccess.ConnectionFactory;
import dialog.DialogFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class FXMLConnectionDetailsDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField ipAddressTextField;
    @FXML private javafx.scene.control.TextField portTextField;
    @FXML private javafx.scene.control.Button connectButton;

    @FXML
    private void connectButtonAction() {
        disableInput();

        // verify input
        String ip = ipAddressTextField.getText();
        String port = portTextField.getText();

        ConnectionFactory cf = ConnectionFactory.getInstance();

        if (ip.equals("")) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Prosím, vyplňte IP adresu.");
        } else if (port.equals("")) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Prosím, vyplňte port.");
        } else if  (ip.equals(cf.getDatabaseIp()) && port.equals(cf.getDatabasePort())) {
            // no changes made
            closeDialog();
        } else if (ConnectionFactory.getInstance().setConnectionDetails(ip, port)) {
            // change successful and verified
            DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Pripojené.");
            closeDialog();
        } else {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Nepodarlo sa poripojiť.");
        }
        enableInput();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        disableInput();
        // allow changes only if no connection can be established with current connection details
        ConnectionFactory cf = ConnectionFactory.getInstance();
        if (cf.hasValidConnectionDetails()) {
            ipAddressTextField.setText(cf.getDatabaseIp());
            portTextField.setText(cf.getDatabasePort());
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

    private void closeDialog() {
        ((Stage) connectButton.getScene().getWindow()).close();
    }
}
