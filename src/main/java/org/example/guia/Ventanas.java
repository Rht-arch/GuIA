package org.example.guia;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

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

    public static void mostrarVentanaModal(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Ventanas.class.getResource(fxml));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void cambiarVentanaConTamaño(Stage stageActual, String fxmlPath,
                                               String titulo, double ancho, double alto,
                                               boolean mantenerPosicion) {
        try {
            // Cargar el FXML
            FXMLLoader loader = new FXMLLoader(Ventanas.class.getResource("/org/example/guia/" + fxmlPath));
            Parent root = loader.load();

            // Crear nueva escena
            Scene escena;
            if (ancho > 0 && alto > 0) {
                escena = new Scene(root, ancho, alto);
            } else {
                escena = new Scene(root);
            }

            // Configurar el nuevo stage
            Stage nuevoStage = new Stage();
            nuevoStage.setScene(escena);

            // Aplicar título si se especificó
            if (titulo != null && !titulo.isEmpty()) {
                nuevoStage.setTitle(titulo);
            }

            // Configurar tamaño mínimo si se especificó tamaño
            if (ancho > 0 && alto > 0) {
                nuevoStage.setMinWidth(ancho);
                nuevoStage.setMinHeight(alto);
            }

            // Manejar posición
            if (mantenerPosicion && stageActual != null) {
                nuevoStage.setX(stageActual.getX());
                nuevoStage.setY(stageActual.getY());
            } else {
                nuevoStage.centerOnScreen();
            }

            // Cerrar ventana actual si existe
            if (stageActual != null) {
                stageActual.close();
            }

            nuevoStage.show();

        } catch (IOException e) {
            System.err.println("Error al cambiar a ventana: " + fxmlPath);
            e.printStackTrace();
        }
    }

}
