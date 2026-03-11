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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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
    private static ProductesController instance;
    private static final int PAGE_SIZE = 100;
    private int paginaActual = 0;
    private List<ProductePreusDTO> productesPerMostrar = new ArrayList<>();
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }   
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this; 
        configurarTaula();
        configurarCheckboxes();

        // Avisemos al MainController de que cambi el modoe
        if (SessionManager.getMainController() != null) {
            SessionManager.getMainController().actualizarModeBusqueda("PRODUCTES");
        }
        
        // si no esta logejat ocultem el check de favorits
        if (!SessionManager.isLoggedIn()) {
            ckbFavorit.setVisible(false);
            colAccions.setVisible(false); 
        }                
        // llegim busqueda 
        String query = SessionManager.getultimaBusqueda();
        if (query != null && !query.isEmpty()) {
            // guardem la query en una variable per usarla en  renderitzarUI
            this.filtreBusqueda = query; 
            SessionManager.setultimaBusqueda(null); 
        }
        
        carregarDades();
        scrollTargetes.vvalueProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal.doubleValue() == 1.0) {
                carregarMesTargetes();
            }
        });
    }       
    /**
     * Metode per carregar mes targetes segons la pagina actual i el tamany de pagina
     */
    
    private void carregarMesTargetes() {

        int inici = paginaActual * PAGE_SIZE;
        int fi = Math.min(inici + PAGE_SIZE, productesPerMostrar.size());

        if (inici >= fi) return; // ya hi ha mes productes

        List<ProductePreusDTO> subllista =
                productesPerMostrar.subList(inici, fi);

        for (ProductePreusDTO p : subllista) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/ProducteCard.fxml")
                );

                Node card = loader.load();
                ProducteItemController controller = loader.getController();
                controller.setData(p);

                containerProductes.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        paginaActual++;
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
    /**
     * Metode per rederitzar la IU cad check
     */
    
    private void renderitzarUI() {
        // Filtrem per favorits
        List<ProductePreusDTO> temporal;
        if (ckbFavorit.isSelected()) {
            temporal = llistaProductes.stream()
                    .filter(p -> SessionManager.esFavorit(p.getProducteId()))
                    .collect(Collectors.toList());
        } else {
            temporal = llistaProductes;
        }

        // Filtrem per text de búsqueda 
        if (filtreBusqueda != null && !filtreBusqueda.isEmpty()) {
            String q = filtreBusqueda.toLowerCase();
            temporal = temporal.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(q) || 
                                 (p.getMarca() != null && p.getMarca().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        // Asignem a la llista final
        this.llistaFiltrada = temporal;

        lblTotalProductes.setText("Total: " + llistaFiltrada.size() + " productes");
        if (chbLlista.isSelected()) {
            scrollTargetes.setVisible(false);
            taulaProductes.setVisible(true);
            taulaProductes.setItems(FXCollections.observableArrayList(llistaFiltrada));
        } else {
            taulaProductes.setVisible(false);
            scrollTargetes.setVisible(true);
            //CarregarTargetesDinamiques(llistaFiltrada);
            paginaActual = 0;
            productesPerMostrar = new ArrayList<>(llistaFiltrada);
            containerProductes.getChildren().clear();

            carregarMesTargetes();
        }
    }
    
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

            private final Text heart = new Text("❤");

            {
                heart.setStyle("-fx-font-size: 20px;");
                heart.setFill(javafx.scene.paint.Color.GRAY);

                heart.setOnMouseClicked(event -> {
                    event.consume();
                    ProductePreusDTO p = getTableView().getItems().get(getIndex());
                    if (p != null) gestionarFavorit(p);
                });

                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                ProductePreusDTO p = getTableView().getItems().get(getIndex());

                if (SessionManager.esFavorit(p.getProducteId())) {
                    heart.setFill(javafx.scene.paint.Color.web("#e74c3c"));
                } else {
                    heart.setFill(javafx.scene.paint.Color.web("#ccc"));
                }

                setGraphic(heart);
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
            if (!event.isConsumed() && event.getClickCount() == 2 && taulaProductes.getSelectionModel().getSelectedItem() != null) {
                ProductePreusDTO seleccionat = taulaProductes.getSelectionModel().getSelectedItem();
                obrirDetallPopup(seleccionat);
            }
        });
    }
    
    private void gestionarFavorit(ProductePreusDTO p) {
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

                    if (yaEsFavorit)
                        SessionManager.getIdsFavorits().remove(prodId);
                    else
                        SessionManager.getIdsFavorits().add(prodId);

                    Platform.runLater(() -> {
                        if (ckbFavorit.isSelected()) {
                            // estem en la vista sol favorits
                            renderitzarUI();   // aqui filtrem unaltra vegada per que si treus un favorit de la llista, desaparegui directament sense necessitat de refrescar tota la vista
                        } else {
                            // Sol refresquem la taula per canviar el color del cor
                            taulaProductes.refresh();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static ProductesController getInstance() { 
    	return instance; 
    }
    
    /**
     * Metode per filtrar desde fora de la vista, el que fa es actualitzar la variable de filtre i re-renderitzar la UI
     * @param text
     */
    public void filtrarDesdeFora(String text) {
        this.filtreBusqueda = text; // Actualitzem el filtre de búsqueda
        renderitzarUI(); // Re-renderitzem la UI per aplicar el nou filtre
    }
    
    /**
     * Metode per afegir un producte a la llista temporal del sessionmanager, que es la que es passa a crear llista per crear una nova llista amb els items seleccionats
     * @param p
     */
    private void afegirALlista(ProductePreusDTO p) {
        if (!SessionManager.isLoggedIn()) {
            // Alerta per iniciar sessió
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