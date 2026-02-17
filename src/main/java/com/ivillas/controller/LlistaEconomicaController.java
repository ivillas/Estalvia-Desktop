package com.ivillas.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.model.SupermercatDTO;
import com.ivillas.request.ItemLlistaRequest;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.service.SupermercatServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXCheckBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LlistaEconomicaController {

    @FXML private TableView<ItemLlistaRequest> tblComparativa;
    @FXML private TableColumn<ItemLlistaRequest, String> colProducto;
    @FXML private TableColumn<ItemLlistaRequest, String> colCantidad;
    @FXML private HBox hbCheckboxes;
    @FXML private VBox vbResultadosCalculo;
    @FXML private SplitPane splitPanePrincipal;

    private List<SupermercatDTO> listaSupers;
    private Map<String, TableColumn<ItemLlistaRequest, BigDecimal>> mapaColumnas = new HashMap<>();

    @FXML
    public void initialize() {
        configurarColumnasFijas();
        cargarDatosIniciales();
    }

    private void configurarColumnasFijas() {
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        
        // Formateador inteligente de unidades (2.000 -> 2, 2.55 -> 2.55)
        colCantidad.setCellValueFactory(data -> {
            BigDecimal cant = data.getValue().getCantidad();
            if (cant == null) return new SimpleStringProperty("");
            
            // stripTrailingZeros quita los ceros sobrantes, toPlainString evita la E de exponencial
            String limpio = cant.stripTrailingZeros().toPlainString();
            String unidad = data.getValue().getUnidad() != null ? data.getValue().getUnidad() : "";
            
            return new SimpleStringProperty(limpio + " " + unidad);
        });
    }

    private void cargarDatosIniciales() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                listaSupers = SupermercatServiceClient.getAll();
                // Simulación: Asignar precios a los items si no los tienen
                List<ProductePreusDTO> todos = ProducteServiceClient.getProductes();
                for (ItemLlistaRequest item : SessionManager.getLlistaTemporal().getItems()) {
                    todos.stream().filter(p -> p.producteId.equals(item.getProductoId()))
                         .findFirst().ifPresent(p -> item.setPrecios(p.precios));
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            generarInterfazDinamica();
            tblComparativa.getItems().setAll(SessionManager.getLlistaTemporal().getItems());
        });
        new Thread(task).start();
    }

    private void generarInterfazDinamica() {
        hbCheckboxes.getChildren().clear();
        for (SupermercatDTO s : listaSupers) {
            String idKey = s.getNom().toLowerCase().replace(" ", "").trim();
            
            // 1. Crear Columna
            TableColumn<ItemLlistaRequest, BigDecimal> col = new TableColumn<>(s.getNom());
            col.setPrefWidth(100);
            col.setCellValueFactory(data -> {
                Map<String, BigDecimal> p = data.getValue().getPrecios();
                return new SimpleObjectProperty<>(p != null ? p.get(idKey) : null);
            });

            // CellFactory para el color verde
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText("-"); setStyle(""); }
                    else {
                        setText(item + "€");
                        if (esElMasBarato(item, getTableRow().getItem())) 
                            setStyle("-fx-background-color: #d4edda; -fx-font-weight: bold;");
                        else setStyle("");
                    }
                }
            });

            mapaColumnas.put(idKey, col);

            // 2. Crear Checkbox
            JFXCheckBox cb = new JFXCheckBox(s.getNom());
            cb.setSelected(s.isActiu());
            actualizarVisibilidadColumna(idKey, s.isActiu());

            cb.selectedProperty().addListener((obs, oldV, newV) -> actualizarVisibilidadColumna(idKey, newV));
            hbCheckboxes.getChildren().add(cb);
        }
    }

    private void actualizarVisibilidadColumna(String idKey, boolean visible) {
        TableColumn col = mapaColumnas.get(idKey);
        if (visible) {
            if (!tblComparativa.getColumns().contains(col)) tblComparativa.getColumns().add(col);
        } else {
            tblComparativa.getColumns().remove(col);
        }
    }

    private boolean esElMasBarato(BigDecimal precio, ItemLlistaRequest item) {
        if (item == null || item.getPrecios() == null) return false;
        return item.getPrecios().values().stream().filter(Objects::nonNull)
                   .min(BigDecimal::compareTo).map(m -> m.compareTo(precio) == 0).orElse(false);
    }

    @FXML
    private void calcularEstalvi() {
        splitPanePrincipal.setDividerPositions(0.3);
        vbResultadosCalculo.getChildren().clear();

        Map<String, List<String>> grupos = new HashMap<>();
        Map<String, BigDecimal> totalesPorSuper = new HashMap<>();
        List<String> productosNoEncontrados = new ArrayList<>(); // <-- PRODUCTOS HUÉRFANOS

        BigDecimal totalOptimizado = BigDecimal.ZERO;
        BigDecimal totalCaro = BigDecimal.ZERO;

        for (ItemLlistaRequest item : tblComparativa.getItems()) {
            String mejorSuper = null;
            BigDecimal precioMin = null;
            BigDecimal precioMax = null;

            if (item.getPrecios() == null) {
                productosNoEncontrados.add(item.getProductoNombre());
                continue;
            }

            // Solo iteramos por los supermercados CUYAS COLUMNAS ESTÁN VISIBLES
            for (SupermercatDTO s : listaSupers) {
                String idKey = s.getNom().toLowerCase().replace(" ", "").trim();
                TableColumn col = mapaColumnas.get(idKey);
                
                if (tblComparativa.getColumns().contains(col)) {
                    BigDecimal p = item.getPrecios().get(idKey);
                    if (p != null) {
                        if (precioMin == null || p.compareTo(precioMin) < 0) {
                            precioMin = p; mejorSuper = s.getNom();
                        }
                        if (precioMax == null || p.compareTo(precioMax) > 0) {
                            precioMax = p;
                        }
                    }
                }
            }

            if (mejorSuper != null) {
                BigDecimal cant = item.getCantidad();
                String cantLimpia = cant.stripTrailingZeros().toPlainString();
                
                BigDecimal subtotal = precioMin.multiply(cant);
                grupos.computeIfAbsent(mejorSuper, k -> new ArrayList<>())
                      .add(item.getProductoNombre() + " x " + cantLimpia);
                
                totalesPorSuper.put(mejorSuper, totalesPorSuper.getOrDefault(mejorSuper, BigDecimal.ZERO).add(subtotal));
                totalOptimizado = totalOptimizado.add(subtotal);
                totalCaro = totalCaro.add(precioMax.multiply(cant));
            } else {
                // Si el producto existe pero no en los supers seleccionados
                productosNoEncontrados.add(item.getProductoNombre());
            }
        }

        // 1. Mostrar Banner de Ahorro
        mostrarResumenAhorro(totalCaro, totalOptimizado);

        // 2. Mostrar Listas por Supermercado
        totalesPorSuper.forEach((name, total) -> {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 10;");
            card.getChildren().add(new Label("--- " + name.toUpperCase() + " ---"));
            grupos.get(name).forEach(linea -> card.getChildren().add(new Label(linea)));
            Label lblTotal = new Label("Total: " + total.setScale(2, RoundingMode.HALF_UP) + "€");
            lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");
            card.getChildren().add(lblTotal);
            vbResultadosCalculo.getChildren().add(card);
        });

        // 3. Mostrar Aviso de Productos No Encontrados
        if (!productosNoEncontrados.isEmpty()) {
            VBox errorBox = new VBox(5);
            errorBox.setStyle("-fx-background-color: #fce4ec; -fx-padding: 10; -fx-border-color: #f06292; -fx-border-radius: 10;");
            Label titleError = new Label("ATENCIÓ: Productes no trobats als supers seleccionats:");
            titleError.setStyle("-fx-text-fill: #c2185b; -fx-font-weight: bold;");
            errorBox.getChildren().add(titleError);
            productosNoEncontrados.forEach(p -> errorBox.getChildren().add(new Label("• " + p)));
            vbResultadosCalculo.getChildren().add(errorBox);
        }
    }

    
    private void mostrarResumenAhorro(BigDecimal totalCaro, BigDecimal totalOptimo) {
        BigDecimal ahorro = totalCaro.subtract(totalOptimo);
        
        VBox banner = new VBox(5);
        banner.setAlignment(Pos.CENTER);
        banner.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 10; -fx-padding: 15;");
        
        Label lblAhorro = new Label("ESTALVI ACONSEGUIT: " + ahorro.setScale(2, RoundingMode.HALF_UP) + " €");
        lblAhorro.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Label lblComparativa = new Label("Preu optimitzat: " + totalOptimo.setScale(2, RoundingMode.HALF_UP) + 
                                         "€ (en lloc de " + totalCaro.setScale(2, RoundingMode.HALF_UP) + "€)");
        lblComparativa.setStyle("-fx-text-fill: #e8f5e9; -fx-font-size: 14px;");
        
        banner.getChildren().addAll(lblAhorro, lblComparativa);
        vbResultadosCalculo.getChildren().add(banner);
    }

    @FXML
    private void volverAtras(ActionEvent event) {
        MainController.getInstance().openCrearLlista();
    }
    
    
    
    @FXML
    private void guardarArchivo() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File (.txt)", "*.txt"));
        File file = fc.showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file)) {
            // Obtenemos el texto del vbResultadosCalculo o lo regeneramos
            for (Node node : vbResultadosCalculo.getChildren()) {
                if (node instanceof VBox) {
                    ((VBox) node).getChildren().forEach(n -> {
                        if (n instanceof Label) out.println(((Label) n).getText());
                    });
                    out.println(); // Espacio entre supermercados
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void enviarEmail() {
        enviarPorProtocolo("mailto:?subject=Llista%20Compra%20Economica&body=");
    }

    private void enviarPorProtocolo(String baseUri) {
        try {
            StringBuilder sb = new StringBuilder("Resum de la meva compra optimitzada:\n\n");
            for (Node node : vbResultadosCalculo.getChildren()) {
                if (node instanceof VBox) {
                    ((VBox) node).getChildren().forEach(n -> {
                        if (n instanceof Label) sb.append(((Label) n).getText()).append("\n");
                    });
                    sb.append("\n");
                }
            }
            
            // Codificamos para URL (importante para que aparezca el cuerpo del mensaje)
            String encodedBody = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8).replace("+", "%20");
            Desktop.getDesktop().browse(new URI(baseUri + encodedBody));
        } catch (Exception e) { e.printStackTrace(); }
    }
      
    @FXML
    private void enviarWhatsApp() {
        // Protocolo de WhatsApp (requiere número o se deja vacío para elegir contacto)
        enviarPorProtocolo("https://wa.me");
    }

    @FXML
    private void enviarTelegram() {
        // Protocolo de Telegram
        enviarPorProtocolo("https://t.me");
    }
    
}