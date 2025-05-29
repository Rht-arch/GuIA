package org.example.guia.Controladores;

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
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.example.guia.DTOs.EmailHeader; // Asegúrate que la ruta sea correcta

import java.io.*; // Importar File, InputStream, InputStreamReader, etc.
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class GmailController {

    // --- Componentes UI Principales ---
    @FXML private Button loadEmailsButton;
    @FXML private Button composeButton;
    @FXML private ListView<EmailHeader> emailListView;
    @FXML private WebView emailContentView;
    @FXML private ProgressIndicator progressIndicator;

    // --- Componentes UI para Composición ---
    @FXML private GridPane composePane;
    @FXML private TextField toField;
    @FXML private TextField subjectField;
    @FXML private TextArea bodyArea;
    @FXML private Button sendButton;
    @FXML private Button cancelComposeButton;
    @FXML private ProgressIndicator sendProgressIndicator;

    // --- Configuración y Constantes API ---
    private static final String APPLICATION_NAME = "Guia JavaFX Gmail"; // O tu nombre
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens"; // Directorio relativo para guardar credenciales
    private static final String CREDENTIALS_FILE_PATH = "/client_secrets.json"; // Ruta DENTRO de resources
    private static final String USER_ID = "me"; // Siempre 'me' para el usuario autenticado
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_SEND);

    // --- Estado ---
    private Gmail service = null; // El servicio Gmail autenticado
    private final ObservableList<EmailHeader> emailHeaders = FXCollections.observableArrayList();
    private String currentApplicationUserId = "defaultUser"; // ID para guardar credenciales (¡IMPLEMENTAR OBTENCIÓN REAL!)

    @FXML
    public void initialize() {
        emailListView.setItems(emailHeaders);

        // Personalizar celdas para mostrar EmailHeader de forma legible
        emailListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EmailHeader item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null); // Limpiar gráfico también
                } else {
                    // Usar el toString() del record/clase EmailHeader
                    setText(item.toString());
                    // Podrías crear un layout más complejo aquí si quisieras
                }
            }
        });

        // Listener para cargar contenido al seleccionar email
        emailListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && !"ERROR".equals(newValue.id())) { // No cargar si es un mensaje de error
                        loadSelectedEmailContent(newValue.id());
                    } else {
                        if (emailContentView != null) {
                            emailContentView.getEngine().loadContent(""); // Limpiar visor
                        }
                    }
                });

        // Ocultar panel de composición inicialmente
        composePane.setVisible(false);
        composePane.setManaged(false);
    }

    // --- Carga de Emails ---

    @FXML
    private void handleLoadEmails() {
        progressIndicator.setVisible(true);
        loadEmailsButton.setDisable(true);
        composeButton.setDisable(true);
        emailHeaders.clear();
        if(emailContentView != null) emailContentView.getEngine().loadContent("");

        Task<List<EmailHeader>> loadEmailsTask = createLoadEmailsTask();

        loadEmailsTask.setOnSucceeded(event -> {
            emailHeaders.addAll(loadEmailsTask.getValue());
            progressIndicator.setVisible(false);
            loadEmailsButton.setDisable(false);
            if (service != null) { // Habilitar solo si el servicio se creó bien
                composeButton.setDisable(false);
            }
            System.out.println("Cabeceras de email cargadas.");
        });

        loadEmailsTask.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            loadEmailsButton.setDisable(false);
            composeButton.setDisable(true); // Mantener deshabilitado
            Throwable error = loadEmailsTask.getException();
            System.err.println("Error al cargar emails:");
            error.printStackTrace();
            // Mostrar error en la lista
            Platform.runLater(() -> // Asegurarse que se ejecuta en el hilo de UI
                    emailHeaders.add(new EmailHeader("ERROR", "System", "Error al cargar: " + error.getMessage(), "")));
            showAlert(AlertType.ERROR, "Error de Carga", "No se pudieron cargar los emails:\n" + error.getMessage());
        });

        new Thread(loadEmailsTask).start();
    }

    private Task<List<EmailHeader>> createLoadEmailsTask() {
        return new Task<>() {
            @Override
            protected List<EmailHeader> call() throws Exception {
                // Obtener servicio (se encarga de autenticar si es necesario)
                service = getGmailService();
                if (service == null) {
                    throw new IOException("No se pudo inicializar el servicio de Gmail.");
                }

                System.out.println("Obteniendo mensajes...");
                ListMessagesResponse response = service.users().messages()
                        .list(USER_ID)
                        .setMaxResults(25L) // Cargar hasta 25 mensajes
                        .execute();

                List<Message> messages = response.getMessages();
                List<EmailHeader> headers = new ArrayList<>();

                if (messages != null && !messages.isEmpty()) {
                    System.out.println("Mensajes encontrados: " + messages.size());
                    // Idealmente usar Batch Request aquí para eficiencia
                    for (Message message : messages) {
                        // Pedir solo los campos necesarios para la lista
                        Message msg = service.users().messages()
                                .get(USER_ID, message.getId())
                                .setFormat("metadata")
                                .setFields("id,snippet,payload/headers")
                                .execute();

                        String subject = findHeaderValue(msg.getPayload().getHeaders(), "Subject");
                        String from = findHeaderValue(msg.getPayload().getHeaders(), "From");
                        String snippet = msg.getSnippet();
                        headers.add(new EmailHeader(msg.getId(), from, subject, snippet));
                    }
                } else {
                    System.out.println("No se encontraron mensajes.");
                    // Opcional: Añadir un item indicando que no hay mensajes
                    // headers.add(new EmailHeader("EMPTY", "System", "No hay mensajes", ""));
                }
                return headers;
            }
        };
    }

    // --- Visualización de Contenido ---

    private void loadSelectedEmailContent(String messageId) {
        if (service == null || emailContentView == null) {
            System.err.println("Servicio Gmail o WebView no disponible para cargar contenido.");
            if (emailContentView != null) emailContentView.getEngine().loadContent("<html><body>Error interno: Servicio no listo.</body></html>");
            return;
        }

        progressIndicator.setVisible(true); // Usar el indicador general
        WebEngine engine = emailContentView.getEngine();
        engine.loadContent("<html><body><i>Cargando contenido...</i></body></html>");

        Task<String> loadContentTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                System.out.println("Obteniendo contenido completo para mensaje ID: " + messageId);
                Message message = service.users().messages()
                        .get(USER_ID, messageId)
                        .setFormat("full") // Pedir mensaje completo
                        .execute();

                return findBodyContent(message.getPayload()); // Parsear y decodificar
            }
        };

        loadContentTask.setOnSucceeded(event -> {
            String bodyContent = loadContentTask.getValue();
            Platform.runLater(() -> {
                if (bodyContent != null) {
                    engine.loadContent(bodyContent, "text/html"); // Cargar HTML o texto preformateado
                } else {
                    engine.loadContent("<html><body>(No se encontró contenido legible)</body></html>");
                }
                progressIndicator.setVisible(false);
            });
        });

        loadContentTask.setOnFailed(event -> {
            Throwable error = loadContentTask.getException();
            System.err.println("Error al cargar contenido del email:");
            error.printStackTrace();
            Platform.runLater(() -> {
                engine.loadContent("<html><body>Error al cargar contenido:<br>" + escapeHtml(error.getMessage()) + "</body></html>");
                progressIndicator.setVisible(false);
            });
        });

        new Thread(loadContentTask).start();
    }

    // --- Métodos de Ayuda para Parseo ---

    private String findBodyContent(MessagePart payload) {
        if (payload == null) return null;

        // Prioridad 1: Buscar text/html
        String htmlBody = findMimePart(payload, "text/html");
        if (htmlBody != null) return htmlBody;

        // Prioridad 2: Buscar text/plain
        String textBody = findMimePart(payload, "text/plain");
        if (textBody != null) {
            // Convertir texto plano a HTML simple para mostrarlo en WebView
            return "<html><body><pre>" + escapeHtml(textBody) + "</pre></body></html>";
        }

        // Si no se encuentra directamente, podría ser multipart sin alternativa clara
        // Podríamos devolver un mensaje indicando que no se pudo renderizar
        System.out.println("No se encontró parte text/html o text/plain principal.");
        return "<html><body>(Contenido no soportado o vacío)</body></html>";
    }

    private String findMimePart(MessagePart part, String mimeType) {
        if (part == null) return null;
        // System.out.println("Checking part MIME: " + part.getMimeType()); // Debug

        // Si el tipo MIME coincide y hay datos en el cuerpo principal de esta parte
        if (mimeType.equals(part.getMimeType()) && part.getBody() != null && part.getBody().getData() != null) {
            // System.out.println("Found matching part with data. Size: " + part.getBody().getSize()); // Debug
            try {
                byte[] decodedBytes = Base64.getUrlDecoder().decode(part.getBody().getData());
                return new String(decodedBytes, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                System.err.println("Error decodificando Base64Url: " + e.getMessage());
                return "<html><body>Error al decodificar contenido.</body></html>";
            }
        }

        // Si es multipart, buscar recursivamente en las subpartes
        if (part.getParts() != null && !part.getParts().isEmpty()) {
            // System.out.println("Checking sub-parts (" + part.getParts().size() + ") of MIME: " + part.getMimeType()); // Debug
            for (MessagePart subPart : part.getParts()) {
                String found = findMimePart(subPart, mimeType);
                if (found != null) {
                    return found; // Devolver el primero que se encuentre
                }
            }
        } else if ("multipart/alternative".equals(part.getMimeType())) {
            // A veces el body está vacío pero las partes sí tienen contenido, re-verificar
            // System.out.println("Multipart/alternative with empty body? Checking parts again.");
            // (El bucle anterior ya lo haría si parts != null)
        }


        return null; // No encontrado en esta rama
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        // Reemplazos básicos para mostrar texto plano como preformateado en HTML
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>"); // Convertir saltos de línea
    }

    private String findHeaderValue(List<MessagePartHeader> headers, String name) {
        if (headers == null) return "N/A";
        for (MessagePartHeader header : headers) {
            if (name.equalsIgnoreCase(header.getName())) {
                return header.getValue();
            }
        }
        return "N/A"; // No encontrado
    }

    // --- Lógica de Composición y Envío ---

    @FXML
    private void handleComposeEmail() {
        if (service == null) {
            showAlert(AlertType.ERROR, "Error", "Debe cargar los emails o iniciar sesión primero.");
            return;
        }
        clearComposeFields();
        composePane.setVisible(true);
        composePane.setManaged(true);
    }

    @FXML
    private void handleCancelCompose() {
        composePane.setVisible(false);
        composePane.setManaged(false);
        clearComposeFields();
    }

    private void clearComposeFields() {
        toField.clear();
        subjectField.clear();
        bodyArea.clear();
        // Resetear también botones e indicador de envío si estaban activos
        sendButton.setDisable(false);
        cancelComposeButton.setDisable(false);
        sendProgressIndicator.setVisible(false);
    }

    @FXML
    private void handleSendEmail() {
        String to = toField.getText();
        String subject = subjectField.getText();
        String body = bodyArea.getText();

        if (to == null || to.trim().isEmpty() || !to.contains("@")) {
            showAlert(AlertType.ERROR, "Error de Validación", "El campo 'Para' es inválido.");
            return;
        }
        // Opcional: Validar asunto/cuerpo si se desea

        sendButton.setDisable(true);
        cancelComposeButton.setDisable(true);
        sendProgressIndicator.setVisible(true);

        Task<Boolean> sendTask = createSendEmailTask(to, subject, body);

        sendTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                showAlert(AlertType.INFORMATION, "Éxito", "¡Correo enviado correctamente!");
                handleCancelCompose(); // Ocultar y limpiar
            });
        });

        sendTask.setOnFailed(event -> {
            Throwable error = sendTask.getException();
            System.err.println("Error al enviar el correo:");
            error.printStackTrace();
            Platform.runLater(() -> {
                showAlert(AlertType.ERROR, "Error de Envío", "No se pudo enviar el correo:\n" + error.getMessage());
                sendButton.setDisable(false);
                cancelComposeButton.setDisable(false);
                sendProgressIndicator.setVisible(false);
            });
        });

        new Thread(sendTask).start();
    }

    private Task<Boolean> createSendEmailTask(String to, String subject, String body) {
        return new Task<>() {
            // Dentro de createSendEmailTask().call()
            @Override
            protected Boolean call() throws Exception {
                if (service == null) { // <--- CORREGIDO (usa el campo 'service')
                    throw new IllegalStateException("El servicio de Gmail no está disponible.");
                }
                String userEmail = findMyEmailAddress(); // Obtener email del usuario actual

                MimeMessage mimeMessage = createMimeMessage(to, userEmail, subject, body);
                String encodedEmail = encodeMimeMessageToBase64url(mimeMessage);
                if (encodedEmail == null) throw new IOException("No se pudo codificar el mensaje.");

                Message googleApiMessage = new Message().setRaw(encodedEmail);

                System.out.println("Enviando mensaje a: " + to + " desde " + userEmail);
                // Asegurarse de usar USER_ID ("me") o el userEmail para el primer parámetro
                service.users().messages().send(USER_ID, googleApiMessage).execute(); // <--- CORREGIDO (usa el campo 'service')
                System.out.println("Mensaje enviado (o en proceso).");
                return true;
            }
        };
    }

    // --- Métodos de Ayuda para MIME y Codificación ---

    private MimeMessage createMimeMessage(String to, String from, String subject, String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        // Poner 'from' aquí ayuda a la estructura MIME, pero Gmail usará la cuenta autenticada.
        // Si 'from' es una dirección real y es un alias configurado, podría funcionar.
        // Usar "me" o la dirección recuperada es más seguro si no manejas alias.
        email.setFrom(new InternetAddress(from)); // 'from' debería ser la dirección del usuario o "me"
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject, "UTF-8");
        email.setText(bodyText, "UTF-8"); // Establece Content-Type: text/plain; charset=UTF-8
        return email;
    }

    private String encodeMimeMessageToBase64url(MimeMessage mimeMessage) throws IOException, MessagingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // --- Autenticación y Obtención del Servicio ---

    private Gmail getGmailService() throws IOException, GeneralSecurityException {
        if (this.service != null) {
            // System.out.println("Reutilizando servicio Gmail existente.");
            return this.service;
        }

        System.out.println("Creando/Obteniendo servicio Gmail...");
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = GmailController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // --- USAR ID DE USUARIO DE LA APP AQUÍ ---
        // ¡NECESITAS IMPLEMENTAR CÓMO OBTENER ESTO DE TU LOGIN!
        // this.currentApplicationUserId = getCurrentApplicationUserIdFromSomewhere();


        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        System.out.println("Iniciando autorización OAuth 2.0 para ID de credencial: " + currentApplicationUserId);
        // Usar el ID de usuario de la app para almacenar/cargar la credencial correcta
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(currentApplicationUserId);
        System.out.println("Autorización completada/cargada para: " + currentApplicationUserId);


        this.service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        System.out.println("Servicio Gmail creado.");
        return this.service;
    }


    // --- Métodos Auxiliares ---

    private String getCurrentApplicationUserIdFromSomewhere() {
        // --- ¡IMPLEMENTACIÓN REAL NECESARIA AQUÍ! ---
        // Obtener el ID/Username del usuario logueado en TU aplicación JavaFX
        System.err.println("ADVERTENCIA: Usando 'defaultUser' para credenciales OAuth. Implementar lógica real.");
        return "defaultUser"; // Placeholder - ¡Cambiar!
    }

    private String findMyEmailAddress() {
        // --- ¡IMPLEMENTACIÓN REAL NECESARIA AQUÍ! ---
        // Podrías obtenerlo del servicio después de autenticar:
         /* try {
              if(service != null) {
                   return service.users().getProfile(USER_ID).execute().getEmailAddress();
              }
         } catch (IOException e) {
              System.err.println("No se pudo obtener la dirección de email del perfil: " + e.getMessage());
         } */
        System.err.println("ADVERTENCIA: findMyEmailAddress() devuelve 'me'.");
        return "me"; // Usar "me" es lo más seguro y generalmente funciona bien
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // Asegurarse que se muestre en el hilo de UI si se llama desde Task
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(alert::showAndWait);
        } else {
            alert.showAndWait();
        }
    }

    public void stopAnimation() {
        // Si tuvieras un AnimationTimer para el fondo, lo detendrías aquí
    }
}