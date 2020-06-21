
package dialog.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import domain.Item;
import domain.ItemMoveLogRecord;
import databaseAccess.QueryHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Dialog for Item transactions.
 */

public class FXMLItemTransactionsDialogController implements Initializable {

    @FXML private javafx.scene.control.TableView mainTable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Populates TableView with provided list of transactions.
     */
    public void initData(Item item) {
        if (item != null) {

            ArrayList<ItemMoveLogRecord> logRecords = QueryHandler.getInstance().getItemTransactions(item.getId());


            TableColumn transDate = new TableColumn("Dátum");
            transDate.setCellValueFactory(new PropertyValueFactory<>("date"));

            TableColumn transAmount = new TableColumn("Množstvo");
            transAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

            TableColumn transUsername = new TableColumn("Používateľ");
            transUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

            mainTable.getColumns().addAll(transDate, transAmount, transUsername);

            if (logRecords != null && !logRecords.isEmpty()) {
                for (ItemMoveLogRecord itemLogRecord : logRecords) {
                    mainTable.getItems().add(itemLogRecord);
                }
            } else {
                mainTable.setPlaceholder(new Label("Zatiaľ žiadne pohyby."));
            }
        }
    }

}
