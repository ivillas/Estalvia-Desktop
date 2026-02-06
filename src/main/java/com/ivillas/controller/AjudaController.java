package com.ivillas.controller;

import java.util.List;

import com.ivillas.model.LlistaDTO;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.service.SupermercatServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AjudaController {

	private MainController mainController;
	
	  @FXML private Label lblNusuaris;
	  @FXML private Label lblNproductes;
	  @FXML private Label lblNsupers;	  
	  @FXML private Label lblNLlistesPubli;
	  @FXML private Label lblNllistesPri;	 
	  @FXML private Label lblVersioApp;	 
	  @FXML private Label lbldataVersio;	 
	  @FXML private Label lbldataProductes;	 
	  @FXML private JFXButton btnTornar;
	  @FXML private JFXButton btnUpdate;
	
	
	@FXML
    public void initialize() throws Exception {

        lblNproductes.setText(String.valueOf(ProducteServiceClient.getProductos().size()));
        lblNsupers.setText(String.valueOf(SupermercatServiceClient.getAll().size()));
        lblNLlistesPubli.setText(String.valueOf(LlistaServiceClient.getPublicas().size()));
        System.out.println("Vista de Inicio cargada correctamente.");
    }
	
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    
    
    
    
}
