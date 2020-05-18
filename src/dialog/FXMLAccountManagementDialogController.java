
package dialog;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import databaseAccess.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.util.Callback;

/**
 * Dialog for account management.
 * Lists all registered user accounts, offers options to modify, delete or add an account.
 */

public class FXMLAccountManagementDialogController implements Initializable {
    @FXML private javafx.scene.control.TableView<Account> mainTable;
    @FXML private javafx.scene.layout.AnchorPane rootAnchorPane;

    private final ObservableList<Account> accountList = FXCollections.observableArrayList();

    /**
     * Loads current account list from DB (using QueryHandler) and populates the table.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TableView setup
        TableColumn fullNameColumn = new TableColumn<Account, String>("meno");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn loginColumn = new TableColumn<Account, String>("login");
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn isAdminTextColumn = new TableColumn<Account, String>("administrátor");
        isAdminTextColumn.setCellValueFactory(new PropertyValueFactory<>("isAdminText"));

        TableColumn accountModifyButtonColumn = new TableColumn<>("úprava");

        // todo: Insert Button
        accountModifyButtonColumn.setSortable(false);

        accountModifyButtonColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Account, Boolean>,
                        ObservableValue<Boolean>>() {

                    @Override
                    public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Account, Boolean> p) {
                        return new SimpleBooleanProperty(p.getValue() != null);
                    }
                });

        accountModifyButtonColumn.setCellFactory(
                new Callback<TableColumn<Account, Boolean>, TableCell<Account, Boolean>>() {

                    @Override
                    public TableCell<Account, Boolean> call(TableColumn<Account, Boolean> p) {
                        return new ButtonCell();
                    }

                });

        mainTable.getColumns().addAll(fullNameColumn, loginColumn, isAdminTextColumn, accountModifyButtonColumn);
        QueryHandler.getInstance().getAccounts(accountList);
        populateTable();
    }

    // todo: Define the button cell
    private class ButtonCell extends TableCell<Account, Boolean> {
        final Button cellButton = new Button("Action");

        final Button cellButton2 = new Button("Action2");

        ButtonCell(){



            cellButton.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent t) {
                    // do something when button clicked
                    Account data = getTableView().getItems().get(getIndex());
                    System.out.println("selected user: " + data.getFullName());
                }
            });

            cellButton2.setOnAction(new EventHandler<ActionEvent>(){

                @Override
                public void handle(ActionEvent t) {
                    // do something when button clicked
                    Account data = getTableView().getItems().get(getIndex());
                    System.out.println("selected user: @@@ " + data.getFullName());
                }
            });
        }

        HBox pane = new HBox(cellButton, cellButton2);

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
        Stage stage = (Stage) mainTable.getScene().getWindow();
        stage.close();
    }

    /**
     * Button 'Vytvorit konto' Opens dialog for new account creation.
     */
    @FXML
    private void newAccountButtonAction() {
        //todo: open dialog for new user creation...
        System.out.println("Opening dialog for new user add action...");
    }

    /**
     * Populates table with provided UserAccounts.
     */
    private void populateTable() {
        mainTable.getItems().clear();
        if (!accountList.isEmpty()) {
            for (Account account : accountList) {
                mainTable.getItems().add(account);
            }
        } else {
            mainTable.setPlaceholder(new Label("Žiadne pouŽívateľské kontá."));
        }
    }

}
