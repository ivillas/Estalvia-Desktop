package com.ivillas.controller;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.model.SupermercatDTO;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Clase Controller de la vista crear Llista
 */
public class CrearLlistaController {

    // IDs del FXML
    @FXML private TextField txtNomLlista, txtquantitat, txtBuscador;
    @FXML private JFXTextArea txtDescripcio;
    @FXML private JFXComboBox<ProductePreusDTO> cmbProductes;
    @FXML private JFXListView<ItemLlistaRequest> listaVisual;
    @FXML private JFXCheckBox chbNomesFavorits;
    @FXML private JFXButton btnAfegirFavorit;
    // Llistes de dades
    private ObservableList<ProductePreusDTO> llistaMestraProductes = FXCollections.observableArrayList();
    private FilteredList<ProductePreusDTO> productesFiltrats;
    private ObservableList<ItemLlistaRequest> itemsEnLlista = FXCollections.observableArrayList();
    //variables
    
    
    
    
    /**
     * Metode dínicialització de la vista
     */
    @FXML
    public void initialize() {
        // Carregar el que hi avia
        itemsEnLlista.setAll(SessionManager.getLlistaTemporal().getItems());
        

        
        txtNomLlista.setText(SessionManager.getLlistaTemporal().getNombre());
        txtDescripcio.setText(SessionManager.getLlistaTemporal().getDescripcion());
    	    
        // per escoltar canvis, cada vegada que es borri o afegeix algo 
    	    itemsEnLlista.addListener((ListChangeListener<ItemLlistaRequest>) c -> {
    	        SessionManager.getLlistaTemporal().setItems(new ArrayList<>(itemsEnLlista));
    	    });

    	    // per els textes, si s'escriu algo i es cambie de vista que es guardi
    	    txtNomLlista.textProperty().addListener((obs, oldV, newV) -> SessionManager.getLlistaTemporal().setNombre(newV));
    	    txtDescripcio.textProperty().addListener((obs, oldV, newV) -> SessionManager.getLlistaTemporal().setDescripcion(newV));

    	    
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
        
     // configuració de la lista visual
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
                        // variable pr saver si hem escrit algun preu
                        boolean algunPreuEscrit = false;

                        // recorrem per escriure els preus i marquem true si aixi es
                        for (Map.Entry<String, BigDecimal> entry : item.getPrecios().entrySet()) {
                            String superNom = entry.getKey();
                            BigDecimal preu = entry.getValue();

                            if (SupermercatServiceClient.getLocalStatus(superNom)) {
                                Label lblPrecio = new Label(superNom + ": " + preu + "€");
                                lblPrecio.setStyle("-fx-font-size: 10px; -fx-text-fill: #555; " +
                                                   "-fx-background-color: #eee; -fx-padding: 2 5; " +
                                                   "-fx-background-radius: 5;");
                                filaPreuss.getChildren().add(lblPrecio);
                                algunPreuEscrit = true; // marquem true per no escriure l'avis
                            }
                        }

                        // si no hem posat cap preu escriurem el missatge
                        if (!algunPreuEscrit) {
                            Label lblAvis = new Label("Avis: No disponible en els supermercats seleccionats");
                            lblAvis.setStyle("-fx-font-size: 15px; -fx-text-fill: #d9534f; " + 
                                               "-fx-font-style: italic; -fx-padding: 2 5;");
                            filaPreuss.getChildren().add(lblAvis);
                        }
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
                    Button btnFavRow = new Button(SessionManager.esFavorit(item.getProductoId()) ? "❤" : "♡");
                    btnFavRow.setStyle("-fx-text-fill: " + (SessionManager.esFavorit(item.getProductoId()) ? "red" : "gray") + "; -fx-background-color: transparent;");

                    btnFavRow.setOnAction(e -> {
                        Long userId = SessionManager.getUsuario().getUserId();
                        Long prodId = item.getProductoId();
                        boolean esYaFav = SessionManager.esFavorit(prodId);

                        new Thread(() -> {
                            try {
                                if (ProducteServiceClient.gestionarFavoritAPI(userId, prodId, !esYaFav)) {
                                    if (esYaFav) SessionManager.getIdsFavorits().remove(prodId);
                                    else SessionManager.getIdsFavorits().add(prodId);
                                    
                                    Platform.runLater(() -> {
                                        // Refrescar el ComboBox por si l'usuari está buscan favorits
                                        if (chbNomesFavorits.isSelected()) {
                                            productesFiltrats.setPredicate(p -> SessionManager.esFavorit(p.getProducteId()));
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
            	//Filtrem per que nomes surton els favorits del SessionManager
                productesFiltrats.setPredicate(p -> SessionManager.esFavorit(p.getProducteId()));
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
        SessionManager.getLlistaTemporal().setItems(new ArrayList<>(itemsEnLlista));
        SessionManager.getLlistaTemporal().setNombre(txtNomLlista.getText());
        SessionManager.getLlistaTemporal().setDescripcion(txtDescripcio.getText());
    }
    
    /**
     * Metode per carregar els productes al combo
     */
    
    private void carregarComboProductes() {
        // usem una task per no congelar la app al obrir el comboBox
        Task<List<ProductePreusDTO>> task = new Task<>() {
            @Override
            protected List<ProductePreusDTO> call() throws Exception {
                // cerregem les dades
                List<ProductePreusDTO> tots = ProducteServiceClient.getProductes();
                List<SupermercatDTO> totsSupers = SupermercatServiceClient.getAll();
                
                // obtenim els noms dels supers actius
                List<String> activos = totsSupers.stream()
                    .filter(SupermercatDTO::isActiu)
                    .map(SupermercatDTO::getNom)
                    .collect(Collectors.toList());

                // filtrem la llista base, nomes productes amb preus dels supers seleccionats
                return tots.stream()
                    .filter(p -> p.precios.keySet().stream().anyMatch(activos::contains))
                    .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            // iniciem la llista amb els filtres aplicats
            llistaMestraProductes.setAll(task.getValue());
            productesFiltrats = new FilteredList<>(llistaMestraProductes, p -> true);
            
            // configurem el buscador, filtre sobre filtre
            txtBuscador.textProperty().addListener((obs, oldV, newV) -> {
                productesFiltrats.setPredicate(p -> {
                    // si el buscador esta en blanc mostrem tots els dels super elejits
                    if (newV == null || newV.isEmpty()) return true;
                    
                    // si escribim anem filtran
                    return p.getNombre().toLowerCase().contains(newV.toLowerCase());
                });
                
                if (!productesFiltrats.isEmpty() && txtBuscador.isFocused()) {
                    cmbProductes.show();
                }
            });

            cmbProductes.setItems(productesFiltrats);
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }
    
   

    /**
     * Metode per afegir un producte a la llista
     */
    @FXML
    private void afegirProducteALlista() {
        ProductePreusDTO seleccionat = cmbProductes.getSelectionModel().getSelectedItem();
        String cantStr = txtquantitat.getText();
        Map<String, BigDecimal> misatgePreus = Map.of("Avis, No disponible en els supermercats seleccionats2", BigDecimal.ZERO);
        // Validem que hi ha selecció i quantitat
        if (seleccionat != null && !cantStr.isEmpty()) {
            try {
            	
                ItemLlistaRequest nouItem = new ItemLlistaRequest();
                System.out.println(seleccionat);
                nouItem.setProductoId(seleccionat.getProducteId());
                nouItem.setProductoNombre(seleccionat.getNombre());
                nouItem.setCantidad(new java.math.BigDecimal(cantStr.trim()));
                nouItem.setUnidad(seleccionat.getUnidad());
                
                if(seleccionat.getPrecios().isEmpty()) {
                	nouItem.setPrecios(misatgePreus);
                }else {
                nouItem.setPrecios(seleccionat.getPrecios());// carregem els preus
                }
                
                // Afegim a la vista (visual)
                itemsEnLlista.add(nouItem);
                
                // Sincronitzem amb SessionManager
                SessionManager.getLlistaTemporal().setItems(new ArrayList<>(itemsEnLlista));
                SessionManager.getLlistaTemporal().setNombre(txtNomLlista.getText());
                SessionManager.getLlistaTemporal().setDescripcion(txtDescripcio.getText());
                
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
    
    
    private boolean noPreus() {
        // si la llista esta buida mostrem missatge i retornem
        if (itemsEnLlista.isEmpty()) {
        	mostrarAlerta("Atenció", "La llista està buida.");
        	return true; 
        }

        for (ItemLlistaRequest item : itemsEnLlista) {
            Map<String, BigDecimal> preus = item.getPrecios();
            
            // els preus son nuls o estan buits
            if (preus == null || preus.isEmpty()) {
                return true; 
            }

            // Comprovem si almenys te un preu actiu
            boolean TeAlmenysUnPreu = false;
            for (String superNom : preus.keySet()) {
                if (SupermercatServiceClient.getLocalStatus(superNom)) {
                    TeAlmenysUnPreu = true;
                    break; //amb un  ni ha prou, sortim del bucle
                }
            }

            // si despues de revisar no te caop actiu retornem true per mostrar el missatge
            if (!TeAlmenysUnPreu) {
                return true;
            }
        }
        
        return false; // si tots els productes tenen almenys un preu valid arribem aqui i retornem false
    }
    

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
        boolean esYaFav = SessionManager.esFavorit(prodId);

        new Thread(() -> {
            try {
            	//Cridem a la API (true si no es favorit per afegirlo, o false si ja ho es)
                boolean exit = ProducteServiceClient.gestionarFavoritAPI(userId, prodId, !esYaFav);
                if (exit) {
                    if (esYaFav) SessionManager.getIdsFavorits().remove(prodId);
                    else SessionManager.getIdsFavorits().add(prodId);

                    Platform.runLater(() -> {
                        if (chbNomesFavorits.isSelected()) {
                            // AIXO FA QUE EL PRODUCTE DESAPAREGUI AL INSTANT
                            productesFiltrats.setPredicate(p -> SessionManager.esFavorit(p.getProducteId()));
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
    	System.out.println("entrant al comparador");
    	if(noPreus()) {
    		mostrarAlerta("Atenció", "Hi ha productes que no tenen preus, primer s'han d'eliminar o has d'elegir mes supermercats");
    	return;
    	}
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