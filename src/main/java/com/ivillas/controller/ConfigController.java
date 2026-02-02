package com.ivillas.controller;

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
	        // 1. Ponemos los supers seleccionados por defecto
	    	cxbMercadona.setSelected(true);
	        cxbCarrefour.setSelected(true);
	        cxbPlus.setSelected(true);
	        cxbConsum.setSelected(true);
	        cxbAlcampo.setSelected(true);

	        // 2. Ocultamos el proxy al inicio
	        actualizarEstadoProxy(false);
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

	    private void actualizarEstadoProxy(boolean seleccionado) {
	        lblIpProxi.setVisible(seleccionado);
	        ((Node)lblIpProxi).setManaged(seleccionado);
	        
	        txtIpProxi.setVisible(seleccionado);
	        ((Node)txtIpProxi).setManaged(seleccionado);
	        
	        lblPortProxi.setVisible(seleccionado);
	        ((Node)lblPortProxi).setManaged(seleccionado);
	        
	        txtPortProxi.setVisible(seleccionado);
	        ((Node)txtPortProxi).setManaged(seleccionado);
	    }
	}


