package com.ivillas.controller;

import com.ivillas.model.UsuariDTO;
import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;

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
        // Usamos el método que ya tienes creado en el controlador para mostrar errores
        mostrarAlerta("Pròximament", "Aquesta funcionalitat d'eliminar el compte encara no està implementada.");
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
