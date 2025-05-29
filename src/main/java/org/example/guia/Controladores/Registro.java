package org.example.guia.Controladores;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.guia.DTOs.Empleado;
import org.example.guia.DTOs.EmpleadoDAO;
import org.example.guia.Ventanas;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;

public class Registro {
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmpresa;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private ImageView imgPerfil;
    @FXML private Button btnSeleccionarImagen;
    @FXML private ComboBox<String> cbCodigoPais;
    @FXML private TextField txtTelefono;
    @FXML private CheckBox checkTerminos;
    @FXML private Button btnRegistrar;
    @FXML private Hyperlink linkLogin;

    private File archivoImagen;
    private EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    @FXML
    public void initialize() {
        // Configurar ComboBox de códigos de país
        cbCodigoPais.getItems().addAll("+34", "+1", "+52", "+54", "+55", "+56", "+57", "+58");
        cbCodigoPais.getSelectionModel().selectFirst();

        // Deshabilitar botón de registro hasta aceptar términos
        btnRegistrar.disableProperty().bind(checkTerminos.selectedProperty().not());
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        Stage stage = (Stage) btnSeleccionarImagen.getScene().getWindow();
        archivoImagen = fileChooser.showOpenDialog(stage);

        if (archivoImagen != null) {
            Image image = new Image(archivoImagen.toURI().toString());
            imgPerfil.setImage(image);
        }
    }

    @FXML
    private void registrarEmpleado() {
        if (!validarCampos()) {
            return;
        }

        if (empleadoDAO.existeEmail(txtEmail.getText())) {
            mostrarAlerta("Error", "El correo electrónico ya está registrado", Alert.AlertType.ERROR);
            return;
        }
        String hashedPassword = BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt());
        Empleado nuevoEmpleado = new Empleado(
                txtNombre.getText(),
                txtApellido.getText(),
                txtEmpresa.getText(),
                txtEmail.getText(),
                hashedPassword, // En producción, hashear aquí
                cbCodigoPais.getValue(),
                txtTelefono.getText(),
                archivoImagen != null ? archivoImagen.getAbsolutePath() : null
        );

        if (empleadoDAO.registrarEmpleado(nuevoEmpleado)) {
            mostrarAlerta("Éxito", "Registro completado correctamente", Alert.AlertType.INFORMATION);
            limpiarFormulario();
        } else {
            mostrarAlerta("Error", "No se pudo completar el registro", Alert.AlertType.ERROR);
        }
        // Antes de guardar el empleado

    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() ||
                txtEmpresa.getText().isEmpty() || txtEmail.getText().isEmpty() ||
                txtPassword.getText().isEmpty() || txtTelefono.getText().isEmpty()) {

            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.ERROR);
            return false;
        }

        if (!txtEmail.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            mostrarAlerta("Error", "Ingrese un correo electrónico válido", Alert.AlertType.ERROR);
            return false;
        }

        if (txtPassword.getText().length() < 8) {
            mostrarAlerta("Error", "La contraseña debe tener al menos 8 caracteres", Alert.AlertType.ERROR);
            return false;
        }

        if (!txtTelefono.getText().matches("^[0-9]+$")) {
            mostrarAlerta("Error", "El teléfono solo debe contener números", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellido.clear();
        txtEmpresa.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtTelefono.clear();
        imgPerfil.setImage(null);
        archivoImagen = null;
        cbCodigoPais.getSelectionModel().selectFirst();
        checkTerminos.setSelected(false);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void irALogin() {
        Stage stage = (Stage) linkLogin.getScene().getWindow();
        Ventanas.cambiarVentana(stage,"login.fxml","Inicio de sesion");
    }
}
