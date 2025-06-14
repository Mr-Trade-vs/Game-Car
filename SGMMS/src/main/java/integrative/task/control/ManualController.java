package integrative.task.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


import java.io.IOException;

public class ManualController {
    @FXML
    private Button btStartGame;

    @FXML
    private void startGame(ActionEvent event) {
        try {
            // Cargar el nuevo archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/integrative/map/map.fxml"));
            Parent root = loader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) btStartGame.getScene().getWindow();

            // Cambiar la escena
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
