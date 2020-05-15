
package dialog;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import databaseAccess.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn.CellEditEvent;

/**
 * Dialog for Item offtake.
 * Lists tuples [expiration; available amount] of chosen Item.
 * Calculates ideal offtake for desired amount (preferring sooner expiry date)
 * Delegates desired offtake to QueryHandler.
 */

public class FXMLItemOfftakeDialogController implements Initializable {
    @FXML private javafx.scene.control.TableView<ItemOfftakeRecord> mainTable;
    @FXML private javafx.scene.control.TextField amountRequestTextField;
    @FXML private javafx.scene.layout.AnchorPane rootAnchorPane;

    private Item item;
    private final ObservableList<ItemOfftakeRecord> requestList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data when dialog is to be shown.
     * Setups default values.
     */
    public void initData(Item item) {
        if (item != null) {
            this.item = item;

            // TableView setup
            TableColumn expirationColumn = new TableColumn<ItemOfftakeRecord, LocalDate>("Expirácia");
            expirationColumn.setCellValueFactory(new PropertyValueFactory<>("expiration"));

            TableColumn currentAmountColumn = new TableColumn<ItemOfftakeRecord, Integer>("K dispozícii");
            currentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));

            TableColumn requestedAmountColumn = new TableColumn<>("VÝBER");
            requestedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAmount"));

            // requested amount column is editable
            requestedAmountColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            requestedAmountColumn.setOnEditCommit(
                    new EventHandler<TableColumn.CellEditEvent<ItemOfftakeRecord, String>>() {
                        @Override
                        public void handle(CellEditEvent<ItemOfftakeRecord, String> t) {
                            (t.getTableView().getItems().get(
                                    t.getTablePosition().getRow())
                            ).setRequestedAmount(Integer.parseInt(t.getNewValue()));
                        }
                    }
            );

            mainTable.getColumns().addAll(expirationColumn, currentAmountColumn, requestedAmountColumn);
            QueryHandler.getInstance().getItemOfftakeRecords(item.getId(), requestList);
            populateTable();
        } else {
            // error - invalid initialization data received
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Údaje sa nepodarilo načítať.");
            cancelButtonAction();
        }
    }

    /**
     * Button 'Vyber' sends desired offtake combination to QueryHandler.
     */
    @FXML
    private void offtakeButtonAction() {
        rootAnchorPane.setDisable(true);
        QueryHandler qh = QueryHandler.getInstance();
        DialogFactory df = DialogFactory.getInstance();
        try {
            if (qh.itemOfftake(item, requestList)) {
                DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Výber položky prebehla úspešne.");
                cancelButtonAction();
            } else {
                df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať. Skontrolujte prosím zadané hodnoty.");
            }
        } catch (Exception e) {
            df.showAlert(Alert.AlertType.ERROR, "Akciu sa nepodarilo vykonať. Skontrolujte prosím zadané hodnoty.");
        }
        rootAnchorPane.setDisable(false);
    }

    /**
     * Button 'Zrusit' Cancels planed changes.
     */
    @FXML
    private void cancelButtonAction() {
        Stage stage = (Stage) mainTable.getScene().getWindow();
        stage.close();
    }

    /**
     * Button 'Vyber optimalnu moznost' Calculates the optimal offtake (according to amount and expiry date).
     */
    @FXML
    private void optimiseButtonAction() {
        int requestedAmount = Integer.parseInt(amountRequestTextField.getText());
        for (ItemOfftakeRecord record : requestList.sorted()) {
            int toBeTaken = Math.min(record.getCurrentAmount(), requestedAmount);
            record.setRequestedAmount(toBeTaken);
            requestedAmount -= toBeTaken;
        }
        populateTable();
        if (requestedAmount > 0) {
            String message = "Požadované množstvo žiaľ nie je dostupné. (chýba " + requestedAmount + " MJ)";
            DialogFactory.getInstance().showAlert(Alert.AlertType.WARNING, message);
        }
    }

    /**
     * Populates table with provided CustomAttributes HashSet.
     */
    private void populateTable() {
        mainTable.getItems().clear();
        if (!requestList.isEmpty()) {
            for (ItemOfftakeRecord record : requestList) {
                mainTable.getItems().add(record);
            }
        } else {
            mainTable.setPlaceholder(new Label("Bez ďalších atribútov."));
        }
    }

}
