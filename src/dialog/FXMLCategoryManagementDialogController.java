
package dialog;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import databaseAccess.*;
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
import supportStructures.EditableBoolean;

/**
 * Dialog for category management.
 * Lists all available categories, offers options to modify, delete or add a category.
 */

public class FXMLCategoryManagementDialogController implements Initializable {

    @FXML private javafx.scene.control.TableView<Category> mainTable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TableView setup
        TableColumn<Category, String> fullNameColumn = new TableColumn<Category, String>("Názov");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Category, String> loginColumn = new TableColumn<Category, String>("Poznámka");
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        TableColumn accountModifyButtonColumn = new TableColumn<>("akcie");
        accountModifyButtonColumn.setSortable(false);

        accountModifyButtonColumn.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<Category, Boolean>, ObservableValue<Boolean>>)
                        p -> new SimpleBooleanProperty(p.getValue() != null));

        accountModifyButtonColumn.setCellFactory(
                (Callback<TableColumn<Category, Boolean>, TableCell<Category, Boolean>>) p -> new ButtonCell());

        mainTable.getColumns().addAll(fullNameColumn, loginColumn, accountModifyButtonColumn);
        populateTable();
    }

    // cell in action column
    private class ButtonCell extends TableCell<Category, Boolean> {
        final Button modifyButton = new Button("Upraviť");
        final Button deleteButton = new Button("Odstrániť");

        ButtonCell() {
            modifyButton.setOnAction(t -> {
                // todo: modify button clicked -> open dialog for modification
                Category targetCategory = getTableView().getItems().get(getIndex());

                EditableBoolean saveRequest = new EditableBoolean(false);

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLCategoryModifyDialog.fxml"));
                Parent root1 = null;
                try {
                    root1 = fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Stage stage = new Stage();
                stage.setScene(new Scene(root1));
                stage.initModality(Modality.APPLICATION_MODAL);
                FXMLCategoryModifyDialogController controller = fxmlLoader.getController();
                controller.initData(targetCategory, saveRequest);
                stage.setTitle("Upraviť kategóriu");
                stage.showAndWait();

                if (saveRequest.get()) {
                    QueryHandler.getInstance().modifyCategory(targetCategory);
                }

                populateTable();
            });

            deleteButton.setOnAction(t -> {
                // todo: delete button clicked -> check and delete
//                Account targetAccount = getTableView().getItems().get(getIndex());
//
//                DialogFactory df = DialogFactory.getInstance();
//                QueryHandler qh = QueryHandler.getInstance();
//
//                Account selectedAccount = null;
//
//                if (qh.hasTransactions(targetAccount.getId())) {
//                    // na pouzivatela su napisane nejake transakcie
//                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLSimpleChoiceDialog.fxml"));
//                    Parent root1 = null;
//                    try {
//                        root1 = fxmlLoader.load();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Stage stage = new Stage();
//                    stage.setScene(new Scene(root1));
//                    stage.initModality(Modality.APPLICATION_MODAL);
//                    FXMLSimpleChoiceDialogController<Account> controller = fxmlLoader.getController();
//                    stage.setTitle("Prevod transakcii");
//
//                    ObservableList<Account> accounts = FXCollections.observableArrayList();
//                    QueryHandler.getInstance().getAccounts(accounts);
//
//                    controller.setChoiceList(accounts);
//                    controller.setLabelText("Vyberte konto pod ktoré budú prevedené transakcie odstráneného konta.");
//
//                    stage.showAndWait();
//
//                    selectedAccount = (Account) controller.getChoice();
//                    if (selectedAccount == null) return;
//                }
//
//                // pokusime sa odstranit vybrany ucet
//                if(qh.deleteAccount(targetAccount, selectedAccount)) {
//                    df.showAlert(Alert.AlertType.INFORMATION, "Konto bolo úspešne odstránené.");
//                } else {
//                    df.showAlert(Alert.AlertType.ERROR, "Konto sa nepodarilo odstrániť");
//                }
//
//                populateTable();
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

    @FXML
    private void closeDialog() {
        ((Stage) mainTable.getScene().getWindow()).close();
    }

    @FXML
    private void newCategoryButtonAction() throws IOException {
        // todo new category dialo open -> check -> QUERY
//        Account newAccount = new Account(0, "", "", "", "", false);
//        EditableBoolean saveRequest = new EditableBoolean(false);
//
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLAccountModifyDialog.fxml"));
//        Parent root1 = fxmlLoader.load();
//        Stage stage = new Stage();
//        stage.setScene(new Scene(root1));
//        stage.initModality(Modality.APPLICATION_MODAL);
//        FXMLAccountModifyDialogController controller = fxmlLoader.getController();
//        controller.initData(newAccount, saveRequest);
//        stage.setTitle("Nové konto");
//        stage.showAndWait();
//
//        if (saveRequest.get()) {
//            QueryHandler.getInstance().createAccount(newAccount);
//        }
//
//        populateTable();
    }

    /**
     * Populates table with provided UserAccounts.
     */
    private void populateTable() {
        ObservableList<Category> categoryList
                = FXCollections.observableArrayList(QueryHandler.getInstance().getCategoryMap().values());
        mainTable.getItems().clear();
        if (!categoryList.isEmpty()) {
            for (Category category : categoryList) {
                mainTable.getItems().add(category);
            }
        } else {
            mainTable.setPlaceholder(new Label("Žiadne kategórie."));
        }
    }

}
