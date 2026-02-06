package com.ivillas.controller;

import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class TargetaController {
    @FXML private Label lblTitulo;
    @FXML private Label lblAutor;
    @FXML private Label lblDescripcion;
    @FXML private Label lblProductos;
    @FXML private Button btnAfegir;
    private MainController mainController;

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
            if (SessionManager.getUsuario() == null) {
                MainController mc = SessionManager.getMainController();
                if (mc != null) {
                    try {
                    	mostrarAlertaExito("Identificat","Has de iniciar sessió per afegir aquesta llista al teu compte.");
						mc.openLoginWindow();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
                return;
            }
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

                btnAfegir.setText("               Afegit!                ");
                btnAfegir.setDisable(true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            
            
        });
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
 
    private void mostrarAlertaExito(String titulo, String msg) { crearAlerta(titulo, msg, AlertType.INFORMATION); }

    private void crearAlerta(String titulo, String msg, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    
}
    

    
