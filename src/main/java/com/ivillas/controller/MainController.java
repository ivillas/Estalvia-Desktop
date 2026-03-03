package com.ivillas.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import com.ivillas.model.UsuariDTO;
import com.ivillas.utils.SessionManager;
import com.jfoenix.controls.JFXButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.control.MenuItem;

import java.io.IOException;
import java.net.URL;
import com.ivillas.service.LlistaServiceClient;
public class MainController {

    @FXML private Label txtTitol;
    @FXML private JFXButton btnUserSession;
    @FXML private JFXButton btnLogout;
    @FXML private JFXButton btnProductes;
    @FXML private JFXButton btnLlistesPrivades;
    @FXML private JFXButton btnLlistesPubliques;
    @FXML private JFXButton btnCrearLlista;
    @FXML private JFXButton btnSupers;
    @FXML private JFXButton btnOpcions;
    @FXML private TextField txtBuscar;
    
    @FXML private StackPane mainDisplayArea;
    private static MainController instance;
    private String modeBusqueda = "PRODUCTES";
    
    
    public MainController() {
        instance = this;
    }
    
    public static MainController getInstance() {
        return instance;
    }
    
    @FXML
    public void initialize() {
    	openInici();
    	SessionManager.setMainController(this);

    	// Configurar menu d'opcions del buscador 
        ContextMenu menuBusqueda = new ContextMenu();
        MenuItem itemProd = new MenuItem("Buscar Productes");
        MenuItem itemLlistes = new MenuItem("Buscar Llistes Públiques");

        itemProd.setOnAction(e -> {
            modeBusqueda = "PRODUCTES";
            txtBuscar.setPromptText("Buscar productes...");
        });
        itemLlistes.setOnAction(e -> {
            modeBusqueda = "LLISTES";
            txtBuscar.setPromptText("Buscar llistes...");
        });
        menuBusqueda.getItems().addAll(itemProd, itemLlistes);

        btnOpcions.setOnMouseClicked(event -> {
            menuBusqueda.show(btnOpcions, event.getScreenX(), event.getScreenY());
        });

        // event del ENTER per el buscador
        txtBuscar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ejecutarBusqueda(txtBuscar.getText());
            }
        });
        
        // buscador en calent
        txtBuscar.textProperty().addListener((obs, oldText, newText) -> {
            // guardem en el sessionmanager per si l'usuari cambia de vista
            SessionManager.setultimaBusqueda(newText);

            // si ja estem en la vista de prodcutes filtrem la taula/targetes al instant
            if ("PRODUCTES".equals(modeBusqueda)) {
                if (ProductesController.getInstance() != null) {
                    ProductesController.getInstance().filtrarDesdeFora(newText);
                }
            } 
            // si estem en llistess, filtrem el FlowPane al instant
            else if ("LLISTES".equals(modeBusqueda)) {
                if (LlistesPubliquesController.getInstance() != null) {
                    LlistesPubliquesController.getInstance().filtrarDesdeFora(newText);
                }
            }
        });
    }
    
    
    /**
     * Metode per al buscador general de la app
     * @param query
     */
    private void ejecutarBusqueda(String query) {
        if (query == null || query.trim().isEmpty()) return;

        // guardem la consulta en el sessionmanager perque la seguent vista la llegeixo
        SessionManager.setultimaBusqueda(query);

        if ("PRODUCTES".equals(modeBusqueda)) {
            openProductes(); // cridem a la vista - obrirProductes
        } else {
            openLlistesPubliques(); // cridem a la vista --  obrirLlistes
        }
    }
    
    
    
    /**
     * Metode que crida a la vista de configuracio
     */
    @FXML
    private void openConfig() {
        try {
            txtTitol.setText("Configura les teves preferencies de la app");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/configuracio.fxml"));
            
            // cargem l'arrel1 (que és un BorderPane)
            BorderPane root = loader.load();

               // per que funciono el botó sortir de la configuració, necessitem passar-li el MainController a ConfigController
            ConfigController configCtrl = loader.getController();
            configCtrl.setMainController(this); 
            // --------------------------------------------------------------------------

            // per forçar el creixement del BorderPane 
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            // per solucionar el problema del  contingut dins
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

            // BINDING TOTAL
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    	

        
    /**
     * Metode que crida a la vista d'inici
     */	
    @FXML
    public void  openInici() {
    	try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/inici.fxml"));
        //carregem el node rel (que es un BorderPane)
        BorderPane root = loader.load();

        // Forcem el creixement: El BorderPane ve amb mides maximes del FXML. Els resetegem.
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        
        
        // extraurem el contingut i el posem al centre per que JavaFX l'obligo a estirarse.
        if (root.getTop() != null) {
            Node contenido = root.getTop();
            root.setTop(null);    // treiem el top
            root.setCenter(contenido); // el posem al centre que si que s'expandeix
            
            // treiem els limits
            if (contenido instanceof Region) {
                Region pane = (Region) contenido;
                pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            }
        }

        // ajustem el contenidor pare
        root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
        root.prefHeightProperty().bind(mainDisplayArea.heightProperty());
       
        mainDisplayArea.getChildren().setAll(root);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Metode que crida a la vista llista economica
     */

    @FXML
    public void openLlistaEco() {
        try {
            txtTitol.setText("Comparativa i Estalvi Optimitzat");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LlistaEco.fxml"));
            BorderPane root = loader.load();

            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            if (root.getCenter() == null && root.getTop() != null) {
                Node contenido = root.getTop();
                root.setTop(null);
                root.setCenter(contenido);
                if (contenido instanceof Region) {
                    Region pane = (Region) contenido;
                    pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                }
            }

            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Error cargando LlistaEco.fxml");
            e.printStackTrace();
        }
    }
    
    /**
     * Metode que crida a la vista Crear llista
     */
    @FXML void openCrearLlista() {
        try {
        	txtTitol.setText("Crea la teva llista de la compra");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CrearLlista.fxml"));
            //carregem el node rel (que es un BorderPane)
            BorderPane root = loader.load();

         // Forcem el creixement: El BorderPane ve amb mides maximes del FXML. Els resetegem.
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

         // extraurem el contingut i el posem al centre per que JavaFX l'obligo a estirarse.
            if (root.getTop() != null) {
                Node contenido = root.getTop();
                root.setTop(null);    // Quitamos del top
                root.setCenter(contenido); // Lo ponemos en el centro (el centro sí se expande)
                
                // treiem els limits
                if (contenido instanceof Region) {
                    Region pane = (Region) contenido;
                    pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                }
            }

         // ajustem el contenidor pare
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Metode que crida a la vista login d'usuari
     */
    @FXML
    public void openLoginWindow() throws IOException {
        // Si ja hi ha una sessio no obrim el login, nem al perfil
        if (SessionManager.isLoggedIn()) {
            actualizarInterfazTrasLogin();
            return;
        }

        // Si no hi ha sessio obrim la finestra de login
        URL fxmlLocation = getClass().getResource("/login.fxml");
        if (fxmlLocation == null) throw new IOException("¡Error! No es troba login.fxml");

        // carregador de vista
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        // obtenim el controlador del login i i passem al mainControler (this)
        AuthController authController = loader.getController();
        authController.setMainController(this); 

        // configura i mostra la finestra
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Acces al compte");
        stage.initModality(Modality.APPLICATION_MODAL); 
        stage.show();
    }
    
    /**
     * Metode que tanca la sesio de l'usuari
     */
    @FXML
    public void handleLogout() {
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
        openInici();
    }
    
    /**
     * Metode que tanca la app 
     */
    @FXML
    private void handleExit() {
        Platform.exit();
        System.exit(0);
    }
    
    /**
     * Metode que crida a la vista de llistes privades 
     */
    
    @FXML
    private void openLlistesPrivades() {
        try {
            txtTitol.setText("Les meves llistes");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/llistesPrivades.fxml"));
            
            BorderPane root = loader.load();

            // Conexió amb el controlador
            LlistesPrivadesController llistapCtrl = loader.getController();
            llistapCtrl.setMainController(this); 

            // Vinculem per que s'estiro
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            // injectem la vista
            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Error carregant llistesPrivades.fxml");
            e.printStackTrace();
        }
    }
    
    /**
     * Metode que crida a la vista de llistes publiques 
     */
    @FXML
    private void openLlistesPubliques() {
        try {
            txtTitol.setText("Llistes creades pels usuaris");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/llistesPubliques.fxml"));
            
            BorderPane root = loader.load();

            // Conexió amb el controlador
            LlistesPubliquesController llistapCtrl = loader.getController();
            llistapCtrl.setMainController(this); 

            // Vinculem per que s'estiro
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            // injectem la vista
            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Error cargando llistesPubliques.fxml");
            e.printStackTrace();
        }
    }
    
    /**
     * Metode que crida a la vista productes  
     */
    @FXML
    private void openProductes() {
        try {
            txtTitol.setText("Llista de productes disponibles amb els seus preus");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/productes.fxml"));
            
            BorderPane root = loader.load();

            // Conexió amb el controlador
            ProductesController productsCtrl = loader.getController();
            productsCtrl.setMainController(this); 

            // Vinculem per que s'estiro
            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            // injectem la vista
            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            System.err.println("Error carregant productes.fxml");
            e.printStackTrace();
        }
    }
    
    /**
     * Metode que crida a la vista d'ajuda e informació  
     */
    @FXML
    private void openAjuda() {
        try {
            txtTitol.setText("Ajuda i informació de la app");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajuda.fxml"));
           
            //carregem el node rel (que es un BorderPane)
            BorderPane root = loader.load();
            
            AjudaController ajudaCtrl = loader.getController();
            ajudaCtrl.setMainController(this); 
          
            //forcem el creixement
            root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

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

            root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
            root.prefHeightProperty().bind(mainDisplayArea.heightProperty());

            mainDisplayArea.getChildren().setAll(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Metode per actualitzar la interface al fer login
     * cambiant labels, mostrant botons...
     */
    public void actualizarInterfazTrasLogin() {
        UsuariDTO user = SessionManager.getUsuario();
        
        if (user == null) {
            System.out.println("ERROR: SessionManager devolvió NULL");
            return;
        } else {
            System.out.println("OK: Usuario recuperado: " + user.getUsername());
        }
        
        // actualitcem els botons laterals
        btnUserSession.setText(user.getUsername());
        btnLogout.setVisible(true);
        btnLogout.setManaged(true);
        btnCrearLlista.setVisible(true);
        btnCrearLlista.setManaged(true);
        btnLlistesPrivades.setVisible(true);
        btnLlistesPrivades.setManaged(true);

        //demanar les dades refrescades
        javafx.concurrent.Task<java.util.Map<String, Long>> task = new javafx.concurrent.Task<>() {
            @Override
            protected java.util.Map<String, Long> call() throws Exception {
                // cridem a l'API per obtenir dades reals
                return LlistaServiceClient.getStats(user.getUserId());
            }
        };

        // si la resposta de l'API te exit
        task.setOnSucceeded(ev -> {
            java.util.Map<String, Long> stats = task.getValue();
            
            // Actualitcem l'objecte user avans d'obrir la vista
            if (stats != null) {
                user.setnLlistesPublices(stats.get("publiques").intValue());
                user.setnLlistesPrivades(stats.get("privades").intValue());
            }

            // carregar la pagina de usuari desde el FXML
            try {
                txtTitol.setText("Perfil d'Usuari");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/perfilUsuari.fxml"));
                BorderPane root = loader.load();

                Object controller = loader.getController();
                
                if (controller instanceof UsuarioController) { 
                    UsuarioController uc = (UsuarioController) controller;
                    uc.setMainController(this);
                    uc.cargarDatos(user);
                }

                root.prefWidthProperty().bind(mainDisplayArea.widthProperty());
                root.prefHeightProperty().bind(mainDisplayArea.heightProperty());
                mainDisplayArea.getChildren().setAll(root);

            } catch (IOException e) {
                System.err.println("Error cargando perfilUsuari.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // si falla l'API carrgem dades que ja teniam per no deixa la pantalla buida
        task.setOnFailed(ev -> {
          
        });

        new Thread(task).start();
    }
    
    /**
     * Metode per actualitzar els titols si entrem en sessió
     */
    public void refrescarVistaActualSiEsPerfil() {
        String titol = txtTitol.getText();
        
        // Si estem en el Perfil
        if ("Perfil d'Usuari".equals(titol)) {
            actualizarInterfazTrasLogin(); 
        } 
        // Si estem en les llistes privades (Meves llistes)
        else if ("Les meves llistes".equals(titol)) {
            openLlistesPrivades(); 
        }
        // si estem en les llistes públiques de altres usuaris
        else if ("Llistes creades pels usuaris".equals(titol)) {
            openLlistesPubliques();
        }
        // Si estem al inici 
        else {
              openInici();
        }
    }
    
    /**
     * Metode per actualitzar el mode de busqueda del buscador general
     * segons elecció de productes o llistes
     */
    public void actualizarModeBusqueda(String mode) {
        this.modeBusqueda = mode; // "PRODUCTES" o "LLISTES"
        if ("PRODUCTES".equals(mode)) {
            txtBuscar.setPromptText("Buscar productes...");
        } else {
            txtBuscar.setPromptText("Buscar llistes...");
        }
    }
    
    
}