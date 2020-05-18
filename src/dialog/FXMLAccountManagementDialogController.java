
package dialog;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import databaseAccess.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn.CellEditEvent;

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
        TableColumn fullNameColumn = new TableColumn<Account, String>("Meno");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn loginColumn = new TableColumn<Account, String>("login");
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn isAdminTextColumn = new TableColumn<Account, String>("administrátor");
        isAdminTextColumn.setCellValueFactory(new PropertyValueFactory<>("isAdminText"));

        TableColumn accountModifyButtonColumn = new TableColumn<Account, String>("úprava");


        mainTable.getColumns().addAll(fullNameColumn, loginColumn, isAdminTextColumn, accountModifyButtonColumn);
        QueryHandler.getInstance().getAccounts(accountList);
        populateTable();
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
