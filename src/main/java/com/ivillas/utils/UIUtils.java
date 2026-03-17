package com.ivillas.utils;


import com.ivillas.model.ProductePreusDTO;
import com.ivillas.service.SupermercatServiceClient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * Classe d'utilitats per a la Interfície d'Usuari (UI).
 * Centralitza elements visuals comuns, com finestres emergents i diàlegs personalitzats,
 * per assegurar la consistència visual en tota l'aplicació.
 */
public class UIUtils {

    /**
     * Mostra una finestra emergent (popup) amb els detalls complets d'un producte.
     * Inclou la imatge centrada, descripció, marca, envàs i una comparativa de preus.
     * 
     * @param p El producte (ProductePreusDTO) del qual es volen mostrar els detalls.
     */
    public static void obrirDetallPopup(ProductePreusDTO p) {
        if (p == null) return;

        // Configuració de l'alerta base
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Especificacions: " + p.getNombre());
        alert.setHeaderText(null);
        alert.setGraphic(null); // Eliminem la icona d'informació per defecte (la "i" blava)

        // Contenidor principal del diàleg
        VBox content = new VBox(20);
        content.setPrefWidth(450); // Amplada fixa per a tots els productes
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Capçalera (Nom i Imatge) - Alineació CENTRADA
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        Label lblTitol = new Label(p.getNombre().toUpperCase());
        lblTitol.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50; -fx-font-family: 'Segoe UI', Arial;");
        lblTitol.setWrapText(true); // Permet que el nom llarg salti de línia
        lblTitol.setTextAlignment(TextAlignment.CENTER);

        ImageView imageView = new ImageView();
        try {
            if (p.getImatge() != null && !p.getImatge().isEmpty()) {
                // Carreguem la imatge amb un tamany estàndard de 220px
                Image image = new Image(p.getImatge(), 220, 220, true, true);
                imageView.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Error carregant la imatge: " + e.getMessage());
        }
        header.getChildren().addAll(lblTitol, imageView);

        // Bloc de Dades Tècniques - Alineació a l'ESQUERRA
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        String dataStyle = "-fx-font-size: 14px; -fx-font-family: 'Segoe UI', Arial; -fx-text-fill: #34495e;";
        
        Label lblDescripcio = new Label("• Descripció: " + (p.getDescripcio() != null ? p.getDescripcio() : "N/A"));
        Label lblMarca = new Label("• Marca: " + (p.getMarca() != null ? p.getMarca() : "N/A"));
        Label lblEnvas = new Label("• Envàs: " + (p.getEnvase() != null ? p.getEnvase() : "N/A"));
        Label lblUpdate = new Label("• Actualitzat: " + (p.getLastUpdate() != null ? p.getLastUpdate() : "N/A"));
        
        // Apliquem estils a les etiquetes
        lblDescripcio.setStyle(dataStyle);
        lblMarca.setStyle(dataStyle);
        lblEnvas.setStyle(dataStyle);
        lblUpdate.setStyle(dataStyle + "-fx-font-style: italic; -fx-font-size: 12px;");

        Label lblPreusTitol = new Label("\nCOMPARATIVA DE PREUS:");
        lblPreusTitol.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");
        
        infoBox.getChildren().addAll(lblDescripcio, lblMarca, lblEnvas, lblUpdate, lblPreusTitol);

        // Secció de Preus actius per supermercat
        if (p.getPrecios() != null && !p.getPrecios().isEmpty()) {
            p.getPrecios().forEach((superNombre, precio) -> {
                // Només mostrem preus de supermercats que estiguin actius al sistema
                if (SupermercatServiceClient.getLocalStatus(superNombre)) {
                    Label lblP = new Label(String.format("  %-12s:  %s €", superNombre.toUpperCase(), precio));
                    // Utilitzem font Monospaced per a que els preus s'alineïn verticalment
                    lblP.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    infoBox.getChildren().add(lblP);
                }
            });
        } else {
            Label lblNo = new Label("No hi ha preus disponibles.");
            lblNo.setStyle(dataStyle);
            infoBox.getChildren().add(lblNo);
        }

        // Muntatge final i exhibició del diàleg
        content.getChildren().addAll(header, infoBox);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(450);
        
        alert.showAndWait();
    }
}