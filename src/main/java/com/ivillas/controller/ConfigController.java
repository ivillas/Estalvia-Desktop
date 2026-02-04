package com.ivillas.controller;

import java.awt.event.ActionEvent;

import com.ivillas.network.HttpClientProvider;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ConfigController {
	  
	    @FXML private JFXCheckBox cxbProxi;
	    @FXML private TextField txtIpProxi;
	    @FXML private TextField txtPortProxi;
	    @FXML private Label lblIpProxi;
	    @FXML private Label lblPortProxi;
        @FXML private JFXButton btnCancelarConfig;

	    // Checkboxes de Supermercados
        
	    @FXML private JFXCheckBox cxbMercadona; // Mercadona
	    @FXML private JFXCheckBox cxbCarrefour; // Carrefour
	    @FXML private JFXCheckBox cxbPlus; // Plus Fresc
	    @FXML private JFXCheckBox cxbConsum; // Consum
	    @FXML private JFXCheckBox cxbAlcampo; // Alcampo
	    
	    private MainController mainController;


	    
	    @FXML
	    public void initialize() {

	        // 1. Recuperar valores actuales del Provider
	        boolean actiu = HttpClientProvider.isUseProxy();	        
	        cxbProxi.setSelected(actiu);
	        txtIpProxi.setText(HttpClientProvider.getProxyHost());
	        
	        // Si el puerto es 0, lo ponemos vacío, si no, lo convertimos a String
	        int puerto = HttpClientProvider.getProxyPort();
	        txtPortProxi.setText(puerto == 0 ? "" : String.valueOf(puerto));

	        // 2. Aplicar visibilidad según el estado recuperado
	        actualizarEstadoProxy(actiu);
	        
	        // ... resto de tu inicialización de supermercados ...
	        // 1. Ponemos los supers seleccionados por defecto
	    	cxbMercadona.setSelected(true);
	        cxbCarrefour.setSelected(true);
	        cxbPlus.setSelected(true);
	        cxbConsum.setSelected(true);
	        cxbAlcampo.setSelected(true);
	    }
	    

	    @FXML
	    private void handleProxyAction() {
	        actualizarEstadoProxy(cxbProxi.isSelected());
	    }
	    
	    public void setMainController(MainController mainController) {
	        this.mainController = mainController;
	    }

	    @FXML
	    private void openInici() {
	        if (mainController != null) {
	            mainController.openInici(); // Llama al método del padre
	        }
	    }
	    
	    @FXML
	    private void handleProxyAction(ActionEvent event) {
	        // Obtenemos el estado directamente del checkbox
	        actualizarEstadoProxy(cxbProxi.isSelected());
	    }
	    
	    private void actualizarEstadoProxy(boolean seleccionado) {
	    	
	        lblIpProxi.setVisible(seleccionado);
	        lblIpProxi.setManaged(seleccionado);
	        
	        txtIpProxi.setVisible(seleccionado);
	        txtIpProxi.setManaged(seleccionado);
	        
	        lblPortProxi.setVisible(seleccionado);
	        lblPortProxi.setManaged(seleccionado);
	        
	        txtPortProxi.setVisible(seleccionado);
	        txtPortProxi.setManaged(seleccionado);
	    }
	    
	    @FXML
	    private void handleGuardarConfig() {
	        try {
	            boolean usarProxy = cxbProxi.isSelected();
	            String host = txtIpProxi.getText();
	            int puerto = 0;

	            if (usarProxy) {
	                String portText = txtPortProxi.getText();
	                if (portText == null || portText.isBlank()) {
	                    // Aquí podrías mostrar una alerta al usuario
	                    System.out.println("Error: El puerto es obligatorio si usas proxy.");
	                    return; 
	                }
	                puerto = Integer.parseInt(portText.trim());
	            }

	            // Llamamos al provider para aplicar los cambios
	            HttpClientProvider.configureProxy(usarProxy, host, puerto);
	            openInici();
	        } catch (NumberFormatException e) {
	            System.err.println("Error: El puerto debe ser un número válido.");
	            // Opcional: Mostrar alerta visual al usuario
	        }
	    }
	    
	}


