package org.example.guia.DAOs;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class EmpleadoPinDAO {
    private static final String URL = "jdbc:mariadb://localhost:3306/guia";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public boolean establecerPIN(int idEmpleado, String pin) throws SQLException {
        String pinHash = BCrypt.hashpw(pin, BCrypt.gensalt());
        String sql = "INSERT INTO empleados_pin (id_empleado, pin) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE pin = VALUES(pin)";

        try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEmpleado);
            stmt.setString(2, pinHash);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean verificarPIN(int idEmpleado, String pin) throws SQLException {
        String sql = "SELECT pin FROM empleados_pin WHERE id_empleado = ?";

        try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEmpleado);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String pinHash = rs.getString("pin");
                    return BCrypt.checkpw(pin, pinHash);
                }
                return false;
            }
        }
    }

    public boolean existePIN(int idEmpleado) throws SQLException {
        String sql = "SELECT 1 FROM empleados_pin WHERE id_empleado = ?";

        try (Connection conn = getConnection();PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEmpleado);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
