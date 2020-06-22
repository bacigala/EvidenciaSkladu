
package dialog.controller;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import databaseAccess.*;
import domain.ExpiryDateWarningRecord;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

/**
 * Dialog for soon expiration date display.
 * Lists all items with soon expiry date.
 */

public class FXMLCheckExpirationDialogController implements Initializable {
    @FXML private javafx.scene.control.TableView<ExpiryDateWarningRecord> mainTable;

    private final ObservableList<ExpiryDateWarningRecord> itemList = FXCollections.observableArrayList();

    /**
     * Requests current list of Items from DB and displays it in the table.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TableView setup
        TableColumn itemNameColumn = new TableColumn<ExpiryDateWarningRecord, String>("Názov");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn itemCurrentAmountColumn = new TableColumn<ExpiryDateWarningRecord, String>("Počet");
        itemCurrentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("expiryAmount"));

        TableColumn itemExpiryDateColumn = new TableColumn<ExpiryDateWarningRecord, String>("Expirácia");
        itemExpiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        TableColumn itemDetailButtonColumn = new TableColumn<>("Detail");
        itemDetailButtonColumn.setSortable(false);

        itemDetailButtonColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<ExpiryDateWarningRecord, Boolean>, ObservableValue<Boolean>>)
                        p -> new SimpleBooleanProperty(p.getValue() != null));

        itemDetailButtonColumn.setCellFactory(
                (Callback<TableColumn<ExpiryDateWarningRecord, Boolean>, TableCell<ExpiryDateWarningRecord, Boolean>>) p -> new ButtonCell());

        mainTable.getColumns().addAll(itemNameColumn, itemCurrentAmountColumn, itemExpiryDateColumn, itemDetailButtonColumn);
        mainTable.setPlaceholder(new Label("Žiadne xz."));
        populateTable();
    }

    // cell in action column
    private class ButtonCell extends TableCell<ExpiryDateWarningRecord, Boolean> {
        final Button detailButton = new Button("Výber");

        ButtonCell() {
            detailButton.setOnAction(t -> {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLItemOfftakeDialog.fxml"));
                Parent root1 = null;
                try {
                    root1 = fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.initModality(Modality.APPLICATION_MODAL);
                FXMLItemOfftakeDialogController controller = fxmlLoader.getController();
                controller.initData(getTableView().getItems().get(getIndex()));
                stage.showAndWait();

                populateTable();
            });
        }

        HBox pane = new HBox(detailButton);

        //Display button if the row is not empty
        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if(!empty){
                setGraphic(pane);
            }
        }
    }

    public void initData() {

    }

    /**
     * Button 'Zavriet' Closes the dialog.
     */
    @FXML
    private void closeButtonAction() {
        ((Stage) mainTable.getScene().getWindow()).close();
    }

    /**
     * Populates table with provided UserAccounts.
     */
    private void populateTable() {
        itemList.clear();
        ComplexQueryHandler.getInstance().getSoonExpiryItems(itemList);
        mainTable.getItems().addAll(itemList);
    }

}
