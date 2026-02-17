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
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;


/**
 * Clase Controller de la vista crear Llista
 */
public class CrearLlistaController {

    // IDs del FXML
    @FXML private TextField txtNomLlista, txtquantitat, txtBuscador;
    @FXML private JFXTextArea txtDescripcio;
    @FXML private JFXComboBox<ProductePreusDTO> cmbProductes;
    @FXML private JFXListView<ItemLlistaRequest> listaVisual; // CORREGIDO: Tipo ItemLlistaRequest
    @FXML private JFXCheckBox chbNomesFavorits;
    @FXML private JFXButton btnAfegirFavorit;
    // Llistes de dades
    private ObservableList<ProductePreusDTO> llistaMestraProductes = FXCollections.observableArrayList();
    private FilteredList<ProductePreusDTO> productesFiltrats;
    private ObservableList<ItemLlistaRequest> itemsEnLlista = FXCollections.observableArrayList();

    /**
     * Metode dínicialització de la vista
     */
    @FXML
    public void initialize() {
        // Carregar el que hi avia
        itemsEnLlista.setAll(SessionManager.getListaTemporal().getItems());
        txtNomLlista.setText(SessionManager.getListaTemporal().getNombre());
        txtDescripcio.setText(SessionManager.getListaTemporal().getDescripcion());
    	    
        // per escoltar canvis, cada vegada que es borri o afegeix algo 
    	    itemsEnLlista.addListener((ListChangeListener<ItemLlistaRequest>) c -> {
    	        SessionManager.getListaTemporal().setItems(new ArrayList<>(itemsEnLlista));
    	    });

    	    // per els textes, si s'escriu algo i es cambie de vista que es guardi
    	    txtNomLlista.textProperty().addListener((obs, oldV, newV) -> SessionManager.getListaTemporal().setNombre(newV));
    	    txtDescripcio.textProperty().addListener((obs, oldV, newV) -> SessionManager.getListaTemporal().setDescripcion(newV));

    	    
    	    listaVisual.setItems(itemsEnLlista);
    	
        carregarComboProductes();
        
        listaVisual.setItems(itemsEnLlista);
        
     // CONFIGURAR EL COMBO (nomes per elejir producte)
        cmbProductes.setCellFactory(lv -> new ListCell<ProductePreusDTO>() {
            @Override
            protected void updateItem(ProductePreusDTO item, boolean empty) {
                super.updateItem(item, empty);
                // aqui nomes el nom(sense quantitat.
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        cmbProductes.setButtonCell(cmbProductes.getCellFactory().call(null));
        
     // configuració de la vista visual
        listaVisual.setCellFactory(lv -> new JFXListCell<ItemLlistaRequest>() {
            @Override
            protected void updateItem(ItemLlistaRequest item, boolean empty) {
            	// Dintre del updateItem de la llistaVisual
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox contenidorFila = new VBox(5);
                    HBox filaSuperior = new HBox(15);
                    filaSuperior.setAlignment(Pos.CENTER_LEFT);
                    HBox filaPreuss = new HBox(10); // aqui posarem els preus

                    if (item.getCantidad() == null) item.setCantidad(java.math.BigDecimal.ONE);
  
                 // .stripTrailingZeros() treu els 0 que sobren (2.000 -> 2)
                 // .toPlainString() evita la notació científica en números molt grans
                 String cantidadLimpia = item.getCantidad().stripTrailingZeros().toPlainString();

                 
                    Label lblNom = new Label(item.getProductoNombre());
                    lblNom.setPrefWidth(200);
                    lblNom.setStyle("-fx-font-weight: bold;");

                    Label lblCantVal = new Label(cantidadLimpia);
                    lblCantVal.setPrefWidth(40);
                    lblCantVal.setAlignment(Pos.CENTER);

                    // --- LÓGICA DE PREUS ---
                    if (item.getPrecios() != null) {
                        item.getPrecios().forEach((superNom, preu) -> {
                            // Sol si el supermerct esta actiu en la configuració
                            if (SupermercatServiceClient.getLocalStatus(superNom)) {
                                Label lblPrecio = new Label(superNom + ": " + preu + "€");
                                lblPrecio.setStyle("-fx-font-size: 10px; -fx-text-fill: #555; " +
                                                   "-fx-background-color: #eee; -fx-padding: 2 5; " +
                                                   "-fx-background-radius: 5;");
                                filaPreuss.getChildren().add(lblPrecio);
                            }
                        });
                    }

                    // --- BOTONS ---
                    // boto afegir unitat
                    Button btnMes = new Button("+");
                    btnMes.setOnAction(e -> {
                        item.setCantidad(item.getCantidad().add(java.math.BigDecimal.ONE));
                        lblCantVal.setText(item.getCantidad().toString());
                        sincronizarAmbSessionManager();
                    });

                    //boto treure unitat
                    Button btnMenys = new Button("-");
                    btnMenys.setOnAction(e -> {
                        if (item.getCantidad().compareTo(java.math.BigDecimal.ONE) > 0) {
                            item.setCantidad(item.getCantidad().subtract(java.math.BigDecimal.ONE));
                            lblCantVal.setText(item.getCantidad().toString());
                            sincronizarAmbSessionManager();
                        }
                    });

                    //boto eliminar producte
                    Button btnDelete = new Button("🗑");
                    btnDelete.setStyle("-fx-text-fill: red;");
                    btnDelete.setOnAction(e -> {
                        itemsEnLlista.remove(item);
                        sincronizarAmbSessionManager();
                    });
                    
                    // boto favorit (afegir/Treure)
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
                                        // Refrescar el ComboBox por si l'usuari está buscan favorits
                                        if (chbNomesFavorits.isSelected()) {
                                            productesFiltrats.setPredicate(p -> SessionManager.esFavorito(p.getProducteId()));
                                        }
                                        // Refrescar la propia llista per cambiar el cor de la fila
                                        listaVisual.refresh();
                                    });
                                }
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }).start();
                    });

                    filaSuperior.getChildren().addAll(lblNom, btnMenys, lblCantVal, btnMes, btnFavRow, btnDelete);
                    
                    //Posem la fila de botons y la fila de preus en el contenidor vertical
                    contenidorFila.getChildren().addAll(filaSuperior, filaPreuss);
                    
                    setGraphic(contenidorFila);
                    setText(null);
                }
            }
        });
        
        chbNomesFavorits.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
            	//Filtrem pr que nomes surton els favorits del SessionManager
                productesFiltrats.setPredicate(p -> SessionManager.esFavorito(p.getProducteId()));
            } else {
                // Restaurem el buscador
                productesFiltrats.setPredicate(p -> true);
            }
        });
        
    }

    
    /**
     * Metode per sincronitzar la llista amb la que es guarda temporalment
     */
    private void sincronizarAmbSessionManager() {
        SessionManager.getListaTemporal().setItems(new ArrayList<>(itemsEnLlista));
        SessionManager.getListaTemporal().setNombre(txtNomLlista.getText());
        SessionManager.getListaTemporal().setDescripcion(txtDescripcio.getText());
    }
    
    /**
     * Metode per carregar els productes al combo
     */
    private void carregarComboProductes() {
        try {
            // Usem el client de productes
            List<ProductePreusDTO> base = ProducteServiceClient.getProductos();
            llistaMestraProductes.setAll(base);
            productesFiltrats = new FilteredList<>(llistaMestraProductes, p -> true);

            txtBuscador.textProperty().addListener((obs, oldV, newV) -> {
                productesFiltrats.setPredicate(p -> {
                    if (newV == null || newV.isEmpty()) return true;
                    return p.getNombre().toLowerCase().contains(newV.toLowerCase());
                });
                
                // Si hi ha resulktats selecciona primer y mostra el combo
                if (!productesFiltrats.isEmpty()) {
                    cmbProductes.show();  
                }
            });

            cmbProductes.setItems(productesFiltrats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Metode per afegir un producte a la llista
     */
    @FXML
    private void afegirProducteALlista() {
        ProductePreusDTO seleccionat = cmbProductes.getSelectionModel().getSelectedItem();
        String cantStr = txtquantitat.getText();

        // Validem que hi ha selecció i quantitat
        if (seleccionat != null && !cantStr.isEmpty()) {
            try {
                ItemLlistaRequest nuevoItem = new ItemLlistaRequest();
                nuevoItem.setProductoId(seleccionat.getProducteId());
                nuevoItem.setProductoNombre(seleccionat.getNombre());
                nuevoItem.setCantidad(new java.math.BigDecimal(cantStr.trim()));
                nuevoItem.setUnidad(seleccionat.getUnidad());
                nuevoItem.setPrecios(seleccionat.getPrecios());// carregem els preus

                // Afegim a la vista (visual)
                itemsEnLlista.add(nuevoItem);
                
                // Sincronitzem amb SessionManager
                SessionManager.getListaTemporal().setItems(new ArrayList<>(itemsEnLlista));
                SessionManager.getListaTemporal().setNombre(txtNomLlista.getText());
                SessionManager.getListaTemporal().setDescripcion(txtDescripcio.getText());
                
                txtquantitat.clear();
                cmbProductes.getSelectionModel().clearSelection();
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "La quantitat ha de ser un número.");
            }
        } else {
            mostrarAlerta("Atenció", "Selecciona un producte i una quantitat.");
        }
    }

    /**
     * Metode per borrar tots els productes
     */
    @FXML
    private void borrarTots() {
        itemsEnLlista.clear();
    }

    /**
     * Metode que crida al de enviar llista al servidor com a PRIVADA
     */
    @FXML private void guardarLlistaPrivada() { 
    	enviarLlistaAlServidor("PRIVADA"); 
    	}
    
    /**
     * Metode que crida al de enviar llista al servidor com a PUBLICA
     */
    @FXML private void publicarLlista() { 
    	enviarLlistaAlServidor("PUBLICA"); }
    

    /**
     * Metode que envia al servidor una llista
     * @param visibilitat (publica o privada)
     */
    private void enviarLlistaAlServidor(String visibilitat) {
        if (itemsEnLlista.isEmpty()) {
            mostrarAlerta("Error", "La llista està buida.");
            return;
        }

        try {
            CrearLlistaRequest req = new CrearLlistaRequest();
            req.setUsuariId(SessionManager.getUsuario().getUserId());
            req.setNombre(txtNomLlista.getText());
            req.setDescripcion(txtDescripcio.getText());
            req.setVisibilidad(visibilitat);
            req.setItems(itemsEnLlista); //enviem els items acumulats

            LlistaServiceClient.crearLlista(req);
            mostrarAlerta("Èxit", "Llista guardada com a " + visibilitat);
            borrarTots();
            txtNomLlista.clear();
            txtDescripcio.clear();
        } catch (Exception e) {
            mostrarAlerta("Error", "No s'ha pogut guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * MEtode per els favorits al combo
     */
    
    @FXML
    private void toggleFavoritEnCombo() {
        ProductePreusDTO seleccionat = cmbProductes.getSelectionModel().getSelectedItem();
        if (seleccionat == null || !SessionManager.isLoggedIn()) return;

        Long userId = SessionManager.getUsuario().getUserId();
        Long prodId = seleccionat.getProducteId();
        boolean esYaFav = SessionManager.esFavorito(prodId);

        new Thread(() -> {
            try {
            	//Cridem a la API (true si no es favorit per afegirlo, o false si ja ho es)
                boolean exit = ProducteServiceClient.gestionarFavoritoAPI(userId, prodId, !esYaFav);
                if (exit) {
                    if (esYaFav) SessionManager.getIdsFavoritos().remove(prodId);
                    else SessionManager.getIdsFavoritos().add(prodId);

                    Platform.runLater(() -> {
                        if (chbNomesFavorits.isSelected()) {
                            // AIXO FA QUE EL PRODUCTE DESAPAREGUI AL INSTANT
                            productesFiltrats.setPredicate(p -> SessionManager.esFavorito(p.getProducteId()));
                        }
                        // aixo actualitze els cors de la llista de abaix
                        listaVisual.refresh(); 
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
    
    /**
     * Metode que obre la vista del comprovador (per gestionar l'estalvi)
     * @param event
     */
    @FXML
    private void enviarComprovador(ActionEvent event) {
        // Usem la instancia per camviar a la vista eco
        MainController.getInstance().openLlistaEco();
    }
    
    
    private void mostrarAlerta(String titol, String missatge) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }
}