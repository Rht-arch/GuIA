package org.example.guia.Controladores;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.guia.DTOs.EmpleadoPinDAO;
import org.example.guia.DTOs.FichajeDAO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.guia.Ventanas;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FichajeController {
    @FXML
    private Text hora;

    @FXML
    private TextField numEmp;

    @FXML
    private PasswordField pin;

    @FXML
    private TextArea historial;

    @FXML
    private Button btnEntrada;

    @FXML
    private Button btnSalida;

    @FXML
    private Button configurarPin;

    private FichajeDAO fichajeDAO;
    private EmpleadoPinDAO empleadoPinDAO;
    private Connection connection;
    private int idEmpleadoActual;

    private void setConnection(Connection conn) {
        this.connection = conn;
    }

    @FXML
    public void initialize() throws SQLException {
        arrancarReloj();
        if (fichajeDAO == null) {
            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/guia", "root", "");
            setConnection(conn);
            this.fichajeDAO = new FichajeDAO();
            this.empleadoPinDAO = new EmpleadoPinDAO();

        }
    }

        public void actualizarHora () {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            hora.setText(now.format(formatter));
        }

        public void arrancarReloj () {
            actualizarHora();
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(1), event -> actualizarHora())
            );
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
        @FXML
        private void verificarEmpleado () {
            try {
                idEmpleadoActual = Integer.parseInt(numEmp.getText());
                // Verificar que el empleado existe
                if (!fichajeDAO.existeEmpleado(idEmpleadoActual)) {
                    mostrarAlerta("Error", "Empleado no encontrado", Alert.AlertType.ERROR);
                    habilitarFichaje(false);
                    return;
                }

                // Verificar que tiene PIN configurado
                if (!empleadoPinDAO.existePIN(idEmpleadoActual)) {
                    mostrarAlerta("Advertencia", "El empleado no tiene PIN configurado", Alert.AlertType.WARNING);
                    habilitarFichaje(false);
                    return;
                }
                if (fichajeDAO.existeEmpleado(idEmpleadoActual)) {
                    habilitarFichaje(true);
                    mostrarAlerta("Verificacion existosa", "Introduzca el PIN", Alert.AlertType.INFORMATION);
                    actualizarHistorial();
                } else {
                    habilitarFichaje(false);
                    mostrarAlerta("Error de verificación", "El empleado no existe", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                habilitarFichaje(false);
                mostrarAlerta("Error de formato", "El ID debe ser un número", Alert.AlertType.ERROR);
            } catch (SQLException e) {
                habilitarFichaje(false);
                mostrarAlerta("Error de datos", "No se pudo verificar el empleado", Alert.AlertType.ERROR);
            }
        }

        private void habilitarFichaje ( boolean habilitar){
            pin.setDisable(!habilitar);
            btnEntrada.setDisable(!habilitar);
            btnSalida.setDisable(!habilitar);
        }

        @FXML
        private void registrarEntrada () {
            registrarFichaje("entrada");
        }

        @FXML
        private void registrarSalida () {
            registrarFichaje("salida");
        }

        private void registrarFichaje (String tipo){
            String pins = pin.getText();

            if (pins.isEmpty() || pins.length() != 4) {
                mostrarAlerta("Error de PIN", "El PIN debe tener exactamente 4 dígitos", Alert.AlertType.ERROR);
                return;
            }

            try {
                if (fichajeDAO.registrarFichaje(idEmpleadoActual, pins, tipo)) {
                    mostrarAlerta("Fichaje registrado",
                            "Se ha registrado correctamente la " + tipo,
                            Alert.AlertType.INFORMATION);
                    actualizarHistorial();
                    pin.clear();
                } else {
                    mostrarAlerta("Error", "No se pudo registrar el fichaje", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                mostrarAlerta("Error de base de datos",
                        "Ocurrió un error al registrar: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }

        private void actualizarHistorial () throws SQLException {
            historial.clear();
            List<String> fichajes = fichajeDAO.listarFichajes(idEmpleadoActual);

            if (fichajes.isEmpty()) {
                historial.setText("No hay fichajes registrados");
            } else {
                historial.setText("Últimos fichajes:\n");
                for (String f : fichajes) {
                    historial.appendText(f + "\n");
                }
            }
        }

        private void mostrarAlerta (String titulo, String mensaje, Alert.AlertType tipo){
            Alert alert = new Alert(tipo);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        }
    @FXML
    private void abrirConfigurarPIN() {
        Stage stage = (Stage) configurarPin.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "ConfigurarPin.fxml", "Configura tu pin");

    }

}



