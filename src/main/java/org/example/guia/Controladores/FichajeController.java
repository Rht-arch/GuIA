package org.example.guia.Controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.guia.DAOs.EmpleadoPinDAO;
import org.example.guia.DAOs.FichajeDAO;
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
    @FXML private Text hora;
    @FXML private TextField numEmp;
    @FXML private PasswordField pin;
    @FXML private TextArea historial;
    @FXML private Button btnEntrada;
    @FXML private Button btnSalida;
    @FXML private Button configurarPin;
    @FXML private Button volver;
    private FichajeDAO fichajeDAO;
    private EmpleadoPinDAO empleadoPinDAO;
    private Connection connection;
    private int idEmpleadoActual;

    @FXML
    public void initialize() throws SQLException {
        arrancarReloj();
        if (fichajeDAO == null) {
            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/guia", "root", "");
            this.connection = conn;
            this.fichajeDAO = new FichajeDAO(connection);
            this.empleadoPinDAO = new EmpleadoPinDAO();
        }
    }

    public void actualizarHora() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        Platform.runLater(() -> hora.setText(now.format(formatter)));
    }

    public void arrancarReloj() {
        actualizarHora();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> actualizarHora())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void verificarEmpleado() {
        try {
            String empText = numEmp.getText().trim();
            if (empText.isEmpty() || !empText.matches("\\d+")) {
                mostrarAlerta("Error", "El ID de empleado debe ser un número", Alert.AlertType.ERROR);
                habilitarFichaje(false);
                return;
            }

            idEmpleadoActual = Integer.parseInt(empText);

            // Verificación en segundo plano para no bloquear la UI
            new Thread(() -> {
                try {
                    if (!fichajeDAO.existeEmpleado(idEmpleadoActual)) {
                        Platform.runLater(() -> {
                            mostrarAlerta("Error", "Empleado no encontrado", Alert.AlertType.ERROR);
                            habilitarFichaje(false);
                        });
                        return;
                    }

                    if (!empleadoPinDAO.existePIN(idEmpleadoActual)) {
                        Platform.runLater(() -> {
                            mostrarAlerta("Advertencia",
                                    "El empleado no tiene PIN configurado",
                                    Alert.AlertType.WARNING);
                            habilitarFichaje(false);
                        });
                        return;
                    }

                    Platform.runLater(() -> {
                        habilitarFichaje(true);
                        mostrarAlerta("Verificación exitosa",
                                "Introduzca su PIN de 4 dígitos",
                                Alert.AlertType.INFORMATION);
                        try {
                            actualizarHistorial();
                        } catch (SQLException e) {
                            mostrarAlerta("Error",
                                    "No se pudo cargar el historial: " + e.getMessage(),
                                    Alert.AlertType.ERROR);
                        }
                    });
                } catch (SQLException e) {
                    Platform.runLater(() -> {
                        mostrarAlerta("Error de base de datos",
                                "No se pudo verificar el empleado: " + e.getMessage(),
                                Alert.AlertType.ERROR);
                        habilitarFichaje(false);
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "El ID debe ser un número válido", Alert.AlertType.ERROR);
            habilitarFichaje(false);
        }
    }

    private void habilitarFichaje(boolean habilitar) {
        pin.setDisable(!habilitar);
        btnEntrada.setDisable(!habilitar);
        btnSalida.setDisable(!habilitar);
        if (habilitar) {
            pin.requestFocus();
        }
    }

    @FXML
    private void registrarEntrada() {
        registrarFichaje("entrada");
    }

    @FXML
    private void registrarSalida() {
        registrarFichaje("salida");
    }

    private void registrarFichaje(String tipo) {
        String pinIngresado = pin.getText().trim();

        if (pinIngresado.isEmpty() || pinIngresado.length() != 4 || !pinIngresado.matches("\\d+")) {
            mostrarAlerta("Error de PIN",
                    "El PIN debe tener exactamente 4 dígitos numéricos",
                    Alert.AlertType.ERROR);
            pin.requestFocus();
            return;
        }

        // Ejecutar en segundo plano para no bloquear la UI
        new Thread(() -> {
            try {
                boolean registroExitoso = fichajeDAO.registrarFichaje(idEmpleadoActual, pinIngresado, tipo);

                Platform.runLater(() -> {
                    if (registroExitoso) {
                        mostrarAlerta("Fichaje registrado",
                                "Se ha registrado correctamente la " + tipo,
                                Alert.AlertType.INFORMATION);
                        pin.clear();
                        try {
                            actualizarHistorial();
                        } catch (SQLException e) {
                            mostrarAlerta("Error",
                                    "No se pudo actualizar el historial: " + e.getMessage(),
                                    Alert.AlertType.ERROR);
                        }
                    } else {
                        mostrarAlerta("Error de PIN",
                                "PIN incorrecto. No se pudo registrar el fichaje",
                                Alert.AlertType.ERROR);
                        pin.requestFocus();
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    mostrarAlerta("Error de base de datos",
                            "Ocurrió un error al registrar: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private void actualizarHistorial() throws SQLException {
        List<String> fichajes = fichajeDAO.listarFichajes(idEmpleadoActual);

        Platform.runLater(() -> {
            historial.clear();
            if (fichajes.isEmpty()) {
                historial.setText("No hay fichajes registrados");
            } else {
                StringBuilder sb = new StringBuilder("Últimos fichajes:\n");
                for (String f : fichajes) {
                    sb.append(f).append("\n");
                }
                historial.setText(sb.toString());
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Platform.runLater(() -> {
            Alert alert = new Alert(tipo);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    @FXML
    public void abrirConfigurarPIN() throws IOException {
        Ventanas.mostrarVentanaModal("Vistas/ConfigurarPin.fxml", "Configura tu PIN");
    }

    public void volveratras(ActionEvent actionEvent) {
        Stage stage = (Stage) volver.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/Pantalla_de_inicio.fxml", "Inicio");
    }
}