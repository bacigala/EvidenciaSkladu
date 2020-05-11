
package dialog;

/**
 * Custom attribute modification dialog.
 */

import java.net.URL;
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
    private Boolean modify, delete;

    /**
     * BUTTON "Ulozit" Changes FLAGS to signalize desired CHANGE.
     */
    @FXML
    private void saveButtonAction(ActionEvent e) {
        DialogFactory df = DialogFactory.getInstance();
        String newName = nameTextField.getText();
        String newValue = valueTextField.getText();
        if (!newName.equals("") && !newValue.equals("")) {
            disableInput();
            this.modify = Boolean.TRUE;
            this.customAttribute = new CustomAttribute(newName, newValue);
            close();
        } else {
            // error - wrong input
            df.showAlert(Alert.AlertType.ERROR, "Zadané údaje sú neplatné.");
        }
    }

    /**
     * BUTTON "Odstranit" Changes flags to signalize desired DELETION.
     */
    @FXML
    private void deleteButtonAction(ActionEvent e) {
        disableInput();
        this.delete = Boolean.TRUE;
        close();
    }

    /**
     * Populates input fields with current values.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nameTextField.setText(customAttribute.getName());
        valueTextField.setText(customAttribute.getValue());
    }

    /**
     * Saves provided pointers (FLAGS) showing desired change / delete action.
     */
    public void initData(CustomAttribute customAttribute, Boolean modify, Boolean delete) {
        if (customAttribute != null && modify != null && delete != null) {
            this.customAttribute = customAttribute;
            this.modify = modify;
            this.delete =  delete;
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
