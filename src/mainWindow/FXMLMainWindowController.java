
package mainWindow;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
//import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.TableColumn;

public class FXMLMainWindowController implements Initializable {
     
    @FXML private javafx.scene.control.TableView<Item> theMainTable;
    @FXML private javafx.scene.control.TextField nameTextField;
    @FXML private javafx.scene.control.TextField codeTextField;
    @FXML private javafx.scene.control.TextField curAmountTextField;
    @FXML private javafx.scene.control.TextField minAmountTextField;
    @FXML private javafx.scene.control.TextField unitTextField;
    @FXML private javafx.scene.control.ChoiceBox<Category> categoryChoiceBox;
    
    @FXML
    private void openConnectionSettings(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLConnection.fxml"));       
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));  
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLConnectionController controller = fxmlLoader.<FXMLConnectionController>getController();
        
        // default db connection details - for test purposes
        controller.initData(new ConnectionDetails("192.168.1.16", "3306",
                "zubardb", "pouzivatel01", "heslo"));
        stage.setTitle("Server");
        stage.showAndWait();
        openLogInSettings();
    }
    
    @FXML
    private void openLogInSettings() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLLogIn.fxml"));       
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));  
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Používateľ");
        stage.showAndWait();
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
              
        theMainTable.getColumns().addAll(catColumn, idColumn, nameColumn,
                barcodeColumn, curAmountColumn, unitColumn);
        
        //autoFitTable(theMainTable);
    }   
    
    @FXML
    private boolean reloadMainTable() {
        clearDetails();
        QueryHandler qh = QueryHandler.getInstance();
        if (qh.reloadItemList()) {
            theMainTable.getItems().clear();
            for (Item i : qh.getItemList()) {
                theMainTable.getItems().add(i);
            } 
            return true;
        }
        return false;
    }
    
    // fires after a row gets selected
    @FXML
    private void itemSelected() {
        Item selectedItem = theMainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            nameTextField.setText(selectedItem.getName());
            codeTextField.setText(selectedItem.getBarcode());
            curAmountTextField.setText(Integer.toString(selectedItem.getCurAmount()));
            minAmountTextField.setText(Integer.toString(selectedItem.getMinAmount()));
            unitTextField.setText(selectedItem.getUnit());
            //TODO: category choiceBox
        }
    }
    
    // button "Vklad"
    @FXML
    private void itemSupply() throws IOException {
        Item selectedItem = theMainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLItemSupplyDialog.fxml"));       
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
    
    // button "Vyber"
    @FXML
    private void itemRequest() {
        Item selectedItem = theMainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            System.out.println("Vyber polozky...");
        }
    }
    
    // button "Pohyby"
    @FXML
    private void itemTransactions() {
        Item selectedItem = theMainTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            System.out.println("Pohyby polozky...");
        }
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

    
    // clears displayed details
    private void clearDetails() {
        nameTextField.setText("");
        codeTextField.setText("");
        curAmountTextField.setText("");
        minAmountTextField.setText("");
        unitTextField.setText("");
        // todo category choiceBox clear
    }
    
}
