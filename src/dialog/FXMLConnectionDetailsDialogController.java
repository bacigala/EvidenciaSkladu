
package dialog;

import java.net.URL;
import java.util.ResourceBundle;

import databaseAccess.QueryHandler;
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
        QueryHandler qh = QueryHandler.getInstance();
        if (qh.hasConnectionDetails()) {

        } else {
            String ip = ipAddressTextField.getText();
            String port = portTextField.getText();
            if (qh.setBasicUserConnectionDetails(ip, port)) {
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
        QueryHandler qh = QueryHandler.getInstance();
        if (qh.hasConnectionDetails()) {
            ipAddressTextField.setText(qh.getDatabaseIp());
            portTextField.setText(qh.getDatabasePort());
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
