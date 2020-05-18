
package dialog;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import databaseAccess.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import supportStructures.EditableBoolean;

/**
 * Dialog for Account modification.
 */

public class FXMLAccountModifyDialogController implements Initializable {

    @FXML private javafx.scene.layout.AnchorPane mainAnchorPane;
    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField surnameTextField;
    @FXML private javafx.scene.control.TextField loginTextField;
    @FXML private javafx.scene.control.PasswordField psw1PasswordField;
    @FXML private javafx.scene.control.PasswordField psw2PasswordField;
    @FXML private javafx.scene.control.CheckBox adminCheckBox;

    private Account account = null;
    private EditableBoolean saveRequest = null;
    private EditableBoolean deleteRequest = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data - account to be edited.
     * Setups default values.
     */
    public void initData(Account account, EditableBoolean saveRequest, EditableBoolean deleteRequest) {
        if (account != null && saveRequest != null && deleteRequest != null) {
            // store received pointers
            this.account = account;
            this.saveRequest = saveRequest;
            this.deleteRequest = deleteRequest;

            this.saveRequest.set(false);
            this.deleteRequest.set(false);

            // fill default values
            nameTextField.setText(account.getName());
            surnameTextField.setText(account.getSurname());
            loginTextField.setText(account.getLogin());
            adminCheckBox.setSelected(account.isAdmin());
        } else {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Nepodarilo sa načítať.");
            closeStage();
            throw new NullPointerException();
        }
    }

    /**
     * Button 'Ulozit' verifies input and requests changes / closes dialog and setups return values.
     */
    @FXML
    private void saveButtonAction() {
        if (!changesMade()) {
            deleteRequest.set(false);
            closeStage();
        }




    }

    /**
     * Button 'Odstranit' Verifies input and setups return values.
     */
    @FXML
    private void deleteButtonAction() throws IOException {
        if (changesMade()) {

        }
        deleteRequest.set(!changesMade());
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
        if (!nameTextField.getText().equals(account.getName())) return true;
        if (!surnameTextField.getText().equals(account.getSurname())) return true;
        if (!loginTextField.getText().equals(account.getLogin())) return true;
        if (adminCheckBox.isSelected() != account.isAdmin()) return true;
        if (!surnameTextField.getText().equals(account.getSurname())) return true;
        if (!psw1PasswordField.getText().equals("")) return true;
        if (!psw2PasswordField.getText().equals("")) return true;
        return false;
    }

}
