package com.ivillas.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ivillas.model.LlistaDTO;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.utils.SessionManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Clase controladora de les Llistes publiques
 */

public class LlistesPubliquesController {

    @FXML private FlowPane contenedorTargetes;
    private MainController mainController;
    private static LlistesPubliquesController instance;

    private List<LlistaDTO> totesLesLlistesCache = new ArrayList<>();
    
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    
    @FXML
    public void initialize() {
    	instance = this;

        if (SessionManager.getMainController() != null) {
            SessionManager.getMainController().actualizarModeBusqueda("LLISTES");
        }
    	
        String query = SessionManager.getultimaBusqueda();
        
        if (query != null && !query.isEmpty()) {
            System.out.println("Buscant llista: " + query);
            
            // Aquí cridem al  mètode de càrrega passant-li el filtre
            cargarListasPubliquesFiltradas(query); 
            
            SessionManager.setultimaBusqueda(null); 
        } else {
            carregarLlistesPubliques();
        }

    }
   
    /*
     * Creem una instancia del controlador
     */
    public static LlistesPubliquesController getInstance() { 
    	return instance; 
    }
    
    /**
     * Metode per carregar les llistes privades
     * @param query
     */
    
    public void cargarListasPubliquesFiltradas(String query) {
        Task<List<LlistaDTO>> task = new Task<>() {
            @Override
            protected List<LlistaDTO> call() throws Exception {
                // cridem a la api per demanar les llistes publiques
                return LlistaServiceClient.getPubliques(); 
            }
        };

        task.setOnSucceeded(e -> {
            List<LlistaDTO> totes = task.getValue();
            this.totesLesLlistesCache = totes; 

            List<LlistaDTO> filtrades = totes.stream()
                .filter(l -> l.getNombre().toLowerCase().contains(query.toLowerCase()))
                .toList();

            llenarInterfaz(filtrades); 
        });

        task.setOnFailed(e -> {
            System.err.println("Error en la cerca: " + task.getException().getMessage());
        });

        Thread th = new Thread(task);
        th.setDaemon(true); 
        th.start();
    }
    
    
    public void carregarLlistesPubliques() {
        // creem una tasca en segon pla
        Task<List<LlistaDTO>> task = new Task<>() {
            @Override
            protected List<LlistaDTO> call() throws Exception {
                // aixo sexecutara fora del fil de la UI aixi evitem bloquejar
                return LlistaServiceClient.getPubliques(); 
            }
        };
        
       
        task.setOnSucceeded(e -> {
            List<LlistaDTO> publiques = task.getValue();
            this.totesLesLlistesCache = publiques; // guardme el cache
            llenarInterfaz(publiques);           // les mostrem
        });

      //Per si volem fer algo al fallar (error de red)
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            
        });

        // arranquem la tasca en un fil nou
        Thread th = new Thread(task);
        th.setDaemon(true); 
        th.start();
    }
    
    /**
     * metode per anar filtrant segons el buscador
     * @param text
     */
 
    public void filtrarDesdeFora(String text) {
        if (text == null || text.isEmpty()) {
            llenarInterfaz(totesLesLlistesCache);
        } else {
            String q = text.toLowerCase();
            List<LlistaDTO> filtradas = totesLesLlistesCache.stream()
                .filter(l -> l.getNombre().toLowerCase().contains(q))
                .toList();
            llenarInterfaz(filtradas);
        }
    }

    /**
     * Metode per omplir
     * @param publiques
     */
    private void llenarInterfaz(List<LlistaDTO> publiques) {
        Platform.runLater(() -> {
            contenedorTargetes.getChildren().clear();
            for (LlistaDTO dto : publiques) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/targetaLista.fxml"));
                    VBox tarjeta = loader.load();

                    TargetaController controller = loader.getController();
                    controller.setData(dto);
                    
                    // eent per fer la targeta gran
                    tarjeta.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) { // doble clic
                            obrirDetalleLista(dto);
                        }
                    });

                    contenedorTargetes.getChildren().add(tarjeta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Metode per obrir els detalls de la llista
     * @param lista
     */
    private void obrirDetalleLista(LlistaDTO lista) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detalleLista.fxml"));
            Parent root = loader.load();

            DetallController controller = loader.getController();
            controller.carregarDades(lista);

            Stage stage = new Stage();
            stage.setTitle("Detall de " + lista.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquejem la finestra de darrera
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}