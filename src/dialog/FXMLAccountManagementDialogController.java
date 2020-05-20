
package dialog;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import databaseAccess.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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
import javafx.event.EventHandler;
import javafx.util.Callback;
import supportStructures.EditableBoolean;

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

        TableColumn accountModifyButtonColumn = new TableColumn<>("akcie");
        accountModifyButtonColumn.setSortable(false);

        accountModifyButtonColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Account, Boolean>, ObservableValue<Boolean>>)
                        p -> new SimpleBooleanProperty(p.getValue() != null));

        accountModifyButtonColumn.setCellFactory(
                (Callback<TableColumn<Account, Boolean>, TableCell<Account, Boolean>>) p -> new ButtonCell());

        mainTable.getColumns().addAll(fullNameColumn, loginColumn, isAdminTextColumn, accountModifyButtonColumn);
        populateTable();
    }

    // cell in action column
    private class ButtonCell extends TableCell<Account, Boolean> {
        final Button modifyButton = new Button("Upraviť");
        final Button deleteButton = new Button("Odstrániť");

        ButtonCell() {
            modifyButton.setOnAction(t -> {
                // todo: modify button clicked -> open dialog for modification
                Account targetAccount = getTableView().getItems().get(getIndex());

                EditableBoolean saveRequest = new EditableBoolean(false);

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLAccountModifyDialog.fxml"));
                Parent root1 = null;
                try {
                    root1 = fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                    // todo: nepodarilo sa otvorit dialog ?
                }
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.initModality(Modality.APPLICATION_MODAL);
                FXMLAccountModifyDialogController controller = fxmlLoader.getController();
                controller.initData(targetAccount, saveRequest);
                stage.setTitle("Upraviť konto");
                stage.showAndWait();

                if (saveRequest.get()) {
                    QueryHandler.getInstance().modifyAccount(targetAccount);
                }

                populateTable();
            });

            deleteButton.setOnAction(t -> {
                // todo: delete button clicked -> check and delete
                Account targetAccount = getTableView().getItems().get(getIndex());
                System.out.println("Account to be deleted: name = " + targetAccount.getFullName());
                populateTable();
            });
        }

        HBox pane = new HBox(modifyButton, deleteButton);

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
    private void newAccountButtonAction() throws IOException {
        Account newAccount = new Account(0, "", "", "", "", false);
        EditableBoolean saveRequest = new EditableBoolean(false);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLAccountModifyDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLAccountModifyDialogController controller = fxmlLoader.getController();
        controller.initData(newAccount, saveRequest);
        stage.setTitle("Nové konto");
        stage.showAndWait();

        if (saveRequest.get()) {
            QueryHandler.getInstance().createAccount(newAccount);
        }

        populateTable();
    }

    /**
     * Populates table with provided UserAccounts.
     */
    private void populateTable() {
        accountList.clear();
        QueryHandler.getInstance().getAccounts(accountList);
        mainTable.getItems().clear();
        if (!accountList.isEmpty()) {
            for (Account account : accountList) {
                mainTable.getItems().add(account);
            }
        } else {
            mainTable.setPlaceholder(new Label("Žiadne používateľské kontá."));
        }
    }

}
