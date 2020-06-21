
package dialog.controller;

/**
 * Custom attribute modification dialog.
 */

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

import dialog.DialogFactory;
import domain.CustomAttribute;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class FXMLCustomAttributeCreateDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField valueTextField;
    @FXML private javafx.scene.control.Button createButton;
    @FXML private javafx.scene.control.Button cancelButton;

    private HashSet<CustomAttribute> attributesToAdd;

    /**
     * BUTTON "Vytvorit" Assigns CustomAttribute to corresponding set.
     */
    @FXML
    private void createButtonAction(ActionEvent e) {
        disableInput();
        DialogFactory df = DialogFactory.getInstance();
        String newName = nameTextField.getText();
        String newValue = valueTextField.getText();
        if (!newName.equals("") && !newValue.equals("")) {
            // values changed -> update set
            this.attributesToAdd.add(new CustomAttribute(newName, newValue));
            close();
        } else {
            // error - wrong input
            df.showAlert(Alert.AlertType.ERROR, "Zadané údaje sú neplatné.");
            enableInput();
        }
    }

    /**
     * BUTTON "Zrusit" Cancels changes.
     */
    @FXML
    private void cancelButtonAction(ActionEvent e) {
        disableInput();
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Saves pointers to provided sets of change / delete attributes.
     * Populates input fields with current values.
     */
    public void initData(HashSet<CustomAttribute> attributesToAdd) {
        if (attributesToAdd != null) {
            this.attributesToAdd = attributesToAdd;
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
