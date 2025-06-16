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
import javafx.event.ActionEvent;
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

import javafx.stage.Stage;
import org.example.guia.DTOs.EmailHeader; // Asegúrate que la ruta sea correcta
import org.example.guia.Ventanas;

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

    /** Botón para cargar la lista de correos electrónicos del usuario. */
    @FXML private Button loadEmailsButton;

    /** Botón para abrir el panel de composición de un nuevo correo. */
    @FXML private Button composeButton;

    /** Lista que muestra los encabezados de los correos cargados. */
    @FXML private ListView<EmailHeader> emailListView;

    /** Visor para mostrar el contenido HTML del correo seleccionado. */
    @FXML private WebView emailContentView;

    /** Indicador visual de progreso para mostrar carga en proceso. */
    @FXML private ProgressIndicator progressIndicator;

    /** Panel que contiene los controles para escribir un nuevo correo. */
    @FXML private GridPane composePane;

    /** Campo de texto para la dirección del destinatario del correo. */
    @FXML private TextField toField;

    /** Campo de texto para el asunto del correo a enviar. */
    @FXML private TextField subjectField;

    /** Área de texto para redactar el cuerpo del correo. */
    @FXML private TextArea bodyArea;

    /** Botón para enviar el correo electrónico redactado. */
    @FXML private Button sendButton;

    /** Botón para cancelar la composición y cerrar el panel de composición. */
    @FXML private Button cancelComposeButton;

    /** Indicador visual que muestra el progreso del envío del correo. */
    @FXML private ProgressIndicator sendProgressIndicator;

    /**
     * Botón para volver atras
     */
    @FXML
    private Button btnVolver;

    /** Nombre de la aplicación que se muestra en la consola de Google API. */
    private static final String APPLICATION_NAME = "Gmail";

    /** Fábrica para parsear y generar objetos JSON. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /** Ruta relativa para almacenar las credenciales OAuth2 obtenidas. */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /** Ruta dentro de resources donde se encuentra el archivo client_secret.json con las credenciales. */
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    /** Identificador del usuario autenticado (siempre "me" para Gmail API). */
    private static final String USER_ID = "me";

    /** Lista de permisos que la aplicación solicitará al usuario (lectura y envío). */
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_SEND);

    /** Servicio Gmail autenticado para hacer peticiones a la API. */
    private Gmail service = null;

    /** Lista observable para mantener actualizada la UI con los encabezados de correo. */
    private final ObservableList<EmailHeader> emailHeaders = FXCollections.observableArrayList();

    /** Identificador para diferenciar credenciales si la app maneja varios usuarios (pendiente implementación). */
    private String currentApplicationUserId = "defaultUser";

    /**
     * Inicializa el controlador tras la carga del FXML.
     * <p>
     * Configura la lista de emails, personaliza la visualización de cada ítem,
     * y define el comportamiento al seleccionar un correo.
     * Además, oculta el panel de composición inicialmente.
     * </p>
     */
    @FXML
    public void initialize() {
        // Vincula la lista observable al ListView para mostrar encabezados
        emailListView.setItems(emailHeaders);

        // Define el formato visual de cada celda en la lista de emails
        emailListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EmailHeader item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Listener para detectar selección de un email y cargar su contenido en el visor
        emailListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && !"ERROR".equals(newValue.id())) {
                        loadSelectedEmailContent(newValue.id());
                    } else {
                        if (emailContentView != null) {
                            emailContentView.getEngine().loadContent("");
                        }
                    }
                });

        // Oculta inicialmente el panel de composición de correos
        composePane.setVisible(false);
        composePane.setManaged(false);
    }

    // --- Carga de Emails ---

    /**
     * Manejador para la acción de cargar los correos electrónicos del usuario.
     * <p>
     * Deshabilita botones, muestra indicador de progreso, limpia la lista y
     * el visor de contenido, y lanza una tarea en segundo plano para obtener
     * los encabezados de los emails desde la API de Gmail.
     * </p>
     * <p>
     * Al finalizar, actualiza la UI con los resultados o muestra un error.
     * </p>
     */
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
            Platform.runLater(() -> // Asegurar ejecución en hilo UI
                    emailHeaders.add(new EmailHeader("ERROR", "System", "Error al cargar: " + error.getMessage(), "")));
            showAlert(AlertType.ERROR, "Error de Carga", "No se pudieron cargar los emails:\n" + error.getMessage());
        });

        new Thread(loadEmailsTask).start();
    }

    /**
     * Crea una tarea (Task) para cargar en background los encabezados de emails.
     * <p>
     * Obtiene el servicio Gmail autenticado, solicita hasta 25 mensajes, y extrae
     * los campos relevantes (ID, remitente, asunto, snippet) para crear una lista
     * de EmailHeader que será usada para actualizar la UI.
     * </p>
     *
     * @return Tarea que devuelve la lista de encabezados de emails.
     */
    private Task<List<EmailHeader>> createLoadEmailsTask() {
        return new Task<>() {
            @Override
            protected List<EmailHeader> call() throws Exception {
                // Obtener servicio (autenticación si necesario)
                service = getGmailService();
                if (service == null) {
                    throw new IOException("No se pudo inicializar el servicio de Gmail.");
                }

                System.out.println("Obteniendo mensajes...");
                ListMessagesResponse response = service.users().messages()
                        .list(USER_ID)
                        .setMaxResults(25L) // Limitar a 25 mensajes
                        .execute();

                List<Message> messages = response.getMessages();
                List<EmailHeader> headers = new ArrayList<>();

                if (messages != null && !messages.isEmpty()) {
                    System.out.println("Mensajes encontrados: " + messages.size());
                    for (Message message : messages) {
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

                }
                return headers;
            }
        };
    }

// --- Visualización de Contenido ---

    /**
     * Carga y muestra el contenido completo del correo seleccionado.
     * <p>
     * Muestra un mensaje de carga mientras obtiene el contenido en segundo plano,
     * luego renderiza el contenido HTML o texto en el WebView.
     * </p>
     *
     * @param messageId ID del mensaje de Gmail cuyo contenido se desea cargar.
     */
    private void loadSelectedEmailContent(String messageId) {
        if (service == null || emailContentView == null) {
            System.err.println("Servicio Gmail o WebView no disponible para cargar contenido.");
            if (emailContentView != null) emailContentView.getEngine().loadContent("<html><body>Error interno: Servicio no listo.</body></html>");
            return;
        }

        progressIndicator.setVisible(true);
        WebEngine engine = emailContentView.getEngine();
        engine.loadContent("<html><body><i>Cargando contenido...</i></body></html>");

        Task<String> loadContentTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                System.out.println("Obteniendo contenido completo para mensaje ID: " + messageId);
                Message message = service.users().messages()
                        .get(USER_ID, messageId)
                        .setFormat("full")
                        .execute();

                return findBodyContent(message.getPayload());
            }
        };

        loadContentTask.setOnSucceeded(event -> {
            String bodyContent = loadContentTask.getValue();
            Platform.runLater(() -> {
                if (bodyContent != null) {
                    engine.loadContent(bodyContent, "text/html");
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

    /**
     * Obtiene el contenido del cuerpo del mensaje en formato HTML o texto plano.
     * <p>
     * Prioriza la búsqueda de la parte "text/html". Si no existe, intenta con "text/plain".
     * En caso de texto plano, convierte el contenido a HTML básico para su visualización.
     * Si no encuentra ningún contenido compatible, devuelve un mensaje indicándolo.
     * </p>
     *
     * @param payload El objeto MessagePart que contiene el cuerpo del mensaje.
     * @return Contenido HTML listo para mostrar en un WebView.
     */
    private String findBodyContent(MessagePart payload) {
        if (payload == null) return null;

        String htmlBody = findMimePart(payload, "text/html");
        if (htmlBody != null) return htmlBody;

        String textBody = findMimePart(payload, "text/plain");
        if (textBody != null) {
            return "<html><body><pre>" + escapeHtml(textBody) + "</pre></body></html>";
        }

        System.out.println("No se encontró parte text/html o text/plain principal.");
        return "<html><body>(Contenido no soportado o vacío)</body></html>";
    }

    /**
     * Busca recursivamente en las partes del mensaje una parte MIME específica.
     * <p>
     * Decodifica el contenido Base64Url de la parte encontrada y lo devuelve como String UTF-8.
     * </p>
     *
     * @param part Parte del mensaje donde buscar.
     * @param mimeType Tipo MIME que se desea encontrar (ej. "text/html", "text/plain").
     * @return Contenido decodificado o null si no se encontró.
     */
    private String findMimePart(MessagePart part, String mimeType) {
        if (part == null) return null;

        if (mimeType.equals(part.getMimeType()) && part.getBody() != null && part.getBody().getData() != null) {
            try {
                byte[] decodedBytes = Base64.getUrlDecoder().decode(part.getBody().getData());
                return new String(decodedBytes, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                System.err.println("Error decodificando Base64Url: " + e.getMessage());
                return "<html><body>Error al decodificar contenido.</body></html>";
            }
        }

        if (part.getParts() != null && !part.getParts().isEmpty()) {
            for (MessagePart subPart : part.getParts()) {
                String found = findMimePart(subPart, mimeType);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * Escapa caracteres especiales para mostrar texto plano en HTML.
     * <p>
     * Convierte caracteres especiales en sus entidades HTML y convierte saltos de línea en &lt;br&gt;.
     * </p>
     *
     * @param text Texto plano que se quiere escapar.
     * @return Texto escapado para HTML.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }

    /**
     * Busca el valor de un encabezado específico dentro de la lista de encabezados.
     *
     * @param headers Lista de encabezados del mensaje.
     * @param name Nombre del encabezado a buscar (ej. "Subject", "From").
     * @return Valor del encabezado si se encuentra, o "N/A" si no.
     */
    private String findHeaderValue(List<MessagePartHeader> headers, String name) {
        if (headers == null) return "N/A";
        for (MessagePartHeader header : headers) {
            if (name.equalsIgnoreCase(header.getName())) {
                return header.getValue();
            }
        }
        return "N/A";
    }

// --- Lógica de Composición y Envío ---

    /**
     * Maneja la acción de mostrar el formulario para componer un nuevo correo.
     * <p>
     * Verifica que el servicio Gmail esté listo antes de permitir escribir un email.
     * </p>
     */
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

    /**
     * Cancela la composición del correo y oculta el formulario.
     */
    @FXML
    private void handleCancelCompose() {
        composePane.setVisible(false);
        composePane.setManaged(false);
        clearComposeFields();
    }

    /**
     * Limpia los campos y restablece controles del formulario de composición.
     */
    private void clearComposeFields() {
        toField.clear();
        subjectField.clear();
        bodyArea.clear();
        sendButton.setDisable(false);
        cancelComposeButton.setDisable(false);
        sendProgressIndicator.setVisible(false);
    }

    /**
     * Maneja el envío del correo electrónico.
     * <p>
     * Valida la dirección del destinatario, deshabilita botones,
     * muestra indicador de progreso y lanza tarea en background para enviar el correo.
     * </p>
     */
    @FXML
    private void handleSendEmail() {
        String to = toField.getText();
        String subject = subjectField.getText();
        String body = bodyArea.getText();

        if (to == null || to.trim().isEmpty() || !to.contains("@")) {
            showAlert(AlertType.ERROR, "Error de Validación", "El campo 'Para' es inválido.");
            return;
        }

        sendButton.setDisable(true);
        cancelComposeButton.setDisable(true);
        sendProgressIndicator.setVisible(true);

        Task<Boolean> sendTask = createSendEmailTask(to, subject, body);

        sendTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                showAlert(AlertType.INFORMATION, "Éxito", "¡Correo enviado correctamente!");
                handleCancelCompose();
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

    /**
     * Crea una tarea para enviar un correo electrónico utilizando la API de Gmail.
     *
     * @param to Destinatario del correo.
     * @param subject Asunto del correo.
     * @param body Cuerpo del mensaje.
     * @return Tarea que devuelve true si el envío fue exitoso.
     */
    private Task<Boolean> createSendEmailTask(String to, String subject, String body) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (service == null) {
                    throw new IllegalStateException("El servicio de Gmail no está disponible.");
                }
                String userEmail = findMyEmailAddress();

                MimeMessage mimeMessage = createMimeMessage(to, userEmail, subject, body);
                String encodedEmail = encodeMimeMessageToBase64url(mimeMessage);
                if (encodedEmail == null) throw new IOException("No se pudo codificar el mensaje.");

                Message googleApiMessage = new Message().setRaw(encodedEmail);

                System.out.println("Enviando mensaje a: " + to + " desde " + userEmail);
                service.users().messages().send(USER_ID, googleApiMessage).execute();
                System.out.println("Mensaje enviado (o en proceso).");
                return true;
            }
        };
    }

// --- Métodos de Ayuda para MIME y Codificación ---

    /**
     * Crea un objeto MimeMessage con los datos proporcionados.
     *
     * @param to Destinatario.
     * @param from Remitente.
     * @param subject Asunto del correo.
     * @param bodyText Cuerpo del mensaje.
     * @return MimeMessage construido.
     * @throws MessagingException En caso de error en la creación del mensaje.
     */
    private MimeMessage createMimeMessage(String to, String from, String subject, String bodyText)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject, "UTF-8");
        email.setText(bodyText, "UTF-8");
        return email;
    }

    /**
     * Codifica un MimeMessage en una cadena Base64url para enviar a través de la API.
     *
     * @param mimeMessage Mensaje MIME a codificar.
     * @return String con el mensaje codificado en Base64url.
     * @throws IOException En caso de error al escribir el mensaje.
     * @throws MessagingException En caso de error en el mensaje MIME.
     */
    private String encodeMimeMessageToBase64url(MimeMessage mimeMessage) throws IOException, MessagingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        mimeMessage.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

// --- Autenticación y Obtención del Servicio ---

    /**
     * Obtiene una instancia autenticada del servicio Gmail para la cuenta del usuario.
     * <p>
     * Si ya existe una instancia creada, la reutiliza.
     * Implementa el flujo OAuth 2.0 para obtener las credenciales y construir el servicio.
     * </p>
     *
     * @return Servicio Gmail autenticado.
     * @throws IOException Si ocurre un error de I/O o falta el archivo de credenciales.
     * @throws GeneralSecurityException Si ocurre un error relacionado con la seguridad.
     */
    private Gmail getGmailService() throws IOException, GeneralSecurityException {
        if (this.service != null) {
            return this.service;
        }

        System.out.println("Creando/Obteniendo servicio Gmail...");
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = GmailController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        System.out.println("Iniciando autorización OAuth 2.0 para ID de credencial: " + currentApplicationUserId);
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(currentApplicationUserId);
        System.out.println("Autorización completada/cargada para: " + currentApplicationUserId);

        this.service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        System.out.println("Servicio Gmail creado.");
        return this.service;
    }





    /**
     * Obtiene la dirección de correo electrónico del usuario autenticado.
     * <p>
     * Lo ideal es obtenerlo a través del servicio Gmail luego de la autenticación,
     * pero mientras tanto devuelve "me" que funciona para la mayoría de llamadas.
     * </p>
     *
     * @return Dirección de correo electrónico del usuario autenticado o "me" como valor seguro.
     */
    private String findMyEmailAddress() {
        System.err.println("ADVERTENCIA: findMyEmailAddress() devuelve 'me'.");
        return "me";
    }

    /**
     * Muestra una alerta de JavaFX con título y contenido proporcionados.
     * <p>
     * Se asegura que la alerta se muestre en el hilo de interfaz de usuario.
     * </p>
     *
     * @param alertType Tipo de alerta (información, error, etc.).
     * @param title Título de la ventana de alerta.
     * @param content Mensaje que se mostrará al usuario.
     */
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(alert::showAndWait);
        } else {
            alert.showAndWait();
        }
    }
    public void volveratras(ActionEvent actionEvent) {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        Ventanas.cambiarVentana(stage, "Vistas/Pantalla_de_inicio.fxml", "Inicio");
    }
}