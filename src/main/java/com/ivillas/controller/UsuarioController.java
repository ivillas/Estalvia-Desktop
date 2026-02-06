package com.ivillas.controller;

import com.ivillas.model.UsuariDTO;
import com.ivillas.service.UsuariServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXButton;

import java.util.Optional; // IMPORTANTE: faltaba este
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType; // IMPORTANTE: este es el correcto
import javafx.scene.control.Label;

public class UsuarioController {
	
	  @FXML private Label lblUsuari;
	  @FXML private Label lblEmail;
	  @FXML private Label lblData;	  
	  @FXML private Label lblLlistesPubliques;
	  @FXML private Label lblLlistesPrivades;	 
	  @FXML private Label lblProductes;	 
	  @FXML private JFXButton btnUserSession;	  
	  
	  private MainController mainController;
	
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void cargarDatos(UsuariDTO user) {
    	
        if (user != null) {
            lblUsuari.setText(user.getUsername()); // Usar la instancia 'user'
            lblEmail.setText(user.getEmail());
            lblData.setText(user.getDataCreacio());  
            lblLlistesPubliques.setText(String.valueOf(user.getnLlistesPublices()));
            lblLlistesPrivades.setText(String.valueOf(user.getnLlistesPrivades()));
            if (SessionManager.isLoggedIn()) {
                lblProductes.setText(String.valueOf(SessionManager.getIdsFavoritos().size()));
            }
            
        }
    }

    @FXML
    private void openInici() {
        if (mainController != null) {
            mainController.openInici(); // Llama al método del padre
        }
    }
    
    
    @FXML
    private void baixaUser() {
        // 1. Crear la alerta usando la clase estándar de JavaFX
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Donar-se de baixa");
        alert.setHeaderText("Estàs a punt d'eliminar el teu compte.");
        alert.setContentText("Tria com vols gestionar les teves llistes:");

        // CORRECCIÓN: Asegúrate de importar javafx.scene.control.ButtonType
        ButtonType btnSoloPrivadas = new ButtonType("Només privades");
        ButtonType btnTodo = new ButtonType("Tot (Llistes i compte)");
        // Para el botón de cancelar, usamos el tipo predefinido
        ButtonType btnCancelar = new ButtonType("Cancel·lar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSoloPrivadas, btnTodo, btnCancelar);

        // 2. Procesar la respuesta
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnSoloPrivadas) {
                ejecutarBorrado("solo_privadas");
            } else if (result.get() == btnTodo) {
                ejecutarBorrado("todo");
            }
        }
    }

    private void ejecutarBorrado(String modo) {
        try {
            // Obtenemos el ID del usuario actual de la sesión
            Long userId = SessionManager.getUsuario().getUserId();
            
            // Llamamos al cliente que conecta con el Backend
            boolean ok = UsuariServiceClient.eliminarCuenta(userId, modo);

            if (ok) {
                mostrarAlerta("Compte eliminat", "El teu compte s'ha eliminat correctament. Fins la propera!");
                
                // Limpiamos sesión y volvemos al inicio (Login/Inici)
                SessionManager.setUsuario(null);
                if (mainController != null) {
                    mainController.handleLogout(); 
                    mainController.openInici();
                }
            } else {
                mostrarAlerta("Error", "No s'ha pogut processar la baixa. Intenta-ho més tard.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de connexió", "No s'ha pogut contactar amb el servidor.");
        }
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
