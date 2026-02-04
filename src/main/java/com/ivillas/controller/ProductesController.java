package com.ivillas.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.service.ProducteServiceClient;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ProductesController {
	
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
}
