
package dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Dialog for single choice - shows one ChoiceBox.
 */

public class FXMLSimpleChoiceDialogController<Q> implements Initializable {

    @FXML private javafx.scene.control.Label mainLabel;
    @FXML private javafx.scene.control.ChoiceBox<Q> mainChoiceBox;

    private Q theChoice = null;

    public void setChoiceList(ObservableList<Q> choiceList) {
        // use empty choiceList if null is received
        mainChoiceBox.setItems(choiceList != null ? choiceList : FXCollections.observableArrayList());
        mainChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
                (ov, value, new_value) -> theChoice = choiceList != null ? choiceList.get(new_value.intValue()) : null
        );
    }

    public void setLabelText(String labelText) {
        mainLabel.setText(labelText);
    }

    public Q getChoice() {
        return theChoice;
    }

    @FXML
    private void choiceButtonAction() {
        disableInput();
        if (theChoice == null) {
            DialogFactory df = DialogFactory.getInstance();
            df.showAlert(Alert.AlertType.ERROR, "Vyberte prosím zo zoznamu.");
            enableInput();
            return;
        }
        closeDialog();
    }

    @FXML
    private void cancelButtonAction() {
        disableInput();
        closeDialog();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    private void closeDialog() {
        ((Stage) mainLabel.getScene().getWindow()).close();
    }

    private void enableInput() {
        mainChoiceBox.setDisable(false);
    }

    private void disableInput() {
        mainChoiceBox.setDisable(true);
    }
    
}
