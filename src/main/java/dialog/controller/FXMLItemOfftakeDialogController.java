
package dialog.controller;

import databaseAccess.CustomExceptions.UserWarningException;
import databaseAccess.ItemDAO;
import dialog.DialogFactory;
import domain.ExpiryDateWarningRecord;
import domain.Item;
import domain.ItemOfftakeRecord;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

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
    @FXML private javafx.scene.control.Button saveButton;

    private Item item;
    private final ObservableList<ItemOfftakeRecord> requestList = FXCollections.observableArrayList();
    private boolean isTrash = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(() -> amountRequestTextField.requestFocus());
    }

    /**
     * Receives initialization data when dialog is to be shown.
     * Setups default values.
     */
    public void initData(Item item) {
        initData(item, false);
    }

    public void initData(Item item, boolean isTrash) {
        this.isTrash = isTrash;
        if (item == null) {
            // error - invalid initialization data received
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Údaje sa nepodarilo načítať.");
            cancelButtonAction();
            return;
        }
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
        refreshTable();
        Property<ObservableList<ItemOfftakeRecord>> listProperty = new SimpleObjectProperty<>(requestList);
        mainTable.setPlaceholder(new Label("Pre túto položku neexistujú záznamy."));
        mainTable.itemsProperty().bind(listProperty);

        if (isTrash) {
            amountRequestTextField.setText(String.valueOf((((ExpiryDateWarningRecord) item).getExpiryAmount())));
            optimiseButtonAction();
        }
    }

    /**
     * Button 'Vyber' sends desired offtake combination to DB.
     */
    @FXML
    private void offtakeButtonAction() {
        rootAnchorPane.setDisable(true);

        // check whether selection was made
        boolean hasRequest = false;
        for (ItemOfftakeRecord record : requestList) {
            if (Integer.parseInt(record.getRequestedAmount()) > 0) {
                hasRequest = true;
                break;
            }
        }

        if (hasRequest) {
            try {
                if (isTrash) {
                    ItemDAO.getInstance().itemTrash(item, requestList);
                } else {
                    ItemDAO.getInstance().itemOfftake(item, requestList);
                }
                DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION,
                        isTrash ? "Položky úspešne odstránené." : "Výber položky prebehla úspešne.");
                cancelButtonAction();
                return;
            } catch (UserWarningException e) {
                DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, e.getMessage());
                refreshTable();
            } catch (Exception e) {
                DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Neočakávaná chyba.");
                cancelButtonAction();
                return;
            }
        } else {
            DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION,
                    isTrash ? "Položka nebola vyhodená." : "Položka nebola vybraná.");
            cancelButtonAction();
            return;
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
            requestList.remove(record);
            ItemOfftakeRecord newRecord = new ItemOfftakeRecord(record.getExpiration(), record.getCurrentAmount());
            newRecord.setRequestedAmount(toBeTaken);
            requestList.add(newRecord);
            requestedAmount -= toBeTaken;
        }

        if (requestedAmount > 0) {
            String message = "Požadované množstvo žiaľ nie je dostupné. (chýba " + requestedAmount + " MJ)";
            DialogFactory.getInstance().showAlert(Alert.AlertType.WARNING, message);
            Platform.runLater(() -> amountRequestTextField.requestFocus());
        } else {
            Platform.runLater(() -> saveButton.requestFocus());
        }
    }

    private void refreshTable() {
        requestList.clear();
        try {
            ItemDAO.getInstance().getItemVarieties(item.getId(), requestList);
        } catch (UserWarningException e) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, e.getMessage());
        } catch (Exception e) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Neočakávaná chyba.");
        }
    }

    @FXML
    private void optimiseInputFocusMove(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            optimiseButtonAction();
            Platform.runLater(() -> saveButton.requestFocus());
        }
    }

}
