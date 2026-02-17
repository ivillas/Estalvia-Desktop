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

public class IniciController {

    @FXML private HBox hbUltimesLlistes;
    @FXML private HBox hbUltimsProductes;

    @FXML
    public void initialize() {
        cargarDashboard();
    }

    private void cargarDashboard() {
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
                    renderizarListas(topListas);
                    renderizarProductos(topProds);
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void renderizarListas(List<LlistaDTO> listas) {
        // Asegúrate de que hbUltimasListas es el ID correcto en el FXML
    	hbUltimesLlistes.getChildren().clear();

        for (LlistaDTO dto : listas) {
            try {
                // Cargamos tu FXML de tarjeta personalizada
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/targetaLista.fxml"));
                VBox tarjeta = loader.load();
                
                // Forzamos un ancho mínimo para que el ScrollPane funcione bien
                tarjeta.setMinWidth(250); 

                // Pasamos los datos al controlador de la tarjeta
                TargetaController controller = loader.getController();
                controller.setData(dto);
                
                // Añadimos el evento de doble clic para abrir el detalle
                tarjeta.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        abrirDetalleLista(dto); // Llamamos a tu método de popup
                    }
                });

                hbUltimesLlistes.getChildren().add(tarjeta);

            } catch (Exception e) {
                System.err.println("Error carregant targeta de llista a l'Inici");
                e.printStackTrace();
            }
        }
    }

    // Método para abrir el popup (copia el que ya tienes o llámalo desde aquí)
    private void abrirDetalleLista(LlistaDTO lista) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detalleLista.fxml"));
            Parent root = loader.load();
            
            DetallController controller = loader.getController();
            controller.carregarDades(lista);

            Stage stage = new Stage();
            stage.setTitle("Detall de: " + lista.getNombre());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renderizarProductos(List<ProductePreusDTO> prods) {
        hbUltimsProductes.getChildren().clear();
        for (ProductePreusDTO p : prods) {
            try {
                // Reutilizamos tu Card de producto existente
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProducteCard.fxml"));
                Node card = loader.load();
                
                // Si el Card es muy grande, podemos re-ajustar su tamaño aquí
                card.setScaleX(0.9); card.setScaleY(0.9);
                
                ProducteItemController controller = loader.getController();
                controller.setData(p);
                
                hbUltimsProductes.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void irACrearLlista() { MainController.getInstance().openCrearLlista(); }
    @FXML private void irAComparador() { MainController.getInstance().openLlistaEco(); }
}
