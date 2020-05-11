
package dialog;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import databaseAccess.Category;
import databaseAccess.Item;
import databaseAccess.QueryHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXMLItemSupplyDialogController implements Initializable {

    @FXML private javafx.scene.control.Label newAmountLabel;
    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField codeTextField;
    @FXML private javafx.scene.control.TextField curAmountTextField;
    @FXML private javafx.scene.control.TextField minAmountTextField;
    @FXML private javafx.scene.control.TextField unitTextField;
    @FXML private javafx.scene.control.ChoiceBox<Category> categoryChoiceBox;
    @FXML private javafx.scene.control.TextField newAmountTextField;
    @FXML private javafx.scene.control.DatePicker newExpirationDatePicker;
    
    private Item item;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }  
    
    public void initData(Item item) {
        if (item != null) {
            this.item = item;
            nameTextField.setText(item.getName());
            codeTextField.setText(item.getBarcode());
            curAmountTextField.setText(Integer.toString(item.getCurAmount()));
            minAmountTextField.setText(Integer.toString(item.getMinAmount()));
            unitTextField.setText(item.getUnit());
            newAmountLabel.setText("Množstvo (" + item.getUnit() + ")");
        }
    }
    
    // button "Vklad"
    @FXML
    private void supplyButton() throws IOException {
        QueryHandler qh = QueryHandler.getInstance();
        DialogFactory df = DialogFactory.getInstance();
        try {
            if (qh.itemSupply(item.getId(), Integer.parseInt(newAmountTextField.getText()), newExpirationDatePicker.getValue())) {
                DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Vklad položky prebehol úspešne.");
                cancelButton();
            } else {
                df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať. Skontrolujte prosím zadané hodnoty.");
            }
        } catch (Exception e) {
            df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať. Skontrolujte prosím zadané hodnoty.");
        }
    }
    
    // button "Zrušiť"
    @FXML
    private void cancelButton() {
        Stage stage = (Stage) newAmountLabel.getScene().getWindow();
        stage.close();
    }
    
}
