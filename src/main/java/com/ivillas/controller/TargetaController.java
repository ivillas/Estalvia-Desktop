package com.ivillas.controller;

import com.ivillas.model.LlistaDTO;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.utils.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class TargetaController {
    @FXML private Label lblTitulo;
    @FXML private Label lblAutor;
    @FXML private Label lblDescripcion;
    @FXML private Label lblProductos;
    @FXML private Button btnAfegir;

    private LlistaDTO listaActual;

    public void setData(LlistaDTO lista) {
        this.listaActual = lista;
        
        lblTitulo.setText(lista.getNombre());
        lblAutor.setText("Autor: " + (lista.getNomAutor() != null ? lista.getNomAutor() : "Anònim"));
        lblDescripcion.setText(lista.getDescripcion());
        
        int totalItems = (lista.getItems() != null) ? lista.getItems().size() : 0;
        lblProductos.setText("Productes: " + totalItems);
        
        btnAfegir.setOnAction(e -> {
            try {
                Long miId = SessionManager.getUsuario().getUserId();
                LlistaServiceClient.copiarAmiLista(listaActual, miId);
                btnAfegir.setText("¡Guardada!");
                btnAfegir.setDisable(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
 
}
    

    
