package com.ivillas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class IniciController {

@FXML private TextField txtBuscador;
    
    @FXML
    public void initialize() {
        // Aquí puedes cargar la lista de productos inicial si lo necesitas
        System.out.println("Vista de Inicio cargada correctamente.");
    }
	
}
