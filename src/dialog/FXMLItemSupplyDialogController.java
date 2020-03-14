
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
        if (qh.itemSupply(item.getId(), Integer.parseInt(newAmountTextField.getText()), newExpirationDatePicker.getValue())) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLSuccessDialog.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));  
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Vklad položky");
            stage.showAndWait();
            cancelButton();
        } else {
            newAmountLabel.setText("FAIL MY DEAR");
        }
    }
    
    // button "Zrušiť"
    @FXML
    private void cancelButton() {
        Stage stage = (Stage) newAmountLabel.getScene().getWindow();
        stage.close();
    }
    
}
