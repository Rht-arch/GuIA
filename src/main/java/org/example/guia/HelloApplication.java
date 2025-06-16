package org.example.guia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Vistas/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640.0, 480.0);
        stage.setTitle("Inicia sesión");
        stage.setResizable(false);
        stage.setFullScreen(false);
        stage.centerOnScreen();
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}