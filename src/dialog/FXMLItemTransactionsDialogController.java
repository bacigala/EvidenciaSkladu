
package dialog;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import databaseAccess.ItemMoveLogRecord;
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
    public void initData(ArrayList<ItemMoveLogRecord> logRecords) {
        if (logRecords != null) {
            TableColumn transDate = new TableColumn("Dátum");
            transDate.setCellValueFactory(new PropertyValueFactory<>("date"));

            TableColumn transAmount = new TableColumn("Množstvo");
            transAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

            TableColumn transUsername = new TableColumn("Používateľ");
            transUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

            mainTable.getColumns().addAll(transDate, transAmount, transUsername);

            if (!logRecords.isEmpty()) {
                for (ItemMoveLogRecord itemLogRecord : logRecords) {
                    mainTable.getItems().add(itemLogRecord);
                }
            } else {
                mainTable.setPlaceholder(new Label("Zatiaľ žiadne pohyby."));
            }
        }
    }

}