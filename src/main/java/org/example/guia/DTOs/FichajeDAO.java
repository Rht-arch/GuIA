package org.example.guia.DTOs;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FichajeDAO {

    private static final String URL = "jdbc:mariadb://localhost:3306/guia";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public boolean existeEmpleado(int id) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM empleados WHERE id = ?";
        try(
                Connection conn = getConnection();PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs= ps.executeQuery()){
                return rs.next();
            }
        }
    }

    public boolean registrarFichaje(int id, String pin, String tipo) throws SQLException {
        String sql = "INSERT INTO fichajes (id_empleado, pin, tipo) VALUES (?, ?, ?)";

        try(Connection conn = getConnection();PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, id);
            ps.setString(2, pin);
            ps.setString(3, tipo);
            return ps.executeUpdate()>0;
        }
    }
    public List<String> listarFichajes(int id) throws SQLException {
        List<String> fichajes = new ArrayList<String>();
        String SQL = "SELECT tipo, fecha_hora FROM fichajes WHERE id_empleado = ? ORDER BY fecha_hora DESC LIMIT 5";

        try(Connection conn = getConnection();PreparedStatement ps = conn.prepareStatement(SQL)){
            ps.setInt(1, id);
            try(ResultSet rs= ps.executeQuery()){
                while(rs.next()){
                    fichajes.add(String.format("%s - %s",
                            rs.getString("tipo").toUpperCase(),
                            rs.getTimestamp("fecha_hora").toLocalDateTime()));
                }
            }
        }
        return fichajes;
    }
}
