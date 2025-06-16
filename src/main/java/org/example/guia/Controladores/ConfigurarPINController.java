package org.example.guia.Controladores;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.guia.DAOs.EmpleadoPinDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.guia.Ventanas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConfigurarPINController {
    @FXML private TextField txtIdEmpleado;
    @FXML private PasswordField txtNuevoPIN;
    @FXML private PasswordField txtConfirmarPIN;
    @FXML private Button btnGuardar;

    private EmpleadoPinDAO pinDAO;
    private Connection connection;

    public void setConnection(Connection conn) {
        this.connection = conn;
    }
    @FXML
    public void initialize() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/guia", "root", "");
        setConnection(conn);
        this.pinDAO = new EmpleadoPinDAO();

    }

    @FXML
    private void configurarPIN() {
        // Validación básica de campos
        if (!validarCampos()) {
            return;
        }

        try {
            int idEmpleado = Integer.parseInt(txtIdEmpleado.getText().trim());
            String nuevoPIN = txtNuevoPIN.getText().trim();

            // Verificar que el empleado existe antes de asignar PIN
            if (!empleadoExiste(idEmpleado)) {
                mostrarAlerta("Error", "No existe empleado con ID: " + idEmpleado, AlertType.ERROR);
                return;
            }

            // Establecer el nuevo PIN
            if (pinDAO.establecerPIN(idEmpleado, nuevoPIN)) {
                mostrarAlerta("Éxito", "PIN configurado correctamente para empleado #" + idEmpleado, AlertType.INFORMATION);
                limpiarCampos();
            } else {
                mostrarAlerta("Error", "No se pudo configurar el PIN", AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El ID debe ser un número válido", AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error de base de datos: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validarCampos() {
        // Validar que el ID no esté vacío
        if (txtIdEmpleado.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Debe ingresar un ID de empleado", AlertType.ERROR);
            return false;
        }

        // Validar formato del PIN
        String nuevoPIN = txtNuevoPIN.getText().trim();
        String confirmacion = txtConfirmarPIN.getText().trim();

        if (nuevoPIN.isEmpty() || confirmacion.isEmpty()) {
            mostrarAlerta("Error", "Debe completar ambos campos de PIN", AlertType.ERROR);
            return false;
        }

        if (!nuevoPIN.equals(confirmacion)) {
            mostrarAlerta("Error", "Los PINs no coinciden", AlertType.ERROR);
            return false;
        }

        if (nuevoPIN.length() != 4 || !nuevoPIN.matches("\\d+")) {
            mostrarAlerta("Error", "El PIN debe tener exactamente 4 dígitos numéricos", AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean empleadoExiste(int idEmpleado) throws SQLException {
        // Consulta simple para verificar existencia del empleado
        try (var stmt = connection.prepareStatement("SELECT 1 FROM empleados WHERE id = ?")) {
            stmt.setInt(1, idEmpleado);
            try (var rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void limpiarCampos() {
        txtNuevoPIN.clear();
        txtConfirmarPIN.clear();
        txtIdEmpleado.requestFocus();
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Fichaje.fxml", "Configura tu pin");
    }

}