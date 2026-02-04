package com.ivillas.controller;

import com.ivillas.model.UsuariDTO;
import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UsuarioController {
	
	  @FXML private Label lblUsuari;
	  @FXML private Label lblEmail;
	  @FXML private Label lblData;	  
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
        }
    }

}
