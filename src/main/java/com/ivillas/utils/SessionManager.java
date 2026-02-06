package com.ivillas.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ivillas.controller.MainController;
import com.ivillas.model.UsuariDTO;
import com.ivillas.request.CrearLlistaRequest;
import com.ivillas.service.ProducteServiceClient;

import javafx.application.Platform;

public class SessionManager {
    private static UsuariDTO usuarioLogueado;
    private static CrearLlistaRequest listaTemporal = new CrearLlistaRequest();
    private static MainController mainController;
    private static Set<Long> idsFavoritos = new HashSet<>();
    
    public static CrearLlistaRequest getListaTemporal() {
        if (listaTemporal.getItems() == null) {
            listaTemporal.setItems(new ArrayList<>());
        }
        return listaTemporal;
    }

    public static Set<Long> getIdsFavoritos() {
        return idsFavoritos;
    }
    
    public static void cargarFavoritos() {
        if (isLoggedIn()) {
            new Thread(() -> {
                try {
                    List<Long> ids = ProducteServiceClient.getIdsFavoritos(usuarioLogueado.getUserId());
                    idsFavoritos.clear();
                    idsFavoritos.addAll(ids);
                    
                    // ¡AQUÍ ESTÁ EL CAMBIO!
                    Platform.runLater(() -> {
                        // Si el MainController está cargado, intentamos refrescar la vista actual
                        if (mainController != null) {
                            mainController.refrescarVistaActualSiEsPerfil();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static boolean esFavorito(Long productoId) {
        return idsFavoritos.contains(productoId);
    }
    
    public static void resetListaTemporal() {
        listaTemporal = new CrearLlistaRequest();
        listaTemporal.setItems(new ArrayList<>());
    }

    public static void setUsuario(UsuariDTO u) { 
        usuarioLogueado = u; 
        if (u != null) {
            cargarFavoritos(); // <--- LLAMADA CRÍTICA AQUÍ
        }
    }
    
    public static UsuariDTO getUsuario() { return usuarioLogueado; }
    
    
    public static boolean isLoggedIn() {
        return usuarioLogueado != null;
    }

    public static void logout() {
    	usuarioLogueado = null;
    	 idsFavoritos.clear();
    }


	public static MainController getMainController() {
		return mainController;
	}
	
    public static void setMainController(MainController controller) {
        mainController = controller;
    }
    
    
}