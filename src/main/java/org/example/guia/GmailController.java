package org.example.guia;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GmailController {
    @FXML private Button loadEmailsButton;
    @FXML private ListView<String> emailListView;
    @FXML private ProgressIndicator progressIndicator;

    // --- Configuración de la API y OAuth ---
    private static final String APPLICATION_NAME = "JavaFX Gmail API Demo"; // Pon tu nombre de app
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    // Directorio para guardar las credenciales del usuario después de la autorización.
    private static final String TOKENS_DIRECTORY_PATH = "tokens"; // Se creará en el directorio de ejecución

    // Alcance (Scope): Qué permisos necesita tu aplicación.
    // GmailScopes.GMAIL_READONLY para solo leer. Hay otros para modificar, enviar, etc.
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/client_secrets.json"; // ¡¡¡Ruta DENTRO de resources!!!

    private static final String USER_ID = "me"; // Representa al usuario autenticado

    private Gmail service = null; // Objeto servicio de Gmail (se inicializa al autenticar)
    private final ObservableList<String> emailSubjects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        emailListView.setItems(emailSubjects);
    }


    /**
     * Manejador para el botón. Inicia la tarea de autenticación y carga de emails.
     */
    @FXML
    private void handleLoadEmails() {
        progressIndicator.setVisible(true);
        loadEmailsButton.setDisable(true);
        emailSubjects.clear(); // Limpiar lista anterior

        // Crear y ejecutar la tarea en segundo plano
        Task<List<String>> loadEmailsTask = createLoadEmailsTask();

        loadEmailsTask.setOnSucceeded(event -> {
            emailSubjects.addAll(loadEmailsTask.getValue());
            progressIndicator.setVisible(false);
            loadEmailsButton.setDisable(false);
            System.out.println("Emails cargados con éxito.");
        });

        loadEmailsTask.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            loadEmailsButton.setDisable(false);
            Throwable error = loadEmailsTask.getException();
            System.err.println("Error al cargar emails:");
            error.printStackTrace();
            // Mostrar un mensaje de error en la UI si es necesario
            emailSubjects.add("Error al cargar emails: " + error.getMessage());
        });

        // Ejecutar la tarea en un nuevo hilo
        new Thread(loadEmailsTask).start();
    }

    /**
     * Crea la tarea que maneja la autenticación (si es necesaria) y la llamada a la API.
     * @return La Tarea configurada.
     */
    private Task<List<String>> createLoadEmailsTask() {
        return new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                // 1. Obtener el servicio Gmail (se encarga de la autenticación)
                service = getGmailService();

                // 2. Realizar la llamada a la API (Ejemplo: obtener últimos 10 mensajes)
                System.out.println("Obteniendo mensajes...");
                ListMessagesResponse response = service.users().messages()
                        .list(USER_ID)
                        .setMaxResults(10L) // Obtener máximo 10 mensajes
                        .execute();

                List<Message> messages = response.getMessages();
                List<String> subjects = new ArrayList<>();

                if (messages == null || messages.isEmpty()) {
                    System.out.println("No se encontraron mensajes.");
                    return Collections.singletonList("Bandeja de entrada vacía o sin mensajes recientes.");
                } else {
                    System.out.println("Mensajes encontrados: " + messages.size());
                    // 3. Obtener detalles (Asunto) de cada mensaje (simplificado)
                    // ¡Ojo! Esto hace una llamada API por cada mensaje, puede ser lento.
                    // En una app real, usarías Batch Requests o pedirías más info en el list.
                    for (Message message : messages) {
                        Message msg = service.users().messages()
                                .get(USER_ID, message.getId())
                                .setFormat("metadata") // Solo necesitamos metadatos/cabeceras
                                .setFields("payload/headers") // Pedir solo las cabeceras
                                .execute();

                        String subject = findHeaderValue(msg.getPayload().getHeaders(), "Subject");
                        String from = findHeaderValue(msg.getPayload().getHeaders(), "From");
                        subjects.add("De: " + from + " - Asunto: " + subject);
                    }
                    return subjects;
                }
            }
        };
    }

    /**
     * Busca un valor específico en una lista de cabeceras de mensaje.
     */
    private String findHeaderValue(List<MessagePartHeader> headers, String name) {
        if (headers == null) return "N/A";
        for (MessagePartHeader header : headers) {
            if (name.equalsIgnoreCase(header.getName())) {
                return header.getValue();
            }
        }
        return "N/A"; // No encontrado
    }


    /**
     * Crea un servicio Gmail autorizado. Inicia el flujo de autorización si es necesario.
     *
     * @return Un servicio Gmail autorizado.
     * @throws IOException Si no se encuentra el client_secrets.json.
     * @throws GeneralSecurityException Si hay problemas con el transporte HTTP.
     */
    private Gmail getGmailService() throws IOException, GeneralSecurityException {
        // Si ya tenemos el servicio, lo reutilizamos
        if (this.service != null) {
            System.out.println("Reutilizando servicio Gmail existente.");
            return this.service;
        }

        System.out.println("Creando nuevo servicio Gmail...");
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Cargar client secrets desde el archivo en resources
        InputStream in = GmailController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Construir el flujo de autorización y disparar la autorización del usuario.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline") // Necesario para obtener refresh_token
                .build();

        // Usar LocalServerReceiver para el flujo de aplicación de escritorio
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build(); // Puerto para la redirección

        // Autorizar - Esto abrirá el navegador si es la primera vez o si las credenciales expiraron
        System.out.println("Iniciando autorización OAuth 2.0...");
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        // 'user' es un identificador local para las credenciales guardadas en FileDataStoreFactory
        System.out.println("Autorización completada o credenciales cargadas.");

        // Crear y devolver el servicio Gmail
        this.service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        System.out.println("Servicio Gmail creado.");
        return this.service;
    }
}
