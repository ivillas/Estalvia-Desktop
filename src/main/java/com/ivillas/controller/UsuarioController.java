package com.ivillas.controller;

import com.ivillas.model.UsuariDTO;
import com.ivillas.service.UsuariServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXButton;

import java.util.Optional; // IMPORTANTE: faltaba este
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType; // IMPORTANTE: este es el correcto
import javafx.scene.control.Label;

/**
 * Controlador per a la vista de perfil d'usuari.
 * Gestiona l'exhibició de dades personals, estadístiques de llistes i favorits,
 * així com el procés de baixa del compte del sistema.
 */
public class UsuarioController {
	
	  // --- Elements de la interfície FXML ---
	  @FXML private Label lblUsuari;           // Nom de l'usuari
	  @FXML private Label lblEmail;            // Correu electrònic
	  @FXML private Label lblData;	           // Data de creació del compte
	  @FXML private Label lblLlistesPubliques; // Comptador de llistes públiques
	  @FXML private Label lblLlistesPrivades;  // Comptador de llistes privades
	  @FXML private Label lblProductes;	       // Comptador de productes preferits
	  @FXML private JFXButton btnUserSession;  // Botó d'acció de sessió
	  
	  private MainController mainController;
	
    /**
     * Estableix la referència al controlador principal per a la navegació.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Carrega les dades de l'usuari i les estadístiques als elements visuals.
     * @param user Objecte DTO amb la informació de l'usuari.
     */
    public void cargarDatos(UsuariDTO user) {
    	
        if (user != null) {
            // Assignació de dades bàsiques
            lblUsuari.setText(user.getUsername());
            lblEmail.setText(user.getEmail());
            lblData.setText(user.getDataCreacio());  
            
            // Assignació de comptadors de llistes
            lblLlistesPubliques.setText(String.valueOf(user.getnLlistesPublices()));
            lblLlistesPrivades.setText(String.valueOf(user.getnLlistesPrivades()));
            
            // Si l'usuari està loguejat, obtenim el total de favorits des del SessionManager
            if (SessionManager.isLoggedIn()) {
                lblProductes.setText(String.valueOf(SessionManager.getIdsFavorits().size()));
            }            
        }
    }

    /**
     * Torna a la pantalla d'inici de l'aplicació.
     */
    @FXML
    private void openInici() {
        if (mainController != null) {
            mainController.openInici(); 
        }
    }
    
    /**
     * Gestiona el procés de baixa de l'usuari mitjançant un diàleg de confirmació.
     * Permet triar entre esborrar només les dades privades o tot el contingut.
     */
    @FXML
    private void baixaUser() {
        // Crear la alerta de confirmació
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Donar-se de baixa");
        alert.setHeaderText("Estàs a punt d'eliminar el teu compte.");
        alert.setContentText("Tria com vols gestionar les teves llistes:");

        // Configuració de botons personalitzats per a les opcions de borrat
        ButtonType btnSoloPrivadas = new ButtonType("Només privades");
        ButtonType btnTodo = new ButtonType("Tot (Llistes i compte)");
        ButtonType btnCancelar = new ButtonType("Cancel·lar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSoloPrivadas, btnTodo, btnCancelar);

        // Processar la resposta de l'usuari
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnSoloPrivadas) {
                ejecutarBorrado("solo_privadas");
            } else if (result.get() == btnTodo) {
                ejecutarBorrado("todo");
            }
        }
    }

    /**
     * Executa la petició d'esborrat al servidor segons el mode triat.
     * @param modo Cadena de text que indica el tipus de borrat ("solo_privadas" o "todo").
     */
    private void ejecutarBorrado(String modo) {
        try {
            // obtenim el Id del usuari actual
            Long userId = SessionManager.getUsuario().getUserId();
            
            // cridem al clien que conecta amb el backend per realitzar l'eliminació
            boolean ok = UsuariServiceClient.eliminarCompte(userId, modo);

            if (ok) {
                mostrarAlerta("Compte eliminat", "El teu compte s'ha eliminat correctament. Fins la propera!");
                
                // llimpiem la secio i tornem al inici tancant la sessió
                SessionManager.setUsuari(null);
                if (mainController != null) {
                    mainController.handleLogout(); 
                    mainController.openInici();
                }
            } else {
                mostrarAlerta("Error", "No s'ha pogut processar la baixa. Intenta-ho més tard.");
            }
        } catch (Exception e) {
            // Gestió d'excepcions en cas de fallada de xarxa o servidor
            e.printStackTrace();
            mostrarAlerta("Error de connexió", "No s'ha pogut contactar amb el servidor.");
        }
    }

    /**
     * Mètode auxiliar per mostrar alertes informatives a l'usuari.
     */
    private void mostrarAlerta(String titol, String missatge) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

}