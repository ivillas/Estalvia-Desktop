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

public class LlistesPubliquesController {

    @FXML private FlowPane contenedorTarjetas;
    private MainController mainController;
    private static LlistesPubliquesController instance;

    private List<LlistaDTO> todasLasListasCache = new ArrayList<>();
    
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // 3. ESTE ES EL MÉTODO: Se ejecuta al cargar la pantalla
    @FXML
    public void initialize() {
    	instance = this;
        // Avisamos al MainController
        if (SessionManager.getMainController() != null) {
            SessionManager.getMainController().actualizarModeBusqueda("LLISTES");
        }
    	
        String query = SessionManager.getultimaBusqueda();
        
        if (query != null && !query.isEmpty()) {
            System.out.println("Buscant llista: " + query);
            
            // Aquí crides al teu mètode de càrrega passant-li el filtre
            cargarListasPubliquesFiltradas(query); 
            
            SessionManager.setultimaBusqueda(null); 
        } else {
            cargarListasPubliques();
        }

    }
   
    public static LlistesPubliquesController getInstance() { 
    	return instance; 
    }
    
    public void cargarListasPubliquesFiltradas(String query) {
        Task<List<LlistaDTO>> task = new Task<>() {
            @Override
            protected List<LlistaDTO> call() throws Exception {
                // Reutilizamos la misma llamada a la API que ya tienes
                return LlistaServiceClient.getPubliques(); 
            }
        };

        task.setOnSucceeded(e -> {
            List<LlistaDTO> todas = task.getValue();
            this.todasLasListasCache = todas; // <-- IMPORTANTE: GUARDAR AQUÍ TAMBIÉN

            List<LlistaDTO> filtradas = todas.stream()
                .filter(l -> l.getNombre().toLowerCase().contains(query.toLowerCase()))
                .toList();

            llenarInterfaz(filtradas); 
        });

        task.setOnFailed(e -> {
            System.err.println("Error en la cerca: " + task.getException().getMessage());
        });

        Thread th = new Thread(task);
        th.setDaemon(true); 
        th.start();
    }
    
    
    public void cargarListasPubliques() {
        // 1. Crear una tarea en segundo plano
        Task<List<LlistaDTO>> task = new Task<>() {
            @Override
            protected List<LlistaDTO> call() throws Exception {
                // Esto se ejecuta fuera del hilo de la UI (sin bloquear la app)
                return LlistaServiceClient.getPubliques(); 
            }
        };
        
        // UN SOLO setOnSucceeded para todo
        task.setOnSucceeded(e -> {
            List<LlistaDTO> publicas = task.getValue();
            this.todasLasListasCache = publicas; // <-- GUARDAMOS EN CACHÉ
            llenarInterfaz(publicas);           // <-- PINTAMOS
        });

        // 3. Qué hacer si falla (error de red)
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            System.err.println("Error de red: " + exception.getMessage());
            // Aquí podrías mostrar un aviso al usuario sin colgar la app
        });

        // 4. Arrancar la tarea en un hilo nuevo
        Thread th = new Thread(task);
        th.setDaemon(true); 
        th.start();
    }
    
 // Método para el filtrado en caliente
    public void filtrarDesdeFora(String text) {
        if (text == null || text.isEmpty()) {
            llenarInterfaz(todasLasListasCache);
        } else {
            String q = text.toLowerCase();
            List<LlistaDTO> filtradas = todasLasListasCache.stream()
                .filter(l -> l.getNombre().toLowerCase().contains(q))
                .toList();
            llenarInterfaz(filtradas);
        }
    }

    private void llenarInterfaz(List<LlistaDTO> publicas) {
        Platform.runLater(() -> {
            contenedorTarjetas.getChildren().clear();
            for (LlistaDTO dto : publicas) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/targetaLista.fxml"));
                    VBox tarjeta = loader.load();
                    
                    // Quitamos estilos de debug y usamos los del FXML
                    TargetaController controller = loader.getController();
                    controller.setData(dto);
                    
                    // EVENTO PARA HACERLA GRANDE (Detalle)
                    tarjeta.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) { // Doble clic para abrir detalle
                            abrirDetalleLista(dto);
                        }
                    });

                    contenedorTarjetas.getChildren().add(tarjeta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void abrirDetalleLista(LlistaDTO lista) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detalleLista.fxml"));
            Parent root = loader.load();
            
            // Supongamos que creas un DetalleController
            DetallController controller = loader.getController();
            controller.carregarDades(lista);

            Stage stage = new Stage();
            stage.setTitle("Detalle de " + lista.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana de atrás
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}