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

	    // Checkboxes de Supermercats
        
	    @FXML private JFXCheckBox cxbMercadona; // Mercadona
	    @FXML private JFXCheckBox cxbCarrefour; // Carrefour
	    @FXML private JFXCheckBox cxbPlus; // Plus Fresc
	    @FXML private JFXCheckBox cxbConsum; // Consum
	    @FXML private JFXCheckBox cxbAlcampo; // Alcampo
	    
	    private MainController mainController;


	    
	    @FXML
	    public void initialize() {

	        // Recuperar els valors actuals del servidor
	        boolean actiu = HttpClientProvider.isUseProxy();	        
	        cxbProxi.setSelected(actiu);
	        txtIpProxi.setText(HttpClientProvider.getProxyHost());
	        
	        // si el port es 0 el posem buit si no el pasem a string
	        int puerto = HttpClientProvider.getProxyPort();
	        txtPortProxi.setText(puerto == 0 ? "" : String.valueOf(puerto));

	        //  aplicar la visibilitat segons el check
	        actualizarEstatProxy(actiu);
	        
	        //Per carregar els supermercats
	        carregarEstatSupermercats();
	    }
	    

	    /**
	     * 
	     */
	    private void carregarEstatSupermercats() {
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
	            //Per obligar a actualitzarse la iu
	            Platform.runLater(() -> {
	                for (SupermercatDTO s : resultado) {
	                    String nom = s.getNom().toLowerCase().replace(" ", "").trim();
	                    boolean estat = s.isActiu();
	                    
	                    System.out.println("UI: Marcando " + nom + " como " + estat);

	                    if (nom.contains("mercadona")) cxbMercadona.setSelected(estat);
	                    else if (nom.contains("alcampo"))   cxbAlcampo.setSelected(estat);
	                    else if (nom.contains("carrefour")) cxbCarrefour.setSelected(estat);
	                    else if (nom.contains("consum"))    cxbConsum.setSelected(estat);
	                    else if (nom.contains("plusfresc")) cxbPlus.setSelected(estat);
	                }
	            });
	        });

	        new Thread(task).start();
	    }
	    
	    /**
	     * Metode per l'accio del proxi (actualitzar depen si es chec¡keja o no)
	     */
	    
	    @FXML
	    private void handleProxyAction() {
	        actualizarEstatProxy(cxbProxi.isSelected());
	    }
	    
	    public void setMainController(MainController mainController) {
	        this.mainController = mainController;
	    }

	    /**
	     * Metode per anar a la pantalla inicial
	     */
	    @FXML
	    private void openInici() {
	        if (mainController != null) {
	            mainController.openInici();
	        }
	    }
	    
	    
	    /**
	     * Metode per obtenir l'estat a l'accio del checkbox
	     * @param event
	     */
	    @FXML
	    private void handleProxyAction(ActionEvent event) {
	        actualizarEstatProxy(cxbProxi.isSelected());
	    }
	    
	    /**
	     * Metode per actualitzar segons el check ver enseñar o ocultar le sopcions del proxi
	     * @param seleccionat
	     */
	    private void actualizarEstatProxy(boolean seleccionat) {
	    	
	        lblIpProxi.setVisible(seleccionat);
	        lblIpProxi.setManaged(seleccionat);
	        
	        txtIpProxi.setVisible(seleccionat);
	        txtIpProxi.setManaged(seleccionat);
	        
	        lblPortProxi.setVisible(seleccionat);
	        lblPortProxi.setManaged(seleccionat);
	        
	        txtPortProxi.setVisible(seleccionat);
	        txtPortProxi.setManaged(seleccionat);
	    }
	    
	    /**
	     * Metode per guardar la configuració
	     */
	    @FXML
	    private void handleGuardarConfig() {
	        try {
	            boolean usarProxy = cxbProxi.isSelected();
	            String host = txtIpProxi.getText();
	            int port = 0;

	            if (usarProxy) {
	                String portText = txtPortProxi.getText();
	                if (portText == null || portText.isBlank()) {
	                    mostrarAlertaExit("Configuració", "El port es obligatori si es fa anar proxi.");
	                    return; 
	                }
	                port = Integer.parseInt(portText.trim());
	            }

	            
	            HttpClientProvider.configureProxy(usarProxy, host, port);
	          
	        } catch (NumberFormatException e) {
	        	mostrarAlertaExit("Configuració", "El port ha de ser un numero valid.");

	        }
	        
	        // guardar els supermercats
	        SupermercatServiceClient.saveStatus("alcampo", cxbAlcampo.isSelected());
	        SupermercatServiceClient.saveStatus("mercadona", cxbMercadona.isSelected());
	        SupermercatServiceClient.saveStatus("carrefour", cxbCarrefour.isSelected());
	        SupermercatServiceClient.saveStatus("consum", cxbConsum.isSelected());
	        SupermercatServiceClient.saveStatus("plusfresc", cxbPlus.isSelected());

	        mostrarAlertaExit("Configuració", "Preferències guardades.");
	        List<SupermercatDTO> llistaImp = null;
	        try {
				llistaImp = SupermercatServiceClient.getAll();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        for(SupermercatDTO i: llistaImp) {
	        	System.out.println(i.getNom() + " estat: " + i.isActiu());	        	
	        }
	        
	        System.out.println();
	        // al final cridem al metode per anar al inici
	        openInici(); 
	        
	    }

		/**
		 * Metode per mostrar alertes d'informació
		 */
	    private void mostrarAlertaExit(String titol, String msg) { crearAlerta(titol, msg, AlertType.INFORMATION); }
	    
		/**
		 * Metode que crea les alertes
		 */
	    private void crearAlerta(String titol, String msg, AlertType tipo) {
	        Alert alert = new Alert(tipo);
	        alert.setTitle(titol);
	        alert.setHeaderText(null);
	        alert.setContentText(msg);
	        alert.showAndWait();
	    }
	}


