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

/**
 * Controlador per a la representació visual d'una llista de la compra en format de targeta.
 * Gestiona la visualització de les dades de la llista i la lògica per afegir els seus 
 * productes a la sessió actual de l'usuari.
 */
public class TargetaController {
    @FXML private Label lblTitulo;       // Títol de la llista
    @FXML private Label lblAutor;        // Nom de l'autor de la llista
    @FXML private Label lblDescripcion;  // Descripció curta de la llista
    @FXML private Label lblProductos;    // Comptador de productes que conté
    @FXML private Button btnAfegir;      // Botó per copiar la llista a la sessió
    private MainController mainController;

    private LlistaDTO listaActual; // DTO que conté les dades de la llista carregada

    /**
     * Assigna les dades de la llista a la targeta i configura els esdeveniments.
     * @param lista Objecte DTO amb la informació de la llista.
     */
    public void setData(LlistaDTO lista) {
        this.listaActual = lista;
        
        // Configuració dels textos visuals
        lblTitulo.setText(lista.getNombre());
        lblAutor.setText("Autor: " + (lista.getNomAutor() != null ? lista.getNomAutor() : "Anònim"));
        lblDescripcion.setText(lista.getDescripcion());
        
        // Càlcul del total de productes
        int totalItems = (lista.getItems() != null) ? lista.getItems().size() : 0;
        lblProductos.setText("Productes: " + totalItems);
        
        // Esdeveniment del botó "Afegir"
        btnAfegir.setOnAction(e -> {
        	
            // Obtenim l'esborrany de la llista temporal de la sessió
            CrearLlistaRequest borrador = SessionManager.getLlistaTemporal();
            
            // Verificació de seguretat: l'usuari ha d'estar loguejat per afegir la llista
            if (SessionManager.getUsuario() == null) {
                MainController mc = SessionManager.getMainController();
                if (mc != null) {
                    try {
                        // Mostrem avís i obrim la finestra de login si no està identificat
                    	mostrarAlertaExito("Identificat","Has de iniciar sessió per afegir aquesta llista al teu compte.");
						mc.openLoginWindow();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
                return;
            }
            
            // Intentem copiar els productes de la llista seleccionada a la llista temporal de l'usuari
            try {
                // Obtenim tots els productes mestres (amb preus actualitzats) per creuar dades
                List<ProductePreusDTO> maestra = ProducteServiceClient.getProductes();

                for (ItemLlistaDTO item : listaActual.getItems()) {
                    ItemLlistaRequest nuevo = new ItemLlistaRequest();
                    nuevo.setProductoId(item.getProductoId());
                    nuevo.setProductoNombre(item.getNombreProducto());
                    nuevo.setCantidad(item.getCantidad() != null ? item.getCantidad() : java.math.BigDecimal.ONE);
                    nuevo.setUnidad(item.getUnidad());

                    // BUSQUEM ELS PREUS EN LA MESTRA USANT L'ID per assegurar dades actuals
                    maestra.stream()
                        .filter(p -> p.getProducteId().equals(item.getProductoId()))
                        .findFirst()
                        .ifPresent(p -> nuevo.setPrecios(p.getPrecios()));

                    // Afegim l'ítem configurat al borrador de la sessió
                    borrador.getItems().add(nuevo);
                }

                // Feedback visual de que l'operació s'ha realitzat correctament
                btnAfegir.setText("               Afegit!                ");
                btnAfegir.setDisable(true);

            } catch (Exception ex) {
                // Gestió d'errors en cas de fallada en la comunicació amb el servei
                ex.printStackTrace();
            }
            
            
            
        });
    }
    
    /**
     * Estableix la referència al controlador principal de l'aplicació.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
 
    /**
     * Mètode auxiliar per mostrar una alerta de tipus informatiu.
     */
    private void mostrarAlertaExito(String titulo, String msg) { crearAlerta(titulo, msg, AlertType.INFORMATION); }

    /**
     * Crea i mostra un diàleg d'alerta personalitzat.
     */
    private void crearAlerta(String titulo, String msg, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    
}




    
