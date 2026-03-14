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


/**
 * Clase controladora de la llista economica
 */

public class LlistaEconomicaController {

    @FXML private TableView<ItemLlistaRequest> tblComparativa;
    @FXML private TableColumn<ItemLlistaRequest, String> colProducte;
    @FXML private TableColumn<ItemLlistaRequest, String> colQuantitat;
    @FXML private HBox hbCheckboxes;
    @FXML private VBox vbResultatCalcul;
    @FXML private SplitPane splitPanePrincipal;

    private List<SupermercatDTO> llistaSupers;
    private Map<String, TableColumn<ItemLlistaRequest, BigDecimal>> mapaColumnes = new HashMap<>();

    /**
     * Inicialitza el controlador, configura les columnes i gestiona l'estat visual
     * de la taula mentre es carreguen les dades.
     */
    @FXML
    public void initialize() {
        // Estableix un missatge personalitzat mentre la taula no té dades
        Label placeholder = new Label("Preparant la comparativa de preus...");
        placeholder.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
        tblComparativa.setPlaceholder(placeholder);

        configurarColumnesFixes();
        carregarDadesInicials();
    }

    /**
     * MEtode per configurar les columne superirors amb la llista de productes
     */
    
    private void configurarColumnesFixes() {
        colProducte.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        
        // Formategem les unitats (2.000 -> 2, 2.55 -> 2.55)
        colQuantitat.setCellValueFactory(data -> {
            BigDecimal cant = data.getValue().getCantidad();
            if (cant == null) return new SimpleStringProperty("");
            
            // stripTrailingZerostreu els ceros sobrants, toPlainString evita la E de exponencial
            String limpio = cant.stripTrailingZeros().toPlainString();
            String unidad = data.getValue().getUnidad() != null ? data.getValue().getUnidad() : "";
            
            return new SimpleStringProperty(limpio + " " + unidad);
        });
    }
    
    /**
     * Metode per carregar les dades inicials
     */

    private void carregarDadesInicials() {
    	
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                llistaSupers = SupermercatServiceClient.getAll();
                // Simulació: Asignar preus als items si no en tenen
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
    
    /**
     * Metode que genera la interface dinamica per els cambis que fa l'usuari
     * al clicka mes supers...
     */

    private void generarInterfazDinamica() {
        hbCheckboxes.getChildren().clear();
        for (SupermercatDTO s : llistaSupers) {
            String idKey = s.getNom().toLowerCase().replace(" ", "").trim();
            
            // Crear columna
            TableColumn<ItemLlistaRequest, BigDecimal> col = new TableColumn<>(s.getNom());
            col.setPrefWidth(100);
            col.setCellValueFactory(data -> {
                Map<String, BigDecimal> p = data.getValue().getPrecios();
                return new SimpleObjectProperty<>(p != null ? p.get(idKey) : null);
            });

            // CellFactory per color verd
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText("-"); setStyle(""); }
                    else {
                        setText(item + "€");
                        if (esElMesBarat(item, getTableRow().getItem())) 
                            setStyle("-fx-background-color: #d4edda; -fx-font-weight: bold;");
                        else setStyle("");
                    }
                }
            });

            mapaColumnes.put(idKey, col);

            // Crear Checkbox
            JFXCheckBox cb = new JFXCheckBox(s.getNom());
            cb.setSelected(s.isActiu());
            actualizarVisibilidadColumna(idKey, s.isActiu());

            cb.selectedProperty().addListener((obs, oldV, newV) -> actualizarVisibilidadColumna(idKey, newV));
            hbCheckboxes.getChildren().add(cb);
        }
    }

    /**
     * Actualitza la visibilitat de les columnes dels preus-supers
     * @param idKey
     * @param visible
     */
    private void actualizarVisibilidadColumna(String idKey, boolean visible) {
        TableColumn col = mapaColumnes.get(idKey);
        if (visible) {
            if (!tblComparativa.getColumns().contains(col)) tblComparativa.getColumns().add(col);
        } else {
            tblComparativa.getColumns().remove(col);
        }
    }

    /**
     * Busca el preu mes baix del producte
     * @param preu
     * @param item
     * @return
     */
    
    private boolean esElMesBarat(BigDecimal preu, ItemLlistaRequest item) {
        if (item == null || item.getPrecios() == null) return false;
        return item.getPrecios().values().stream().filter(Objects::nonNull)
                   .min(BigDecimal::compareTo).map(m -> m.compareTo(preu) == 0).orElse(false);
    }

    /**
     * Metode que calcula l'estalvi aconsegit (Sempre comparant amb el mes alt)
     */
    @FXML
    private void calcularEstalvi() {
        splitPanePrincipal.setDividerPositions(0.3);
        vbResultatCalcul.getChildren().clear();

        Map<String, List<String>> grups = new HashMap<>();
        Map<String, BigDecimal> totalsPerSuper = new HashMap<>();
        List<String> productesNoTrobats = new ArrayList<>(); // productes que no s'han trobat en els supers

        BigDecimal totalOptimitzat = BigDecimal.ZERO;
        BigDecimal totalCar = BigDecimal.ZERO;

        for (ItemLlistaRequest item : tblComparativa.getItems()) {
            String millorSuper = null;
            BigDecimal preuMin = null;
            BigDecimal preuMax = null;

            if (item.getPrecios() == null) {
                productesNoTrobats.add(item.getProductoNombre());
                continue;
            }

            // nomes iterem pels supers que son visibles
            for (SupermercatDTO s : llistaSupers) {
                String idKey = s.getNom().toLowerCase().replace(" ", "").trim();
                TableColumn col = mapaColumnes.get(idKey);
                
                if (tblComparativa.getColumns().contains(col)) {
                    BigDecimal p = item.getPrecios().get(idKey);
                    if (p != null) {
                        if (preuMin == null || p.compareTo(preuMin) < 0) {
                            preuMin = p; millorSuper = s.getNom();
                        }
                        if (preuMax == null || p.compareTo(preuMax) > 0) {
                            preuMax = p;
                        }
                    }
                }
            }

            if (millorSuper != null) {
                BigDecimal cant = item.getCantidad();
                String quantitatNeta = cant.stripTrailingZeros().toPlainString();
                
                BigDecimal subtotal = preuMin.multiply(cant);
                grups.computeIfAbsent(millorSuper, k -> new ArrayList<>())
                      .add(item.getProductoNombre() + " x " + quantitatNeta + " unitats");
                
                totalsPerSuper.put(millorSuper, totalsPerSuper.getOrDefault(millorSuper, BigDecimal.ZERO).add(subtotal));
                totalOptimitzat = totalOptimitzat.add(subtotal);
                totalCar = totalCar.add(preuMax.multiply(cant));
            } else {
                // si el producte existeix pero no s'ha trobat en els supers
                productesNoTrobats.add(item.getProductoNombre());
            }
        }

        // Mostrar l'estalvi
        mostrarResumEstalvi(totalCar, totalOptimitzat);

        // mostrar les llistes per supermercat
        totalsPerSuper.forEach((name, total) -> {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 10;");
            card.getChildren().add(new Label("--- " + name.toUpperCase() + " ---"));
            grups.get(name).forEach(linea -> card.getChildren().add(new Label(linea)));
            Label lblTotal = new Label("Total: " + total.setScale(2, RoundingMode.HALF_UP) + "€");
            lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");
            card.getChildren().add(lblTotal);
            vbResultatCalcul.getChildren().add(card);
        });

        // mostrar avis productes no trobats
        if (!productesNoTrobats.isEmpty()) {
            VBox errorBox = new VBox(5);
            errorBox.setStyle("-fx-background-color: #fce4ec; -fx-padding: 10; -fx-border-color: #f06292; -fx-border-radius: 10;");
            Label titleError = new Label("ATENCIÓ: Productes no trobats als supers seleccionats:");
            titleError.setStyle("-fx-text-fill: #c2185b; -fx-font-weight: bold;");
            errorBox.getChildren().add(titleError);
            productesNoTrobats.forEach(p -> errorBox.getChildren().add(new Label("• " + p)));
            vbResultatCalcul.getChildren().add(errorBox);
        }
    }

    /**
     * Metode que mostrara l'estalvi aconseguit
     * @param totalCaro
     * @param totalOptim
     */
    private void mostrarResumEstalvi(BigDecimal totalCaro, BigDecimal totalOptim) {
        BigDecimal ahorro = totalCaro.subtract(totalOptim);
        
        VBox banner = new VBox(5);
        banner.setAlignment(Pos.CENTER);
        banner.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 10; -fx-padding: 15;");
        
        Label lblAhorro = new Label("ESTALVI ACONSEGUIT: " + ahorro.setScale(2, RoundingMode.HALF_UP) + " €");
        lblAhorro.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Label lblComparativa = new Label("Preu optimitzat: " + totalOptim.setScale(2, RoundingMode.HALF_UP) + 
                                         "€ (en lloc de " + totalCaro.setScale(2, RoundingMode.HALF_UP) + "€)");
        lblComparativa.setStyle("-fx-text-fill: #e8f5e9; -fx-font-size: 14px;");
        
        banner.getChildren().addAll(lblAhorro, lblComparativa);
        vbResultatCalcul.getChildren().add(banner);
    }
    
    /**
     * Metode per tornar enrere d ela vista
     * @param event
     */

    @FXML
    private void volverAtras(ActionEvent event) {
        MainController.getInstance().openCrearLlista();
    }
    
    /**
     * Metode per guardar en un archiu la llista 
     * Segons el que hem elegit
     */
    
    @FXML
    private void guardarArchiu() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File (.txt)", "*.txt"));
        File file = fc.showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file)) {
            // Obtenim el vdResultatCalcul 
            for (Node node : vbResultatCalcul.getChildren()) {
                if (node instanceof VBox) {
                    ((VBox) node).getChildren().forEach(n -> {
                        if (n instanceof Label) out.println(((Label) n).getText());
                    });
                    out.println(); // espai entre supermercats
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * MEtode per enviar per email
     * 
     */
    @FXML
    private void enviarEmail() {
        enviarPorProtocolo("mailto:?subject=Llista%20Compra%20Economica&body=");
    }

    /**
     * 
     * @param baseUri
     */
    private void enviarPorProtocolo(String baseUri) {
        try {
            StringBuilder sb = new StringBuilder("Resum de la meva compra optimitzada:\n\n");
            for (Node node : vbResultatCalcul.getChildren()) {
                if (node instanceof VBox) {
                    ((VBox) node).getChildren().forEach(n -> {
                        if (n instanceof Label) sb.append(((Label) n).getText()).append("\n");
                    });
                    sb.append("\n");
                }
            }
            
            // Codifiquem per URL (importantper que aparegui el cos del missatge)
            String encodedBody = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8).replace("+", "%20");
            Desktop.getDesktop().browse(new URI(baseUri + encodedBody));
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    /**
     * Metode per encviar per watsapp
     * Atraves de la web
     */
      
    @FXML
    private void enviarWhatsApp() {
        // requereix numero o es deixa buit per buscar contacte
        enviarPorProtocolo("https://wa.me");
    }

    /**
     * Metode per enviar per letegram
     * Atraves de la web
     */
    @FXML
    private void enviarTelegram() {
        // Protocol de Telegram
        enviarPorProtocolo("https://t.me");
    }
    
}