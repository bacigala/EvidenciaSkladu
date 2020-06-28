
package dialog.controller;

import databaseAccess.CustomExceptions.UserWarningException;
import databaseAccess.ItemDAO;
import dialog.DialogFactory;
import domain.Item;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
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

        // verify input
        int itemAmouunt;
        try {
            if (newAmountTextField.getText().equals("")) throw new IllegalArgumentException();
            itemAmouunt = Integer.parseInt(newAmountTextField.getText());
        } catch (Exception e) {
            df.showAlert(Alert.AlertType.ERROR, "Prosím, vyplňte platné množstvo.");
            Platform.runLater(() -> newAmountTextField.requestFocus());
            return;
        }

        LocalDate expiration;
        try {
            if (newExpirationDatePicker.getValue().toString().equals("")) throw new IllegalArgumentException();
            expiration = newExpirationDatePicker.getValue();
        } catch (Exception e) {
            df.showAlert(Alert.AlertType.ERROR, "Prosím, vyplňte platný dátm expirácie.");
            Platform.runLater(() -> newExpirationDatePicker.requestFocus());
            return;
        }


        try {
            ItemDAO.getInstance().itemSupply(item.getId(), itemAmouunt, expiration);
            df.showAlert(Alert.AlertType.INFORMATION, "Vklad položky prebehol úspešne.");
        } catch (UserWarningException e) {
            df.showAlert(Alert.AlertType.WARNING, e.getMessage());
        } catch (Exception e) {
            df.showAlert(Alert.AlertType.ERROR, "Neočakávaná chyba.");
        } finally {
            cancelButton();
        }
    }
    
    // button "Zrušiť"
    @FXML
    private void cancelButton() {
        Stage stage = (Stage) newAmountLabel.getScene().getWindow();
        stage.close();
    }
    
}
