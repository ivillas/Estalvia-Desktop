package com.ivillas.utils;

import java.util.ArrayList;

import com.ivillas.model.UsuariDTO;
import com.ivillas.request.CrearLlistaRequest;

public class SessionManager {
    private static UsuariDTO usuarioLogueado;
    private static CrearLlistaRequest listaTemporal = new CrearLlistaRequest();
    
    public static CrearLlistaRequest getListaTemporal() {
        if (listaTemporal.getItems() == null) {
            listaTemporal.setItems(new ArrayList<>());
        }
        return listaTemporal;
    }

    
    public static void resetListaTemporal() {
        listaTemporal = new CrearLlistaRequest();
        listaTemporal.setItems(new ArrayList<>());
    }

    public static void setUsuario(UsuariDTO u) { usuarioLogueado = u; }
    public static UsuariDTO getUsuario() { return usuarioLogueado; }
    
    
    public static boolean isLoggedIn() {
        return usuarioLogueado != null;
    }

    public static void logout() {
    	usuarioLogueado = null;
    }
    
    
    
}