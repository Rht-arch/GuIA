package org.example.guia.DTOs;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Empleado {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty apellido = new SimpleStringProperty();
    private final StringProperty empresa = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty telefono = new SimpleStringProperty();
    private final StringProperty codigoPais = new SimpleStringProperty();
    private final StringProperty imagenPerfil = new SimpleStringProperty();

    // Constructores
    public Empleado(int idEmpleado, String nombre, String apellido, String email) {}

    public Empleado(String nombre, String apellido, String empresa, String email,
                    String password, String codigoPais, String telefono, String imagenPerfil) {
        setNombre(nombre);
        setApellido(apellido);
        setEmpresa(empresa);
        setEmail(email);
        setPassword(password);
        setCodigoPais(codigoPais);
        setTelefono(telefono);
        setImagenPerfil(imagenPerfil);
    }

    // Getters y Setters como propiedades
    public IntegerProperty idProperty() { return id; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty apellidoProperty() { return apellido; }
    public StringProperty empresaProperty() { return empresa; }
    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty telefonoProperty() { return telefono; }
    public StringProperty codigoPaisProperty() { return codigoPais; }
    public StringProperty imagenPerfilProperty() { return imagenPerfil; }

    // Getters y Setters normales
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }

    String getApellido() { return apellido.get(); }
    public void setApellido(String apellido) { this.apellido.set(apellido); }

    String getEmpresa() { return empresa.get(); }
    public void setEmpresa(String empresa) { this.empresa.set(empresa); }

    String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }

    protected String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }

    String getCodigoPais() { return codigoPais.get(); }
    private void setCodigoPais(String codigoPais) { this.codigoPais.set(codigoPais); }

    String getTelefono() { return telefono.get(); }
    private void setTelefono(String telefono) { this.telefono.set(telefono); }

    String getImagenPerfil() { return imagenPerfil.get(); }
    private void setImagenPerfil(String imagenPerfil) {}

}
