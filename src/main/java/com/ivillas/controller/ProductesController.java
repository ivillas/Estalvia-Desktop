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
import javafx.scene.control.TableRow;
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
    
    private List<ProductePreusDTO> llistaProductes = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // Para el LocalDateTime
    
    // Añade este método para que el MainController pueda "registrarse"
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }   
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarCheckboxes();
        
        // Si no está logeado, ocultamos la opción de favoritos
        if (!SessionManager.isLoggedIn()) {
            ckbFavorit.setVisible(false);
            colAccions.setVisible(false); // Oculta la columna de los corazones en la tabla
        }        
        carregarDades();
    }
        
    

    private void configurarCheckboxes() {
        // Exclusividad entre Tarjetas y Lista
        chbTarget.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) { chbLlista.setSelected(false); renderitzarUI(); }
        });
        chbLlista.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) { chbTarget.setSelected(false); renderitzarUI(); }
        });
        // Filtro favoritos
        ckbFavorit.selectedProperty().addListener((obs, oldV, newV) -> renderitzarUI());
        
        chbTarget.setSelected(true); // Estado inicial
    }

    private void carregarDades() {
        Task<List<ProductePreusDTO>> task = new Task<>() {
            @Override
            protected List<ProductePreusDTO> call() throws Exception {
                // Usamos tu HttpClientProvider para obtener la URL y el Cliente
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
            lblTotalProductes.setText("Total: " + llistaProductes.size() + " productes"); // <--- Añadir esto
            renderitzarUI();
        });
        
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    private void renderitzarUI() {
        // Filtramos la lista según el checkbox de favoritos
        List<ProductePreusDTO> listaFiltrada;
        if (ckbFavorit.isSelected()) {
            listaFiltrada = llistaProductes.stream()
                    .filter(p -> SessionManager.esFavorito(p.getProducteId()))
                    .collect(Collectors.toList());
        } else {
            listaFiltrada = llistaProductes;
        }

        if (chbLlista.isSelected()) {
            scrollTargetes.setVisible(false);
            tablaProductes.setVisible(true);
            tablaProductes.setItems(FXCollections.observableArrayList(listaFiltrada));
        } else {
            tablaProductes.setVisible(false);
            scrollTargetes.setVisible(true);
            cargarTarjetasDinamicasFiltradas(listaFiltrada);
        }
    }
    
    private void abrirDetallePopup(ProductePreusDTO p) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Especificacions: " + p.getNombre());
        alert.setHeaderText(p.getNombre().toUpperCase());

        // Construcción del contenido siguiendo tu lógica de filtros
        StringBuilder sb = new StringBuilder();
        sb.append("Marca: ").append(p.getMarca() != null ? p.getMarca() : "N/A").append("\n");
        sb.append("Envàs: ").append(p.getEnvase() != null ? p.getEnvase() : "N/A").append("\n");
        sb.append("Última actualització: ").append(p.getLastUpdate()).append("\n\n");
        
        sb.append("COMPARATIVA DE PREUS ACTIUS:\n");
        sb.append("------------------------------------------\n");

        if (p.getPrecios() != null && !p.getPrecios().isEmpty()) {
            p.getPrecios().forEach((superNombre, precio) -> {
                // Usamos tu lógica de configuración para filtrar
                if (SupermercatServiceClient.getLocalStatus(superNombre)) {
                    sb.append(String.format(" • %-15s : %s €\n", superNombre.toUpperCase(), precio));
                }
            });
        } else {
            sb.append("No hi ha preus disponibles actualment.");
        }

        // Aplicar un estilo básico para que no se vea el Alert de Windows nativo tan seco
        alert.getDialogPane().setPrefWidth(450);
        alert.setContentText(sb.toString());
        
        // Si quieres que se abra al clicar en la tabla o en la tarjeta, llama a este método
        alert.showAndWait();
    }
    
    private void cargarTarjetasDinamicasFiltradas(List<ProductePreusDTO> lista) {
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
                    controller.ocultarCorazon(); 
                } else if (SessionManager.esFavorito(p.getProducteId())) {
                    controller.marcarCorazonRojo(); // Deberás crear este método en el item controller
                }

                containerProductes.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
        
    
    private void configurarTabla() {
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

        // 3. COLUMNA ACCIONS (CORAZÓN FAVORITOS)
        colAccions.setCellFactory(param -> new TableCell<>() {
            private final Label iconFavorit = new Label("❤");
            {
                iconFavorit.setCursor(Cursor.HAND);
                iconFavorit.setOnMouseClicked(event -> {
                    ProductePreusDTO p = getTableView().getItems().get(getIndex());
                    if (p != null) gestionarFavorito(p, iconFavorit);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    ProductePreusDTO p = getTableView().getItems().get(getIndex());
                    if (SessionManager.esFavorito(p.getProducteId())) {
                        iconFavorit.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 20px;");
                    } else {
                        iconFavorit.setStyle("-fx-text-fill: #ccc; -fx-font-size: 20px;");
                    }
                    setGraphic(iconFavorit);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // 4. COLUMNA ANYADIR (BOTÓN MÁS)
        colAnyadir.setCellFactory(param -> new TableCell<>() {
            private final Button btnPlus = new Button("＋");
            {
                btnPlus.setStyle("-fx-background-color: #79EDED; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnPlus.setCursor(Cursor.HAND);
                btnPlus.setOnAction(event -> {
                    ProductePreusDTO p = getTableView().getItems().get(getIndex());
                    if (p != null) afegirALlista(p); // Usamos tu método que ya funciona
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

    
    private void gestionarFavorito(ProductePreusDTO p, Label icon) {
        if (!SessionManager.isLoggedIn()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sessió necessària");
            alert.setContentText("Has d'estar loguejat per afegir productes a preferits.");
            alert.showAndWait();
            return;
        }

        Long userId = SessionManager.getUsuario().getUserId();
        Long prodId = p.getProducteId();
        boolean yaEsFavorito = SessionManager.esFavorito(prodId);

        // Hilo secundario para no congelar la UI
        new Thread(() -> {
            try {
                // true para añadir, false para quitar
                boolean exito = ProducteServiceClient.gestionarFavoritoAPI(userId, prodId, !yaEsFavorito);
                
                if (exito) {
                    // Actualizamos la memoria del SessionManager
                    if (yaEsFavorito) SessionManager.getIdsFavoritos().remove(prodId);
                    else SessionManager.getIdsFavoritos().add(prodId);

                    // Refrescamos color en el hilo de JavaFX
                    Platform.runLater(() -> {
                        // Si estamos filtrando por favoritos, debemos refrescar la UI
                        if (ckbFavorit.isSelected()) {
                            renderitzarUI(); // Esto volverá a filtrar y el producto desaparecerá
                        } else {
                            // Si no estamos filtrando, solo cambiamos el color del corazón
                            if (yaEsFavorito) icon.setStyle("-fx-text-fill: #ccc;");
                            else icon.setStyle("-fx-text-fill: #e74c3c;");
                        }
                        
                        // Actualizar contador del perfil si es necesario
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
            // Alerta simple para no fallar
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Has de iniciar sessió.");
            alert.showAndWait();
            return;
        }

        // 1. Creamos el objeto que YA usas en tu CrearLlistaController
        ItemLlistaRequest nuevoItem = new ItemLlistaRequest();
        nuevoItem.setProductoId(p.getProducteId());
        nuevoItem.setProductoNombre(p.getNombre());
        nuevoItem.setCantidad(java.math.BigDecimal.ONE);
        
        // 2. Pasamos los precios para que se vean en la lista visual (como tienes en tu cellFactory)
        nuevoItem.setPrecios(p.getPrecios());

        // 3. Lo añadimos directamente a la lista del SessionManager
        // Al cambiar de vista a CrearLlistaController, el initialize cargará esto automáticamente
        SessionManager.getListaTemporal().getItems().add(nuevoItem);

        // 4. Feedback en consola
        System.out.println("Afegit ItemLlistaRequest: " + nuevoItem.getProductoNombre());
    }

}
	
	/*
	
	@FXML private Label txtTitol; // Etiqueta per mostrar el títol
    @FXML private TableView<ProductePreusDTO> tablaProductos; // Taula per mostrar els productes
    @FXML private TextField txtBuscador; // Camp de text per buscar productes
    private List<ProductePreusDTO> listaMaestra = new ArrayList<>(); // Llista mestre de productes
    private MainController mainController; // Controlador principal

    // Mètode per establir el controlador principal
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
	
    // Mètode que gestiona la visualització dels productes
    @FXML
    private void handleBtnProductes() {
        txtTitol.setText("Catàleg de Productes"); // Estableix el títol de la vista
        tablaProductos.setVisible(true); // Fa visible la taula de productes
        tablaProductos.setManaged(true); // Gestiona la visibilitat de la taula
        cargarProductosDesdeNAS(); // Carrega els productes des d'una font externa
    }

    // Mètode per carregar productes des d'una font externa
    private void cargarProductosDesdeNAS() {
        Task<List<ProductePreusDTO>> task = new Task<>() {
            @Override protected List<ProductePreusDTO> call() throws Exception {
                return ProducteServiceClient.getProductos(); // Crida al servei per obtenir productes
            }
        };

        // Acció a realitzar quan la tasca s'ha completat amb èxit
        task.setOnSucceeded(e -> {
            listaMaestra = task.getValue(); // Actualitza la llista mestre amb els productes obtinguts
            renderizarProductos(listaMaestra); // Renderitza els productes a la taula
        });
        new Thread(task).start(); // Executa la tasca en un nou fil
    }

    // Mètode per renderitzar els productes a la taula
    private void renderizarProductos(List<ProductePreusDTO> lista) {
        this.listaMaestra = lista; // Actualitza la llista per les columnes
        configurarColumnasDinamicas(); // Configura les columnes dinàmiques de la taula
        tablaProductos.getItems().setAll(lista); // Estableix els elements de la taula
    }

    // Mètode per gestionar la cerca de productes
    @FXML
    private void handleSearch() {
        String texto = txtBuscador.getText().toLowerCase(); // Obté el text del cercador
        List<ProductePreusDTO> filtrados = listaMaestra.stream()
            .filter(p -> p.nombre.toLowerCase().contains(texto) || p.marca.toLowerCase().contains(texto)) // Filtra els productes
            .collect(Collectors.toList());
        renderizarProductos(filtrados); // Renderitza els productes filtrats
    }
	
    // Mètode per configurar les columnes dinàmiques de la taula
    private void configurarColumnasDinamicas() {
        if (tablaProductos == null) return; // Comprova si la taula és nul·la

        tablaProductos.getColumns().clear(); // Neteja les columnes existents

        // 1. Columna Producto
        TableColumn<ProductePreusDTO, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nombre)); // Estableix el valor de la cel·la
        tablaProductos.getColumns().add(colNombre); // Afegeix la columna a la taula

        // 2. Columna Marca
        TableColumn<ProductePreusDTO, String> colMarca = new TableColumn<>("Marca");
        colMarca.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().marca)); // Estableix el valor de la cel·la
        tablaProductos.getColumns().add(colMarca); // Afegeix la columna a la taula

        // 3. Columnes Dinàmiques per Supermercat (Assegurant que la llista mestre ja té dades)
        if (!listaMaestra.isEmpty()) {
            Set<String> supers = listaMaestra.stream()
                .flatMap(p -> p.precios.keySet().stream()) // Obté els noms dels supermercats
                .collect(Collectors.toSet());

            for (String s : supers) {
                TableColumn<ProductePreusDTO, String> col = new TableColumn<>(s);
                col.setCellValueFactory(data -> {
                    BigDecimal precio = data.getValue().precios.get(s); // Obté el preu del supermercat
                    return new SimpleStringProperty(precio != null ? precio.toString() + " €" : "-"); // Estableix el valor de la cel·la
                });
                tablaProductos.getColumns().add(col); // Afegeix la columna a la taula
            }
        }
    }

    // Mètode per crear una targeta visual per a cada producte
    private VBox crearCard(ProductePreusDTO p) {
        // 1. Crear el contenidor principal del cuadrito
        VBox card = new VBox(8); // 8 és l'espai entre elements
        card.setPrefWidth(220);
        card.setMinWidth(220);
        card.setMaxWidth(220);
        
        // Estil visual (Sombra i vores arrodonides)
        card.setStyle("-fx-background-color: white; " +
                      "-fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 5, 0); " +
                      "-fx-padding: 15;");

        // 2. Títol (Nom del producte)
        Label lblNombre = new Label(p.nombre != null ? p.nombre.toUpperCase() : "Producte sense nom");
        lblNombre.setWrapText(true); // Permet el salt de línia
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        lblNombre.setMinHeight(40); // Perquè tots tinguin la mateixa alçada de text

        // 3. Subtítol (Marca)
        Label lblMarca = new Label(p.marca != null ? p.marca : "Marca blanca");
        lblMarca.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        // 4. Contenidor de Preus
        VBox vboxPrecios = new VBox(4);
        vboxPrecios.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #EEE; -fx-border-width: 1 0 0 0;");
        
        if (p.precios != null && !p.precios.isEmpty()) {
            p.precios.forEach((supermercado, precio) -> {
                HBox filaPrecio = new HBox();
                Label lblSuper = new Label(supermercado + ": ");
                Label lblPrecio = new Label(String.format("%.2f €", precio.doubleValue())); // Format del preu
                
                lblSuper.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                lblPrecio.setStyle("-fx-font-weight: bold; -fx-text-fill: #6200EE; -fx-font-size: 11px;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS); // Empuja el preu a la dreta
                
                filaPrecio.getChildren().addAll(lblSuper, spacer, lblPrecio); // Afegeix els elements a la fila
                vboxPrecios.getChildren().add(filaPrecio); // Afegeix la fila al contenidor de preus
            });
        } else {
            vboxPrecios.getChildren().add(new Label("Sense preus disponibles")); // Missatge si no hi ha preus
        }

        // 5. Unir tot
        card.getChildren().addAll(lblNombre, lblMarca, vboxPrecios); // Afegeix tots els elements a la targeta
        
        return card; // Retorna la targeta creada
    }
}*/
