package com.ivillas.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.service.SupermercatServiceClient;
import com.ivillas.service.UsuariServiceClient;
import com.jfoenix.controls.JFXButton;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;

public class AjudaController {

	private MainController mainController;
	
	  @FXML private Label lblNusuaris;
	  @FXML private Label lblNproductes;
	  @FXML private Label lblNsupers;	  
	  @FXML private Label lblNLlistesPubli;
	  @FXML private Label lblNllistesPri;	 
	  @FXML private Label lblVersioApp;	 
	  @FXML private Label lbldataVersio;	 
	  @FXML private Label lbldataproductes;	 
	  @FXML private JFXButton btnTornar;
	  @FXML private JFXButton btnUpdate;
	
	
	  @FXML
	  public void initialize() {
	      // Usamos un Task para que la UI no se congele mientras el servidor responde
	      Task<Map<String, String>> task = new Task<>() {
	          @Override
	          protected Map<String, String> call() throws Exception {
	              Map<String, String> resultados = new HashMap<>();

	              // 1. Cargamos datos simples
	              resultados.put("usuaris", String.valueOf(UsuariServiceClient.getTotalUsuaris()));
	              resultados.put("supers", String.valueOf(SupermercatServiceClient.getAll().size()));
	              resultados.put("publi", String.valueOf(LlistaServiceClient.getPublicas().size()));
	              resultados.put("privi", String.valueOf(LlistaServiceClient.getTotalPrivades()));

	              // 2. Cargamos productos UNA SOLA VEZ
	              List<ProductePreusDTO> productos = ProducteServiceClient.getProductos();
	              resultados.put("nProd", String.valueOf(productos.size()));

	              // 3. Obtenemos la fecha y le damos formato
	              LocalDateTime fecha = ProducteServiceClient.ultimaDataProductes(productos);
	              if (fecha != null) {
	                  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	                  resultados.put("fecha", fecha.format(formatter));
	              } else {
	                  resultados.put("fecha", "No disponible");
	              }

	              return resultados;
	          }
	      };

	      // Cuando el hilo termina, asignamos todos los textos a la vez
	      task.setOnSucceeded(e -> {
	          Map<String, String> res = task.getValue();
	          lblNusuaris.setText(res.get("usuaris"));
	          lblNproductes.setText(res.get("nProd"));
	          lblNsupers.setText(res.get("supers"));
	          lblNLlistesPubli.setText(res.get("publi"));
	          lblNllistesPri.setText(res.get("privi"));
	          lbldataproductes.setText(res.get("fecha"));
	          System.out.println("Vista d'Ajuda actualitzada correctament.");
	      });

	      task.setOnFailed(e -> {
	          System.err.println("Error carregant estadístiques");
	          task.getException().printStackTrace();
	      });

	      new Thread(task).start();
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
    public void buscarActualitzacio() {
    	 mostrarAlertaExito("Actualització", "No s'han trobat actualitzacions, ja tens la versió mes recent.");
    }
    
    private void mostrarAlertaExito(String titulo, String msg) { crearAlerta(titulo, msg, AlertType.INFORMATION); }

    private void crearAlerta(String titulo, String msg, AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    
    
}
