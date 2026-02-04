package com.ivillas.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.ivillas.model.ProductePreusDTO;
import com.ivillas.model.UsuariDTO;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainController {

    @FXML private Label txtTitol; // CAMBIADO: JFXLabel -> Label
    @FXML private JFXButton btnUserSession;
    @FXML private JFXButton btnLogout;
    @FXML private JFXButton btnProductes;
    @FXML private JFXButton btnLlistesPrivades;
    @FXML private JFXButton btnLlistesPubliques;
    @FXML private JFXButton btnCrearLlista;
    @FXML private JFXButton btnSupers;
    @FXML private TextField txtBuscador;
    @FXML private StackPane mainDisplayArea;

    @FXML
    public void initialize() {
       

    }

    @FXML
    private void openConfig() {
        try {
            txtTitol.setText("Configura les teves preferencies de la app");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/configuracio.fxml"));
            
            // Cargamos el nodo raíz
            BorderPane root = loader.load();

            // --- ESTA ES LA CONEXIÓN QUE TE FALTA PARA QUE EL BOTÓN 'SORTIR' FUNCIONE ---
            ConfigController configCtrl = loader.getController();
            configCtrl.setMainController(this); 
            // --------------------------------------------------------------------------

            // 1. FORZAR CRECIMIENTO
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            // 2. SOLUCIONAR EL PROBLEMA DEL <TOP> (Mover contenido al centro)
            if (root.getTop() != null) {
                Node contenido = root.getTop();
                root.setTop(null);    
                root.setCenter(contenido); 
                
                if (contenido instanceof Region) {
                    Region pane = (Region) contenido;
                    pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                }
            }

            // 3. BINDING TOTAL
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    	

        
    	
    @FXML
    public void  openInici() {
    	try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/inici.fxml"));
     // Cargamos el nodo raíz (que es un BorderPane)
        BorderPane root = loader.load();

        // 1. FORZAR CRECIMIENTO: El BorderPane viene con tamaños máximos del FXML. Los reseteamos.
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        
        // 2. SOLUCIONAR EL PROBLEMA DEL <TOP>: 
        // Tu FXML tiene el AnchorPane dentro de <top>. El <top> NUNCA se expande verticalmente.
        // Extraemos el contenido y lo ponemos en el centro para que JavaFX lo obligue a estirarse.
        if (root.getTop() != null) {
            Node contenido = root.getTop();
            root.setTop(null);    // Quitamos del top
            root.setCenter(contenido); // Lo ponemos en el centro (el centro sí se expande)
            
            // Si el contenido es un AnchorPane (como en tu FXML), quitamos sus límites también
            if (contenido instanceof Region) {
                Region pane = (Region) contenido;
                pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            }
        }

        // 3. BINDING TOTAL: Ajuste al contenedor padre
        root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
        root.prefHeightProperty().bind(mainDisplayArea.heightProperty());
       
        mainDisplayArea.getChildren().setAll(root);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    
    
    @FXML
    private void openCrearLlista() {
        try {
        	txtTitol.setText("Crea la teva llista de la compra");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CrearLlista.fxml"));
            // Cargamos el nodo raíz (que es un BorderPane)
            BorderPane root = loader.load();

            // 1. FORZAR CRECIMIENTO: El BorderPane viene con tamaños máximos del FXML. Los reseteamos.
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            // 2. SOLUCIONAR EL PROBLEMA DEL <TOP>: 
            // Tu FXML tiene el AnchorPane dentro de <top>. El <top> NUNCA se expande verticalmente.
            // Extraemos el contenido y lo ponemos en el centro para que JavaFX lo obligue a estirarse.
            if (root.getTop() != null) {
                Node contenido = root.getTop();
                root.setTop(null);    // Quitamos del top
                root.setCenter(contenido); // Lo ponemos en el centro (el centro sí se expande)
                
                // Si el contenido es un AnchorPane (como en tu FXML), quitamos sus límites también
                if (contenido instanceof Region) {
                    Region pane = (Region) contenido;
                    pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                }
            }

            // 3. BINDING TOTAL: Ajuste al contenedor padre
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    
    



    @FXML
    private void openLoginWindow() throws IOException {
        // 1. Localizar el FXML
        URL fxmlLocation = getClass().getResource("/login.fxml");
        if (fxmlLocation == null) {
            throw new IOException("¡Error! No es troba login.fxml");
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
        stage.setTitle("Acces al compte");
        stage.initModality(Modality.APPLICATION_MODAL); 
        stage.show();
    }
    
    @FXML
    private void handleLogout() {
        SessionManager.logout();
        btnUserSession.setText("Login");
        btnLogout.setVisible(false);
        btnLogout.setManaged(false);
        btnLlistesPrivades.setVisible(false);
        btnLlistesPrivades.setManaged(false);
        btnCrearLlista.setVisible(false);
        btnCrearLlista.setManaged(false);
        btnSupers.setVisible(false);
        btnSupers.setManaged(false);

    }
    
    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }
    

    


    

    @FXML
    private void openProducts() {
        try {
        	txtTitol.setText("Els teus productes favorits");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/productes.fxml"));
            // Cargamos el nodo raíz (que es un BorderPane)
            BorderPane root = loader.load();

            // 1. FORZAR CRECIMIENTO: El BorderPane viene con tamaños máximos del FXML. Los reseteamos.
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            // 2. SOLUCIONAR EL PROBLEMA DEL <TOP>: 
            // Tu FXML tiene el AnchorPane dentro de <top>. El <top> NUNCA se expande verticalmente.
            // Extraemos el contenido y lo ponemos en el centro para que JavaFX lo obligue a estirarse.
            if (root.getTop() != null) {
                Node contenido = root.getTop();
                root.setTop(null);    // Quitamos del top
                root.setCenter(contenido); // Lo ponemos en el centro (el centro sí se expande)
                
                // Si el contenido es un AnchorPane (como en tu FXML), quitamos sus límites también
                if (contenido instanceof Region) {
                    Region pane = (Region) contenido;
                    pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                }
            }

            // 3. BINDING TOTAL: Ajuste al contenedor padre
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void openLlistesPrivades() {
        try {
            txtTitol.setText("Les meves llistes");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/llistesPrivades.fxml"));
            
            BorderPane root = loader.load();

            // Conexión con el controlador
            LlistesPrivadesController llistapCtrl = loader.getController();
            llistapCtrl.setMainController(this); 

            // Vincular al área principal para que se estire
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            // Inyectar en la vista
            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Error carregant llistesPrivades.fxml");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void openLlistesPubliques() {
        try {
            txtTitol.setText("Llistes creades pels usuaris");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/llistesPubliques.fxml"));
            
            BorderPane root = loader.load();

            // Conexión con el controlador
            LlistesPubliquesController llistapCtrl = loader.getController();
            llistapCtrl.setMainController(this); 

            // Vincular al área principal para que se estire
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            // Inyectar en la vista
            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Error cargando llistesPubliques.fxml");
            e.printStackTrace();
        }
    }
    
    
    @FXML
    private void openProductes() {
        try {
            txtTitol.setText("Llista de productes disponibles amb els seus preus");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/productes.fxml"));
            
            BorderPane root = loader.load();

            // Conexión con el controlador
            ProductesController productsCtrl = loader.getController();
            productsCtrl.setMainController(this); 

            // Vincular al área principal para que se estire
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            // Inyectar en la vista
            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Error carregant productes.fxml");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void openAjuda() {
        try {
            txtTitol.setText("Ajuda i informació de la app");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajuda.fxml"));
           
            // Cargamos el nodo raíz
            BorderPane root = loader.load();

            // --- ESTA ES LA CONEXIÓN QUE TE FALTA PARA QUE EL BOTÓN 'SORTIR' FUNCIONE ---
            AjudaController ajudaCtrl = loader.getController();
            ajudaCtrl.setMainController(this); 
            // --------------------------------------------------------------------------

            // 1. FORZAR CRECIMIENTO
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            // 2. SOLUCIONAR EL PROBLEMA DEL <TOP> (Mover contenido al centro)
            if (root.getTop() != null) {
                Node contenido = root.getTop();
                root.setTop(null);    
                root.setCenter(contenido); 
                
                if (contenido instanceof Region) {
                    Region pane = (Region) contenido;
                    pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                }
            }

            // 3. BINDING TOTAL
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void actualizarInterfazTrasLogin() {
        UsuariDTO user = SessionManager.getUsuario();
       // if (user == null) return;

        // DEBUG 1: ¿Realmente SessionManager tiene al usuario?
        if (user == null) {
            System.out.println("ERROR: SessionManager devolvió NULL");
            return;
        } else {
            System.out.println("OK: Usuario recuperado: " + user.getUsername());
        }
        
        // 1. Actualizar botones laterales (tu código actual)
        btnUserSession.setText(user.getUsername());
        btnLogout.setVisible(true);
        btnLogout.setManaged(true);
        btnCrearLlista.setVisible(true);
        btnCrearLlista.setManaged(true);
        btnLlistesPrivades.setVisible(true);
        btnLlistesPrivades.setManaged(true);
        // ... activar el resto de botones ...

        // 2. Cargar la "página" de usuario desde el FXML
        try {
            txtTitol.setText("Perfil d'Usuari");
            
            // Cargamos el archivo FXML específico
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/perfilUsuari.fxml"));
            BorderPane root = loader.load();

            // 3. Pasar datos al controlador de la vista cargada (si es necesario)
            // Por ejemplo, si tienes un UsuarioController para gestionar esa vista:
            Object controller = loader.getController();
            if (controller instanceof UsuarioController) {
                ((UsuarioController) controller).setMainController(this);
                System.out.println("Enviando datos al UsuarioController...");
                ((UsuarioController) controller).cargarDatos(user);
            }

            // 4. Ajustes de expansión (idéntico a tus otros métodos)
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            if (root.getTop() != null) {
                Node contenido = root.getTop();
                root.setTop(null);    
                root.setCenter(contenido); 
            }

            // 5. Vincular tamaño e inyectar en el área central
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Error cargando usuario.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}