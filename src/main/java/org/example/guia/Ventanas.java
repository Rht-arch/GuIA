package org.example.guia;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Ventanas {
    public static void cargarVista(Stage stage, String fxmlPath, String titulo) {
        try {
            Parent root = FXMLLoader.load(Ventanas.class.getResource(fxmlPath));
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.err.println("Error al cargar la vista: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void cambiarVentana(Stage stageActual, String fxmlPath, String titulo) {
        Stage nuevoStage = new Stage();
        cargarVista(nuevoStage, fxmlPath, titulo);
        stageActual.close();
    }
}
