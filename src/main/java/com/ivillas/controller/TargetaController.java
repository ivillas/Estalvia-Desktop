package com.ivillas.controller;

import com.ivillas.model.ItemLlistaDTO;
import com.ivillas.model.LlistaDTO;
import com.ivillas.request.CrearLlistaRequest;
import com.ivillas.request.ItemLlistaRequest;
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
            // 1. Obtener el borrador del manager
            CrearLlistaRequest borrador = SessionManager.getListaTemporal();
            
            // 2. Volcar los productos de la lista pública al borrador
            for (ItemLlistaDTO item : listaActual.getItems()) {
                ItemLlistaRequest nuevo = new ItemLlistaRequest();
                nuevo.setProductoId(item.getProductoId());
                nuevo.setProductoNombre(item.getNombreProducto());
                nuevo.setCantidad(item.getCantidad());
                nuevo.setUnidad(item.getUnidad());
                
                borrador.getItems().add(nuevo);
            }
            
            // 3. OPCIONAL: Si quieres que el nombre también se copie al borrador:
            if(borrador.getNombre() == null || borrador.getNombre().isEmpty()) {
                borrador.setNombre(listaActual.getNombre());
            }

            btnAfegir.setText("Afegit al borrador!");
        });
    }
 
}
    

    
