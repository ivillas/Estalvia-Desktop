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


public class DetallController {
    @FXML private Label lblTitulo, lblAutor, lblDescripcion;
    @FXML private Button btnCopiarDetalle1, btnEliminarLlista;
    @FXML private TableView<ItemLlistaDTO> tablaProductos;
    @FXML private TableColumn<ItemLlistaDTO, String> colNombre, colCantidad, colUnidad;
    
    private LlistaDTO listaActual;
    
    @FXML
    public void cargarDatos(LlistaDTO lista) {
        this.listaActual = lista;
        lblTitulo.setText(lista.getNombre());
        lblAutor.setText("Autor: " + lista.getNomAutor());
        lblDescripcion.setText(lista.getDescripcion());

        // Configuración de tabla...
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        tablaProductos.setItems(FXCollections.observableArrayList(lista.getItems()));

        // --- SEGURIDAD SIN CAMBIAR EL DTO ---
        // Comparamos el nombre del autor (String) con el nombre del usuario logueado
        if (SessionManager.isLoggedIn() && lista.getNomAutor() != null) {
            String nombreLogueado = SessionManager.getUsuario().getUsername();
            boolean esAutor = lista.getNomAutor().equals(nombreLogueado);
            btnEliminarLlista.setVisible(esAutor);
        } else {
            btnEliminarLlista.setVisible(false);
        }
    }
    

    @FXML
    private void eliminarLlista() {
        if (this.listaActual == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminació");
        alert.setHeaderText(null);
        alert.setContentText("Vols eliminar la llista '" + listaActual.getNombre() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Long llistaId = listaActual.getListaId(); 
                Long usuariId = SessionManager.getUsuario().getUserId();

                // 1. Borrado físico en el servidor
                LlistaServiceClient.eliminarLlista(llistaId, usuariId);

                // 2. Cerramos la ventana modal de detalle
                cerrarVentana();
                
                // 3. Hilo con pequeño retardo para asegurar que el refresco vea el cambio
                new Thread(() -> {
                    try { 
                        Thread.sleep(300); // 300ms es el tiempo ideal para estabilidad
                    } catch (InterruptedException e) {}
                    
                    javafx.application.Platform.runLater(() -> {
                        if (SessionManager.getMainController() != null) {
                            // Esto refresca los contadores de favoritos/stats si es necesario
                            if (SessionManager.isLoggedIn()) {
                                SessionManager.cargarFavoritos(); 
                            }
                            // Esto refresca la vista (Privadas, Públicas o Perfil)
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
    
    
    
    
    
    
    @FXML
    private void copiarLista() {
    
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


        btnCopiarDetalle1.setText("Afegit!");
        btnCopiarDetalle1.setDisable(true);

        
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    
    }
    
    
    private void mostrarAlertaExito(String titulo, String msg) { crearAlerta(titulo, msg, AlertType.INFORMATION); }

    private void crearAlerta(String titulo, String msg, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    

    
    
    @FXML
    private void cerrarVentana() {
        ((Stage) lblTitulo.getScene().getWindow()).close();
    }
}