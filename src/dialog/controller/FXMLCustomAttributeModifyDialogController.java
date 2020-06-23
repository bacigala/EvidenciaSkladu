
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

public class FXMLCustomAttributeModifyDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField valueTextField;
    @FXML private javafx.scene.control.Button saveButton;
    @FXML private javafx.scene.control.Button cancelButton;

    private CustomAttribute originalAttribute;
    private HashSet<CustomAttribute> attributesToAdd, attributesToDelete;
    private boolean newAttribute = false;

    /**
     * BUTTON "Ulozit" Assigns CustomAttribute to corresponding set.
     */
    @FXML
    private void saveButtonAction(ActionEvent e) {
        disableInput();
        DialogFactory df = DialogFactory.getInstance();
        String newName = nameTextField.getText();
        String newValue = valueTextField.getText();
        if (!nameOrValueChanged()) {
            // nothing changed -> close window
            close();
            return;
        }
        if (!newName.equals("") && !newValue.equals("")) {
            // values changed -> update sets
            attributesToAdd.add(new CustomAttribute(newName, newValue));
            if (!newAttribute) {
                if(!attributesToAdd.remove(originalAttribute)) attributesToDelete.add(originalAttribute);
            }
            close();
        } else {
            // error - wrong input
            df.showAlert(Alert.AlertType.ERROR, "Zadané údaje sú neplatné.");
            enableInput();
        }
    }

    /**
     * BUTTON "Zrusit" Closes dialog with no impact.
     */
    @FXML
    private void cancelButtonAction() {
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
        newAttribute = true;
        this.originalAttribute = new CustomAttribute("", "");
        this.attributesToAdd = attributesToAdd;
    }

    public void initData(CustomAttribute originalAttribute, HashSet<CustomAttribute> attributesToAdd,
                         HashSet<CustomAttribute> attributesToDelete) {
        if (originalAttribute == null || attributesToAdd == null || attributesToDelete == null) {
            // error - invalid init data received
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Dáta sa nepodarilo načítať.");
            close();
            return;
        }

        newAttribute = false;
        this.originalAttribute = originalAttribute;
        nameTextField.setText(originalAttribute.getName());
        valueTextField.setText(originalAttribute.getValue());

        this.attributesToAdd = attributesToAdd;
        this.attributesToDelete =  attributesToDelete;
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

    /**
     * @return true if the name / value was changed
     */
    private boolean nameOrValueChanged() {
        return !originalAttribute.getName().equals(nameTextField.getText())
                || !originalAttribute.getValue().equals(valueTextField.getText());
    }
    
}
