
package mainWindow;

import databaseAccess.ConnectionFactory;
import databaseAccess.CustomExceptions.UserWarningException;
import databaseAccess.ItemDAO;
import databaseAccess.Login;
import dialog.DialogFactory;
import dialog.controller.*;
import domain.CustomAttribute;
import domain.Item;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Properties;
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
    @FXML private javafx.scene.control.Button searchButton;
    @FXML private javafx.scene.control.TextField searchTextField;
    @FXML private javafx.scene.control.Menu adminMenu;
    @FXML private javafx.scene.control.Label lastRefreshLabel;
    @FXML private javafx.scene.control.CheckBox autoRefreshCheckBox;


    //stores currently selected item custom attributes
    private HashSet<CustomAttribute> selectedItemCustomAttributes;

    // thread for table auto-refresh
    private TableRefreshThread tableRefreshThread;

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

        // try to retrieve server IP and port from properties, otherwise use default
        String ip = "";
        String port = "";

        try {
            Properties appProps = new Properties();
            Path PropertyFile = Paths.get("EvidenciaSkladu.properties");
            Reader PropReader = Files.newBufferedReader(PropertyFile);
            appProps.load(PropReader);

            ip = appProps.getProperty("server-ip", "");
            port = appProps.getProperty("server-port", "");
            PropReader.close();
        } catch (IOException e) {
            System.err.println("PropertiesFileNotFoundException: " + e.getMessage());
        }

        // test default connection settings, require login information
        if (ConnectionFactory.getInstance().setConnectionDetails(ip, port)) {
            // successfully established database connection
            openLogInSettings();
        } else {
            String message = "Nepodarilo sa pripojiť na server s použitím prednastavných hodnôt, upravte ich prosím " +
                    "v nastaveniach.";
            DialogFactory.getInstance().showAlert(Alert.AlertType.INFORMATION, message);
        }
    }

    /**
     * Requests update of item list in ItemDAO and reloads main table.
     */
    @FXML
    private void reloadMainTable() {
        reloadMainTable("");
    }

    private void reloadMainTable(String searchPattern) {
        clearItemDetails();
        try {
            if (searchPattern.equals("")) {
                ItemDAO.getInstance().reloadItemList();
            } else {
                ItemDAO.getInstance().reloadItemList(searchPattern);
            }
            Platform.runLater(() -> {
                mainTable.getItems().clear();
                mainTable.getItems().addAll(ItemDAO.getInstance().getItemList());
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                lastRefreshLabel.setText("Aktualizované " + sdf.format(cal.getTime()));
            });
        } catch (UserWarningException e) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.WARNING, e.getMessage());
        } catch (Exception e) {
            DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Neočakávaná chyba.");
            e.printStackTrace();
        }
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
            autoRefreshPause();

            // load custom attributes
            try {
                HashSet<CustomAttribute> newCustomAttributes = ItemDAO.getInstance().getItemCustomAttributes(selectedItem.getId());
                selectedItemPropertiesTable.getItems().addAll(newCustomAttributes);
                selectedItemCustomAttributes = newCustomAttributes;
            } catch (UserWarningException e) {
                DialogFactory.getInstance().showAlert(Alert.AlertType.WARNING, e.getMessage());
            } catch (Exception e) {
                DialogFactory.getInstance().showAlert(Alert.AlertType.ERROR, "Neočakávaná chyba.");
            }

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
            autoRefreshPause();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLItemSupplyDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));  
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemSupplyDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem);
            stage.setTitle("Vklad položky " + selectedItem.getName());
            stage.showAndWait();
            autoRefreshResume();
        }
    }

    /**
     * BUTTON "Vyber" (item withdrawal)
     */
    @FXML
    private void itemRequest() throws IOException {
        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            autoRefreshPause();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLItemOfftakeDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemOfftakeDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem);
            stage.setTitle("Výber položky " + selectedItem.getName());
            stage.showAndWait();
            autoRefreshResume();
        }
    }

    /**
     * BUTTON "Pohyby" (item move history)
     */
    @FXML
    private void itemTransactions() throws IOException {
        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            autoRefreshPause();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLItemTransactionsDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemTransactionsDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem);
            stage.setTitle("Pohyby položky " + selectedItem.getName());
            stage.showAndWait();
            autoRefreshResume();
        }
    }

    /**
     * BUTTON "Uprava" (item modify)
     */
    @FXML
    private void itemModify() throws IOException {

        Item selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            autoRefreshPause();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLItemModifyDialog.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLItemModifyDialogController controller = fxmlLoader.getController();
            controller.initData(selectedItem, selectedItemCustomAttributes);
            stage.setTitle("Úprava položky " + selectedItem.getName());
            stage.showAndWait();
            autoRefreshResume();
        }
    }

    /**
     * MENU-ITEM "Sprava DB -> nova polozka" (new item)
     * Opens dialog for new item creation. (only for administrators)
     */
    @FXML
    private void openNewItemDialog() throws IOException {
        autoRefreshPause();
        Item newItem = new Item(0, "", "", 0, 0, "", "", 1);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLItemModifyDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLItemModifyDialogController controller = fxmlLoader.getController();
        controller.initData(newItem, selectedItemCustomAttributes, true);
        stage.setTitle("Nová položka");
        stage.showAndWait();
        autoRefreshResume();
    }

    /**
     * MENU-ITEM "Sprava DB -> pouzivatelia" (account management)
     * Opens dialog for account management. (only for administrators)
     */
    @FXML
    private void openAccountManagement() throws IOException {
        autoRefreshPause();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLAccountManagementDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLAccountManagementDialogController controller = fxmlLoader.getController();
        controller.initData();
        stage.setTitle("Používateľské účty");
        stage.showAndWait();
        autoRefreshResume();
    }

    /**
     * MENU-ITEM "Sprava DB -> kategorie" (category management)
     * Opens dialog for category management. (only for administrators)
     */
    @FXML
    private void openCategoryManagement() throws IOException {
        autoRefreshPause();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLCategoryManagementDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Správa kategórií");
        stage.showAndWait();
        autoRefreshResume();
    }

    /**
     * MENU ITEM "Kontrola" -> "Expiracia" Opens ExpiryCheckDialog.
     */
    @FXML
    private void expiryDateCheckAction() throws IOException {
        autoRefreshPause();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLCheckExpirationDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLCheckExpirationDialogController controller = fxmlLoader.getController();
        controller.initData();
        stage.setTitle("Expirované položky");
        stage.showAndWait();
        autoRefreshResume();
    }

    /**
     * MENU ITEM "Kontrola" -> "Nizky stav" Opens StockCheckDialog.
     */
    @FXML
    private void StockCheckAction() throws IOException {
        autoRefreshPause();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLCheckAmountDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Položky v nedostatočnom množstve");
        stage.showAndWait();
        autoRefreshResume();
    }

    /**
     * MENU ITEM "Kontrola" -> "Spotreba" Opens ConsumptionOverviewDialog.
     */
    @FXML
    private void ConsumptionCheckAction() throws IOException {
        autoRefreshPause();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/FXMLConsumptionOverviewDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Spotreba - prehľad");
        stage.showAndWait();
        autoRefreshResume();
    }

    /**
     * Disables GUI functions and hides all visible information which require login to be viewed.
     */
    private void userLogoutState() {
        clearItemDetails();
        mainTable.getItems().clear();
        adminMenu.setDisable(true);

        // auto-refresh
        databaseRefreshButton.setDisable(true);
        autoRefreshCheckBox.setDisable(true);
        autoRefreshStop();
    }

    /**
     * Enables GUI functions for logged-in user.
     */
    private void userLoginState() {
        adminMenu.setDisable(!Login.getInstance().hasAdmin());

        // auto-refresh
        databaseRefreshButton.setDisable(false);
        autoRefreshCheckBox.setDisable(false);
        autoRefreshCheckBox.setSelected(true);
        autoRefreshStart();
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

    // menu option
    @FXML
    private void closeApplicationAction(){
        Stage stage = (Stage) mainTable.getScene().getWindow();
        stage.close();
    }

    // called after last application window has been closed
    public void applicationClose() {
        autoRefreshStop();
        Login.getInstance().logOut();
    }


    // AUTOMATIC TABLE REFRESHING

    // thread for main table refresh
    private class TableRefreshThread extends Thread {
        public boolean canRun = true;
        private boolean stopRequest = false;

        @Override
        public void run() {
            while (!stopRequest) {
                if (canRun) reloadMainTable();
                try {
                    sleep(600000);
                } catch (InterruptedException ignored) {

                }
            }
        }

        public void requestStop() {
            stopRequest = true;
            tableRefreshThread = null;
        }
    }

    // checkbox for auto refresh toogled
    @FXML
    private void autoRefreshCheckBoxAction() {
        if (autoRefreshCheckBox.isSelected()) {
            autoRefreshStart();
        } else {
            autoRefreshStop();
        }
    }

    // called after user-action on refreshed data || user checks auto-refresh CheckBox
    private void autoRefreshStart() {
        if (tableRefreshThread == null) {
            tableRefreshThread = new TableRefreshThread();
            tableRefreshThread.setDaemon(true);
            tableRefreshThread.start();
            autoRefreshCheckBox.setTextFill(Paint.valueOf("green"));
        } else {
            autoRefreshResume();
        }
    }

    // called when user performs actions - no need to reload
    private void autoRefreshPause() {
        if (tableRefreshThread != null) {
            tableRefreshThread.canRun = false;
            autoRefreshCheckBox.setTextFill(Paint.valueOf("red"));
        }
    }

    @FXML
    private void autoRefreshResume() {
        if (tableRefreshThread != null) {
            tableRefreshThread.canRun = true;
            autoRefreshCheckBox.setTextFill(Paint.valueOf("green"));
        }
        reloadMainTable();
    }

    // no more auto-refresh expected e.g. auto-refresh CheckBox was unchecked
    private void autoRefreshStop() {
        if (tableRefreshThread != null) {
            tableRefreshThread.requestStop();
            autoRefreshCheckBox.setTextFill(Paint.valueOf("red"));
        }
    }

    // ITEM SEARCH
    @FXML
    private void searchButtonAction() {
        searchButton.setDisable(true);
        if (searchTextField.getText().equals("")) {
            autoRefreshResume();
        } else {
            autoRefreshPause();
            searchButton.setText("Hľadám...");
            reloadMainTable(searchTextField.getText());
            searchButton.setText("Vyhľadať");
        }
        searchButton.setDisable(false);
    }

    @FXML
    private void searchTextFieldKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            searchButtonAction();
        }
    }

}
