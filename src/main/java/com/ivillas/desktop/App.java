package com.ivillas.desktop;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Calse main per l'execució de l'aplicació
 */

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // codi de carrega del FXML
        Parent root = FXMLLoader.load(getClass().getResource("/vista.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Estalvia!  La teva aplicació d'estalvi en les compres");
        Image icon = new Image(getClass().getResourceAsStream("/imatges/icona.png"));
        System.out.println(icon.getUrl());
        stage.getIcons().add(icon); 
        stage.show();
        
        

    }

    public static void main(String[] args) {
        launch();
    }
}
