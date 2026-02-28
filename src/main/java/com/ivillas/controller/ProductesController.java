package com.ivillas.controller;

import javafx.scene.control.ScrollPane;

import javafx.scene.Cursor;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ivillas.model.ProductePreusDTO;
import com.ivillas.model.SupermercatDTO;
import com.ivillas.network.HttpClientProvider;
import com.ivillas.request.ItemLlistaRequest;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.service.SupermercatServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXCheckBox;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;

public class ProductesController implements Initializable {
	@FXML private Label lblTotalProductes;
	@FXML private Button btnFavorit;
    @FXML private FlowPane containerProductes;
    @FXML private JFXCheckBox chbTarget, chbLlista, ckbFavorit;
    @FXML private TableView<ProductePreusDTO> taulaProductes;
    @FXML private TableColumn<ProductePreusDTO, String> colNom, colMarca;
    @FXML private TableColumn<ProductePreusDTO, String> colPreu;
    @FXML private ScrollPane scrollTargetes;
    private MainController mainController;
    @FXML private TableColumn<ProductePreusDTO, Void> colAccions;
    @FXML private TableColumn<ProductePreusDTO, Void> colAfegir;
    private List<ProductePreusDTO> llistaFiltrada;
    private List<ProductePreusDTO> llistaProductes = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); 
    private String filtreBusqueda = "";
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }   
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTaula();
        configurarCheckboxes();
        
        // si no esta logejat ocultem el check de favorits
        if (!SessionManager.isLoggedIn()) {
            ckbFavorit.setVisible(false);
            colAccions.setVisible(false); 
        }        
        
        // LEER BÚSQUEDA
        String query = SessionManager.getultimaBusqueda();
        if (query != null && !query.isEmpty()) {
            // Guardamos la query en una variable para usarla en renderitzarUI
            this.filtreBusqueda = query; 
            SessionManager.setultimaBusqueda(null); 
        }
        
        carregarDades();
    }       
    

    /**
     * Metode per configurar el checkbox de la vista
     */
    
    private void configurarCheckboxes() {
        // Per tarjetes o llista
        chbTarget.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) { chbLlista.setSelected(false); renderitzarUI(); }
        });
        chbLlista.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) { chbTarget.setSelected(false); renderitzarUI(); }
        });
        // filtre favorits
        ckbFavorit.selectedProperty().addListener((obs, oldV, newV) -> renderitzarUI());
        
        chbTarget.setSelected(true); //estat inicial
    }

    /**
     * MEtode per carregar les dades (productes)
     */
   
    
    
    private void carregarDades() {
        Task<List<ProductePreusDTO>> task = new Task<>() {
            @Override
            protected List<ProductePreusDTO> call() throws Exception {
                // obtenim els productes
                List<ProductePreusDTO> totsProductes = ProducteServiceClient.getProductes();
                
               //obtenim els supers actius
                List<String> supersActiusNoms = new ArrayList<>();
                
                
               
                
                try {

                    List<SupermercatDTO> totsSupers = SupermercatServiceClient.getAll();
                    for (SupermercatDTO s : totsSupers) {
                        if (s.isActiu() ) {
                            supersActiusNoms.add(s.getNom());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error obtenint supermercats: " + e.getMessage());
                }

                // nomes els productes amb un supermercat com a minim a la llista s'afegeigen
                return totsProductes.stream()
                    .filter(prod -> prod.precios.keySet().stream()
                        .anyMatch(supersActiusNoms::contains))
                    .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            // asignem la llista filtrada que ve del fil
            this.llistaProductes = task.getValue();
            
            if (llistaProductes != null) {
                // actualitzem el total de productes a la label
                lblTotalProductes.setText("Total: " + llistaProductes.size() + " productes");
                // cridems a renderizarUIper aplicar el filtre
                renderitzarUI();
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /*
    
    private void carregarDades() {
        Task<List<ProductePreusDTO>> task = new Task<>() {
            @Override
            protected List<ProductePreusDTO> call() throws Exception {
                return ProducteServiceClient.getProductes(); 
            }
        };
        
        List<String> supersActius = null;
		try {
		supersActius = SupermercatServiceClient.supersActius();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

        try {
			for(SupermercatDTO l: SupermercatServiceClient.getAll()) {
				if(l.isActiu()) {
					supersActius.add(l.getNom());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        

        List<ProductePreusDTO> llistaNeta, LlistaSupers;
        llistaNeta = task.getValue();
        for (ProductePreusDTO i : llistaNeta) {
        	Map<String, BigDecimal> map = i.precios;
        	for( Map.Entry<String, BigDecimal> p : map.entrySet()) {
        		if(supersActius.contains(p.getKey())) {
        			llistaNeta.add(i);
        			break;
        		}
        	}
        }
        
        System.out.println("Supers: " + supersActius);
        System.out.println(llistaNeta);


        task.setOnSucceeded(e -> {
           // this.llistaProductes = task.getValue();
            this.llistaProductes = llistaNeta;
            // verifiquem que no sigui null
            if (llistaProductes != null) {
                lblTotalProductes.setText("Total: " + llistaProductes.size() + " productes");
                renderitzarUI();
            }
        });
        
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            exception.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true); // El hilo se cerrará si cierras la app
        thread.start();
    }
    
    
    */
    

    /**
     * Metode per rederitzar la IU cad check
     */
    
    private void renderitzarUI() {
        // 1. Filtramos por favoritos
        List<ProductePreusDTO> temporal;
        if (ckbFavorit.isSelected()) {
            temporal = llistaProductes.stream()
                    .filter(p -> SessionManager.esFavorit(p.getProducteId()))
                    .collect(Collectors.toList());
        } else {
            temporal = llistaProductes;
        }

        // 2. Filtramos por el texto de búsqueda (NUEVO)
        if (filtreBusqueda != null && !filtreBusqueda.isEmpty()) {
            String q = filtreBusqueda.toLowerCase();
            temporal = temporal.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(q) || 
                                 (p.getMarca() != null && p.getMarca().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        // 3. Asignamos a la lista final
        this.llistaFiltrada = temporal;

        // 4. El resto de tu código igual...
        lblTotalProductes.setText("Total: " + llistaFiltrada.size() + " productes");
        if (chbLlista.isSelected()) {
            scrollTargetes.setVisible(false);
            taulaProductes.setVisible(true);
            taulaProductes.setItems(FXCollections.observableArrayList(llistaFiltrada));
        } else {
            taulaProductes.setVisible(false);
            scrollTargetes.setVisible(true);
            CarregarTargetesDinamiques(llistaFiltrada);
        }
    }
    
    
    /*
    private void renderitzarUI() {
        // Filtrem segons el chek de favorits
        
        if (ckbFavorit.isSelected()) {
            llistaFiltrada = llistaProductes.stream()
                    .filter(p -> SessionManager.esFavorit(p.getProducteId()))
                    .collect(Collectors.toList());
           
        } else {
            llistaFiltrada = llistaProductes;
        }
        lblTotalProductes.setText("Total: " + llistaFiltrada.size() + " productes");
        if (chbLlista.isSelected()) {
            scrollTargetes.setVisible(false);
            taulaProductes.setVisible(true);
            taulaProductes.setItems(FXCollections.observableArrayList(llistaFiltrada));
        } else {
            taulaProductes.setVisible(false);
            scrollTargetes.setVisible(true);
            CarregarTargetesDinamiques(llistaFiltrada);
        }
    }
    */
    
    
    /**
     * Metode per obrir el detall del producte
     * @param p
     */
    
    private void obrirDetallPopup(ProductePreusDTO p) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Especificacions: " + p.getNombre());
        alert.setHeaderText(p.getNombre().toUpperCase());

        // construccio del contingut segons la logica del filtre
        StringBuilder sb = new StringBuilder();
        sb.append("Marca: ").append(p.getMarca() != null ? p.getMarca() : "N/A").append("\n");
        sb.append("Envàs: ").append(p.getEnvase() != null ? p.getEnvase() : "N/A").append("\n");
        sb.append("Última actualització: ").append(p.getLastUpdate()).append("\n\n");
        
        sb.append("COMPARATIVA DE PREUS ACTIUS:\n");
        sb.append("------------------------------------------\n");

        if (p.getPrecios() != null && !p.getPrecios().isEmpty()) {
            p.getPrecios().forEach((superNombre, precio) -> {
                // usem la logica per filtrar
                if (SupermercatServiceClient.getLocalStatus(superNombre)) {
                    sb.append(String.format(" • %-15s : %s €\n", superNombre.toUpperCase(), precio));
                }
            });
        } else {
            sb.append("No hi ha preus disponibles actualment.");
        }

        // apliquem estil per que no e vegi sec
        alert.getDialogPane().setPrefWidth(450);
        alert.setContentText(sb.toString());
        
        // metode per obrir els detalls en els clicks
        alert.showAndWait();
    }
    
    private void CarregarTargetesDinamiques(List<ProductePreusDTO> lista) {
        containerProductes.getChildren().clear();
        int limite = Math.min(lista.size(), 100); 

        for (int i = 0; i < limite; i++) {
            ProductePreusDTO p = lista.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProducteCard.fxml"));
                Node card = loader.load();
                ProducteItemController controller = loader.getController();
                controller.setData(p);
                
                // logica per el cor de la targeta
                if (!SessionManager.isLoggedIn()) {
                    controller.ocultarCor(); 
                } else if (SessionManager.esFavorit(p.getProducteId())) {
                    controller.marcarCorVermell(); 
                }

                containerProductes.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }        
    
    private void configurarTaula() {
        // Columnes basiques
        colNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        
        // Columna preu + supermercat
        colPreu.setCellValueFactory(cellData -> {
            ProductePreusDTO p = cellData.getValue();
            Map<String, BigDecimal> preus = p.getPrecios();
            if (preus != null && !preus.isEmpty()) {
                Map.Entry<String, BigDecimal> minEntry = preus.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .min(Map.Entry.comparingByValue())
                    .orElse(null);
                if (minEntry != null) {
                    String text = String.format("%.2f € (%s)", minEntry.getValue(), minEntry.getKey().toUpperCase());
                    return new SimpleStringProperty(text);
                }
            }
            return new SimpleStringProperty("Sense preu");
        });

        // columnes  accions favorits
        colAccions.setCellFactory(param -> new TableCell<>() {
            private final Label iconFavorit = new Label("❤");
            {
                iconFavorit.setCursor(Cursor.HAND);
                iconFavorit.setOnMouseClicked(event -> {
                    ProductePreusDTO p = getTableView().getItems().get(getIndex());
                    if (p != null) gestionarFavorit(p, iconFavorit);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    ProductePreusDTO p = getTableView().getItems().get(getIndex());
                    if (SessionManager.esFavorit(p.getProducteId())) {
                        iconFavorit.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 20px;");
                    } else {
                        iconFavorit.setStyle("-fx-text-fill: #ccc; -fx-font-size: 20px;");
                    }
                    setGraphic(iconFavorit);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        

        // boto mes (afegir)
        colAfegir.setCellFactory(param -> new TableCell<>() {
            private final Button btnPlus = new Button("＋");
            {
                btnPlus.setStyle("-fx-background-color: #79EDED; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnPlus.setCursor(Cursor.HAND);
                btnPlus.setOnAction(event -> {
                    ProductePreusDTO p = getTableView().getItems().get(getIndex());
                    if (p != null) afegirALlista(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnPlus);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        taulaProductes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && taulaProductes.getSelectionModel().getSelectedItem() != null) {
                ProductePreusDTO seleccionat = taulaProductes.getSelectionModel().getSelectedItem();
                obrirDetallPopup(seleccionat);
            }
        });
    }
    
    private void gestionarFavorit(ProductePreusDTO p, Label icon) {
        if (!SessionManager.isLoggedIn()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sessió necessària");
            alert.setContentText("Has d'estar loguejat per afegir productes a preferits.");
            alert.showAndWait();
            return;
        }

        Long userId = SessionManager.getUsuario().getUserId();
        Long prodId = p.getProducteId();
        boolean yaEsFavorit = SessionManager.esFavorit(prodId);

        // fil secundari per no congelar l'app
        new Thread(() -> {
            try {
                // true per afegir, false per treure
                boolean exito = ProducteServiceClient.gestionarFavoritAPI(userId, prodId, !yaEsFavorit);
                
                if (exito) {
                    // Actualizem la memoria del SessionManager
                    if (yaEsFavorit) SessionManager.getIdsFavorits().remove(prodId);
                    else SessionManager.getIdsFavorits().add(prodId);

                    // Refresquem el color en el fil de JavaFX
                    Platform.runLater(() -> {
                        // si estem filtran per favorits, hem de refrescar la UI
                        if (ckbFavorit.isSelected()) {
                            renderitzarUI(); // aqui desapareixera el producte
                        } else {
                            // si no filtrem nome cambiem el producte
                            if (yaEsFavorit) icon.setStyle("-fx-text-fill: #ccc;");
                            else icon.setStyle("-fx-text-fill: #e74c3c;");
                        }
                        
                        // actualitcem el controlador si es necesari
                        if (SessionManager.getMainController() != null) {
                            SessionManager.getMainController().refrescarVistaActualSiEsPerfil();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void afegirALlista(ProductePreusDTO p) {
        if (!SessionManager.isLoggedIn()) {
            // Alerta per iniciar sessio
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Has de iniciar sessió.");
            alert.showAndWait();
            return;
        }

        ItemLlistaRequest nouItem = new ItemLlistaRequest();
        nouItem.setProductoId(p.getProducteId());
        nouItem.setProductoNombre(p.getNombre());
        nouItem.setCantidad(java.math.BigDecimal.ONE);
        
        // Pasem els preus per que es vegin a la llista
        nouItem.setPrecios(p.getPrecios());

        //  afegim directament a la llista del sessionmanager
        // Al cambiar de vista a CrearLlistaController, el initialize carregara aixo automaticament
        SessionManager.getLlistaTemporal().getItems().add(nouItem);

        // Feedback en consola
        System.out.println("Afegit ItemLlistaRequest: " + nouItem.getProductoNombre());
    }
}