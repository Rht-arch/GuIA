package org.example.guia.Controladores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class AgendaController { // O CalendarioController

    @FXML
    private Label mesAnioLabel;
    @FXML
    private GridPane panelDiasCalendario;

    private YearMonth currentYearMonth;
    private Map<LocalDate, List<String>> eventos = new HashMap<>();

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        actualizarCalendario();
    }

    @FXML
    void handleAnteriorAction(ActionEvent event) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        actualizarCalendario();
    }

    @FXML
    void handleSiguienteAction(ActionEvent event) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        actualizarCalendario();
    }

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
                    eventIndicator.setVisible(false);
                    StackPane.setAlignment(eventIndicator, Pos.BOTTOM_RIGHT);
                    StackPane.setMargin(eventIndicator, new Insets(0, 8, 8, 0));

                    dayCell.getChildren().addAll(dayLabel, eventIndicator);

                    final LocalDate fechaCelda = currentYearMonth.atDay(diaActualNum);
                    actualizarEstiloCelda(dayCell, dayLabel, eventIndicator, fechaCelda);

                    dayCell.setOnMouseClicked(e -> mostrarDialogoYEventos(fechaCelda, dayCell, dayLabel, eventIndicator));
                    panelDiasCalendario.add(dayCell, col, fila);
                    diaActualNum++;
                }
            }
            if (diaActualNum > numDiasMes) break;
        }
    }

    private void actualizarEstiloCelda(StackPane cell, Label label, Circle indicator, LocalDate fecha) {
        cell.getStyleClass().remove("day-cell-today");
        indicator.setVisible(false);

        if (eventos.containsKey(fecha) && !eventos.get(fecha).isEmpty()) {
            indicator.setVisible(true);
        }

        if (fecha.equals(LocalDate.now())) {
            cell.getStyleClass().add("day-cell-today");
        }
    }

    private void mostrarDialogoYEventos(LocalDate fecha, StackPane cell, Label label, Circle indicator) {
        List<String> eventosDelDia = eventos.get(fecha);

        if (eventosDelDia != null && !eventosDelDia.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Eventos del Día");
            alert.setHeaderText("Eventos para el " + fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"))));

            StringBuilder content = new StringBuilder();
            for (int i = 0; i < eventosDelDia.size(); i++) {
                content.append(" • ").append(eventosDelDia.get(i)).append("\n");
            }
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
                eventos.computeIfAbsent(fecha, k -> new ArrayList<>()).add(descripcionEvento);
                actualizarEstiloCelda(cell, label, indicator, fecha);
            }
        });
    }
}