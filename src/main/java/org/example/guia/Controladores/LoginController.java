package org.example.guia.Controladores;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.guia.DTOs.Empleado;
import org.example.guia.DAOs.EmpleadoDAO;
import org.example.guia.Ventanas;
import org.example.guia.DAOs.UserSession;

import java.sql.SQLException;

/**
 * Controlador para la ventana de login.
 * <p>
 * Maneja la autenticación del usuario y la navegación hacia otras vistas,
 * además de iniciar la sesión global con los datos del empleado autenticado.
 * </p>
 */
public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegistro;

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas.
     * <p>
     * Si la autenticación es exitosa, inicia la sesión y cambia a la ventana principal.
     * Si falla, muestra un mensaje de error.
     * </p>
     */
    @FXML
    private void iniciarSesion() {
        try {
            Empleado empleado = empleadoDAO.autenticar(txtEmail.getText(), txtPassword.getText());

            if (empleado != null) {
                UserSession.startSession(empleado);
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                Ventanas.cambiarVentana(stage, "Vistas/Pantalla_de_inicio.fxml", "Bienvenido " + empleado.getNombre());
            } else {
                mostrarAlerta("Error", "Credenciales incorrectas", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al conectar con la base de datos", Alert.AlertType.ERROR);
        }
    }

    /**
     * Navega hacia la ventana de registro de usuario.
     */
    @FXML
    private void irARegistro() {
        Stage stage = (Stage) linkRegistro.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/registro.fxml", "Registro de Usuario");
    }

    /**
     * Muestra una alerta modal con el título, mensaje y tipo especificados.
     *
     * @param titulo Título de la alerta.
     * @param mensaje Mensaje a mostrar.
     * @param tipo Tipo de alerta (ERROR, INFORMATION, etc.).
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
