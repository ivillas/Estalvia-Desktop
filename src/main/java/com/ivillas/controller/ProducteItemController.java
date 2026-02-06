package com.ivillas.controller;

import com.ivillas.model.ProductePreusDTO; // Tu DTO
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.math.BigDecimal;

public class ProducteItemController {
    @FXML private ImageView imgProducte;
    @FXML private Label lblNom, lblPreu;
    private MainController mainController;
    
    
    private ProductePreusDTO producto;


    
    // Este método lo llamaremos desde el bucle del controlador principal
    public void setData(ProductePreusDTO p) {
        this.producto = p;
        lblNom.setText(p.getNombre());
        
        // Cogemos el primer precio del Map para mostrarlo en la tarjeta
        if (p.getPrecios() != null && !p.getPrecios().isEmpty()) {
            BigDecimal primerPrecio = p.getPrecios().values().iterator().next();
            lblPreu.setText(primerPrecio.toString() + " €");
        }

        if (p.getImatge() != null && !p.getImatge().isBlank()) {
            imgProducte.setImage(new Image(p.getImatge(), true));
        }
    }

    @FXML
    private void abrirDetalle() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalls del Producte");
        alert.setHeaderText(producto.getNombre() + " (" + producto.getMarca() + ")");
        
        // Construimos la lista de precios dinámicamente desde el Map
        StringBuilder sb = new StringBuilder();
        sb.append("Descripció: ").append(producto.getDescripcio()).append("\n\n");
        sb.append("Comparativa de Preus:\n");
        
        if (producto.getPrecios() != null) {
            producto.getPrecios().forEach((supermercado, precio) -> {
                sb.append("- ").append(supermercado.toUpperCase())
                  .append(": ").append(precio).append(" €\n");
            });
        }

        alert.setContentText(sb.toString());
        
        // Estilo básico para que combine con tu app
        alert.getDialogPane().setStyle("-fx-background-color: #F3F4F6;");
        alert.showAndWait();
    }
}