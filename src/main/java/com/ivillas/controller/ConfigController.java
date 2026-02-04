package com.ivillas.controller;

import java.awt.event.ActionEvent;
import java.util.List;

import com.ivillas.model.SupermercatDTO;
import com.ivillas.network.HttpClientProvider;
import com.ivillas.service.SupermercatServiceClient;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class ConfigController {
	  
	    @FXML private JFXCheckBox cxbProxi;
	    @FXML private TextField txtIpProxi;
	    @FXML private TextField txtPortProxi;
	    @FXML private Label lblIpProxi;
	    @FXML private Label lblPortProxi;
        @FXML private JFXButton btnCancelarConfig;

	    // Checkboxes de Supermercados
        
	    @FXML private JFXCheckBox cxbMercadona; // Mercadona
	    @FXML private JFXCheckBox cxbCarrefour; // Carrefour
	    @FXML private JFXCheckBox cxbPlus; // Plus Fresc
	    @FXML private JFXCheckBox cxbConsum; // Consum
	    @FXML private JFXCheckBox cxbAlcampo; // Alcampo
	    
	    private MainController mainController;


	    
	    @FXML
	    public void initialize() {

	        // 1. Recuperar valores actuales del Provider
	        boolean actiu = HttpClientProvider.isUseProxy();	        
	        cxbProxi.setSelected(actiu);
	        txtIpProxi.setText(HttpClientProvider.getProxyHost());
	        
	        // Si el puerto es 0, lo ponemos vacío, si no, lo convertimos a String
	        int puerto = HttpClientProvider.getProxyPort();
	        txtPortProxi.setText(puerto == 0 ? "" : String.valueOf(puerto));

	        // 2. Aplicar visibilidad según el estado recuperado
	        actualizarEstadoProxy(actiu);
	        
	        // ... resto de tu inicialización de supermercados ...
	        // 1. Ponemos los supers seleccionados por defecto
	        cargarEstadoSupermercados();
	    }
	    

	    
	    private void cargarEstadoSupermercados() {
	        Task<List<SupermercatDTO>> task = new Task<>() {
	            @Override protected List<SupermercatDTO> call() throws Exception {
	                return SupermercatServiceClient.getAll();
	            }
	        };

	        
	        task.setOnFailed(e -> {
	            System.out.println("!!! EL TASK FALLÓ !!!");
	            task.getException().printStackTrace(); 
	        });
	        
	        task.setOnSucceeded(e -> {
	            List<SupermercatDTO> resultado = task.getValue();
	            // ESTO ES LO QUE FALTA: Obligar a la UI a actualizarse
	            Platform.runLater(() -> {
	                for (SupermercatDTO s : resultado) {
	                    String nom = s.getNom().toLowerCase().replace(" ", "").trim();
	                    boolean estado = s.isActiu();
	                    
	                    System.out.println("UI: Marcando " + nom + " como " + estado);

	                    if (nom.contains("mercadona")) cxbMercadona.setSelected(estado);
	                    else if (nom.contains("alcampo"))   cxbAlcampo.setSelected(estado);
	                    else if (nom.contains("carrefour")) cxbCarrefour.setSelected(estado);
	                    else if (nom.contains("consum"))    cxbConsum.setSelected(estado);
	                    else if (nom.contains("plusfresc")) cxbPlus.setSelected(estado);
	                }
	            });
	        });

	        new Thread(task).start();
	    }
	    
	    @FXML
	    private void handleProxyAction() {
	        actualizarEstadoProxy(cxbProxi.isSelected());
	    }
	    
	    public void setMainController(MainController mainController) {
	        this.mainController = mainController;
	    }

	    @FXML
	    private void openInici() {
	        if (mainController != null) {
	            mainController.openInici(); // Llama al método del padre
	        }
	    }
	    
	    @FXML
	    private void handleProxyAction(ActionEvent event) {
	        // Obtenemos el estado directamente del checkbox
	        actualizarEstadoProxy(cxbProxi.isSelected());
	    }
	    
	    private void actualizarEstadoProxy(boolean seleccionado) {
	    	
	        lblIpProxi.setVisible(seleccionado);
	        lblIpProxi.setManaged(seleccionado);
	        
	        txtIpProxi.setVisible(seleccionado);
	        txtIpProxi.setManaged(seleccionado);
	        
	        lblPortProxi.setVisible(seleccionado);
	        lblPortProxi.setManaged(seleccionado);
	        
	        txtPortProxi.setVisible(seleccionado);
	        txtPortProxi.setManaged(seleccionado);
	    }
	    
	    @FXML
	    private void handleGuardarConfig() {
	        try {
	            boolean usarProxy = cxbProxi.isSelected();
	            String host = txtIpProxi.getText();
	            int puerto = 0;

	            if (usarProxy) {
	                String portText = txtPortProxi.getText();
	                if (portText == null || portText.isBlank()) {
	                    // Aquí podrías mostrar una alerta al usuario
	                    System.out.println("Error: El puerto es obligatorio si usas proxy.");
	                    return; 
	                }
	                puerto = Integer.parseInt(portText.trim());
	            }

	            // Llamamos al provider para aplicar los cambios
	            HttpClientProvider.configureProxy(usarProxy, host, puerto);
	          
	        } catch (NumberFormatException e) {
	            System.err.println("Error: El puerto debe ser un número válido.");
	            // Opcional: Mostrar alerta visual al usuario
	        }
	        
	        // 2. Guardar supermercados
	        SupermercatServiceClient.saveStatus("alcampo", cxbAlcampo.isSelected());
	        SupermercatServiceClient.saveStatus("mercadona", cxbMercadona.isSelected());
	        SupermercatServiceClient.saveStatus("carrefour", cxbCarrefour.isSelected());
	        SupermercatServiceClient.saveStatus("consum", cxbConsum.isSelected());
	        SupermercatServiceClient.saveStatus("plusfresc", cxbPlus.isSelected());


	        // DEBUG: Verificar qué estamos intentando guardar
	        System.out.println("DEBUG UI: Guardando Plusfresc como: " + cxbPlus.isSelected());

	        mostrarAlertaExito("Configuració", "Preferències guardades.");
	        
	        // SOLO AL FINAL DE TODO cerramos o navegamos
	        openInici(); 
	        
	    }
	    
	    private void mostrarAlerta(String titulo, String msg) { crearAlerta(titulo, msg, AlertType.ERROR); }
	    private void mostrarAlertaExito(String titulo, String msg) { crearAlerta(titulo, msg, AlertType.INFORMATION); }

	    private void crearAlerta(String titulo, String msg, AlertType tipo) {
	        Alert alert = new Alert(tipo);
	        alert.setTitle(titulo);
	        alert.setHeaderText(null);
	        alert.setContentText(msg);
	        alert.showAndWait();
	    }
	}


