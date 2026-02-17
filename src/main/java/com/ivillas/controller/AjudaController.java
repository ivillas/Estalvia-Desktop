package com.ivillas.controller;

/**
 * Clase controlador de la vista ajuda
 * @author ivilla
 * @Version 1.0
 */

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
	@FXML
	private Label lblNusuaris;
	@FXML
	private Label lblNproductes;
	@FXML
	private Label lblNsupers;
	@FXML
	private Label lblNLlistesPubli;
	@FXML
	private Label lblNllistesPri;
	@FXML
	private Label lblVersioApp;
	@FXML
	private Label lbldataVersio;
	@FXML
	private Label lbldataproductes;
	@FXML
	private JFXButton btnTornar;
	@FXML
	private JFXButton btnUpdate;

	/**
	 * Metode que carrega al iniciar on posem les dades a les labels
	 */

	@FXML
	public void initialize() {
		// Usem un task per que la UI no es ongeli mentre el servidor respon
		Task<Map<String, String>> task = new Task<>() {
			@Override
			protected Map<String, String> call() throws Exception {
				Map<String, String> resultat = new HashMap<>();

				// Carregem les dades simples
				resultat.put("usuaris", String.valueOf(UsuariServiceClient.getTotalUsuaris()));
				resultat.put("supers", String.valueOf(SupermercatServiceClient.getAll().size()));
				resultat.put("publi", String.valueOf(LlistaServiceClient.getPublicas().size()));
				resultat.put("privi", String.valueOf(LlistaServiceClient.getTotalPrivades()));

				// Carregem els productes una sola vegada i y mirem el tamany
				List<ProductePreusDTO> productos = ProducteServiceClient.getProductos();
				resultat.put("nProd", String.valueOf(productos.size()));

				// Obtenim la data i li donem format
				LocalDateTime fecha = ProducteServiceClient.ultimaDataProductes(productos);
				if (fecha != null) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
					resultat.put("Data", fecha.format(formatter));
				} else {
					resultat.put("Data", "No disponible");
				}

				return resultat;
			}
		};

		// Quan el fil acaba signem tots els textes
		task.setOnSucceeded(e -> {
			Map<String, String> res = task.getValue();
			lblNusuaris.setText(res.get("usuaris"));
			lblNproductes.setText(res.get("nProd"));
			lblNsupers.setText(res.get("supers"));
			lblNLlistesPubli.setText(res.get("publi"));
			lblNllistesPri.setText(res.get("privi"));
			lbldataproductes.setText(res.get("Data"));
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

	// Cridem al metode pare
	@FXML
	private void openInici() {
		if (mainController != null) {
			mainController.openInici();
		}
	}

	/**
	 * Metode que buscara actualitzacions
	 */

	@FXML
	public void buscarActualitzacio() {
		mostrarAlertaExit("Actualització", "No s'han trobat actualitzacions, ja tens la versió mes recent.");
	}

	/**
	 * Metode per mostrar una alerta
	 * @param titol
	 * @param msg
	 */
	private void mostrarAlertaExit(String titol, String msg) {
		crearAlerta(titol, msg, AlertType.INFORMATION);
	}

	/**
	 * Metode per crear l'alerta
	 * @param titol
	 * @param msg
	 * @param tipus
	 */
	private void crearAlerta(String titol, String msg, AlertType tipus) {
		Alert alert = new Alert(tipus);
		alert.setTitle(titol);
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}

}
