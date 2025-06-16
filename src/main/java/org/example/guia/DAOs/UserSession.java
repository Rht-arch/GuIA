package org.example.guia.DAOs;

import org.example.guia.DTOs.Empleado;

/**
 * Clase singleton que maneja la sesión del usuario en la aplicación.
 * Proporciona métodos para iniciar, acceder y cerrar la sesión.
 */
public class UserSession {
    private static UserSession instance;
    private Empleado empleado;

    // Constructor privado para prevenir instanciación directa
    private UserSession(Empleado empleado) {
        this.empleado = empleado;
    }

    /**
     * Inicia una nueva sesión de usuario.
     * @param empleado El empleado que inicia sesión
     * @throws IllegalStateException si ya hay una sesión activa
     */
    public static void startSession(Empleado empleado) {
        if (instance != null) {
            throw new IllegalStateException("Ya existe una sesión activa");
        }
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        instance = new UserSession(empleado);
    }

    /**
     * Obtiene la instancia activa de la sesión.
     * @return La instancia de UserSession
     * @throws IllegalStateException si no hay sesión activa
     */
    public static UserSession getInstance() {
        if (instance == null) {
            throw new IllegalStateException("No hay sesión activa");
        }
        return instance;
    }

    /**
     * Obtiene el empleado asociado a la sesión actual.
     * @return El objeto Empleado
     */
    public Empleado getEmpleado() {
        return this.empleado;
    }

    /**
     * Verifica si hay una sesión activa.
     * @return true si hay sesión activa, false en caso contrario
     */
    public static boolean isSessionActive() {
        return instance != null;
    }

    /**
     * Cierra la sesión actual y limpia los datos.
     */
    public static void cleanUserSession() {
        instance = null;
    }

    /**
     * Método de conveniencia para obtener el ID del empleado en sesión.
     * @return El ID del empleado
     * @throws IllegalStateException si no hay sesión activa
     */
    public static int getCurrentEmployeeId() {
        if (!isSessionActive()) {
            throw new IllegalStateException("No hay sesión activa");
        }
        return instance.getEmpleado().getId();
    }

    /**
     * Método de conveniencia para obtener el nombre completo del empleado en sesión.
     * @return Nombre completo (nombre + apellido)
     * @throws IllegalStateException si no hay sesión activa
     */
    public static String getCurrentEmployeeFullName() {
        if (!isSessionActive()) {
            throw new IllegalStateException("No hay sesión activa");
        }
        Empleado emp = instance.getEmpleado();
        return emp.getNombre() + " " + emp.getApellido();
    }
}