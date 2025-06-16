package org.example.guia.Controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.guia.DAOs.Conexion;
import org.example.guia.Ventanas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Controlador para la vista de calendario y gestión de eventos.
 * <p>
 * Muestra un calendario interactivo mensual que permite al usuario visualizar eventos
 * y agregar nuevos en una fecha específica. Se conecta con una base de datos para persistencia.
 * </p>
 *
 * <p><strong>Autor:</strong> Rafael Haro</p>
 * <p><strong>Curso:</strong> 2º DAM</p>
 */
public class AgendaController {

    /** Etiqueta que muestra el mes y el año actual. */
    @FXML
    private Label mesAnioLabel;

    /** GridPane que contiene el calendario visual con los días del mes. */
    @FXML
    private GridPane panelDiasCalendario;

    /** Objeto que representa el mes y año actual mostrado. */
    private YearMonth currentYearMonth;

    /** Mapa que almacena eventos por fecha. */
    private final Map<LocalDate, List<String>> eventos = new HashMap<>();

    /** ID del usuario autenticado (ejemplo: ID 1 = Rafael Haro). */
    private int idUsuarioActual = 1;

    /** Boton de volver atras */

    @FXML
    private Button btnVolver;

    /**
     * Inicializa la vista cargando los eventos del mes actual
     * y generando el calendario.
     */
    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        cargarEventosDelMes();
        actualizarCalendario();
    }

    /**
     * Cambia la vista al mes anterior en el calendario.
     *
     * @param event evento generado al hacer clic en el botón de "mes anterior".
     */
    @FXML
    void handleAnteriorAction(ActionEvent event) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        cargarEventosDelMes();
        actualizarCalendario();
    }

    /**
     * Cambia la vista al mes siguiente en el calendario.
     *
     * @param event evento generado al hacer clic en el botón de "mes siguiente".
     */
    @FXML
    void handleSiguienteAction(ActionEvent event) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        cargarEventosDelMes();
        actualizarCalendario();
    }

    /**
     * Genera el calendario gráfico del mes actual y lo muestra en el GridPane.
     * También indica los días con eventos.
     */
    private void actualizarCalendario() {
        panelDiasCalendario.getChildren().clear();

        String nombreMes = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        mesAnioLabel.setText(nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1) + " " + currentYearMonth.getYear());

        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label lblDiaSemana = new Label(diasSemana[i]);
            GridPane.setHalignment(lblDiaSemana, HPos.CENTER);
            panelDiasCalendario.add(lblDiaSemana, i, 0);
        }

        LocalDate primerDiaDelMes = currentYearMonth.atDay(1);
        DayOfWeek diaSemanaPrimerDia = primerDiaDelMes.getDayOfWeek();
        int offsetDiasInicio = diaSemanaPrimerDia.getValue() - 1;

        int diaActualNum = 1;
        int numDiasMes = currentYearMonth.lengthOfMonth();

        for (int fila = 1; fila < 7; fila++) {
            for (int col = 0; col < 7; col++) {
                if ((fila == 1 && col < offsetDiasInicio) || diaActualNum > numDiasMes) {
                    StackPane emptyCell = new StackPane();
                    emptyCell.getStyleClass().add("empty-day-cell");
                    panelDiasCalendario.add(emptyCell, col, fila);
                } else {
                    StackPane dayCell = new StackPane();
                    dayCell.getStyleClass().add("day-cell");

                    Label dayLabel = new Label(String.valueOf(diaActualNum));
                    dayLabel.getStyleClass().add("day-number-label");

                    Circle eventIndicator = new Circle(5);
                    eventIndicator.getStyleClass().add("event-indicator");
                    StackPane.setAlignment(eventIndicator, Pos.BOTTOM_RIGHT);
                    StackPane.setMargin(eventIndicator, new Insets(0, 8, 8, 0));

                    dayCell.getChildren().addAll(dayLabel, eventIndicator);

                    final LocalDate fechaCelda = currentYearMonth.atDay(diaActualNum);
                    actualizarEstiloCelda(dayCell, eventIndicator, fechaCelda);

                    dayCell.setOnMouseClicked(e -> mostrarDialogoYEventos(fechaCelda, dayCell, eventIndicator));
                    panelDiasCalendario.add(dayCell, col, fila);
                    diaActualNum++;
                }
            }
            if (diaActualNum > numDiasMes) break;
        }
    }

    /**
     * Aplica estilos visuales a una celda según si contiene eventos o si representa el día actual.
     *
     * @param cell celda del día a modificar.
     * @param indicator círculo indicador de evento.
     * @param fecha fecha correspondiente a la celda.
     */
    private void actualizarEstiloCelda(StackPane cell, Circle indicator, LocalDate fecha) {
        cell.getStyleClass().remove("day-cell-today");
        indicator.setVisible(false);

        if (eventos.containsKey(fecha) && !eventos.get(fecha).isEmpty()) {
            indicator.setVisible(true);
        }

        if (fecha.equals(LocalDate.now())) {
            cell.getStyleClass().add("day-cell-today");
        }
    }

    /**
     * Muestra un diálogo con los eventos del día y permite agregar uno nuevo.
     *
     * @param fecha la fecha seleccionada.
     * @param cell celda del calendario seleccionada.
     * @param indicator círculo visual para indicar evento.
     */
    private void mostrarDialogoYEventos(LocalDate fecha, StackPane cell, Circle indicator) {
        List<String> eventosDelDia = eventos.get(fecha);

        if (eventosDelDia != null && !eventosDelDia.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Eventos del Día");
            alert.setHeaderText("Eventos para el " + fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"))));
            StringBuilder content = new StringBuilder();
            eventosDelDia.forEach(ev -> content.append(" • ").append(ev).append("\n"));
            alert.setContentText(content.toString().trim());
            alert.showAndWait();
        }

        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Añadir Nuevo Evento");
        dialogo.setHeaderText("Añadir nuevo evento para: " + fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"))));
        dialogo.setContentText("Descripción del nuevo evento:");

        Optional<String> resultado = dialogo.showAndWait();
        resultado.ifPresent(descripcionEvento -> {
            if (!descripcionEvento.trim().isEmpty()) {
                if (insertarEventoEnDB(fecha, descripcionEvento)) {
                    eventos.computeIfAbsent(fecha, k -> new ArrayList<>()).add(descripcionEvento);
                    actualizarEstiloCelda(cell, indicator, fecha);
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error de Base de Datos");
                    errorAlert.setHeaderText("No se pudo guardar el evento.");
                    errorAlert.setContentText("Ocurrió un error al intentar conectar con la base de datos.");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    /**
     * Carga desde la base de datos todos los eventos del mes actual para el usuario actual.
     */
    private void cargarEventosDelMes() {
        eventos.clear();

        LocalDate primerDia = currentYearMonth.atDay(1);
        LocalDate ultimoDia = currentYearMonth.atEndOfMonth();

        String sql = "SELECT fecha_evento, descripcion FROM eventos WHERE id_empleado = ? AND fecha_evento BETWEEN ? AND ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuarioActual);
            pstmt.setDate(2, java.sql.Date.valueOf(primerDia));
            pstmt.setDate(3, java.sql.Date.valueOf(ultimoDia));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalDate fecha = rs.getDate("fecha_evento").toLocalDate();
                String descripcion = rs.getString("descripcion");
                eventos.computeIfAbsent(fecha, k -> new ArrayList<>()).add(descripcion);
            }
            System.out.println("Eventos cargados para el mes: " + currentYearMonth);
        } catch (SQLException e) {
            System.err.println("Error al cargar eventos del mes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserta un nuevo evento en la base de datos.
     *
     * @param fecha fecha del evento.
     * @param descripcion descripción del evento.
     * @return true si se insertó correctamente, false si hubo error.
     */
    private boolean insertarEventoEnDB(LocalDate fecha, String descripcion) {
        String sql = "INSERT INTO eventos (id_empleado, fecha_evento, descripcion) VALUES (?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuarioActual);
            pstmt.setDate(2, java.sql.Date.valueOf(fecha));
            pstmt.setString(3, descripcion);
            pstmt.executeUpdate();
            System.out.println("Evento guardado en la base de datos.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar evento en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void volveratras(ActionEvent actionEvent) {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/Pantalla_de_inicio.fxml", "Inicio");
    }
}
