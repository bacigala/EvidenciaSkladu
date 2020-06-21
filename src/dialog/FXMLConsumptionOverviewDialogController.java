
package dialog;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import databaseAccess.*;
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
 * Dialog for consumption overview.
 * Lists all items with their last month use, average month use and average month trash.
 */

public class FXMLConsumptionOverviewDialogController implements Initializable {
    @FXML private javafx.scene.control.TableView<ConsumptionOverviewRecord> mainTable;

    private final ObservableList<ConsumptionOverviewRecord> itemList = FXCollections.observableArrayList();

    /**
     * Requests current list of Items from DB and displays it in the table.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TableView setup
        TableColumn nameColumn = new TableColumn<ConsumptionOverviewRecord, String>("Názov");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn lastMonthColumn = new TableColumn<ConsumptionOverviewRecord, Double>("posledných 30 dní");
        lastMonthColumn.setCellValueFactory(new PropertyValueFactory<>("lastMonth"));

        TableColumn avgMonthColumn = new TableColumn<ConsumptionOverviewRecord, Double>("priemerný mesiac");
        avgMonthColumn.setCellValueFactory(new PropertyValueFactory<>("avgMonth"));

        TableColumn avgTrashColumn = new TableColumn<ConsumptionOverviewRecord, Double>("priemerný mesačný nadbytok");
        avgTrashColumn.setCellValueFactory(new PropertyValueFactory<>("avgTrash"));

        mainTable.getColumns().addAll(nameColumn, lastMonthColumn, avgMonthColumn, avgTrashColumn);
        mainTable.setPlaceholder(new Label("Žiadne záznamy."));

        //todo  bind tableview with observable list
        Property<ObservableList<ConsumptionOverviewRecord>> itemListProperty = new SimpleObjectProperty<>(itemList);
        mainTable.itemsProperty().bind(itemListProperty); // The Binding

        populateTable();
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
        QueryHandler.getInstance().getConsumptionOverviewRecords(itemList);
    }

}
