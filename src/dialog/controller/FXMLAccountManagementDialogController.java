
package dialog.controller;

import databaseAccess.AccountDAO;
import dialog.DialogFactory;
import domain.Account;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Dialog for account management.
 * Lists all registered user accounts, offers options to modify, delete or add an account.
 */

public class FXMLAccountManagementDialogController implements Initializable {

    // dialog components
    @FXML private javafx.scene.control.TableView<Account> mainTable;

    private final ObservableList<Account> accountList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TableView setup
        TableColumn<Account, String> fullNameColumn = new TableColumn<>("meno");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Account, String> loginColumn = new TableColumn<>("login");
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));

        TableColumn<Account, String> isAdminTextColumn = new TableColumn<>("administrátor");
        isAdminTextColumn.setCellValueFactory(new PropertyValueFactory<>("isAdminText"));

        TableColumn accountModifyButtonColumn = new TableColumn<>("akcie");
        accountModifyButtonColumn.setSortable(false);

        accountModifyButtonColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Account, Boolean>, ObservableValue<Boolean>>)
                        p -> new SimpleBooleanProperty(p.getValue() != null));

        accountModifyButtonColumn.setCellFactory(p -> new ButtonCell());

        mainTable.getColumns().addAll(fullNameColumn, loginColumn, isAdminTextColumn, accountModifyButtonColumn);
        mainTable.setPlaceholder(new Label("Žiadne používateľské kontá."));
        Property<ObservableList<Account>> accountListProperty = new SimpleObjectProperty<>(accountList);
        mainTable.itemsProperty().bind(accountListProperty);
        populateTable();
    }

    // cell in accountModifyButtonColumn
    private class ButtonCell extends TableCell<Account, Boolean> {
        final Button modifyButton = new Button("Upraviť");
        final Button deleteButton = new Button("Odstrániť");

        ButtonCell() {
            modifyButton.setOnAction(t -> {
                Account targetAccount = getTableView().getItems().get(getIndex());

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLAccountModifyDialog.fxml"));
                Parent root1 = null;
                try {
                    root1 = fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stage stage = new Stage();
                assert root1 != null;
                stage.setScene(new Scene(root1));
                stage.initModality(Modality.APPLICATION_MODAL);
                FXMLAccountModifyDialogController controller = fxmlLoader.getController();
                controller.initData(targetAccount);
                stage.setTitle("Upraviť konto");
                stage.showAndWait();

                populateTable();
            });

            deleteButton.setOnAction(t -> {
                Account targetAccount = getTableView().getItems().get(getIndex());
                DialogFactory df = DialogFactory.getInstance();

                Account newTransactionOwnerAccount = null;

                if (AccountDAO.getInstance().hasTransactions(targetAccount.getId())) {
                    // na pouzivatela su napisane nejake transakcie
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLSimpleChoiceDialog.fxml"));
                    Parent root1 = null;
                    try {
                        root1 = fxmlLoader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Stage stage = new Stage();
                    assert root1 != null;
                    stage.setScene(new Scene(root1));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    FXMLSimpleChoiceDialogController<Account> controller = fxmlLoader.getController();
                    stage.setTitle("Prevod transakcii");

                    ObservableList<Account> accounts = FXCollections.observableArrayList();
                    AccountDAO.getInstance().getAccounts(accounts);

                    controller.setChoiceList(accounts);
                    controller.setLabelText("Transkakcie previesť pod konto:");

                    stage.showAndWait();

                    newTransactionOwnerAccount = controller.getChoice();
                    if (newTransactionOwnerAccount == null) return;
                }

                // pokusime sa odstranit vybrany ucet
                if(AccountDAO.getInstance().deleteAccount(targetAccount, newTransactionOwnerAccount)) {
                    df.showAlert(Alert.AlertType.INFORMATION, "Konto bolo úspešne odstránené.");
                } else {
                    df.showAlert(Alert.AlertType.ERROR, "Konto sa nepodarilo odstrániť");
                }

                populateTable();
            });
        }

        HBox pane = new HBox(modifyButton, deleteButton);

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

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLAccountModifyDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLAccountModifyDialogController controller = fxmlLoader.getController();
        controller.initData(newAccount);
        stage.setTitle("Nové konto");
        stage.showAndWait();

        populateTable();
    }

    /**
     * Populates table with provided UserAccounts.
     */
    private void populateTable() {
        accountList.clear();
        AccountDAO.getInstance().getAccounts(accountList);
    }

}
