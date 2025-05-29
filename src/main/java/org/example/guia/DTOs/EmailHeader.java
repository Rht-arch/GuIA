package org.example.guia.DTOs;

public record EmailHeader(String id, String from, String subject, String snippet) {
    @Override
    public String toString() {
        // Formato simple para la lista
        return String.format("De: %s - Asunto: %s",
                (from != null ? from : "N/A"), // Manejar posible null
                (subject != null ? subject : "(Sin Asunto)")); // Manejar posible null
    }
}
