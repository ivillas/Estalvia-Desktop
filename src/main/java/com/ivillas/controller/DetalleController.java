package com.ivillas.controller;

import com.ivillas.model.ItemLlistaDTO;
import com.ivillas.model.LlistaDTO;
import com.ivillas.service.LlistaServiceClient;
import com.ivillas.utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


public class DetalleController {
    @FXML private Label lblTitulo, lblAutor, lblDescripcion;
    @FXML private TableView<ItemLlistaDTO> tablaProductos;
    @FXML private TableColumn<ItemLlistaDTO, String> colNombre, colCantidad, colUnidad;
    
    private LlistaDTO listaActual;

    public void cargarDatos(LlistaDTO lista) {
        this.listaActual = lista;
        lblTitulo.setText(lista.getNombre());
        lblAutor.setText("Autor: " + lista.getNomAutor());
        lblDescripcion.setText(lista.getDescripcion());

        // Configurar columnas de la tabla (deben coincidir con los nombres en ItemLlistaDTO)
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        tablaProductos.setItems(FXCollections.observableArrayList(lista.getItems()));
    }

    @FXML
    private void copiarLista() {
        try {
            Long miId = SessionManager.getUsuario().getUserId();
            LlistaServiceClient.copiarAmiLista(listaActual, miId);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Llista copiada amb èxit!");
            alert.showAndWait();
            cerrarVentana();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarVentana() {
        ((Stage) lblTitulo.getScene().getWindow()).close();
    }
}