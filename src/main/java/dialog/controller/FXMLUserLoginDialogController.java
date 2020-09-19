
package dialog.controller;

import databaseAccess.ConnectionFactory;
import databaseAccess.Login;
import dialog.DialogFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;

public class FXMLUserLoginDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField usernameTextField;
    @FXML private javafx.scene.control.TextField passwordTextField;
    @FXML private javafx.scene.control.Button loginButton;

    /**
     * BUTTON "Prihlasit sa" Performs user login / logoff.
     */
    @FXML
    private void loginButtonAction() {
        DialogFactory df = DialogFactory.getInstance();
        if (ConnectionFactory.getInstance().hasValidConnectionDetails()) {
            if (Login.getInstance().hasUser()) {
                String lastLoggedUserName = Login.getInstance().getLoggedUserFullName();
                Login.getInstance().logOut();
                df.showAlert(Alert.AlertType.INFORMATION, "Používateľ: " +
                        lastLoggedUserName + " bol odhlásený.");
                loginButton.setText("Prihlásiť sa");
                enableInput();
            } else {
                String username = usernameTextField.getText();
                String password = passwordTextField.getText();

                // verify input
                if (username.equals("") || username.length() > 50) {
                    df.showAlert(Alert.AlertType.ERROR, "Prosím, vyplňte platné prihlasovacie meno.");
                    Platform.runLater(() -> usernameTextField.requestFocus());
                    return;
                }
                if (password.equals("") || password.length() > 50) {
                    df.showAlert(Alert.AlertType.ERROR, "Prosím, vyplňte platné heslo.");
                    Platform.runLater(() -> passwordTextField.requestFocus());
                    return;
                }

                if (Login.getInstance().logIn(username, password)) {
                    // successful login
                    disableInput();
                    df.showAlert(Alert.AlertType.INFORMATION, "Prihlásený používateľ: " +
                            Login.getInstance().getLoggedUserFullName() + ".");
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.close();
                } else {
                    // unsuccessful login
                    df.showAlert(Alert.AlertType.ERROR, "Nesprávne prihlasovacie údaje.");
                    usernameTextField.requestFocus();
                }              
            }                        
        } else {
            // error - no server connection
            df.showAlert(Alert.AlertType.ERROR, "Nepodarilo sa pripojiť k databáze.");
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startUp();
    }   
    
    private void enableInput() {
        usernameTextField.setDisable(false);
        passwordTextField.setDisable(false);
        loginButton.setDisable(false);
    }
    
    private void disableInput() {
        usernameTextField.setDisable(true);
        passwordTextField.setDisable(true);
    }

    /**
     * MENU ITEM "Pripojenie -> Nastavenia" Opens connection details dialog.
     */
    @FXML
    private void openConnectionDetails() {
        DialogFactory.getInstance().showConnectionDetailsDialog();
        startUp();
    }

    private void startUp() {
        DialogFactory df = DialogFactory.getInstance();
        if (ConnectionFactory.getInstance().hasValidConnectionDetails()) {
            if (Login.getInstance().hasUser()) {
                // user logged in - offer logout
                disableInput();
                usernameTextField.setText(Login.getInstance().getLoggedUserUsername());
                loginButton.setText("Odhlásiť");
                Platform.runLater(() -> loginButton.requestFocus());
            } else {
                // nobody logged in - offer login
                loginButton.setText("Prihlásiť sa");

                String username = "";
                // try to fetch username from app properties
                try {
                    Properties appProps = new Properties();
                    Path PropertyFile = Paths.get("EvidenciaSkladu.properties");
                    Reader PropReader = Files.newBufferedReader(PropertyFile);
                    appProps.load(PropReader);

                    username = appProps.getProperty("username", "");
                    PropReader.close();
                } catch (IOException e) {
                    System.err.println("PropertiesFileNotFoundException: " + e.getMessage());
                }

                usernameTextField.setText(username);
                enableInput();
                if (username.equals("")) {
                    Platform.runLater(() -> usernameTextField.requestFocus());
                } else {
                    Platform.runLater(() -> passwordTextField.requestFocus());
                }
            }
        } else {
            // error - no server connection
            disableInput();
            loginButton.setDisable(true);
            df.showAlert(Alert.AlertType.ERROR, "Nepodarilo sa pripojiť k databáze.");
        }
    }
    
}
