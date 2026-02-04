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
import com.ivillas.service.AuthServiceClient;
import com.ivillas.utils.SessionManager;

// Si vas a usar alertas para errores o éxito:
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AuthController {
    
    private MainController mainController; 
    @FXML private TextField txtUserLogin, txtUserReg, txtEmailReg, txtEmailForgot;
    @FXML private PasswordField txtPassLogin, txtPassReg;
    @FXML private VBox paneLogin, paneRegister, paneForgot;
    @FXML private javafx.scene.control.Button btnEntrar;

    
    @FXML
    public void initialize() {
        // Reemplaza 'btnEntrar' por el ID de tu botón de Login
    	btnEntrar.setDefaultButton(true); 
    }
    
    @FXML
    private void handleLogin() {
        if (camposVacios(txtUserLogin, txtPassLogin)) return;

        AuthServiceClient.login(txtUserLogin.getText(), txtPassLogin.getText(), 
            usuario -> Platform.runLater(() -> {
                SessionManager.setUsuario(usuario);
                if (mainController != null) mainController.actualizarInterfazTrasLogin();
                txtUserLogin.getScene().getWindow().hide(); 
            }),
            error -> Platform.runLater(() -> mostrarAlerta("Error", error))
        );
    }
 
    @FXML
    private void handleRegister() {
        if (camposVacios(txtUserReg, txtEmailReg, txtPassReg)) return;

        AuthServiceClient.register(txtUserReg.getText(), txtEmailReg.getText(), txtPassReg.getText(),
            () -> Platform.runLater(() -> {
                mostrarAlertaExito("Èxit", "Usuari registrat correctament.");
                showLogin();
            }),
            error -> Platform.runLater(() -> mostrarAlerta("Error", error))
        );
    }

    
    @FXML 
    private void processRecovery() {
        String email = txtEmailForgot.getText();
        
        if (email == null || email.isBlank()) {
            mostrarAlerta("Error", "Per favor, introdueix un email vàlid.");
            return;
        }

        // Llamamos al Service que limpiamos antes
        AuthServiceClient.processRecovery(email, 
            () -> Platform.runLater(() -> {
                mostrarAlertaExito("Enviat", "S'ha enviat un correu amb les instruccions.");
                showLogin(); // Volver al panel de login
            }),
            error -> Platform.runLater(() -> mostrarAlerta("Error", error))
        );
    }
    
    // --- Navegación Simplificada ---
    @FXML private void showRegister() { cambiarPanel(paneRegister); }
    @FXML private void showLogin()    { cambiarPanel(paneLogin); }
    @FXML private void handleForgotPass() { cambiarPanel(paneForgot); }

    private void cambiarPanel(VBox panelVisible) {
        paneLogin.setVisible(panelVisible == paneLogin);
        paneLogin.setManaged(panelVisible == paneLogin);
        paneRegister.setVisible(panelVisible == paneRegister);
        paneRegister.setManaged(panelVisible == paneRegister);
        paneForgot.setVisible(panelVisible == paneForgot);
        paneForgot.setManaged(panelVisible == paneForgot);
    }

    // --- Utilidades ---
    private boolean camposVacios(TextField... campos) {
        for (TextField f : campos) {
            if (f.getText().isEmpty()) {
                mostrarAlerta("Error", "Per favor, omple tots els camps.");
                return true;
            }
        }
        return false;
    }

    private void mostrarAlerta(String titulo, String msg) { crearAlerta(titulo, msg, AlertType.ERROR); }
    private void mostrarAlertaExito(String titulo, String msg) { crearAlerta(titulo, msg, AlertType.INFORMATION); }

    private void crearAlerta(String titulo, String msg, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void setMainController(MainController mainController) { this.mainController = mainController; }
}