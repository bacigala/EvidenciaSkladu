
package dialog.controller;

import databaseAccess.ItemDAO;
import dialog.DialogFactory;
import domain.Item;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class FXMLItemSupplyDialogController implements Initializable {

    @FXML private javafx.scene.control.Label newAmountLabel;
    @FXML private javafx.scene.control.TextField newAmountTextField;
    @FXML private javafx.scene.control.DatePicker newExpirationDatePicker;
    
    private Item item;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }  
    
    public void initData(Item item) {
        if (item == null) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Chyba, neplatné údaje.");
            cancelButton();
            return;
        }
        this.item = item;
        newAmountLabel.setText("Množstvo (" + item.getUnit() + ")");
    }
    
    // button "Vklad"
    @FXML
    private void supplyButton() {
        DialogFactory df = DialogFactory.getInstance();
        try {
            if (ItemDAO.getInstance().itemSupply(item.getId(), Integer.parseInt(newAmountTextField.getText()), newExpirationDatePicker.getValue())) {
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
