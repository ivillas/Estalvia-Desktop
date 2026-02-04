package com.ivillas.controller;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.request.CrearLlistaRequest;
import com.ivillas.request.ItemLlistaRequest;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CrearLlistaController {

    // 1. IDs del FXML
    @FXML private TextField txtNomLlista, txtCantidad, txtBuscador;
    @FXML private JFXTextArea txtDescripcio;
    @FXML private JFXComboBox<ProductePreusDTO> cmbProductes;
    @FXML private JFXListView<ItemLlistaRequest> listaVisual; // CORREGIDO: Tipo ItemLlistaRequest

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
        
        // 2. CONFIGURAR EL COMBO (Para que no salgan los códigos raros @1717cbf8)
        cmbProductes.setCellFactory(lv -> new ListCell<ProductePreusDTO>() {
            @Override
            protected void updateItem(ProductePreusDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNombre());
            }
        });

        // SOLUCIÓN AL TYPE MISMATCH: Quitamos el genérico de la izquierda en la lambda
        listaVisual.setCellFactory(lv -> new JFXListCell<ItemLlistaRequest>() {
            @Override
            protected void updateItem(ItemLlistaRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox cell = new HBox(15);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    
                    // Usamos el campo que añadiste al Request
                    Label lblNom = new Label(item.getProductoNombre());
                    lblNom.setPrefWidth(200);
                    lblNom.setStyle("-fx-font-weight: bold;");
                    
                    Label lblCant = new Label(item.getCantidad().toString());
                    lblCant.setPrefWidth(40);
                    lblCant.setAlignment(Pos.CENTER);
                    
                    Button btnMas = new Button("+");
                    btnMas.setOnAction(e -> {
                        item.setCantidad(item.getCantidad().add(java.math.BigDecimal.ONE));
                        lblCant.setText(item.getCantidad().toString());
                    });

                    Button btnMenos = new Button("-");
                    btnMenos.setOnAction(e -> {
                        if(item.getCantidad().compareTo(java.math.BigDecimal.ONE) > 0) {
                            item.setCantidad(item.getCantidad().subtract(java.math.BigDecimal.ONE));
                            lblCant.setText(item.getCantidad().toString());
                        }
                    });

                    Button btnDelete = new Button("🗑");
                    btnDelete.setStyle("-fx-text-fill: red;");
                    btnDelete.setOnAction(e -> itemsEnLista.remove(item));

                    cell.getChildren().addAll(lblNom, btnMenos, lblCant, btnMas, btnDelete);
                    setGraphic(cell);
                    setText(null);
                }
            }
        });
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

        if (seleccionado != null && !cantStr.isEmpty()) {
            try {
                ItemLlistaRequest nuevoItem = new ItemLlistaRequest();
                nuevoItem.setProductoId(seleccionado.getProducteId());
                nuevoItem.setProductoNombre(seleccionado.getNombre()); // Verifica nombre
                nuevoItem.setCantidad(new java.math.BigDecimal(cantStr));
                nuevoItem.setUnidad(seleccionado.getUnidad());

                itemsEnLista.add(nuevoItem);
             // ACTUALIZAR EL TEMPORAL: Guardamos el estado actual en el SessionManager
                SessionManager.getListaTemporal().setItems(new ArrayList<>(itemsEnLista));
                SessionManager.getListaTemporal().setNombre(txtNomLlista.getText());
                SessionManager.getListaTemporal().setDescripcion(txtDescripcio.getText());
                txtCantidad.clear();
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "La quantitat ha de ser un número.");
            }
        } else {
            mostrarAlerta("Atenció", "Selecciona un producte y una quantitat.");
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
    
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}