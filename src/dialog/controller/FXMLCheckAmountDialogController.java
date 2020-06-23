
package dialog.controller;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import databaseAccess.*;
import domain.Item;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
 * Dialog for low stock items.
 * Lists all items with insufficient amount in stock.
 */

public class FXMLCheckAmountDialogController implements Initializable {
    @FXML private javafx.scene.control.TableView<Item> mainTable;

    private final ObservableList<Item> itemList = FXCollections.observableArrayList();

    /**
     * Requests current list of Items from DB and displays it in the table.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TableView setup
        TableColumn itemNameColumn = new TableColumn<Item, String>("Názov");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn itemCurrentAmountColumn = new TableColumn<Item, String>("Minimum");
        itemCurrentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("minAmount"));

        TableColumn itemExpiryDateColumn = new TableColumn<Item, String>("Aktuálne");
        itemExpiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("curAmount"));

        TableColumn itemDetailButtonColumn = new TableColumn<>("Možnosti");
        itemDetailButtonColumn.setSortable(false);

        itemDetailButtonColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Item, Boolean>, ObservableValue<Boolean>>)
                        p -> new SimpleBooleanProperty(p.getValue() != null));

        itemDetailButtonColumn.setCellFactory(
                (Callback<TableColumn<Item, Boolean>, TableCell<Item, Boolean>>) p -> new ButtonCell());

        mainTable.getColumns().addAll(itemNameColumn, itemCurrentAmountColumn, itemExpiryDateColumn, itemDetailButtonColumn);
        mainTable.setPlaceholder(new Label("Žiadne záznamy."));
        Property<ObservableList<Item>> itemListProperty = new SimpleObjectProperty<>(itemList);
        mainTable.itemsProperty().bind(itemListProperty);

        populateTable();
    }

    // cell in action column
    private class ButtonCell extends TableCell<Item, Boolean> {
        final Button supplyButton = new Button("Vklad");

        ButtonCell() {
            supplyButton.setOnAction(t -> {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLItemSupplyDialog.fxml"));
                Parent root1;
                try {
                    root1 = fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.initModality(Modality.APPLICATION_MODAL);
                FXMLItemSupplyDialogController controller = fxmlLoader.getController();
                controller.initData(getTableView().getItems().get(getIndex()));
                stage.showAndWait();

                populateTable();
            });
        }

        HBox pane = new HBox(supplyButton);

        //Display button if the row is not empty
        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (empty || t == null) {
                setGraphic(null);
                return;
            }
            setGraphic(pane);
        }
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
        ComplexQueryHandler.getInstance().getLowStockItems(itemList);
    }

}
