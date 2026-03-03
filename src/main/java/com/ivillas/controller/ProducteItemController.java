package com.ivillas.controller;

import com.ivillas.model.ProductePreusDTO;
import com.ivillas.request.ItemLlistaRequest;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.math.BigDecimal;

import com.ivillas.service.ProducteServiceClient;
import com.ivillas.utils.SessionManager;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import java.util.Collections;

/**
 * Clase controladora de cada item / producte
 */

public class ProducteItemController {
    @FXML private ImageView imgProducte;
    @FXML private Label lblNom, lblPreu, btnFavorit;
    
    private ProductePreusDTO producte;

    
    /**
     * Metode per omplir del dades que mostrara del producte
     * @param p
     */
    public void setData(ProductePreusDTO p) {
        this.producte = p;
        lblNom.setText(p.getNombre());
        
        // Busquem el preu mes barat
        if (p.getPrecios() != null && !p.getPrecios().isEmpty()) {
            BigDecimal minPrecio = Collections.min(p.getPrecios().values());
            lblPreu.setText(minPrecio.toString() + " €");
        }

        if (p.getImatge() != null && !p.getImatge().isBlank()) {
            imgProducte.setImage(new Image(p.getImatge(), true));
        }

        // Actualizar l'estat del cor segons el SessionManager
        if (SessionManager.isLoggedIn() && SessionManager.esFavorit(p.getProducteId())) {
            marcarCorVermell();
        } else {
            resetCor();
        }
    }

    /**
     * Metode per afegir o treure un prodcte a favorit polsant el cor
     * @param event
     */
    @FXML
    private void toggleFavorito(MouseEvent event) {
        event.consume(); // Evita que s'obri el detall al polsar el cor
        if (!SessionManager.isLoggedIn()) return;

        Long userId = SessionManager.getUsuario().getUserId();
        Long prodId = producte.getProducteId();
        boolean yaEsFav = SessionManager.esFavorit(prodId);

        new Thread(() -> {
            try {
                if (ProducteServiceClient.gestionarFavoritAPI(userId, prodId, !yaEsFav)) {
                    if (yaEsFav) SessionManager.getIdsFavorits().remove(prodId);
                    else SessionManager.getIdsFavorits().add(prodId);
                    
                    Platform.runLater(() -> {
                        if (!yaEsFav) marcarCorVermell();
                        else resetCor();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    /**
     * Metode per afegir un producte a la llista (la temporal de la sessió)
     */
    @FXML
    private void afegirALlista() {
        // Usem ItemLlistaRequest real
        ItemLlistaRequest nuevoItem = new ItemLlistaRequest();
        nuevoItem.setProductoId(producte.getProducteId());
        nuevoItem.setProductoNombre(producte.getNombre());
        nuevoItem.setCantidad(BigDecimal.ONE);
        nuevoItem.setPrecios(producte.getPrecios());

        // Afegim a la llista temporal del SessionManager
        SessionManager.getLlistaTemporal().getItems().add(nuevoItem);
        System.out.println("Afegit a listaTemporal: " + producte.getNombre());
    }
    
    /**
     * Metode per ocultar el cor
     * si no estem logejats
     */

    public void ocultarCor() {
        if (btnFavorit != null) {
            btnFavorit.setVisible(false);
            btnFavorit.setManaged(false);
        }
    }

    /**
     * Metode per pintar el cor de vermell
     * quan es favorit
     */
    public void marcarCorVermell() {
        if (btnFavorit != null) {
            btnFavorit.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 20px;");
        }
    }

    /**
     * Metode que torna el cor al seu nestat inicial, sense pintar
     */
    private void resetCor() {
        if (btnFavorit != null) {
            btnFavorit.setStyle("-fx-text-fill: #ccc; -fx-font-size: 20px;");
        }
    }

    /**
     * Metode per obrir el detall del producte
     */
    @FXML
    private void obrirDetall() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalls del Producte");
        alert.setHeaderText(producte.getNombre() + " (" + (producte.getMarca() != null ? producte.getMarca() : "") + ")");
        
        StringBuilder sb = new StringBuilder();
        sb.append("Descripció: ").append(producte.getDescripcio()).append("\n\n");
        sb.append("Comparativa de Preus:\n");
        
        if (producte.getPrecios() != null) {
            producte.getPrecios().forEach((supermercado, precio) -> {
                sb.append("- ").append(supermercado.toUpperCase()).append(": ").append(precio).append(" €\n");
            });
        }
        alert.setContentText(sb.toString());
        alert.getDialogPane().setStyle("-fx-background-color: #F3F4F6;");
        alert.showAndWait();
    }
}