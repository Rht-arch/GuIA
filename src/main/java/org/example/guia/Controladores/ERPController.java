package org.example.guia.Controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.control.ProgressIndicator;
import javafx.concurrent.Worker;
import javafx.stage.Stage;
import org.example.guia.Ventanas;

/**
 * Controlador JavaFX para mostrar la interfaz de inicio de sesión de Odoo en una ventana WebView.
 * <p>Este controlador carga la página de login de Odoo mediante un componente WebView integrado
 * y muestra un indicador de progreso mientras se realiza la carga.</p>
 *
 * <p><strong>Autor:</strong> Rafael Haro<br>
 * <strong>Curso:</strong> 2º DAM<br>
 * <strong>Asignatura:</strong> Desarrollo de Interfaces</p>
 */
public class ERPController {

    /** Componente WebView que mostrará el sitio web de Odoo. */
    @FXML private WebView odoo;

    /** Indicador visual de carga mientras se carga la página web. */
    @FXML private ProgressIndicator progress;

    /**
     * Botón para volver atras
     */
    @FXML
    private Button btnVolver;


    /**
     * Método que se ejecuta automáticamente al cargar el FXML.
     * Inicializa la configuración del navegador y carga la URL de Odoo.
     */
    @FXML
    public void initialize() {
        if (odoo == null) {
            throw new IllegalStateException("WebView no fue inyectado correctamente. Verifica el archivo FXML.");
        }

        configurarWebView();
        cargarLoginOdoo();
    }

    /**
     * Configura las propiedades básicas del WebView, incluyendo el agente de usuario,
     * habilita JavaScript y vincula el progreso de carga al indicador visual.
     */
    private void configurarWebView() {
        WebEngine engine = odoo.getEngine();

        // Activar JavaScript en el motor de la WebView
        engine.setJavaScriptEnabled(true);

        // Establecer un agente de usuario personalizado (simula un navegador moderno)
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

        // Mostrar el indicador de progreso mientras se está cargando la página
        progress.visibleProperty().bind(engine.getLoadWorker().runningProperty());

        // Escuchar cambios de estado del motor de carga para depuración
        engine.getLoadWorker().stateProperty().addListener((obs, estadoAnterior, estadoNuevo) -> {
            System.out.println("Estado de carga: " + estadoNuevo);
            if (estadoNuevo == Worker.State.FAILED) {
                System.err.println("Error al cargar la página: " + engine.getLoadWorker().getException());
            }
        });
    }

    /**
     * Carga la URL de inicio de sesión de Odoo en la WebView.
     */
    private void cargarLoginOdoo() {
        String urlLogin = "https://prueba300.odoo.com/web/login";
        odoo.getEngine().load(urlLogin);
    }
    public void volveratras(ActionEvent actionEvent) {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/Pantalla_de_inicio.fxml", "Inicio");
    }
}
