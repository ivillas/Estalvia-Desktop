package com.ivillas.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.ivillas.model.LlistaDTO;
import com.ivillas.model.ProductePreusDTO;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.service.ProducteServiceClient;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Clase que mostra la vista inicial de l'aplicació
 * es pot cridar per mortrar la vista
 */
public class IniciController {

    @FXML private HBox hbUltimesLlistes;
    @FXML private HBox hbUltimsProductes;

    @FXML
    public void initialize() {
        carregarInici();
    }

    /**
     * Metode per carregar la finestra central que conte les llistes y els productes
     */
    
    private void carregarInici() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // obtenim les llistes publiques y ordenem per dataCreacio (descendent)
                List<LlistaDTO> listas = LlistaServiceClient.getPubliques();
                listas.sort((a, b) -> b.getDataCreacio().compareTo(a.getDataCreacio()));
                List<LlistaDTO> topListas = listas.stream().limit(10).collect(Collectors.toList());

                // 2. Obtener Productos y ordenar por lastUpdate (Descendente)
                List<ProductePreusDTO> productos = ProducteServiceClient.getProductes();
                productos.sort((a, b) -> b.getLastUpdate().compareTo(a.getLastUpdate()));
                List<ProductePreusDTO> topProds = productos.stream().limit(10).collect(Collectors.toList());

                // Renderizar en UI
                Platform.runLater(() -> {
                    recarregarLlistes(topListas);
                    carregarProductes(topProds);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    /**^
     * Metode que carrega les llistes 
     * @param llistes
     */
    private void recarregarLlistes(List<LlistaDTO> llistes) {
        // ID del HBox per les llistes en el FXML
    	hbUltimesLlistes.getChildren().clear();

        for (LlistaDTO dto : llistes) {
            try {
                // carrgem el FXML de la tarjeta personalitzada
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/targetaLista.fxml"));
                VBox targeta = loader.load();
                
                // Forcem un minim d'ample
                targeta.setMinWidth(250); 

                // pasem les dades al controlador de la targeta
                TargetaController controller = loader.getController();
                controller.setData(dto);
                
                // Afegim l'event del doble click per obrir la targeta
                targeta.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        obrirDetallLlista(dto); // cridem per obrir el detall
                    }
                });

                hbUltimesLlistes.getChildren().add(targeta);

            } catch (Exception e) {
                System.err.println("Error carregant targeta de llista a l'Inici");
                e.printStackTrace();
            }
        }
    }

    /**
     * Metode per obrir el popup de detall de llista
     */
    private void obrirDetallLlista(LlistaDTO llista) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detalleLista.fxml"));
            Parent root = loader.load();
            
            DetallController controller = loader.getController();
            controller.carregarDades(llista);

            Stage stage = new Stage();
            stage.setTitle("Detall de: " + llista.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarProductes(List<ProductePreusDTO> prods) {
    	// ID del HBox per als productes en el FXML
        hbUltimsProductes.getChildren().clear();
        for (ProductePreusDTO p : prods) {
            try {
                // reutilitcem la card del producte existent
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProducteCard.fxml"));
                Node card = loader.load();
                
                // si es molt gran reajustem aqui
                card.setScaleX(0.9); card.setScaleY(0.9);
                
                ProducteItemController controller = loader.getController();
                controller.setData(p);
                
                hbUltimsProductes.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
