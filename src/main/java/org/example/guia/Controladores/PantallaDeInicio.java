package org.example.guia.Controladores;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.guia.DAOs.UserSession;
import org.example.guia.Ventanas;

/**
 * Controlador de la pantalla principal de la aplicación.
 * Gestiona la navegación a diferentes módulos como Agenda, Fichaje, Gmail y ERP.
 * @author Rafael Haro
 */
public class PantallaDeInicio {
    @FXML
    private Button agenda;
    @FXML
    private Button erp;
    @FXML
    private Button fichaje;
    @FXML
    private Button gmail;

    /**
     * Navega a la ventana de la Agenda.
     * Obtiene la ventana actual y la cambia a la vista de calendario.
     */
    @FXML
    private void abrirAgenda() {
        Stage stage = (Stage) agenda.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/calendario.fxml", "Agenda");
    }

    /**
     * Navega a la ventana de Fichaje.
     * Obtiene la ventana actual y la cambia a la vista de fichaje.
     */
    @FXML
    private void abrirFichaje() {
        Stage stage = (Stage) fichaje.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/fichaje.fxml", "Fichaje");
        System.out.println(UserSession.getInstance().getEmpleado());
    }

    /**
     * Navega a la ventana de Gmail.
     * Obtiene la ventana actual y la cambia a la vista de Gmail.
     */
    @FXML
    private void abrirGmail() {
        Stage stage = (Stage) gmail.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/gmail.fxml", "Gmail");
    }

    /**
     * Navega a la ventana del ERP.
     * Obtiene la ventana actual y la cambia a la vista de ERP.
     */
    @FXML
    private void abrirErp() {
        Stage stage = (Stage) erp.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/erp.fxml", "Erp");
    }
}
