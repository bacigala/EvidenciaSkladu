
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
    @FXML private javafx.scene.control.Button deleteButton;

    private CustomAttribute originalAttribute;
    private HashSet<CustomAttribute> attributesToAdd, attributesToDelete;

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
            this.attributesToAdd.add(new CustomAttribute(newName, newValue));
            deleteButtonAction(new ActionEvent());
        } else {
            // error - wrong input
            df.showAlert(Alert.AlertType.ERROR, "Zadané údaje sú neplatné.");
            enableInput();
        }
    }

    /**
     * BUTTON "Odstranit" Assigns CustomAttribute to corresponding set.
     */
    @FXML
    private void deleteButtonAction(ActionEvent e) {
        disableInput();
        if (this.attributesToAdd.contains(originalAttribute)) {
            this.attributesToAdd.remove(originalAttribute);
        } else {
            this.attributesToDelete.add(originalAttribute);
        }
        close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Saves pointers to provided sets of change / delete attributes.
     * Populates input fields with current values.
     */
    public void initData(CustomAttribute customAttribute, HashSet<CustomAttribute> attributesToAdd,
                         HashSet<CustomAttribute> attributesToDelete) {
        if (customAttribute != null && attributesToAdd != null && attributesToDelete != null) {
            this.originalAttribute = customAttribute;
            this.attributesToAdd = attributesToAdd;
            this.attributesToDelete =  attributesToDelete;
            nameTextField.setText(customAttribute.getName());
            valueTextField.setText(customAttribute.getValue());

            /**
             * Disables / enables DELETE button after original name / value changes.
             */
            nameTextField.textProperty().addListener((obs, oldText, newText) -> {
                deleteButton.setDisable(nameOrValueChanged());
            });
            valueTextField.textProperty().addListener((obs, oldText, newText) -> {
                deleteButton.setDisable(nameOrValueChanged());
            });

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

    /**
     * @return true if the name / value was changed
     */
    private boolean nameOrValueChanged() {
        return !originalAttribute.getName().equals(nameTextField.getText())
                || !originalAttribute.getValue().equals(valueTextField.getText());
    }
    
}
