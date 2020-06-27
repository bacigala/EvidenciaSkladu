
package dialog.controller;

import databaseAccess.CustomExceptions.UserWarningException;
import databaseAccess.ItemDAO;
import dialog.DialogFactory;
import domain.Item;
import domain.ItemMoveLogRecord;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Dialog for Item transactions view.
 */

public class FXMLItemTransactionsDialogController implements Initializable {

    @FXML private javafx.scene.control.TableView<ItemMoveLogRecord> mainTable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Populates TableView with provided list of transactions.
     */
    public void initData(Item item) {
        if (item == null) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Nepodarilo sa načítať.");
            closeDialog();
            return;
        }

        TableColumn<ItemMoveLogRecord, Date> transDate = new TableColumn<>("Dátum");
        transDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<ItemMoveLogRecord, Integer> transAmount = new TableColumn<>("Množstvo");
        transAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<ItemMoveLogRecord, Date> itemExpirationColumn = new TableColumn<>("Expirácia");
        itemExpirationColumn.setCellValueFactory(new PropertyValueFactory<>("expiration"));

        TableColumn<ItemMoveLogRecord, String> transUsername = new TableColumn<>("Používateľ");
        transUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        mainTable.getColumns().addAll(transDate, transAmount, itemExpirationColumn, transUsername);
        mainTable.setPlaceholder(new Label("Zatiaľ žiadne pohyby."));

        try {
            ArrayList<ItemMoveLogRecord> logRecords = ItemDAO.getInstance().getItemTransactions(item.getId());
            mainTable.getItems().addAll(logRecords);
        } catch (UserWarningException e) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, e.getMessage());
            closeDialog();
        } catch (Exception e) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Neočakávaná chyba.");
            e.printStackTrace();
            closeDialog();
        }
    }

    private void closeDialog() {
        ((Stage) mainTable.getScene().getWindow()).close();
    }

}
