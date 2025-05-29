package org.example.guia.DTOs;

import java.util.Date;

public class Email {
    private String id;
    private String de;
    private String dest;
    private String asunto;
    private String mensaje;
    private Date fecha;

    public Email(String id, Date fecha, String de, String dest, String asunto, String mensaje) {
        this.id = id;
        this.fecha = fecha;
        this.de = de;
        this.dest = dest;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    public String getId() {
        return id;
    }

    public String getDe() {
        return de;
    }

    public String getDest() {
        return dest;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public void setDe(String de) {
        this.de = de;
    }

    public void setId(String id) {
        this.id = id;
    }
}
