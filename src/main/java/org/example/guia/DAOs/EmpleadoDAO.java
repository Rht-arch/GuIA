package org.example.guia.DAOs;

import org.example.guia.DTOs.Empleado;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.nio.file.*;

public class EmpleadoDAO {
    private static final String URL = "jdbc:mariadb://localhost:3306/guia";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public boolean registrarEmpleado(Empleado empleado) {
        String sql = "INSERT INTO empleados (nombre, apellido, nombre_empresa, correo_electronico, " +
                "contraseña, codigo_pais, telefono, imagen_perfil) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, empleado.getNombre());
            stmt.setString(2, empleado.getApellido());
            stmt.setString(3, empleado.getEmpresa());
            stmt.setString(4, empleado.getEmail());
            stmt.setString(5, empleado.getPassword()); // Debería estar hasheado
            stmt.setString(6, empleado.getCodigoPais());
            stmt.setString(7, empleado.getTelefono());

            // Manejo de la imagen de perfil
            if (empleado.getImagenPerfil() != null && !empleado.getImagenPerfil().isEmpty()) {
                Path path = Paths.get(empleado.getImagenPerfil());
                byte[] imagenBytes = Files.readAllBytes(path);
                stmt.setBytes(8, imagenBytes);
            } else {
                stmt.setNull(8, Types.BLOB);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        empleado.setId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existeEmail(String email) {
        String sql = "SELECT 1 FROM empleados WHERE correo_electronico = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Empleado autenticar(String email, String password) throws SQLException {
        String sql = "SELECT id as id_empleado, nombre, apellido, contraseña " +
        "FROM empleados WHERE correo_electronico = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && BCrypt.checkpw(password, rs.getString("contraseña"))) {
                return new Empleado(
                        rs.getInt("id_empleado"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        email
                );
            }
            return null;
        }
    }
}
