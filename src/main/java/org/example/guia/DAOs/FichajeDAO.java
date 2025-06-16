package org.example.guia.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FichajeDAO {
    private Connection connection;

    public FichajeDAO(Connection connection) {
        this.connection = connection;
    }

    // Verifica si un empleado existe
    public boolean existeEmpleado(int idEmpleado) throws SQLException {
        String sql = "SELECT 1 FROM empleados WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idEmpleado);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Registra un fichaje (entrada o salida) verificando el PIN
    public boolean registrarFichaje(int idEmpleado, String pinUsado, String tipo) throws SQLException {
        // Primero verificar el PIN
        EmpleadoPinDAO pinDAO = new EmpleadoPinDAO();
        boolean pinValido = pinDAO.verificarPIN(idEmpleado, pinUsado);

        if (!pinValido) {
            // Registrar el intento fallido pero devolver false
            registrarIntentoFallido(idEmpleado, pinUsado, tipo);
            return false;
        }

        String sql = "INSERT INTO fichajes (id_empleado, tipo, pin_usado, es_pin_valido) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idEmpleado);
            stmt.setString(2, tipo);
            stmt.setString(3, pinUsado);
            stmt.setBoolean(4, true);

            return stmt.executeUpdate() > 0;
        }
    }

    // Registra intentos fallidos de fichaje
    private void registrarIntentoFallido(int idEmpleado, String pinUsado, String tipo) throws SQLException {
        String sql = "INSERT INTO fichajes (id_empleado, tipo, pin_usado, es_pin_valido) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idEmpleado);
            stmt.setString(2, tipo);
            stmt.setString(3, pinUsado);
            stmt.setBoolean(4, false);

            stmt.executeUpdate();
        }
    }

    // Obtiene el historial de fichajes de un empleado
    public List<String> listarFichajes(int idEmpleado) throws SQLException {
        List<String> fichajes = new ArrayList<>();
        String sql = "SELECT tipo, fecha_hora, es_pin_valido " +
                "FROM fichajes " +
                "WHERE id_empleado = ? " +
                "ORDER BY fecha_hora DESC " +
                "LIMIT 10";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idEmpleado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    String fecha = rs.getTimestamp("fecha_hora").toString();
                    boolean valido = rs.getBoolean("es_pin_valido");

                    String estado = valido ? "✓" : "✗ (PIN incorrecto)";
                    fichajes.add(String.format("[%s] %s - %s", estado, tipo, fecha));
                }
            }
        }
        return fichajes;
    }
}