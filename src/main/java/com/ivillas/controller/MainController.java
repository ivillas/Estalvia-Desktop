package com.ivillas.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import javafx.scene.control.Separator;

import java.io.IOException;
import java.net.URL;

import com.ivillas.model.UsuariDTO;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXButton;

public class MainController {

    @FXML private VBox contentArea; // Vinculado al fx:id del centro
    @FXML private JFXButton btnUserSession;
    @FXML private JFXButton btnLogout;
    @FXML private HBox mainDisplayArea;

    @FXML
    private void openLoginWindow() throws IOException {
        // 1. Localizar el FXML
        URL fxmlLocation = getClass().getResource("/login.fxml");
        if (fxmlLocation == null) {
            throw new IOException("¡Error! No se encuentra login.fxml");
        }

        // 2. USAR EL CARGADOR DE FORMA NO ESTÁTICA
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load(); // Aquí se carga la vista

        // 3. LA MAGIA: Obtener el controlador del login y pasarle ESTE MainController (this)
        AuthController authController = loader.getController();
        authController.setMainController(this); 

        // 4. Configurar y mostrar la ventana
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Acceso al Sistema");
        stage.initModality(Modality.APPLICATION_MODAL); 
        stage.show();
    }
    
    @FXML
    private void handleLogout() {
        SessionManager.logout();
        btnUserSession.setText("Login");
        btnLogout.setVisible(false);
        btnLogout.setManaged(false);
        mainDisplayArea.getChildren().clear(); 
        // Aquí podrías recargar las tarjetas por defecto si quisieras
    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }

    public void actualizarInterfazTrasLogin() {
        UsuariDTO user = SessionManager.getUsuario();
        if (user == null) return;

        // 1. Actualizamos botones laterales
        btnUserSession.setText(user.getUsername());
        btnLogout.setVisible(true);
        btnLogout.setManaged(true);

        // 2. Limpiamos el área principal (el HBox de las tarjetas pequeñas)
        mainDisplayArea.getChildren().clear();
        
        // OPCIONAL: Si quieres que ocupe todo el alto, 
        // asegúrate de que el padre de mainDisplayArea no tenga otros hijos estorbando
        
        // 3. Crear una "Super Card" grande
        VBox superCard = new VBox(20);
        superCard.setAlignment(Pos.TOP_LEFT);
        superCard.setPadding(new Insets(40));
        
        // Hacemos que la tarjeta se expanda
        superCard.setPrefWidth(800); 
        superCard.setMinWidth(600);
        superCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 8);");

        // Título con estilo moderno
        Label title = new Label("Perfil d'Usuari");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1E1E2E;");
        
        // Datos con iconos o etiquetas claras
        Label name = new Label("👤 Nom d'usuari:  " + user.getUsername());
        Label email = new Label("✉️ Correu electrònic:  " + user.getEmail());
        Label fecha = new Label("📅 Membre des de:  " + user.getDataCreacio());

        String estiloTexto = "-fx-font-size: 18px; -fx-text-fill: #444444;";
        name.setStyle(estiloTexto);
        email.setStyle(estiloTexto);
        fecha.setStyle(estiloTexto);

        // Añadir todo a la super card
        superCard.getChildren().addAll(title, new Separator(), name, email, fecha);
        
        // 4. Inyectar la super card en el área principal
        mainDisplayArea.getChildren().add(superCard);
    }
    
}