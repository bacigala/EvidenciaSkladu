
package dialog.controller;

import java.net.URL;
import java.util.*;

import databaseAccess.CategoryDAO;
import databaseAccess.CustomExceptions.UserWarningException;
import dialog.DialogFactory;
import domain.Category;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Dialog for Account modification / creation.
 * Initialized with Category -> init_cat_id == 0 ? create_new : modify
 */

public class FXMLCategoryModifyDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField noteTextField;

    private Category category = null;
    private boolean newCategory;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data - category to be edited.
     * Setups default values.
     */
    public void initData(Category category) {
        if (category == null) {
            closeStage();
            return;
        }
        newCategory = category.getId() == 0;

        // store received pointers
        this.category= category;

        // fill default values
        nameTextField.setText(category.getName());
        noteTextField.setText(category.getNote());
    }

    /**
     * Button 'Ulozit' verifies input and requests category modification / creation.
     */
    @FXML
    private void saveButtonAction() {
        if (changesMade()) {
            DialogFactory df = DialogFactory.getInstance();

            // verify input
            if (nameTextField.getText().equals("")) {
                df.showAlert(Alert.AlertType.WARNING, "Prosím, vyplňte názov.");
                return;
            }

            // request modification / creation
            category.setName(nameTextField.getText());
            category.setNote(noteTextField.getText());

            try {
                if (newCategory) {
                    CategoryDAO.getInstance().createCategory(category);
                    df.showAlert(Alert.AlertType.INFORMATION, "Kategória úspešne vytvorená.");
                } else {
                    CategoryDAO.getInstance().modifyCategory(category);
                    df.showAlert(Alert.AlertType.INFORMATION, "Kategória úspešne modifikovaná.");
                }
            } catch (UserWarningException e) {
                df.showAlert(Alert.AlertType.WARNING, e.getMessage());
            } catch (Exception e) {
                df.showAlert(Alert.AlertType.WARNING, "Operáciu as nepodarilo vykonať.");
            }
        }
        closeStage();
    }

    /**
     * Button 'Odstranit' Verifies input and setups return values.
     */
    @FXML
    private void cancelButtonAction() {
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
