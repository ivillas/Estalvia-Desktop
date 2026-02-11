package com.ivillas.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ivillas.model.LlistaDTO;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.utils.SessionManager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

	public class LlistesPrivadesController {
	    @FXML private FlowPane contenedorTarjetas;
	    @FXML private CheckBox checkPublicas;
	    @FXML private CheckBox checkPrivadas;

	    private List<LlistaDTO> todasMisListas = new ArrayList<>();
	    private MainController mainController;

	    @FXML
	    public void initialize() {
	        System.out.println("Vista inicializada.");
	        // Aseguramos que empiecen marcados antes de recibir cualquier dato
	        if (checkPublicas != null) checkPublicas.setSelected(true);
	        if (checkPrivadas != null) checkPrivadas.setSelected(true);
	    }

	    
	    public void setMainController(MainController mainController) {
	        this.mainController = mainController;
	        System.out.println("MainController inyectado. Iniciando carga...");
	        cargarDatosPrivados(); // <--- IMPORTANTE: Llamar aquí
	        checkPublicas.setSelected(true);
	        checkPrivadas.setSelected(true);
	    }

	    public void cargarDatosPrivados() {
	        if (SessionManager.getUsuario() == null) {
	            System.err.println("Error: No hay usuario en SessionManager");
	            return;
	        }
	        
	        Long userId = SessionManager.getUsuario().getUserId();
	        System.out.println("Pidiendo listas al servidor para user: " + userId);

	        Task<List<LlistaDTO>> task = new Task<>() {
	            @Override
	            protected List<LlistaDTO> call() throws Exception {
	                // Forzamos la llamada al método que acepta el ID
	                return LlistaServiceClient.getPorUsuario(userId); 
	            }
	        };

	        task.setOnSucceeded(e -> {
	            todasMisListas = task.getValue();
	            System.out.println("Total listas recibidas del servidor: " + todasMisListas.size());
	            aplicarFiltro(); 
	        });

	        task.setOnFailed(e -> {
	            System.err.println("Fallo en la tarea:");
	            task.getException().printStackTrace();
	        });

	        Thread th = new Thread(task);
	        th.setDaemon(true);
	        th.start();
	    }

	    @FXML
	    public void aplicarFiltro() {
	        // VALIDACIÓN CRÍTICA: Si el FXML llama a esto antes de cargar los datos o los IDs
	        if (checkPublicas == null || checkPrivadas == null || todasMisListas == null) {
	            System.out.println("Filtro cancelado: Componentes aún no inyectados o lista vacía.");
	            return; 
	        }

	        try {
	            List<LlistaDTO> filtradas = todasMisListas.stream()
	                .filter(lista -> {
	                    String vis = lista.getVisibilitat();
	                    if (vis == null) return false;
	                    
	                    boolean esPublica = "PUBLICA".equalsIgnoreCase(vis);
	                    boolean esPrivada = "PRIVADA".equalsIgnoreCase(vis);

	                    return (checkPublicas.isSelected() && esPublica) || 
	                           (checkPrivadas.isSelected() && esPrivada);
	                })
	                .collect(Collectors.toList());

	            llenarInterfaz(filtradas);
	        } catch (Exception e) {
	            System.err.println("Error dentro del stream de filtrado: " + e.getMessage());
	        }
	    }

	    private void llenarInterfaz(List<LlistaDTO> listas) {
	        Platform.runLater(() -> {
	            contenedorTarjetas.getChildren().clear();
	            for (LlistaDTO dto : listas) {
	                try {
	                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/targetaLista.fxml"));
	                    VBox tarjeta = loader.load();
	                    TargetaController controller = loader.getController();
	                    controller.setData(dto);
	                    
	                    tarjeta.setOnMouseClicked(ev -> {
	                        if (ev.getClickCount() == 2) abrirDetalleLista(dto);
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
