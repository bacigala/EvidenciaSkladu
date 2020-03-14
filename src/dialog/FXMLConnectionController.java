
package dialog;

import java.net.URL;
import java.util.ResourceBundle;

import databaseAccess.ConnectionDetails;
import databaseAccess.QueryHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FXMLConnectionController implements Initializable {

    @FXML private javafx.scene.control.Button cancelButton;
    @FXML private javafx.scene.control.Button connectButton;
    @FXML private javafx.scene.control.TextField ipAddressTextField;
    @FXML private javafx.scene.control.TextField portTextField;
    @FXML private javafx.scene.control.TextField usernameTextField;
    @FXML private javafx.scene.control.TextField passwordTextField;
    @FXML private javafx.scene.control.Label infoLabel;

    @FXML
    private void cancelButtonAction(ActionEvent event){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void connectButtonAction(ActionEvent e) {
        QueryHandler qh = QueryHandler.getInstance();
        if (qh.hasConnectionDetails()) {
            if (qh.dropConnectionDetails()) {
                qh.logOut();
                connectButton.setText("Pripojiť");
                infoLabel.setTextFill(Color.web("red"));
                infoLabel.setText("Nepripojené");
                enableInput();
            }
        } else {
            String ip = ipAddressTextField.getText();
            String port = portTextField.getText();
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();
            ConnectionDetails cd = new ConnectionDetails (ip, port, "zubardb",
                    username, password);
            if (qh.setConnectionDetails(cd)) {
                infoLabel.setTextFill(Color.web("green"));
                infoLabel.setText("Pripojené");
                connectButton.setText("Odpojiť");
                disableInput();
            } else {
                infoLabel.setTextFill(Color.web("red"));
                infoLabel.setText("Nepodarilo sa pripojiť");
            }
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        QueryHandler qh = QueryHandler.getInstance();
        if (qh.hasConnectionDetails()) {
            infoLabel.setTextFill(Color.web("green"));
            infoLabel.setText("Pripojené");
            disableInput();
            connectButton.setText("Odpojiť");
            ConnectionDetails cd = qh.getConnectionDettails();
            ipAddressTextField.setText(cd.getIp());
            portTextField.setText(cd.getPort());
            usernameTextField.setText(cd.getUsername());
            passwordTextField.setText(cd.getPassword());
        } else {
            infoLabel.setTextFill(Color.web("red"));
            infoLabel.setText("Nepripojené");
            enableInput();
            connectButton.setText("Pripojiť");
        }
    }    
    
    public void initData(ConnectionDetails cd) {
        if (cd != null) {
            ipAddressTextField.setText(cd.getIp());
            portTextField.setText(cd.getPort());
            usernameTextField.setText(cd.getUsername());
            passwordTextField.setText(cd.getPassword());
        }
    }
    
    private void enableInput() {
        ipAddressTextField.setDisable(false);
        portTextField.setDisable(false);
        usernameTextField.setDisable(false);
        passwordTextField.setDisable(false);
    }   
    
    private void disableInput() {
        ipAddressTextField.setDisable(true);
        portTextField.setDisable(true);
        usernameTextField.setDisable(true);
        passwordTextField.setDisable(true);
    }
    
}
