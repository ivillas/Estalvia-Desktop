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

/**
 * Clase controladora de les Llistes privades
 */

	public class LlistesPrivadesController {
	    @FXML private FlowPane contenedorTargetes;
	    @FXML private CheckBox checkPubliques;
	    @FXML private CheckBox checkPrivades;

	    private List<LlistaDTO> totesLesMevesLlistes = new ArrayList<>();
	    private MainController mainController;

	    @FXML
	    public void initialize() {
	        // ens aseguremq ue estiguin totes dos marcades
	        if (checkPubliques != null) checkPubliques.setSelected(true);
	        if (checkPrivades != null) checkPrivades.setSelected(true);
	    }

	    
	    public void setMainController(MainController mainController) {
	        this.mainController = mainController;
	        cargarDesdePrivades(); 
	        checkPubliques.setSelected(true);
	        checkPrivades.setSelected(true);
	    }

	    /**
	     * Metode per carregar les llistes privades
	     * segons l'ID del usuari en sessioó
	     */
	    
	    public void cargarDesdePrivades() {
	        if (SessionManager.getUsuario() == null) {
	            return;
	        }
	        
	        Long userId = SessionManager.getUsuario().getUserId();

	        Task<List<LlistaDTO>> task = new Task<>() {
	            @Override
	            protected List<LlistaDTO> call() throws Exception {
	                
	                return LlistaServiceClient.getPerUsuari(userId); 
	            }
	        };

	        task.setOnSucceeded(e -> {
	            totesLesMevesLlistes = task.getValue();
	            aplicarFiltre(); 
	        });

	        task.setOnFailed(e -> {
	            task.getException().printStackTrace();
	        });

	        Thread th = new Thread(task);
	        th.setDaemon(true);
	        th.start();
	    }

	    /**
	     * Metode per aplicar filtre segons fem els check
	     */
	    
	    @FXML
	    public void aplicarFiltre() {
	        if (checkPubliques == null || checkPrivades == null || totesLesMevesLlistes == null) {
	            return; 
	        }

	        try {
	            List<LlistaDTO> filtradas = totesLesMevesLlistes.stream()
	                .filter(lista -> {
	                    String vis = lista.getVisibilitat();
	                    if (vis == null) return false;
	                    
	                    boolean esPublica = "PUBLICA".equalsIgnoreCase(vis);
	                    boolean esPrivada = "PRIVADA".equalsIgnoreCase(vis);

	                    return (checkPubliques.isSelected() && esPublica) || 
	                           (checkPrivades.isSelected() && esPrivada);
	                })
	                .collect(Collectors.toList());

	            omplenarInterface(filtradas);
	        } catch (Exception e) {
	            System.err.println( e.getMessage());
	        }
	    }

	    /**
	     * Metode per omplenar la vista amb les llistes
	     * @param llistes
	     */
	    private void omplenarInterface(List<LlistaDTO> llistes) {
	        Platform.runLater(() -> {
	            contenedorTargetes.getChildren().clear();
	            for (LlistaDTO dto : llistes) {
	                try {
	                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/targetaLista.fxml"));
	                    VBox tarjeta = loader.load();
	                    TargetaController controller = loader.getController();
	                    controller.setData(dto);
	                    
	                    tarjeta.setOnMouseClicked(ev -> {
	                        if (ev.getClickCount() == 2) obrirDetallLlista(dto);
	                    });
	                    contenedorTargetes.getChildren().add(tarjeta);
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        });
	    }

    /**
     * Metode per obrri el popup del detall de la llista
     * @param lista
     */
    private void obrirDetallLlista(LlistaDTO lista) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detalleLista.fxml"));
            Parent root = loader.load();
            
            // instanciem 
            DetallController controller = loader.getController();
            controller.carregarDades(lista);

            Stage stage = new Stage();
            stage.setTitle("Detall de " + lista.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
