package com.ivillas.controller;

import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import com.ivillas.service.AuthServiceClient;
import com.ivillas.utils.SessionManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Clase controlador de la vista de inici de sessió
 * @author ivilla
 * @Version 1.0
 */

public class AuthController {

	private MainController mainController;
	@FXML
	private TextField txtUserLogin, txtUserReg, txtEmailReg, txtEmailForgot;
	@FXML
	private PasswordField txtPassLogin, txtPassReg;
	@FXML
	private VBox paneLogin, paneRegister, paneForgot;
	@FXML
	private javafx.scene.control.Button btnEntrar;

	
	/**
	 * Metode inicialitzacions
	 */
	@FXML
	public void initialize() {
		btnEntrar.setDefaultButton(true); // per poder accedir al boto amb enter desde el teclat
	}

	/**
	 * Metode per identificarse
	 */
	
	@FXML
	private void handleLogin() {
		if (camposVacios(txtUserLogin, txtPassLogin))
			return;

		AuthServiceClient.login(txtUserLogin.getText(), txtPassLogin.getText(), usuari -> Platform.runLater(() -> {
			SessionManager.setUsuari(usuari);
			if (mainController != null)
				mainController.actualizarInterfazTrasLogin();
			txtUserLogin.getScene().getWindow().hide();
		}), error -> Platform.runLater(() -> mostrarAlerta("Error", error)));
	}

	/**
	 * Metode per registrarse
	 */
		
	@FXML
	private void handleRegister() {
		if (camposVacios(txtUserReg, txtEmailReg, txtPassReg))
			return;

		AuthServiceClient.register(txtUserReg.getText(), txtEmailReg.getText(), txtPassReg.getText(),
				() -> Platform.runLater(() -> {
					mostrarAlertaExit("Èxit", "Usuari registrat correctament.");
					showLogin();
				}), error -> Platform.runLater(() -> mostrarAlerta("Error", error)));
	}
	
	/**
	 * Metode per recuperar la contrasenya
	 */

	@FXML
	private void processRecovery() {
		String email = txtEmailForgot.getText();

		if (email == null || email.isBlank()) {
			mostrarAlerta("Error", "Per favor, introdueix un email vàlid.");
			return;
		}

		// Cridem al service que hem llimpiat abans
		AuthServiceClient.processRecovery(email, () -> Platform.runLater(() -> {
			mostrarAlertaExit("Enviat", "S'ha enviat un correu amb les instruccions.");
			showLogin(); // Tornar al panel del login
		}), error -> Platform.runLater(() -> mostrarAlerta("Error", error)));
	}

	// Navegacio simplificada cambian de panells
	@FXML
	private void showRegister() {
		cambiarPanel(paneRegister);
	}

	@FXML
	private void showLogin() {
		cambiarPanel(paneLogin);
	}

	@FXML
	private void handleForgotPass() {
		cambiarPanel(paneForgot);
	}

	
	private void cambiarPanel(VBox panelVisible) {
		paneLogin.setVisible(panelVisible == paneLogin);
		paneLogin.setManaged(panelVisible == paneLogin);
		paneRegister.setVisible(panelVisible == paneRegister);
		paneRegister.setManaged(panelVisible == paneRegister);
		paneForgot.setVisible(panelVisible == paneForgot);
		paneForgot.setManaged(panelVisible == paneForgot);
	}

	// per mirar camps buits
	private boolean camposVacios(TextField... camps) {
		for (TextField f : camps) {
			if (f.getText().isEmpty()) {
				mostrarAlerta("Error", "Per favor, omple tots els camps.");
				return true;
			}
		}
		return false;
	}

	/**
	 * Metode per mostrar alertes de error
	 */
	
	private void mostrarAlerta(String titol, String msg) {
		crearAlerta(titol, msg, AlertType.ERROR);
	}

	/**
	 * Metode per mostrar alertes d'informació
	 */

	private void mostrarAlertaExit(String titol, String msg) {
		crearAlerta(titol, msg, AlertType.INFORMATION);
	}
	
	/**
	 * Metode que crea les alertes
	 */
	private void crearAlerta(String titol, String msg, AlertType tipus) {
		Alert alert = new Alert(tipus);
		alert.setTitle(titol);
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}

	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}

}