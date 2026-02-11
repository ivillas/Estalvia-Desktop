package com.ivillas.controller;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import java.util.Map;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.service.ProducteServiceClient;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class ProductoController {
    @FXML private TextField txtBuscador;
    @FXML private FlowPane contenedorProductos;
    @FXML private FlowPane flowProductos; 
    
    private List<ProductePreusDTO> todosLosProductos = new ArrayList<>();

    // Método para cargar datos del NAS
    public void cargarDatos() {
        Task<List<ProductePreusDTO>> tarea = new Task<>() {
            @Override
            protected List<ProductePreusDTO> call() throws Exception {
                return ProducteServiceClient.getProductos();
            }
        };

        
        
        tarea.setOnSucceeded(e -> {
            todosLosProductos = tarea.getValue();
            actualizarVista(todosLosProductos);
        });
        
        new Thread(tarea).start();
    }

    // Método para filtrar y mostrar
    private void actualizarVista(List<ProductePreusDTO> listaFiltrada) {
        if (flowProductos == null) {
            System.out.println("Error: flowProductos no está vinculado al FXML");
            return;
        }
        
        flowProductos.getChildren().clear();
        for (ProductePreusDTO p : listaFiltrada) {
            // Usamos 'this.' para asegurar que busque el método en esta misma clase
            VBox card = this.crearCard(p); 
            flowProductos.getChildren().add(card);
        }
    }
    
    private VBox crearCard(ProductePreusDTO p) {
        VBox card = new VBox(8);
        // ... (todo el código del diseño que te pasé antes)
        return card;
    }

    @FXML
    public void alEscribirBuscador() {
        String filtro = txtBuscador.getText().toLowerCase();
        List<ProductePreusDTO> filtrados = todosLosProductos.stream()
            .filter(p -> p.nombre.toLowerCase().contains(filtro) || p.marca.toLowerCase().contains(filtro))
            .collect(Collectors.toList());
        actualizarVista(filtrados);
    }
}