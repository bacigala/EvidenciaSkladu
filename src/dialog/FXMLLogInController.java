
package dialog;

import java.net.URL;
import java.util.ResourceBundle;

import databaseAccess.QueryHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;

public class FXMLLogInController implements Initializable {

    @FXML private javafx.scene.control.Button loginButton;
    @FXML private javafx.scene.control.TextField usernameTextField;
    @FXML private javafx.scene.control.TextField passwordTextField;
    @FXML private javafx.scene.control.Label infoLabel;
    
    @FXML
    private void loginButtonAction(ActionEvent e) {
        QueryHandler qh = QueryHandler.getInstance();
        if (qh.hasConnectionDetails()) {
            if (qh.hasUser()) {
                // odhlasenie
                qh.logOut();
                infoLabel.setTextFill(Color.web("red"));
                infoLabel.setText("Prihláste sa");
                loginButton.setText("Prihlásiť sa");
                enableInput();
            } else {
                //prihlasenie
                String username = usernameTextField.getText();
                String password = passwordTextField.getText();
                if (qh.logIn(username, password)) {
                    infoLabel.setTextFill(Color.web("green"));
                    infoLabel.setText("Vitajte, " + qh.getLoggedUserName());
                    loginButton.setText("Odhlásiť sa");
                    disableInput();
                } else {
                    infoLabel.setTextFill(Color.web("red"));
                    infoLabel.setText("Nepodarilo sa pripojiť");
                }              
            }                        
        } else {
            // chyba - najprv pripoj server
            infoLabel.setTextFill(Color.web("red"));
            infoLabel.setText("Server nedostupný!");
            disableInput();           
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        QueryHandler qh = QueryHandler.getInstance();
        if (qh.hasConnectionDetails()) {
            if (qh.hasUser()) {
                // odhlasenie
                infoLabel.setTextFill(Color.web("green"));
                infoLabel.setText(qh.getLoggedUserName());
                loginButton.setText("Odhlásiť");
                disableInput();
            } else {
                //prihlasenie
                qh.logOut();
                infoLabel.setText("");
                loginButton.setText("Prihlásiť sa");
                enableInput();           
            }                        
        } else {
            // chyba - najprv pripoj server
            infoLabel.setTextFill(Color.web("red"));
            infoLabel.setText("Server nedostupný!");
            disableInput();           
        }
    }   
    
    private void enableInput() {
        usernameTextField.setDisable(false);
        passwordTextField.setDisable(false);
    }
    
    private void disableInput() {
        usernameTextField.setDisable(true);
        passwordTextField.setDisable(true);
    }
    
}
