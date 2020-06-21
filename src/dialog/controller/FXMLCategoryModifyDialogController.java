
package dialog.controller;

import java.net.URL;
import java.util.*;

import dialog.DialogFactory;
import domain.Category;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import supportStructures.EditableBoolean;

/**
 * Dialog for Account modification.
 */

public class FXMLCategoryModifyDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField noteTextField;

    private Category category = null;
    private EditableBoolean saveRequest = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data - category to be edited.
     * Setups default values.
     */
    public void initData(Category category, EditableBoolean saveRequest) {
        if (category != null && saveRequest != null) {
            // store received pointers
            this.category= category;
            this.saveRequest = saveRequest;
            this.saveRequest.set(false);

            // fill default values
            nameTextField.setText(category.getName());
            noteTextField.setText(category.getNote());
        } else {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Nepodarilo sa načítať.");
            closeStage();
        }
    }

    /**
     * Button 'Ulozit' verifies input and requests changes / closes dialog and setups return values.
     */
    @FXML
    private void saveButtonAction() {
        if (changesMade()) {
            DialogFactory df = DialogFactory.getInstance();
            if (nameTextField.getText().equals("")) {
                df.showAlert(Alert.AlertType.WARNING, "Prosím, vyplňte názov.");
                return;
            }

            // no errors -> close and save
            category.setName(nameTextField.getText());
            category.setNote(noteTextField.getText());

            saveRequest.set(true);
        } else {
            // nenastali zmeny -> zatvor dialog
            saveRequest.set(false);
        }
        closeStage();
    }

    /**
     * Button 'Odstranit' Verifies input and setups return values.
     */
    @FXML
    private void cancelButtonAction() {
        saveRequest.set(false);
        closeStage();
    }

    /**
     * Closes the stage.
     */
    private void closeStage() {
        Stage stage = (Stage) nameTextField.getScene().getWindow();
        stage.close();
    }

    /**
     * Checks whether values were changed.
     * @return true if values were changed.
      */
    private boolean changesMade() {
        if (!nameTextField.getText().equals(category.getName())) return true;
        return !noteTextField.getText().equals(category.getNote());
    }

}
