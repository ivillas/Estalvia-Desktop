package com.ivillas.controller;

import javafx.scene.control.TextField;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import jakarta.mail.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivillas.model.UsuariDTO;
import com.ivillas.utils.SessionManager;

// Si vas a usar alertas para errores o éxito:
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AuthController {
	
	private MainController mainController; 
    // Para Login
    @FXML private TextField txtUserLogin;
    @FXML private PasswordField txtPassLogin;

    // Para Registro (Cambiados para coincidir con el FXML)
    @FXML private TextField txtUserReg;
    @FXML private TextField txtEmailReg;
    @FXML private PasswordField txtPassReg;

    @FXML private VBox paneLogin, paneRegister;
    @FXML private VBox paneForgot; // Añade este ID
    @FXML private TextField txtEmailForgot;
    
        
    private final HttpClient httpClient = HttpClient.newHttpClient();
   // private final HttpClient httpClient = HttpClient.newBuilder()
     //       .proxy(ProxySelector.of(new InetSocketAddress("192.168.2.1", 3128))) 
       //     .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String BASE_URL = "localhost:8081/auth";
    
 // Datos del servidor (Ejemplo con Gmail)
    private final String remitente = "tu_correo@gmail.com";
    // OJO: No es tu contraseña normal, es una "Contraseña de Aplicación" de Google
    private final String appPassword = "abcd efgh ijkl mnop"; 

    // Acción para cambiar a Registro
    @FXML
    private void showRegister() {
        paneLogin.setVisible(false);
        paneLogin.setManaged(false);
        paneRegister.setVisible(true);
        paneRegister.setManaged(true);
    }

    // Acción para cambiar a Login
    @FXML
    private void showLogin() {
        // Ocultamos todos los paneles secundarios
        paneRegister.setVisible(false);
        paneRegister.setManaged(false);
        paneForgot.setVisible(false);
        paneForgot.setManaged(false);

        // Mostramos el panel de login
        paneLogin.setVisible(true);
        paneLogin.setManaged(true);
    }

    // Lógica de Registro (usando tus parámetros)
    @FXML
    private void handleRegister() {
        String user = txtUserReg.getText();
        String email = txtEmailReg.getText();
        String pass = txtPassReg.getText();

        if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            mostrarAlerta("Error", "Tots els camps son obligatoris.");
            return;
        }

        Map<String, String> datos = Map.of(
            "username", user,
            "email", email,
            "password", pass
        );

        enviarPeticion("/register", datos, "Usuari registrat correctament.");
    }
    
    private void enviarPeticion(String endpoint, Map<String, String> data, String mensajeExito) {
        try {
            String body = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        try {
                            int status = response.statusCode();
                            String responseBody = response.body();

                            System.out.println("RESPUESTA DEL SERVIDOR (" + status + "): " + responseBody);

                            if (status == 200) {
                                if (endpoint.contains("login")) {
                                    // Intentamos parsear solo si la respuesta es JSON
                                    if (responseBody != null && responseBody.startsWith("{")) {
                                        UsuariDTO user = objectMapper.readValue(responseBody, UsuariDTO.class);
                                        SessionManager.setUsuario(user);

                                        if (mainController != null) {
                                            mainController.actualizarInterfazTrasLogin();
                                        }
                                        // Cerramos la ventana de login
                                        txtUserLogin.getScene().getWindow().hide(); 
                                    } else {
                                        mostrarAlerta("Error", "Respuesta inesperada del servidor: " + responseBody);
                                    }
                                } else {
                                    // Registro u otros endpoints
                                    mostrarAlertaExito("Éxit", mensajeExito);
                                    showLogin();
                                }
                            } else {
                                // Si el backend devuelve error con mensaje
                                String msg = responseBody != null && !responseBody.isEmpty()
                                        ? responseBody
                                        : "Código " + status;
                                mostrarAlerta("Error", "El servidor respondió: " + msg);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            mostrarAlerta("Error", "Error al procesar los datos del servidor: " + e.getMessage());
                        }
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> mostrarAlerta("Error", "No hay conexión con el servidor"));
                    return null;
                });

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al enviar la petición: " + e.getMessage());
        }
    }

    private String hashPassword(String passwordPlana) {
        // El método hashpw genera el hash. 
        // gensalt() determina la complejidad (por defecto es 10)
        return BCrypt.hashpw(passwordPlana, BCrypt.gensalt());
    }

	// Opción Recuperar Contraseña
    @FXML
    private void handleForgotPass() {
        System.out.println("Canvian vista a recuperar...");
        // Ocultamos Login
        paneLogin.setVisible(false);
        paneLogin.setManaged(false);
        // Mostramos Recuperar
        paneForgot.setVisible(true);
        paneForgot.setManaged(true);
    }
    
    private void enviarEmail(String destinatario, String codigo) {
        String remitente = "tu_correo@gmail.com";
        String password = "tu_password_de_aplicacion"; // No es la normal de Gmail

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Importante: PasswordAuthentication también debe ser de jakarta.mail
                return new PasswordAuthentication(remitente, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Codi de Recuperació - Estalvia!");
            message.setText("El teu codi de seguretat es: " + codigo);

            Transport.send(message);
            System.out.println("Email eniat exitosament.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogin() {
        String user = txtUserLogin.getText();
        String pass = txtPassLogin.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            mostrarAlerta("Error", "Per favor, omple tots els camps.");
            return;
        }

        enviarPeticion("/login", Map.of("username", user, "password", pass), "¡Benvingut!");
    }
    
    @FXML 
    private void processRecovery() {
        String email = txtEmailForgot.getText();
        if(email.isEmpty()) {
            mostrarAlerta("Error", "Per favor, introdueix un email.");
        } else {
            System.out.println("Enviando recuperación a: " + email);
            // Aquí podrías añadir la lógica de BD más tarde
            mostrarAlertaExito("Enviat", "S'han enviat les instruccions al correu.");
            showLogin(); // Vuelve al login automáticamente
        }
    }
    
 // Añade este método para las alertas de éxito
    private void mostrarAlertaExito(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    // Este es el método de verificación que usa la librería BCrypt
    private boolean verificarPassword(String passwordPlana, String hashDeLaBD) {
        try {
            return BCrypt.checkpw(passwordPlana, hashDeLaBD);
        } catch (Exception e) {
            return false;
        }
    }    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}