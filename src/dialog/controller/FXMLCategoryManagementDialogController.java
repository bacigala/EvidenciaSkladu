
package dialog.controller;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import databaseAccess.*;
import dialog.DialogFactory;
import domain.Category;
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
                Category targetCategory = getTableView().getItems().get(getIndex());

                EditableBoolean saveRequest = new EditableBoolean(false);

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLCategoryModifyDialog.fxml"));
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
                    CategoryDAO.getInstance().modifyCategory(targetCategory);
                }

                populateTable();
            });

            deleteButton.setOnAction(t -> {
                // delete button clicked -> check and delete
                Category targetCategory = getTableView().getItems().get(getIndex());

                DialogFactory df = DialogFactory.getInstance();
                ComplexQueryHandler qh = ComplexQueryHandler.getInstance();

                Category selectedCategory = null;

                if (CategoryDAO.getInstance().hasItems(targetCategory.getId())) {
                    // v kategorii su nejake polozky
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLSimpleChoiceDialog.fxml"));
                    Parent root1 = null;
                    try {
                        root1 = fxmlLoader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    FXMLSimpleChoiceDialogController<Category> controller = fxmlLoader.getController();
                    stage.setTitle("Prevod položiek");

                    ObservableList<Category> categoreis = FXCollections.observableArrayList();
                    categoreis.addAll(CategoryDAO.getInstance().getCategoryMap().values());

                    controller.setChoiceList(categoreis);
                    controller.setLabelText("Vyberte kategóriu pod ktorú budú prevedené položky odstránenej kategoórie.");

                    stage.showAndWait();

                    selectedCategory = (Category) controller.getChoice();
                    if (selectedCategory == null) return;
                    if (selectedCategory.getId() == targetCategory.getId()) return;
                }

                // pokusime sa odstranit vybranu kategoriu
                if(CategoryDAO.getInstance().deleteCategory(targetCategory, selectedCategory)) {
                    df.showAlert(Alert.AlertType.INFORMATION, "Kategória bola úspešne odstránená");
                } else {
                    df.showAlert(Alert.AlertType.ERROR, "Kategóriu sa nepodarilo odstrániť");
                }

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

    @FXML
    private void closeDialog() {
        ((Stage) mainTable.getScene().getWindow()).close();
    }

    @FXML
    private void newCategoryButtonAction() throws IOException {
        // todo new category dialo open -> check -> QUERY
        Category newCategory = new Category(0, 0, "", "", "");
        EditableBoolean saveRequest = new EditableBoolean(false);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../fxml/FXMLCategoryModifyDialog.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root1));
        stage.initModality(Modality.APPLICATION_MODAL);
        FXMLCategoryModifyDialogController controller = fxmlLoader.getController();
        controller.initData(newCategory, saveRequest);
        stage.setTitle("Nová kategória");
        stage.showAndWait();

        if (saveRequest.get()) {
            CategoryDAO.getInstance().createCategory(newCategory);
        }

        ItemDAO.getInstance().reloadItemList();
        populateTable();
    }

    /**
     * Populates table with provided UserAccounts.
     */
    private void populateTable() {
        ObservableList<Category> categoryList
                = FXCollections.observableArrayList(CategoryDAO.getInstance().getCategoryMap().values());
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
