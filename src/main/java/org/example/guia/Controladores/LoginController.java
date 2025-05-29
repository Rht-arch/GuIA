package org.example.guia.Controladores;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.guia.DTOs.Empleado;
import org.example.guia.DTOs.EmpleadoDAO;
import org.example.guia.Ventanas;

import java.sql.SQLException;

public class LoginController {
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegistro;

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    @FXML
    private void iniciarSesion() {
        try {
            Empleado empleado = empleadoDAO.autenticar(txtEmail.getText(), txtPassword.getText());

            if (empleado != null) {
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                Ventanas.cambiarVentana(stage, "Pantalla_de_inicio.fxml",
                        "Bienvenido " + empleado.getNombre());
            } else {
                mostrarAlerta("Error", "Credenciales incorrectas", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al conectar con la base de datos", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void irARegistro() {
        Stage stage = (Stage) linkRegistro.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "registro.fxml", "Registro de Usuario");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}