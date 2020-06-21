
package dialog.controller;

import java.net.URL;
import java.util.*;

import dialog.DialogFactory;
import domain.Account;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data - account to be edited.
     * Setups default values.
     */
    public void initData(Account account, EditableBoolean saveRequest) {
        if (account != null && saveRequest != null) {
            // store received pointers
            this.account = account;
            this.saveRequest = saveRequest;
            this.saveRequest.set(false);

            // fill default values
            nameTextField.setText(account.getName());
            surnameTextField.setText(account.getSurname());
            loginTextField.setText(account.getLogin());
            adminCheckBox.setSelected(account.isAdmin());
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
                df.showAlert(Alert.AlertType.WARNING, "Prosím, vyplňte meno.");
                return;
            }
            if (surnameTextField.getText().equals("")) {
                df.showAlert(Alert.AlertType.WARNING, "Prosím, vyplňte priezvisko.");
                return;
            }
            if (loginTextField.getText().equals("")) {
                df.showAlert(Alert.AlertType.WARNING, "Prosím, vyplňte prihlasovacie meno.");
                return;
            }
            // check password fields
            if (!psw1PasswordField.getText().equals(psw2PasswordField.getText())) {
                df.showAlert(Alert.AlertType.WARNING, "Heslo a overenie hesla sa nezhodujú.");
                return;
            }

            // no errors -> close and save
            account.setName(nameTextField.getText());
            account.setSurname(surnameTextField.getText());
            account.setLogin(loginTextField.getText());
            account.setPassword(psw1PasswordField.getText());
            account.setAdmin(adminCheckBox.isSelected());

            saveRequest.set(true);
            closeStage();
        } else {
            // nenastali zmeny -> zatvor dialog
            saveRequest.set(false);
            closeStage();
        }
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
