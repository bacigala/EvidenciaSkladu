
package dialog;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import databaseAccess.Category;
import databaseAccess.CustomAttribute;
import databaseAccess.Item;
import databaseAccess.QueryHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for Item modification.
 */

public class FXMLItemModifyDialogController implements Initializable {

    @FXML
    private javafx.scene.layout.AnchorPane mainAnchorPane;
    @FXML
    private javafx.scene.control.TextField nameTextField;
    @FXML
    private javafx.scene.control.TextField codeTextField;
    @FXML
    private javafx.scene.control.TextField curAmountTextField;
    @FXML
    private javafx.scene.control.TextField minAmountTextField;
    @FXML
    private javafx.scene.control.TextField unitTextField;
    @FXML
    private javafx.scene.control.ChoiceBox<Category> categoryChoiceBox;
    @FXML
    private javafx.scene.control.TableView<CustomAttribute> tableCustomAttributes;

    private Item item;
    private HashSet<CustomAttribute> attributesToAdd = new HashSet<>();
    private HashSet<CustomAttribute> attributesToDelete = new HashSet<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data after dialog is shown.
     * Setups default values.
     */
    public void initData(Item item, HashSet<CustomAttribute> customAttributes) {
        if (item != null) {
            QueryHandler qh = QueryHandler.getInstance();
            this.item = item;
            nameTextField.setText(item.getName());
            codeTextField.setText(item.getBarcode());
            curAmountTextField.setText(Integer.toString(item.getCurAmount()));
            minAmountTextField.setText(Integer.toString(item.getMinAmount()));
            unitTextField.setText(item.getUnit());
            categoryChoiceBox.getItems().addAll(qh.getCategoryMap().values());
            categoryChoiceBox.setValue(qh.getCategoryMap().get(item.getCategory()));

            // custom attributes table
            TableColumn attributeName = new TableColumn("Atribút");
            attributeName.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn attributeValue = new TableColumn("Hodnota");
            attributeValue.setCellValueFactory(new PropertyValueFactory<>("value"));

            tableCustomAttributes.getColumns().addAll(attributeName, attributeValue);

            populateCustomAttributesTable(customAttributes);
            if (customAttributes != null) {
                for (CustomAttribute ca : customAttributes) {
                    tableCustomAttributes.getItems().add(ca);
                }
            }
        }
    }

    /**
     * Button 'Ulozit' sends desired changes to QueryHandler.
     */
    @FXML
    private void saveButton() throws IOException {
        // todo: optimalizacia planovanych zmien v databaze
        // todo: implement the save button
//        QueryHandler qh = QueryHandler.getInstance();
//        DialogFactory df = DialogFactory.getInstance();
//        try {
//            if (qh.itemSupply(item.getId(), Integer.parseInt(newAmountTextField.getText()), newExpirationDatePicker.getValue())) {
//                DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Vklad položky prebehol úspešne.");
//                cancelButton();
//            } else {
//                df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať. Skontrolujte prosím zadané hodnoty.");
//            }
//        } catch (Exception e) {
//            df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať. Skontrolujte prosím zadané hodnoty.");
//        }
    }

    /**
     * Button 'Pridat udaj' Creates dialog for new CustomAttribute input.
     */
    @FXML
    private void newCustomAttributeButton() {
        // todo: popup new ADD CUSTOM ATTRIBUTE WINDOW
    }

    /**
     * Button 'Zrusit' Cancels planed changes.
     */
    @FXML
    private void cancelButton() {
        Stage stage = (Stage) nameTextField.getScene().getWindow();
        stage.close();
    }

    /**
     * CustomAttributeChange dialog popup on custom attribute clicked.
     *
     * @throws IOException
     */
    @FXML
    private void showCustomAttributeChangeDialog() throws IOException {
        mainAnchorPane.setDisable(true);
        CustomAttribute selected = tableCustomAttributes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            CustomAttribute newAttribute = selected.copy();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLCustomAttributeModifyDialog.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLCustomAttributeModifyDialogController controller = fxmlLoader.getController();
            controller.initData(selected, attributesToAdd, attributesToDelete);
            stage.showAndWait();
        }
        System.out.println(attributesToAdd);
        System.out.println(attributesToDelete);
        // todo: Vyhodnotenie zmien, aktualizacia aktualnych detailov v zobrazeni
        mainAnchorPane.setDisable(false);
    }

    /**
     * Populates table with provided CustomAttributes HashSet.
     */
    private void populateCustomAttributesTable(HashSet<CustomAttribute> customAttributes) {
        tableCustomAttributes.getItems().clear();
        if (customAttributes !=null) {
            for (CustomAttribute ca : customAttributes) {
                tableCustomAttributes.getItems().add(ca);
            }
        } else {
            tableCustomAttributes.setPlaceholder(new Label("Bez ďalších atribútov."));
        }
    }

}
