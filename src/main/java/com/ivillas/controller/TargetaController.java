package com.ivillas.controller;

import java.util.List;

import com.ivillas.model.ItemLlistaDTO;
import com.ivillas.model.LlistaDTO;
import com.ivillas.model.ProductePreusDTO;
import com.ivillas.request.CrearLlistaRequest;
import com.ivillas.request.ItemLlistaRequest;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.service.ProducteServiceClient;
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
            CrearLlistaRequest borrador = SessionManager.getListaTemporal();
            
            // Obtenemos todos los productos (con sus precios) para cruzar datos
            try {
                List<ProductePreusDTO> maestra = ProducteServiceClient.getProductos();

                for (ItemLlistaDTO item : listaActual.getItems()) {
                    ItemLlistaRequest nuevo = new ItemLlistaRequest();
                    nuevo.setProductoId(item.getProductoId());
                    nuevo.setProductoNombre(item.getNombreProducto());
                    nuevo.setCantidad(item.getCantidad() != null ? item.getCantidad() : java.math.BigDecimal.ONE);
                    nuevo.setUnidad(item.getUnidad());

                    // BUSCAMOS LOS PRECIOS EN LA MAESTRA USANDO EL ID
                    maestra.stream()
                        .filter(p -> p.getProducteId().equals(item.getProductoId()))
                        .findFirst()
                        .ifPresent(p -> nuevo.setPrecios(p.getPrecios()));

                    borrador.getItems().add(nuevo);
                }

                btnAfegir.setText("Afegit!");
                btnAfegir.setDisable(true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
 
}
    

    
