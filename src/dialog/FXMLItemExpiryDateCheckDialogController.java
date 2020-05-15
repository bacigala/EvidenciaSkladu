
package dialog;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import databaseAccess.ExpiryDateWarningRecord;
import databaseAccess.Item;
import databaseAccess.ItemMoveLogRecord;
import databaseAccess.QueryHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Dialog for soon expiry date warnings.
 */

public class FXMLItemExpiryDateCheckDialogController implements Initializable {
    @FXML private javafx.scene.control.TableView<ExpiryDateWarningRecord> mainTable;

    /**
     * Populates TableView with list of warnings.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        ArrayList<ExpiryDateWarningRecord> records = QueryHandler.getInstance().getExpiryDateWarnings();

        TableColumn itemNAmeColumn = new TableColumn("Položka");
        itemNAmeColumn.setCellValueFactory(new PropertyValueFactory<>("itemNAmeColumn"));

        TableColumn expiryDateColumn = new TableColumn("Expirácia");
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expityDate"));

        TableColumn amountColumn = new TableColumn("Množstvo");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        mainTable.getColumns().addAll(itemNAmeColumn, expiryDateColumn, amountColumn);

        if (records != null && !records.isEmpty()) {
            for (ExpiryDateWarningRecord record : records) {
                mainTable.getItems().add(record);
            }
        } else {
            mainTable.setPlaceholder(new Label("Bez upozornení."));
        }
    }

}
