
package mainWindow;

import databaseAccess.ConnectionFactory;
import databaseAccess.ItemDAO;
import databaseAccess.Login;
import dialog.DialogFactory;
import dialog.controller.*;
import domain.CustomAttribute;
import domain.Item;
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
    private HashSet<CustomAttribute> selectedItemCustomAttributes;

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
        //set of columns for default view
        TableColumn catColumn = new TableColumn("Kategória");
        catColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn nameColumn = new TableColumn("Názov");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn barcodeColumn = new TableColumn("Kód");
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        
        TableColumn curAmountColumn = new TableColumn("Aktuálny stav");
        curAmountColumn.setCellValueFactory(new PropertyValueFactory<>("curAmount"));
        
        TableColumn unitColumn = new TableColumn("Jednotka");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        mainTable.getColumns().addAll(catColumn, nameColumn, barcodeColumn, curAmountColumn, unitColumn);

        mainTable.setPlaceholder(new Label("Niet čo zobraziť :("));

        // aside item details table
        TableColumn attributeName = new TableColumn("Atribút");
        attributeName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn attributeValue = new TableColumn("Hodnota");
        attributeValue.setCellValueFactory(new PropertyValueFactory<>("value"));

        selectedItemPropertiesTable.getColumns().addAll(attributeName, attributeValue);
        selectedItemPropertiesTable.setPlaceholder(new Label("Bez ďalších atribútov."));

        // test default connection settings, require login information
        if (ConnectionFactory.getInstance().setBasicUserConnectionDetails()) {
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
        if (ItemDAO.getInstance().reloadItemList()) {
            mainTable.getItems().clear();
            mainTable.getItems().addAll(ItemDAO.getInstance().getItemList());
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

        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            HashSet<CustomAttribute> newCustomAttributes = ItemDAO.getInstance().getItemCustomAttributes(selectedItem.getId());
            if (newCustomAttributes != null) {
                for (CustomAttribute ca : newCustomAttributes) {
                    selectedItemPropertiesTable.getItems().add(ca);
                }
            }
            selectedItemCustomAttributes = newCustomAttributes;

            //enable buttons for item manipulation
            itemSupplyButton.setDisable(false);
            itemWithdrawalButton.setDisable(false);
            if (Login.getInstance().hasAdmin()) {
                itemDetailsChangeButton.setDisable(false);
                itemMoveHistoryButton.setDisable(false);
            }
        }
    }

    /**
     * BUTTON "Vklad" (item supply)
     * @throws IOException on error
     */
    @FXML
    private void itemSupply() throws IOException {
        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLItemSupplyDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));  
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemSupplyDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem);
            stage.setTitle("Vklad položky " + selectedItem.getName());
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLItemOfftakeDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemOfftakeDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem);
            stage.setTitle("Výber položky " + selectedItem.getName());
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLItemTransactionsDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemTransactionsDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem);
            stage.setTitle("Pohyby položky " + selectedItem.getName());
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLItemModifyDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemModifyDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem, selectedItemCustomAttributes);
            stage.setTitle("Úprava položky " + selectedItem.getName());
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
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLItemModifyDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLItemModifyDialogController controller = fxmlLoader.getController();
        controller.initData(newItem, selectedItemCustomAttributes, true);
        stage.setTitle("Nová položka");
        stage.showAndWait();

        reloadMainTable();
    }

    /**
     * MENU-ITEM "Sprava DB -> pouzivatelia" (account management)
     * Opens dialog for account management. (only for administrators)
     */
    @FXML
    private void openAccountManagement() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLAccountManagementDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLAccountManagementDialogController controller = fxmlLoader.getController();
        controller.initData();
        stage.setTitle("Používateľské účty");
        stage.showAndWait();
        reloadMainTable();
    }

    /**
     * MENU-ITEM "Sprava DB -> kategorie" (category management)
     * Opens dialog for category management. (only for administrators)
     */
    @FXML
    private void openCategoryManagement() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLCategoryManagementDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Správa kategórií");
        stage.showAndWait();
        reloadMainTable();
    }

    /**
     * MENU ITEM "Kontrola" -> "Expiracia" Opens ExpiryCheckDialog.
     */
    @FXML
    private void expiryDateCheckAction() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLCheckExpirationDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLCheckExpirationDialogController controller = fxmlLoader.getController();
        controller.initData();
        stage.setTitle("Expirované položky");
        stage.showAndWait();
        reloadMainTable();
    }

    /**
     * MENU ITEM "Kontrola" -> "Nizky stav" Opens StockCheckDialog.
     */
    @FXML
    private void StockCheckAction() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLCheckAmountDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Položky v nedostatočnom množstve");
        stage.showAndWait();
        reloadMainTable();
    }

    /**
     * MENU ITEM "Kontrola" -> "Spotreba" Opens ConsumptionOverviewDialog.
     */
    @FXML
    private void ConsumptionCheckAction() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../dialog/fxml/FXMLConsumptionOverviewDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Spotreba - prehľad");
        stage.showAndWait();
    }

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
        adminMenu.setDisable(!Login.getInstance().hasAdmin());
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
        Login.getInstance().logOut();
        Stage stage = (Stage) mainTable.getScene().getWindow();
        stage.close();
    }

}
