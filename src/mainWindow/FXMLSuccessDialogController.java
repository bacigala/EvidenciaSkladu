
package mainWindow;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

public class FXMLSuccessDialogController implements Initializable {

    @FXML private javafx.scene.control.Label infoLabel;
    @FXML private javafx.scene.control.Button confirmButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
   
    } 
    
    public void initData(String message) {
        if (message != null && !message.equals("")) {
            infoLabel.setText(message);
        }
    }
    
    @FXML
    private void confirmButtonAction() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
    
}
