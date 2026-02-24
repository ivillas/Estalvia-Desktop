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
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ivillas.model.ProductePreusDTO;
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
    @FXML private TableView<ProductePreusDTO> tablaProductes;
    @FXML private TableColumn<ProductePreusDTO, String> colNom, colMarca;
    @FXML private TableColumn<ProductePreusDTO, String> colPreu;
    @FXML private ScrollPane scrollTargetes;
    private MainController mainController;
    @FXML private TableColumn<ProductePreusDTO, Void> colAccions;
    @FXML private TableColumn<ProductePreusDTO, Void> colAnyadir;
    private List<ProductePreusDTO> listaFiltrada;
    private List<ProductePreusDTO> llistaProductes = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); 
    
    
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
                String url = HttpClientProvider.getBaseUrl() + "/productos/con-precios";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = HttpClientProvider.getClient()
                        .send(request, HttpResponse.BodyHandlers.ofString());

                return objectMapper.readValue(response.body(), 
                        new TypeReference<List<ProductePreusDTO>>() {});
            }
        };

        task.setOnSucceeded(e -> {
            this.llistaProductes = task.getValue();
            if(ckbFavorit.isSelected()) {
            	System.out.println("Aqui 1");
            	lblTotalProductes.setText("Total: " + listaFiltrada.size() + " productes");
            }
            lblTotalProductes.setText("Total: " + llistaProductes.size() + " productes"); 
            renderitzarUI();
        });
        
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    /**
     * Metode per rederitzar la IU cad check
     */
    
    private void renderitzarUI() {
        // Filtrem segons el chek de favorits
        
        if (ckbFavorit.isSelected()) {
            listaFiltrada = llistaProductes.stream()
                    .filter(p -> SessionManager.esFavorit(p.getProducteId()))
                    .collect(Collectors.toList());
           
        } else {
            listaFiltrada = llistaProductes;
        }
        lblTotalProductes.setText("Total: " + listaFiltrada.size() + " productes");
        if (chbLlista.isSelected()) {
            scrollTargetes.setVisible(false);
            tablaProductes.setVisible(true);
            tablaProductes.setItems(FXCollections.observableArrayList(listaFiltrada));
        } else {
            tablaProductes.setVisible(false);
            scrollTargetes.setVisible(true);
            CarregarTargetesDinamiques(listaFiltrada);
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
                
                // Lógica para el corazón de la tarjeta
                if (!SessionManager.isLoggedIn()) {
                    controller.ocultarCor(); 
                } else if (SessionManager.esFavorit(p.getProducteId())) {
                    controller.marcarCorVermell(); // Deberás crear este método en el item controller
                }

                containerProductes.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
        
    
    private void configurarTaula() {
        // 1. Columnas básicas
        colNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        
        // 2. Columna Precio + Supermercado
        colPreu.setCellValueFactory(cellData -> {
            ProductePreusDTO p = cellData.getValue();
            Map<String, BigDecimal> precios = p.getPrecios();
            if (precios != null && !precios.isEmpty()) {
                Map.Entry<String, BigDecimal> minEntry = precios.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .min(Map.Entry.comparingByValue())
                    .orElse(null);
                if (minEntry != null) {
                    String texto = String.format("%.2f € (%s)", minEntry.getValue(), minEntry.getKey().toUpperCase());
                    return new SimpleStringProperty(texto);
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

        // boto mes (afegir
        colAnyadir.setCellFactory(param -> new TableCell<>() {
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

        ItemLlistaRequest nuevoItem = new ItemLlistaRequest();
        nuevoItem.setProductoId(p.getProducteId());
        nuevoItem.setProductoNombre(p.getNombre());
        nuevoItem.setCantidad(java.math.BigDecimal.ONE);
        
        // Pasem els preus per que es vegin a la llista
        nuevoItem.setPrecios(p.getPrecios());

        //  afegim directament a la llista del sessionmanager
        // Al cambiar de vista a CrearLlistaController, el initialize carregara aixo automaticament
        SessionManager.getLlistaTemporal().getItems().add(nuevoItem);

        // Feedback en consola
        System.out.println("Afegit ItemLlistaRequest: " + nuevoItem.getProductoNombre());
    }
}