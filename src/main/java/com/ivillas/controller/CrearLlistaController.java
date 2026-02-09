package com.ivillas.controller;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.request.CrearLlistaRequest;
import com.ivillas.request.ItemLlistaRequest;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.service.SupermercatServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CrearLlistaController {

    // 1. IDs del FXML
    @FXML private TextField txtNomLlista, txtCantidad, txtBuscador;
    @FXML private JFXTextArea txtDescripcio;
    @FXML private JFXComboBox<ProductePreusDTO> cmbProductes;
    @FXML private JFXListView<ItemLlistaRequest> listaVisual; // CORREGIDO: Tipo ItemLlistaRequest
    @FXML private JFXCheckBox chbNomesFavorits;
    @FXML private JFXButton btnAfegirFavorit;
    // 2. Listas de datos
    private ObservableList<ProductePreusDTO> listaMaestraProductes = FXCollections.observableArrayList();
    private FilteredList<ProductePreusDTO> productosFiltrados;
    private ObservableList<ItemLlistaRequest> itemsEnLista = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Cargar lo que ya había
        itemsEnLista.setAll(SessionManager.getListaTemporal().getItems());
        txtNomLlista.setText(SessionManager.getListaTemporal().getNombre());
        txtDescripcio.setText(SessionManager.getListaTemporal().getDescripcion());
    	    
    	    // 2. ESCUCHAR CAMBIOS: Cada vez que se borre o añada algo, se guarda en el Manager
    	    itemsEnLista.addListener((ListChangeListener<ItemLlistaRequest>) c -> {
    	        SessionManager.getListaTemporal().setItems(new ArrayList<>(itemsEnLista));
    	    });

    	    // 3. ESCUCHAR TEXTOS: Si el usuario escribe y cambia de vista, que se guarde
    	    txtNomLlista.textProperty().addListener((obs, oldV, newV) -> SessionManager.getListaTemporal().setNombre(newV));
    	    txtDescripcio.textProperty().addListener((obs, oldV, newV) -> SessionManager.getListaTemporal().setDescripcion(newV));

    	    
    	    listaVisual.setItems(itemsEnLista);
    	
        cargarComboProductes();
        
        listaVisual.setItems(itemsEnLista);
        
     // 1. CONFIGURAR EL COMBO (Solo para elegir el producto)
        cmbProductes.setCellFactory(lv -> new ListCell<ProductePreusDTO>() {
            @Override
            protected void updateItem(ProductePreusDTO item, boolean empty) {
                super.updateItem(item, empty);
                // Aquí NO hay cantidad. Solo el nombre.
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        cmbProductes.setButtonCell(cmbProductes.getCellFactory().call(null));
        // SOLUCIÓN AL TYPE MISMATCH: Quitamos el genérico de la izquierda en la lambda
     // 2. CONFIGURAR LA LISTA VISUAL (Donde ya están añadidos los productos)
        listaVisual.setCellFactory(lv -> new JFXListCell<ItemLlistaRequest>() {
            @Override
            protected void updateItem(ItemLlistaRequest item, boolean empty) {
            	// Dentro del updateItem de la listaVisual
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox contenedorFila = new VBox(5);
                    HBox filaSuperior = new HBox(15);
                    filaSuperior.setAlignment(Pos.CENTER_LEFT);
                    HBox filaPrecios = new HBox(10); // <--- AQUÍ SE METERÁN LOS PRECIOS

                    if (item.getCantidad() == null) item.setCantidad(java.math.BigDecimal.ONE);
  
                 // .stripTrailingZeros() quita los ceros sobrantes (2.000 -> 2)
                 // .toPlainString() evita que use notación científica en números muy grandes
                 String cantidadLimpia = item.getCantidad().stripTrailingZeros().toPlainString();

                 
                    Label lblNom = new Label(item.getProductoNombre());
                    lblNom.setPrefWidth(200);
                    lblNom.setStyle("-fx-font-weight: bold;");

                    Label lblCantVal = new Label(cantidadLimpia);
                    lblCantVal.setPrefWidth(40);
                    lblCantVal.setAlignment(Pos.CENTER);

                    // --- LÓGICA DE PRECIOS (ESTO ES LO QUE FALTABA) ---
                    if (item.getPrecios() != null) {
                        item.getPrecios().forEach((superNombre, precio) -> {
                            // Solo si el súper está activo en Configuración
                            if (SupermercatServiceClient.getLocalStatus(superNombre)) {
                                Label lblPrecio = new Label(superNombre + ": " + precio + "€");
                                lblPrecio.setStyle("-fx-font-size: 10px; -fx-text-fill: #555; " +
                                                   "-fx-background-color: #eee; -fx-padding: 2 5; " +
                                                   "-fx-background-radius: 5;");
                                filaPrecios.getChildren().add(lblPrecio);
                            }
                        });
                    }

                    // --- BOTONES (Copia estos tal cual los tenías) ---
                    Button btnMas = new Button("+");
                    btnMas.setOnAction(e -> {
                        item.setCantidad(item.getCantidad().add(java.math.BigDecimal.ONE));
                        lblCantVal.setText(item.getCantidad().toString());
                        sincronizarConSessionManager();
                    });

                    Button btnMenos = new Button("-");
                    btnMenos.setOnAction(e -> {
                        if (item.getCantidad().compareTo(java.math.BigDecimal.ONE) > 0) {
                            item.setCantidad(item.getCantidad().subtract(java.math.BigDecimal.ONE));
                            lblCantVal.setText(item.getCantidad().toString());
                            sincronizarConSessionManager();
                        }
                    });

                    Button btnDelete = new Button("🗑");
                    btnDelete.setStyle("-fx-text-fill: red;");
                    btnDelete.setOnAction(e -> {
                        itemsEnLista.remove(item);
                        sincronizarConSessionManager();
                    });
                    
                    Button btnFavRow = new Button(SessionManager.esFavorito(item.getProductoId()) ? "❤" : "♡");
                    btnFavRow.setStyle("-fx-text-fill: " + (SessionManager.esFavorito(item.getProductoId()) ? "red" : "gray") + "; -fx-background-color: transparent;");

                    btnFavRow.setOnAction(e -> {
                        Long userId = SessionManager.getUsuario().getUserId();
                        Long prodId = item.getProductoId();
                        boolean esYaFav = SessionManager.esFavorito(prodId);

                        new Thread(() -> {
                            try {
                                if (ProducteServiceClient.gestionarFavoritoAPI(userId, prodId, !esYaFav)) {
                                    if (esYaFav) SessionManager.getIdsFavoritos().remove(prodId);
                                    else SessionManager.getIdsFavoritos().add(prodId);
                                    
                                    Platform.runLater(() -> {
                                        // Refrescar el ComboBox por si el usuario está buscando favoritos
                                        if (chbNomesFavorits.isSelected()) {
                                            productosFiltrados.setPredicate(p -> SessionManager.esFavorito(p.getProducteId()));
                                        }
                                        // Refrescar la propia lista para cambiar el corazón de la fila
                                        listaVisual.refresh();
                                    });
                                }
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }).start();
                    });

                    filaSuperior.getChildren().addAll(lblNom, btnMenos, lblCantVal, btnMas, btnFavRow, btnDelete);
                    
                    // Metemos la fila de botones y la fila de precios en el contenedor vertical
                    contenedorFila.getChildren().addAll(filaSuperior, filaPrecios);
                    
                    setGraphic(contenedorFila);
                    setText(null);
                }
            }
        });
        
        chbNomesFavorits.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                // Filtramos para que solo salgan los favoritos del SessionManager
                productosFiltrados.setPredicate(p -> SessionManager.esFavorito(p.getProducteId()));
            } else {
                // Restauramos el predicado normal (o el del buscador si hay algo escrito)
                productosFiltrados.setPredicate(p -> true);
            }
        });
        
    }

    private void sincronizarConSessionManager() {
        SessionManager.getListaTemporal().setItems(new ArrayList<>(itemsEnLista));
        SessionManager.getListaTemporal().setNombre(txtNomLlista.getText());
        SessionManager.getListaTemporal().setDescripcion(txtDescripcio.getText());
    }
    
    private void cargarComboProductes() {
        try {
            // Usamos tu cliente de productos
            List<ProductePreusDTO> base = ProducteServiceClient.getProductos();
            listaMaestraProductes.setAll(base);
            productosFiltrados = new FilteredList<>(listaMaestraProductes, p -> true);

            txtBuscador.textProperty().addListener((obs, oldV, newV) -> {
                productosFiltrados.setPredicate(p -> {
                    if (newV == null || newV.isEmpty()) return true;
                    return p.getNombre().toLowerCase().contains(newV.toLowerCase());
                });
                
                // TRUCO: Si hay resultados, selecciona el primero y abre el combo
                if (!productosFiltrados.isEmpty()) {
                    cmbProductes.show(); 
                    // Opcional: cmbProductes.getSelectionModel().selectFirst(); 
                }
            });

            cmbProductes.setItems(productosFiltrados);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void afegirProducteALlista() {
        ProductePreusDTO seleccionado = cmbProductes.getSelectionModel().getSelectedItem();
        String cantStr = txtCantidad.getText();

        // Validamos que haya selección y cantidad
        if (seleccionado != null && !cantStr.isEmpty()) {
            try {
                ItemLlistaRequest nuevoItem = new ItemLlistaRequest();
                nuevoItem.setProductoId(seleccionado.getProducteId());
                nuevoItem.setProductoNombre(seleccionado.getNombre());
                nuevoItem.setCantidad(new java.math.BigDecimal(cantStr.trim()));
                nuevoItem.setUnidad(seleccionado.getUnidad());
                nuevoItem.setPrecios(seleccionado.getPrecios()); // Cargamos los precios

                // Añadir a la lista visual
                itemsEnLista.add(nuevoItem);
                
                // Sincronizar con SessionManager
                SessionManager.getListaTemporal().setItems(new ArrayList<>(itemsEnLista));
                SessionManager.getListaTemporal().setNombre(txtNomLlista.getText());
                SessionManager.getListaTemporal().setDescripcion(txtDescripcio.getText());
                
                txtCantidad.clear();
                cmbProductes.getSelectionModel().clearSelection();
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "La quantitat ha de ser un número.");
            }
        } else {
            mostrarAlerta("Atenció", "Selecciona un producte i una quantitat.");
        }
    }

    @FXML
    private void borrarTodos() {
        itemsEnLista.clear();
    }

    @FXML private void guardarLlistaPrivada() { enviarLlistaAlServidor("PRIVADA"); }
    @FXML private void publicarLlista() { enviarLlistaAlServidor("PUBLICA"); }

    private void enviarLlistaAlServidor(String visibilidad) {
        if (itemsEnLista.isEmpty()) {
            mostrarAlerta("Error", "La llista està buida.");
            return;
        }

        try {
            CrearLlistaRequest req = new CrearLlistaRequest();
            req.setUsuariId(SessionManager.getUsuario().getUserId());
            req.setNombre(txtNomLlista.getText());
            req.setDescripcion(txtDescripcio.getText());
            req.setVisibilidad(visibilidad);
            req.setItems(itemsEnLista); // Enviamos los items acumulados

            LlistaServiceClient.crearLlista(req);
            mostrarAlerta("Èxit", "Llista guardada com a " + visibilidad);
            borrarTodos();
            txtNomLlista.clear();
            txtDescripcio.clear();
        } catch (Exception e) {
            mostrarAlerta("Error", "No s'ha pogut guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void toggleFavoritoEnCombo() {
        ProductePreusDTO seleccionado = cmbProductes.getSelectionModel().getSelectedItem();
        if (seleccionado == null || !SessionManager.isLoggedIn()) return;

        Long userId = SessionManager.getUsuario().getUserId();
        Long prodId = seleccionado.getProducteId();
        boolean esYaFav = SessionManager.esFavorito(prodId);

        new Thread(() -> {
            try {
                // Llamamos a la API (true si no es fav para añadirlo, false si ya lo es)
                boolean exito = ProducteServiceClient.gestionarFavoritoAPI(userId, prodId, !esYaFav);
                if (exito) {
                    if (esYaFav) SessionManager.getIdsFavoritos().remove(prodId);
                    else SessionManager.getIdsFavoritos().add(prodId);

                    Platform.runLater(() -> {
                        if (chbNomesFavorits.isSelected()) {
                            // Esto hace que el producto desaparezca del ComboBox al instante
                            productosFiltrados.setPredicate(p -> SessionManager.esFavorito(p.getProducteId()));
                        }
                        // Esto actualiza los corazones de la lista visual de abajo
                        listaVisual.refresh(); 
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
    
    @FXML
    private void enviarComprovador(ActionEvent event) {
        // Usamos la instancia del MainController para cambiar la vista
        MainController.getInstance().openLlistaEco();
    }
    
    @FXML
    private void toggleFavoritoSeleccionado() {
        ProductePreusDTO seleccionado = cmbProductes.getSelectionModel().getSelectedItem();
        if (seleccionado == null || !SessionManager.isLoggedIn()) return;

        Long userId = SessionManager.getUsuario().getUserId();
        Long prodId = seleccionado.getProducteId();
        boolean esYaFavorito = SessionManager.esFavorito(prodId);

        new Thread(() -> {
            try {
                // Llamamos al Service que actualizamos antes
                boolean exito = ProducteServiceClient.gestionarFavoritoAPI(userId, prodId, !esYaFavorito);
                if (exito) {
                    if (esYaFavorito) SessionManager.getIdsFavoritos().remove(prodId);
                    else SessionManager.getIdsFavoritos().add(prodId);
                    
                    // Si tienes el checkbox de "Només Favorits" activo, refrescamos el filtro
                    Platform.runLater(() -> {
                        if (chbNomesFavorits.isSelected()) {
                            productosFiltrados.setPredicate(p -> SessionManager.esFavorito(p.getProducteId()));
                        }
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}