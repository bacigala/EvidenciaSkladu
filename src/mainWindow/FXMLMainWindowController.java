
package mainWindow;

import databaseAccess.CustomAttribute;
import databaseAccess.Item;
import databaseAccess.QueryHandler;
import dialog.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

/**
 * The main window of the application.
 */

public class FXMLMainWindowController implements Initializable {
     
    @FXML private javafx.scene.control.TableView<Item> mainTable;
    @FXML private javafx.scene.control.TableView<CustomAttribute> selectedItemPropertiesTable;
    @FXML private javafx.scene.control.Button itemSupplyButton;
    @FXML private javafx.scene.control.Button itemWithdrawalButton;
    @FXML private javafx.scene.control.Button itemDetailsChangeButton;
    @FXML private javafx.scene.control.Button itemMoveHistoryButton;
    @FXML private javafx.scene.control.Button databaseRefreshButton;
    @FXML private javafx.scene.control.Menu adminMenu;

    //stores currently selected item custom attributes
    private HashSet<CustomAttribute> selectedItemCustomAttributes = null;

    @FXML
    private void openLogInSettings() {
        if (DialogFactory.getInstance().showUserLoginDialog()) {
            userLoginState();
        } else {
            userLogoutState();
        }
        reloadMainTable();
    }
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //table adjust
        TableColumn catColumn = new TableColumn("Kategória");
        catColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        
        TableColumn idColumn = new TableColumn("Id (debug)");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn nameColumn = new TableColumn("Názov");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn barcodeColumn = new TableColumn("Kód");
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        
        TableColumn curAmountColumn = new TableColumn("Aktuálny stav");
        curAmountColumn.setCellValueFactory(new PropertyValueFactory<>("curAmount"));
        
        TableColumn unitColumn = new TableColumn("Jednotka");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
              
        mainTable.getColumns().addAll(catColumn, idColumn, nameColumn,
                barcodeColumn, curAmountColumn, unitColumn);

        mainTable.setPlaceholder(new Label("Niet čo zobraziť :("));

        // aside item details table
        TableColumn attributeName = new TableColumn("Atribút");
        attributeName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn attributeValue = new TableColumn("Hodnota");
        attributeValue.setCellValueFactory(new PropertyValueFactory<>("value"));

        selectedItemPropertiesTable.getColumns().addAll(attributeName, attributeValue);
        selectedItemPropertiesTable.setPlaceholder(new Label("Bez ďalších atribútov."));

        // test default connection settings, require login information
        QueryHandler queryHandler = QueryHandler.getInstance();
        if (queryHandler.setBasicUserConnectionDetails()) {
            // successfully established database connection
            openLogInSettings();
        } else {
            String message = "Nepodarilo sa pripojiť na server s použitím prednastavných hodnôt, upravte ich prosím " +
                    "v nastaveniach.";
            DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, message);
        }
    }   
    
    @FXML
    private boolean reloadMainTable() {
        clearItemDetails();
        QueryHandler qh = QueryHandler.getInstance();
        if (qh.reloadItemList()) {
            mainTable.getItems().clear();
            for (Item i : qh.getItemList()) {
                mainTable.getItems().add(i);
            } 
            return true;
        }
        return false;
    }

    /**
     * Loads item details after one is selected.
     */
    @FXML
    private void itemSelected() {
        // clear previously loaded custom attributes
        selectedItemPropertiesTable.getItems().clear();

        QueryHandler qh = QueryHandler.getInstance();
        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            HashSet<CustomAttribute> newCustomAttributes = qh.getItemCustomAttributes(selectedItem.getId());
            if (newCustomAttributes != null) {
                for (CustomAttribute ca : newCustomAttributes) {
                    selectedItemPropertiesTable.getItems().add(ca);
                }
            }
            selectedItemCustomAttributes = newCustomAttributes;

            //enable buttons for item manipulation
            itemSupplyButton.setDisable(false);
            itemWithdrawalButton.setDisable(false);
            if (QueryHandler.getInstance().hasAdmin()) {
                itemDetailsChangeButton.setDisable(false);
                itemMoveHistoryButton.setDisable(false);
            }
        }
    }

    /**
     * BUTTON "Vklad" (item supply)
     * @throws IOException
     */
    @FXML
    private void itemSupply() throws IOException {
        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/FXMLItemSupplyDialog.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));  
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemSupplyDialogController controller = fxmlLoader.<FXMLItemSupplyDialogController>getController();
            controller.initData(selectedItem);
            stage.showAndWait();
            reloadMainTable();
        }
    }

    /**
     * BUTTON "Vyber" (item withdrawal)
     */
    @FXML
    private void itemRequest() throws IOException {
        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/FXMLItemOfftakeDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemOfftakeDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem);
            stage.showAndWait();
            reloadMainTable();
        }
    }

    /**
     * BUTTON "Pohyby" (item move history)
     */
    @FXML
    private void itemTransactions() throws IOException {
        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/FXMLItemTransactionsDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemTransactionsDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem);
            stage.showAndWait();
            reloadMainTable();
        }
    }

    /**
     * BUTTON "Uprava" (item modify)
     */
    @FXML
    private void itemModify() throws IOException {
        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/FXMLItemModifyDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemModifyDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem, selectedItemCustomAttributes);
            stage.showAndWait();
            reloadMainTable();
        }
    }

    /**
     * MENU-ITEM "Sprava DB -> nova polozka" (new item)
     * Opens dialog for new item creation. (only for administrators)
     */
    @FXML
    private void openNewItemDialog() throws IOException {
        Item newItem = new Item(0, "", "", 0, 0, "", "", 1);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/FXMLItemModifyDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLItemModifyDialogController controller = fxmlLoader.getController();
        controller.initData(newItem, selectedItemCustomAttributes, true);
        stage.showAndWait();

        reloadMainTable();
    }

    /**
     * MENU-ITEM "Sprava DB -> pouzivatelia" (account management)
     * Opens dialog for account management. (only for administrators)
     */
    @FXML
    private void openAccountManagement() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/FXMLAccountManagementDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLAccountManagementDialogController controller = fxmlLoader.getController();
        controller.initData();
        stage.showAndWait();
        reloadMainTable();
    }

    /**
     * MENU-ITEM "Sprava DB -> kategorie" (category management)
     * Opens dialog for category management. (only for administrators)
     */
    @FXML
    private void openCategoryManagement() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/FXMLCategoryManagementDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLCategoryManagementDialogController controller = fxmlLoader.getController();
        controller.initData();
        stage.showAndWait();
        reloadMainTable();
    }

    /**
     * MENU ITEM "Kontrola" -> "Expiracia" Opens ExpiryCheckDialog.
     */
    @FXML
    private void expiryDateCheckAction() {
        // todo: implement this
    }

    /**
     * MENU ITEM "Kontrola" -> "Nizky stav" Opens StockCheckDialog.
     */
    @FXML
    private void StockCheckAction() {
        // todo: implement this too :)
    }
         
    // todo ensures that all columns are wide enough to show full content
    /*
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void autoFitTable(TableView tableView) {
        tableView.getItems().addListener(new ListChangeListener<Object>() {
            @Override
            public void onChanged(Change<?> c) {
                for (Object column : tableView.getColumns()) {
                    try {
                        columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }
    
     */

    /**
     * Disables GUI functions and hides all visible information which require login to be viewed.
     */
    private void userLogoutState() {
        clearItemDetails();
        mainTable.getItems().clear();
        databaseRefreshButton.setDisable(true);
        adminMenu.setDisable(true);
    }

    /**
     * Enables GUI functions for logged-in user.
     */
    private void userLoginState() {
        databaseRefreshButton.setDisable(false);
        adminMenu.setDisable(!QueryHandler.getInstance().hasAdmin());
    }

    /**
     * Clears displayed item details and disables buttons related to item manipulation.
     */
    private void clearItemDetails() {
        selectedItemPropertiesTable.getItems().clear();
        selectedItemCustomAttributes = null;
        itemSupplyButton.setDisable(true);
        itemWithdrawalButton.setDisable(true);
        itemDetailsChangeButton.setDisable(true);
        itemMoveHistoryButton.setDisable(true);
    }

    /**
     * Closes application.
     */
    @FXML
    private void closeApplicationAction(){
        QueryHandler queryHandler = QueryHandler.getInstance();
        queryHandler.logOut();
        Stage stage = (Stage) mainTable.getScene().getWindow();
        stage.close();
    }

}
