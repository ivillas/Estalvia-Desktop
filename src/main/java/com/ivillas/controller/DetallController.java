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
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.ivillas.utils.SessionManager;
import com.ivillas.service.LlistaServiceClient; 

/**
 * Clase controlador del detall d'una llista
 */

public class DetallController {
    @FXML private Label lblTitol, lblAutor, lblDescripcio;
    @FXML private Button btnCopiarDetall, btnEliminarLlista, btnEditarLlista;
    @FXML private TableView<ItemLlistaDTO> taulaProductes;
    @FXML private TableColumn<ItemLlistaDTO, String> colNom, colQuantitat, colUnitat;
    
    private LlistaDTO llistaActual;
    
    
    /**
     * Carrega les dades de la llista que se li pasa
     * @param llista
     */
    @FXML
    public void carregarDades(LlistaDTO llista) {
        this.llistaActual = llista;
        lblTitol.setText(llista.getNombre());
        lblAutor.setText("Autor: " + llista.getNomAutor());
        lblDescripcio.setText(llista.getDescripcion());

        // Configuració de la taula...
        colNom.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colQuantitat.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnitat.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        taulaProductes.setItems(FXCollections.observableArrayList(llista.getItems()));

        // Comparem el nom del autor (String) amb el nom d'usuari logejat
        if (SessionManager.isLoggedIn() && llista.getNomAutor() != null) {
            String nombreLogueado = SessionManager.getUsuario().getUsername();
            boolean esAutor = llista.getNomAutor().equals(nombreLogueado);
            btnEliminarLlista.setVisible(esAutor);
            btnEditarLlista.setVisible(esAutor);
        } else {
            btnEliminarLlista.setVisible(false);
            btnEditarLlista.setVisible(false);
        }
    }
    

    /**
     * Metode per eliminar la llista, nomes la pot eliminar l'usuari que la crear
     * ha de ser el que esta en la sessió
     */
    @FXML
    private void eliminarLlista() {
        if (this.llistaActual == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminació");
        alert.setHeaderText(null);
        alert.setContentText("Vols eliminar la llista '" + llistaActual.getNombre() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Long llistaId = llistaActual.getListaId(); 
                Long usuariId = SessionManager.getUsuario().getUserId();

                // Borrat en el servidor
                LlistaServiceClient.eliminarLlista(llistaId, usuariId);

                // tanquem la finestra del detall
                tancarFinestra();
                
                //fil de petit retard per asegurar que el refresc veigi el canvi
                new Thread(() -> {
                    try { 
                        Thread.sleep(300); // 300ms de retard
                    } catch (InterruptedException e) {}
                    
                    javafx.application.Platform.runLater(() -> {
                        if (SessionManager.getMainController() != null) {
                            // aixo refresca els controldors de favoritos/stats si es necesari
                            if (SessionManager.isLoggedIn()) {
                                SessionManager.carregarFavorits(); 
                            }
                            // Aixo refresca la vista (Privades, Públicas o Perfil)
                            SessionManager.getMainController().refrescarVistaActualSiEsPerfil();
                        }
                    });
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                crearAlerta("Error", "No s'ha pogut eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    
    /**
     * Metode que copia tots els productes de una llista a la de la sessió
     * els afegeix
     */
     
    @FXML
    private void copiarLlista() {
    
    CrearLlistaRequest borrador = SessionManager.getLlistaTemporal();
    if (SessionManager.getUsuario() == null) {
        MainController mc = SessionManager.getMainController();
        if (mc != null) {
            try {
            	mostrarAlertaExit("Identificat","Has de iniciar sessió per afegir aquesta llista al teu compte.");
				mc.openLoginWindow();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
        return;
    }
    //obtenim tots els productes (amb els preus) per creuar dades
    try {
        List<ProductePreusDTO> mestra = ProducteServiceClient.getProductes();

        for (ItemLlistaDTO item : llistaActual.getItems()) {
            ItemLlistaRequest nou = new ItemLlistaRequest();
            nou.setProductoId(item.getProductoId());
            nou.setProductoNombre(item.getNombreProducto());
            nou.setCantidad(item.getCantidad() != null ? item.getCantidad() : java.math.BigDecimal.ONE);
            nou.setUnidad(item.getUnidad());

            // busquem preus en la mestra usant el ID
            mestra.stream()
                .filter(p -> p.getProducteId().equals(item.getProductoId()))
                .findFirst()
                .ifPresent(p -> nou.setPrecios(p.getPrecios()));

            borrador.getItems().add(nou);
        }


        btnCopiarDetall.setText("Afegit!");
        btnCopiarDetall.setDisable(true);

        
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    
    }
    
    /**
     * Metode per mortar alertes
     */
    
    private void mostrarAlertaExit(String titol, String msg) { crearAlerta(titol, msg, AlertType.INFORMATION); }

    private void crearAlerta(String titol, String msg, AlertType tipus) {
        Alert alert = new Alert(tipus);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    /**
     * Metode que tancat la finestra(vista)
     */
        
    @FXML
    private void tancarFinestra() {
        ((Stage) lblTitol.getScene().getWindow()).close();
    }
    
    
    /**
     * Metode per editar una llista, carrega els productes a la llista temporal i obre la vista de creació
     */
        
    @FXML
    private void editarLlista() {
        if (this.llistaActual == null) return;

        // Obte la llista temporal actual de la sessió per comprovar si hi ha productes pendents
        CrearLlistaRequest listaTemporalActual = SessionManager.getLlistaTemporal();

        // Si tenim productes a la llista temporal, demanem confirmació abans de carregar la nova llista per editar
        if (listaTemporalActual != null && listaTemporalActual.getItems() != null && !listaTemporalActual.getItems().isEmpty()) {
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Editar llista");
            alert.setHeaderText(null);
            alert.setContentText("Si edites aquesta llista, es perdran els productes que tens actualment a la cistella de creació. Vols continuar?");

            // carreguem la nova llista només si l'usuari confirma que vol continuar
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                carregarLlistaPerEditar(this.llistaActual);
            }
        } else {
            // Si la cistella de creació està buida, carreguem directament la llista per editar sense demanar confirmació
            carregarLlistaPerEditar(this.llistaActual);
        }
    }
    
    private void carregarLlistaPerEditar(LlistaDTO llista) {
        try {
            CrearLlistaRequest nouBorrador = new CrearLlistaRequest();
            nouBorrador.setItems(new java.util.ArrayList<>());

            nouBorrador.setListaId(llista.getListaId()); 
            nouBorrador.setNombre(llista.getNombre());    
            nouBorrador.setDescripcion(llista.getDescripcion());
            nouBorrador.setVisibilidad(llista.getVisibilitat());
            
            List<ProductePreusDTO> mestra = ProducteServiceClient.getProductes();

            for (ItemLlistaDTO item : llista.getItems()) {
                ItemLlistaRequest nouItem = new ItemLlistaRequest();
                nouItem.setProductoId(item.getProductoId());
                nouItem.setProductoNombre(item.getNombreProducto());
                nouItem.setCantidad(item.getCantidad() != null ? item.getCantidad() : java.math.BigDecimal.ONE);
                nouItem.setUnidad(item.getUnidad());

                mestra.stream()
                    .filter(p -> p.getProducteId().equals(item.getProductoId()))
                    .findFirst()
                    .ifPresent(p -> nouItem.setPrecios(p.getPrecios()));

                nouBorrador.getItems().add(nouItem);
            }

            SessionManager.setLlistaTemporal(nouBorrador);
            tancarFinestra();
            
            if (SessionManager.getMainController() != null) {
                SessionManager.getMainController().openCrearLlista(); 
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            crearAlerta("Error", "No s'ha pogut carregar la llista per editar: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    
    
}