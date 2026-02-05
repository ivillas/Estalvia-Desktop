package com.ivillas.controller;

import java.io.IOException;
import java.util.List;

import com.ivillas.model.LlistaDTO;
import com.ivillas.service.LlistaServiceClient;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LlistesPubliquesController {

    @FXML private FlowPane contenedorTarjetas;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // 3. ESTE ES EL MÉTODO: Se ejecuta al cargar la pantalla
    @FXML
    public void initialize() {
        cargarListasPubliques();
    }

    
    
    public void cargarListasPubliques() {
        // 1. Crear una tarea en segundo plano
        Task<List<LlistaDTO>> task = new Task<>() {
            @Override
            protected List<LlistaDTO> call() throws Exception {
                // Esto se ejecuta fuera del hilo de la UI (sin bloquear la app)
                return LlistaServiceClient.getPublicas(); 
            }
        };

        // 2. Qué hacer cuando la tarea termine con éxito
        task.setOnSucceeded(e -> {
            List<LlistaDTO> publicas = task.getValue();
            llenarInterfaz(publicas); // Pintamos los datos
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
            controller.cargarDatos(lista);

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