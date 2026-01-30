package com.ivillas.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.ivillas.model.ProductePreusDTO;
import com.ivillas.model.UsuariDTO;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainController {

    @FXML private Label txtTitol; // CAMBIADO: JFXLabel -> Label
    @FXML private JFXButton btnUserSession;
    @FXML private JFXButton btnLogout;
    @FXML private JFXButton btnProductes;
    @FXML private TableView<ProductePreusDTO> tablaProductos;
    @FXML private TextField txtBuscador;
    
    private List<ProductePreusDTO> listaMaestra = new ArrayList<>();

    @FXML
    public void initialize() {
        // Ocultar tabla al inicio
        tablaProductos.setVisible(false);
        tablaProductos.setManaged(false);
    }

    @FXML
    private void handleBtnProductes() {
        txtTitol.setText("Catàleg de Productes");
        tablaProductos.setVisible(true);
        tablaProductos.setManaged(true);
        cargarProductosDesdeNAS();
    }

    
    private void cargarProductosDesdeNAS() {
        Task<List<ProductePreusDTO>> task = new Task<>() {
            @Override protected List<ProductePreusDTO> call() throws Exception {
                return ProducteServiceClient.getProductos(); // Tu método existente
            }
        };

        task.setOnSucceeded(e -> {
            listaMaestra = task.getValue();
            renderizarProductos(listaMaestra);
        });
        new Thread(task).start();
    }

    /*
    private void renderizarProductos(List<ProductePreusDTO> lista) {
        flowProductos.getChildren().clear();
        for (ProductePreusDTO p : lista) {
            flowProductos.getChildren().add(crearCard(p));
        }
    }
*/
    /*
    private void renderizarProductos(List<ProductePreusDTO> lista) {
        tablaProductos.getColumns().clear(); // Limpiar configuración previa

        // 1. Columna fija para el nombre del producto
        TableColumn<ProductePreusDTO, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        tablaProductos.getColumns().add(colNombre);

        // 2. Crear columnas dinámicas para cada Supermercado
        // Suponiendo que ProductePreusDTO tiene un Map<String, Double> precios;
        Set<String> supermercados = lista.stream()
            .flatMap(p -> p.getPrecios().keySet().stream())
            .collect(Collectors.toSet());

        for (String superm : supermercados) {
            TableColumn<ProductePreusDTO, String> colSuper = new TableColumn<>(superm);
            colSuper.setCellValueFactory(data -> {
                BigDecimal precio = data.getValue().getPrecios().get(superm);
                return new SimpleStringProperty(precio != null ? precio + " €" : "-");
            });
            tablaProductos.getColumns().add(colSuper);
        }

        // 3. Cargar los datos (Instantáneo gracias a la virtualización)
        tablaProductos.getItems().setAll(lista);
    }
    
       */
    private void renderizarProductos(List<ProductePreusDTO> lista) {
        this.listaMaestra = lista; // Actualizamos la lista para las columnas
        configurarColumnasDinamicas(); 
        tablaProductos.getItems().setAll(lista); 
    }
 
    
    
    @FXML
    private void handleSearch() {
        String texto = txtBuscador.getText().toLowerCase();
        List<ProductePreusDTO> filtrados = listaMaestra.stream()
            .filter(p -> p.nombre.toLowerCase().contains(texto) || p.marca.toLowerCase().contains(texto))
            .collect(Collectors.toList());
        renderizarProductos(filtrados);
    }


    @FXML
    private void openLoginWindow() throws IOException {
        // 1. Localizar el FXML
        URL fxmlLocation = getClass().getResource("/login.fxml");
        if (fxmlLocation == null) {
            throw new IOException("¡Error! No es troba login.fxml");
        }

        // 2. USAR EL CARGADOR DE FORMA NO ESTÁTICA
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load(); // Aquí se carga la vista

        // 3. LA MAGIA: Obtener el controlador del login y pasarle ESTE MainController (this)
        AuthController authController = loader.getController();
        authController.setMainController(this); 

        // 4. Configurar y mostrar la ventana
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Acces al compte");
        stage.initModality(Modality.APPLICATION_MODAL); 
        stage.show();
    }
    
    @FXML
    private void handleLogout() {
        SessionManager.logout();
        btnUserSession.setText("Login");
        btnLogout.setVisible(false);
        btnLogout.setManaged(false);
        tablaProductos.getItems().clear(); 
        tablaProductos.getColumns().clear(); 
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }
    
    private VBox crearCard(ProductePreusDTO p) {
        // 1. Crear el contenedor principal del cuadrito
        VBox card = new VBox(8); // 8 es el espaciado entre elementos
        card.setPrefWidth(220);
        card.setMinWidth(220);
        card.setMaxWidth(220);
        
        // Estilo visual (Sombra y bordes redondeados)
        card.setStyle("-fx-background-color: white; " +
                      "-fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                      "-fx-padding: 15;");

        // 2. Título (Nombre del producto)
        Label lblNombre = new Label(p.nombre != null ? p.nombre.toUpperCase() : "Producte sense nom");
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        lblNombre.setMinHeight(40); // Para que todos tengan el mismo alto de texto

        // 3. Subtítulo (Marca)
        Label lblMarca = new Label(p.marca != null ? p.marca : "Marca blanca");
        lblMarca.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        // 4. Contenedor de Precios
        VBox vboxPrecios = new VBox(4);
        vboxPrecios.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #EEE; -fx-border-width: 1 0 0 0;");
        
        if (p.precios != null && !p.precios.isEmpty()) {
            p.precios.forEach((supermercado, precio) -> {
                HBox filaPrecio = new HBox();
                Label lblSuper = new Label(supermercado + ": ");
                Label lblPrecio = new Label(String.format("%.2f €", precio.doubleValue()));
                
                lblSuper.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                lblPrecio.setStyle("-fx-font-weight: bold; -fx-text-fill: #6200EE; -fx-font-size: 11px;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS); // Empuja el precio a la derecha
                
                filaPrecio.getChildren().addAll(lblSuper, spacer, lblPrecio);
                vboxPrecios.getChildren().add(filaPrecio);
            });
        } else {
            vboxPrecios.getChildren().add(new Label("Sense preus disponobles"));
        }

        // 5. Unir todo
        card.getChildren().addAll(lblNombre, lblMarca, vboxPrecios);
        
        return card;
    }

    

    private void configurarColumnasDinamicas() {
        if (tablaProductos == null) return; // Seguridad extra

        tablaProductos.getColumns().clear();

        // 1. Columna Producto
        TableColumn<ProductePreusDTO, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nombre));
        tablaProductos.getColumns().add(colNombre);

        // 2. Columna Marca
        TableColumn<ProductePreusDTO, String> colMarca = new TableColumn<>("Marca");
        colMarca.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().marca));
        tablaProductos.getColumns().add(colMarca);

        // 3. Columnas Dinámicas por Supermercado (Asumiendo que listaMaestra ya tiene datos)
        if (!listaMaestra.isEmpty()) {
            Set<String> supers = listaMaestra.stream()
                .flatMap(p -> p.precios.keySet().stream())
                .collect(Collectors.toSet());

            for (String s : supers) {
                TableColumn<ProductePreusDTO, String> col = new TableColumn<>(s);
                col.setCellValueFactory(data -> {
                    BigDecimal precio = data.getValue().precios.get(s);
                    return new SimpleStringProperty(precio != null ? precio.toString() + " €" : "-");
                });
                tablaProductos.getColumns().add(col);
            }
        }
    }
    
    public void actualizarInterfazTrasLogin() {
        UsuariDTO user = SessionManager.getUsuario();
        if (user == null) return;

        // 1. Actualizamos botones laterales
        btnUserSession.setText(user.getUsername());
        btnLogout.setVisible(true);
        btnLogout.setManaged(true);

        // 2. Limpiamos el área principal (el HBox de las tarjetas pequeñas)
        tablaProductos.getItems().clear(); 
        tablaProductos.getColumns().clear(); 
        
        // OPCIONAL: Si quieres que ocupe todo el alto, 
        // asegúrate de que el padre de mainDisplayArea no tenga otros hijos estorbando
        
        // 3. Crear una "Super Card" grande
        VBox superCard = new VBox(20);
        superCard.setAlignment(Pos.TOP_LEFT);
        superCard.setPadding(new Insets(40));
        
        // Hacemos que la tarjeta se expanda
        superCard.setPrefWidth(800); 
        superCard.setMinWidth(600);
        superCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 8);");

        // Título con estilo moderno
        Label title = new Label("Perfil d'Usuari");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1E1E2E;");
        
        // Datos con iconos o etiquetas claras
        Label name = new Label("👤 Nom d'usuari:  " + user.getUsername());
        Label email = new Label("✉️ Correu electrònic:  " + user.getEmail());
        Label fecha = new Label("📅 Membre des de:  " + user.getDataCreacio());

        String estiloTexto = "-fx-font-size: 18px; -fx-text-fill: #444444;";
        name.setStyle(estiloTexto);
        email.setStyle(estiloTexto);
        fecha.setStyle(estiloTexto);

        // Añadir todo a la super card
        superCard.getChildren().addAll(title, new Separator(), name, email, fecha);
        
        // 4. Inyectar la super card en el área principal
        tablaProductos.getItems().clear(); 
        tablaProductos.getColumns().clear(); 
    }
    
}