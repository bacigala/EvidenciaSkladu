
package dialog.controller;

import databaseAccess.ConnectionFactory;
import dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

        // check ip pattern
        boolean validIp;
        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            validIp = matcher.matches();
        } catch (PatternSyntaxException ex) {
            validIp = false;
        }
        boolean validPort;
        try {
            Pattern pattern = Pattern.compile("^[0-9]{1,5}$");
            Matcher matcher = pattern.matcher(port);
            validPort = matcher.matches();
        } catch (PatternSyntaxException ex) {
            validPort = false;
        }

        if (!validIp) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Prosím, vyplňte platnú IP adresu.");
            Platform.runLater(() -> ipAddressTextField.requestFocus());
        } else if (!validPort) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Prosím, vyplňte platný port.");
            Platform.runLater(() -> portTextField.requestFocus());
        } else if  (ip.equals(cf.getDatabaseIp()) && port.equals(cf.getDatabasePort())) {
            // no changes made
            closeDialog();
        } else if (cf.setConnectionDetails(ip, port)) {
            // change successful and verified
            DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Pripojené.");
            closeDialog();

            //write new connection details to app properties
            try {
                Properties appProps = new Properties();
                Path PropertyFile = Paths.get("EvidenciaSkladu.properties");
                try {
                    Reader PropReader = Files.newBufferedReader(PropertyFile);
                    appProps.load(PropReader);
                } catch (Exception e) {
                    System.out.println("no recent property file found");
                }
                appProps.setProperty("server-ip", ip);
                appProps.setProperty("server-port", port);

                Writer PropWriter = Files.newBufferedWriter(PropertyFile);
                appProps.store(PropWriter,"Application Properties for app Evidencia Skladu");
                PropWriter.close();
            } catch (Exception Ex) {
                System.err.println("Properties Write Exception: " + Ex.getMessage());
            }

            return;
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
