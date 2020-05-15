
package dialog;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import databaseAccess.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn.CellEditEvent;


/**
 * Dialog for Item modification.
 */

public class FXMLItemOfftakeDialogController implements Initializable {

    @FXML private javafx.scene.control.TableView<ItemOfftakeRecord> mainTable;
    @FXML private javafx.scene.control.TextField amountRequestTextField;
    @FXML private javafx.scene.layout.AnchorPane rootAnchorPane;

    private Item item;
    private ObservableList<ItemOfftakeRecord> requestSet = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Receives initialization data after dialog is shown.
     * Setups default values.
     */
    public void initData(Item item) {
        if (item != null) {
            QueryHandler qh = QueryHandler.getInstance();
            this.item = item;

            // custom attributes table
            TableColumn expirationColumn = new TableColumn("Expirácia");
            expirationColumn.setCellValueFactory(new PropertyValueFactory<>("expiration"));

            TableColumn currentAmountColumn = new TableColumn("K dispozícii");
            currentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));

            TableColumn requestedAmountColumn = new TableColumn("VÝBER");
            requestedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAmount"));
            requestedAmountColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            requestedAmountColumn.setOnEditCommit(
                    new EventHandler<TableColumn.CellEditEvent<ItemOfftakeRecord, String>>() {
                        @Override
                        public void handle(CellEditEvent<ItemOfftakeRecord, String> t) {
                            ((ItemOfftakeRecord) t.getTableView().getItems().get(
                                    t.getTablePosition().getRow())
                            ).setRequestedAmount(Integer.parseInt(t.getNewValue()));
                        }
                    }
            );

            mainTable.getColumns().addAll(expirationColumn, currentAmountColumn, requestedAmountColumn);
            QueryHandler.getInstance().getItemOfftakeRecords(item.getId(), requestSet);
            populateTable();
        }
    }

    /**
     * Button 'Vlozit' sends desired changes to QueryHandler.
     */
    @FXML
    private void saveButton() throws IOException {
        // todo: implement save button with high SERIALIZABLE transaction
        rootAnchorPane.setDisable(true);
        QueryHandler qh = QueryHandler.getInstance();
        DialogFactory df = DialogFactory.getInstance();
        try {
            if (qh.itemOfftake(item, requestSet)) {
                DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, "Úprava položky prebehla úspešne.");
                cancelButton();
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
    private void cancelButton() {
        Stage stage = (Stage) mainTable.getScene().getWindow();
        stage.close();
    }

    /**
     * Button 'Vyber optimalnu moznost' Calculates the optimal offtake (according to amount and expiry date).
     */
    @FXML
    private void optimiseButton() {
        // todo implement optimise button
        int requestedAmount = Integer.parseInt(amountRequestTextField.getText());
        for (ItemOfftakeRecord record : requestSet.sorted()) {
            int toBeTaken = Math.min(record.getCurrentAmount(), requestedAmount);
            record.setRequestedAmount(toBeTaken);
            requestedAmount -= toBeTaken;
        }
        populateTable();
    }

    /**
     * Populates table with provided CustomAttributes HashSet.
     */
    private void populateTable() {
        mainTable.getItems().clear();
        if (!requestSet.isEmpty()) {
            for (ItemOfftakeRecord record : requestSet) {
                mainTable.getItems().add(record);
            }
        } else {
            mainTable.setPlaceholder(new Label("Bez ďalších atribútov."));
        }
    }

}
