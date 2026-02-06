package com.ivillas.controller;

import com.ivillas.model.ProductePreusDTO; // Tu DTO
import com.ivillas.request.ItemLlistaRequest;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.math.BigDecimal;


import com.ivillas.model.ProductePreusDTO;
import com.ivillas.service.ProducteServiceClient;
import com.ivillas.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import java.math.BigDecimal;
import java.util.Collections;

public class ProducteItemController {
    @FXML private ImageView imgProducte;
    @FXML private Label lblNom, lblPreu, btnFavorit;
    
    private ProductePreusDTO producto;

    public void setData(ProductePreusDTO p) {
        this.producto = p;
        lblNom.setText(p.getNombre());
        
        // Buscamos el precio más barato en lugar del primero
        if (p.getPrecios() != null && !p.getPrecios().isEmpty()) {
            BigDecimal minPrecio = Collections.min(p.getPrecios().values());
            lblPreu.setText(minPrecio.toString() + " €");
        }

        if (p.getImatge() != null && !p.getImatge().isBlank()) {
            imgProducte.setImage(new Image(p.getImatge(), true));
        }

        // Actualizar estado del corazón según el SessionManager
        if (SessionManager.isLoggedIn() && SessionManager.esFavorito(p.getProducteId())) {
            marcarCorazonRojo();
        } else {
            resetCorazon();
        }
    }

    @FXML
    private void toggleFavorito(MouseEvent event) {
        event.consume(); // Evita que se abra el detalle al clicar el corazón
        if (!SessionManager.isLoggedIn()) return;

        Long userId = SessionManager.getUsuario().getUserId();
        Long prodId = producto.getProducteId();
        boolean yaEsFav = SessionManager.esFavorito(prodId);

        new Thread(() -> {
            try {
                if (ProducteServiceClient.gestionarFavoritoAPI(userId, prodId, !yaEsFav)) {
                    if (yaEsFav) SessionManager.getIdsFavoritos().remove(prodId);
                    else SessionManager.getIdsFavoritos().add(prodId);
                    
                    Platform.runLater(() -> {
                        if (!yaEsFav) marcarCorazonRojo();
                        else resetCorazon();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void afegirALlista() {
        // Usamos tu ItemLlistaRequest real
        ItemLlistaRequest nuevoItem = new ItemLlistaRequest();
        nuevoItem.setProductoId(producto.getProducteId());
        nuevoItem.setProductoNombre(producto.getNombre());
        nuevoItem.setCantidad(BigDecimal.ONE);
        nuevoItem.setPrecios(producto.getPrecios());

        // Añadir a la lista temporal del SessionManager
        SessionManager.getListaTemporal().getItems().add(nuevoItem);
        System.out.println("Afegit a listaTemporal: " + producto.getNombre());
    }

    public void ocultarCorazon() {
        if (btnFavorit != null) {
            btnFavorit.setVisible(false);
            btnFavorit.setManaged(false);
        }
    }

    public void marcarCorazonRojo() {
        if (btnFavorit != null) {
            btnFavorit.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 20px;");
        }
    }

    private void resetCorazon() {
        if (btnFavorit != null) {
            btnFavorit.setStyle("-fx-text-fill: #ccc; -fx-font-size: 20px;");
        }
    }

    @FXML
    private void abrirDetalle() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalls del Producte");
        alert.setHeaderText(producto.getNombre() + " (" + (producto.getMarca() != null ? producto.getMarca() : "") + ")");
        
        StringBuilder sb = new StringBuilder();
        sb.append("Descripció: ").append(producto.getDescripcio()).append("\n\n");
        sb.append("Comparativa de Preus:\n");
        
        if (producto.getPrecios() != null) {
            producto.getPrecios().forEach((supermercado, precio) -> {
                sb.append("- ").append(supermercado.toUpperCase()).append(": ").append(precio).append(" €\n");
            });
        }
        alert.setContentText(sb.toString());
        alert.getDialogPane().setStyle("-fx-background-color: #F3F4F6;");
        alert.showAndWait();
    }
}