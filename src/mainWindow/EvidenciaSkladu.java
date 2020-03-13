
package mainWindow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EvidenciaSkladu extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLMainWindow.fxml"));        
        Scene scene = new Scene(root);         
        stage.setScene(scene);
        stage.setTitle("Evidencia inventáru v sklade");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
