package org.example.guia.DAOs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:mariadb://localhost/guia";

    /** Usuario de acceso a la base de datos. */
    private static final String USER = "root";

    /** Contraseña del usuario. */
    private static final String PASSWORD = "";

    /**
     * Establece y devuelve una conexión a la base de datos.
     *
     * @return una conexión activa a la base de datos.
     * @throws SQLException si ocurre un error durante la conexión.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // En versiones modernas no es obligatorio, pero previene errores
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver de MySQL no encontrado.");
            e.printStackTrace();
            throw new SQLException("Driver no encontrado", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
