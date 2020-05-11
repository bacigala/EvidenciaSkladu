
package dialog;

/**
 * Custom attribute modification dialog.
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import databaseAccess.CustomAttribute;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class FXMLCustomAttributeModifyDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField valueTextField;
    @FXML private javafx.scene.control.Button saveButton;
    @FXML private javafx.scene.control.Button deleteButton;

    private CustomAttribute customAttribute;
    private ArrayList<CustomAttribute> attributesToAdd, attributesToDelete;

    /**
     * BUTTON "Ulozit" Adds attribute to desired list.
     */
    @FXML
    private void saveButtonAction(ActionEvent e) {
        disableInput();
        DialogFactory df = DialogFactory.getInstance();
        String newName = nameTextField.getText();
        String newValue = valueTextField.getText();
        if (!newName.equals("") && !newValue.equals("")) {
            disableInput();
            this.attributesToDelete.add(customAttribute);
            this.attributesToAdd.add(new CustomAttribute(newName, newValue));
            close();
        } else {
            // error - wrong input
            df.showAlert(Alert.AlertType.ERROR, "Zadané údaje sú neplatné.");
        }
        enableInput();
    }

    /**
     * BUTTON "Odstranit" Changes flags to signalize desired DELETION.
     */
    @FXML
    private void deleteButtonAction(ActionEvent e) {
        disableInput();
        this.attributesToDelete.add(customAttribute);
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Saves provided lists of change / delete attributes.
     * Populates input fields with current values.
     */
    public void initData(CustomAttribute customAttribute, ArrayList<CustomAttribute> attributesToAdd,
                         ArrayList<CustomAttribute> attributesToDelete) {
        if (customAttribute != null && attributesToAdd != null && attributesToDelete != null) {
            this.customAttribute = customAttribute;
            this.attributesToAdd = attributesToAdd;
            this.attributesToDelete =  attributesToDelete;
            nameTextField.setText(customAttribute.getName());
            valueTextField.setText(customAttribute.getValue());
        } else {
            // error - invalid init data received
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Dáta sa nepodarilo načítať.");
            close();
        }
    }

    /**
     * Closes dialog.
     */
    private void close() {
        Stage stage = (Stage) nameTextField.getScene().getWindow();
        stage.close();
    }

    /**
     * Enables input fields.
     */
    private void enableInput() {
        nameTextField.setDisable(false);
        valueTextField.setDisable(false);
    }

    /**
     * Disables input fields.
     */
    private void disableInput() {
        nameTextField.setDisable(true);
        valueTextField.setDisable(true);
    }
    
}
