
package mainWindow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EvidenciaSkladu extends Application {

    private FXMLMainWindowController mainWindowController = null;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(EvidenciaSkladu.class.getResource("/fxml/FXMLMainWindow.fxml"));
        Parent root = fxmlLoader.load();
        mainWindowController = fxmlLoader.getController();
        Scene scene = new Scene(root);         
        stage.setScene(scene);
        stage.setTitle("Evidencia inventáru v sklade");
        stage.show();
    }

    @Override
    public void stop(){
        // application stop -> close all connections, stop refresh thread
        mainWindowController.applicationClose();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
