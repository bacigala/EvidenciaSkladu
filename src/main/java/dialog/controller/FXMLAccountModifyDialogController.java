
package dialog.controller;

import databaseAccess.AccountDAO;
import databaseAccess.CustomExceptions.UserWarningException;
import dialog.DialogFactory;
import domain.Account;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Dialog for Account modification.
 * Saves as new / Updates given account (saves as new if account ID == 0)
 */

public class FXMLAccountModifyDialogController implements Initializable {

    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField surnameTextField;
    @FXML private javafx.scene.control.TextField loginTextField;
    @FXML private javafx.scene.control.PasswordField psw1PasswordField;
    @FXML private javafx.scene.control.PasswordField psw2PasswordField;
    @FXML private javafx.scene.control.CheckBox adminCheckBox;

    private Account account = null;
    private boolean newAccount = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data - account to be edited.
     * Setups default values.
     */
    public void initData(Account account) {
        if (account == null) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Nepodarilo sa načítať.");
            closeStage();
            return;
        }

        this.account = account;
        newAccount = account.getId() == 0;

        // fill default values
        nameTextField.setText(account.getName());
        surnameTextField.setText(account.getSurname());
        loginTextField.setText(account.getLogin());
        adminCheckBox.setSelected(account.isAdmin());
    }

    /**
     * Button 'Ulozit' verifies input and requests changes / closes dialog and setups return values.
     */
    @FXML
    private void saveButtonAction() {
        if (!changesMade()) {
            closeStage();
            return;
        }

        // check user input
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
        if (newAccount && psw1PasswordField.getText().equals("")) {
            df.showAlert(Alert.AlertType.WARNING, "Prosím vypňte heslo.");
            return;
        }

        // no errors -> close and save
        account.setName(nameTextField.getText());
        account.setSurname(surnameTextField.getText());
        account.setLogin(loginTextField.getText());
        account.setPassword(psw1PasswordField.getText());
        account.setAdmin(adminCheckBox.isSelected());

        if (newAccount) {
            try {
                AccountDAO.getInstance().createAccount(account);
                df.showAlert(Alert.AlertType.INFORMATION, "Nové konto bolo úspešne vytvorené.");
            } catch (UserWarningException e) {
                    df.showAlert(Alert.AlertType.ERROR, e.getMessage());
            } catch (Exception e) {
                    df.showAlert(Alert.AlertType.ERROR, "Konto sa nepodarilo vytvoriť.");
            }
        } else {
            try {
                AccountDAO.getInstance().modifyAccount(account);
                df.showAlert(Alert.AlertType.INFORMATION, "Konto bolo úspešne upravené.");
            } catch (UserWarningException e) {
                df.showAlert(Alert.AlertType.ERROR, e.getMessage());
            } catch (Exception e) {
                df.showAlert(Alert.AlertType.ERROR, "Konto sa nepodarilo upraviť.");
            }
        }

        closeStage();
    }

    /**
     * Button 'Zrusit' Verifies input and setups return values.
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
        if (!nameTextField.getText().equals(account.getName())) return true;
        if (!surnameTextField.getText().equals(account.getSurname())) return true;
        if (!loginTextField.getText().equals(account.getLogin())) return true;
        if (adminCheckBox.isSelected() != account.isAdmin()) return true;
        if (!surnameTextField.getText().equals(account.getSurname())) return true;
        if (!psw1PasswordField.getText().equals("")) return true;
        return !psw2PasswordField.getText().equals("");
    }

}
