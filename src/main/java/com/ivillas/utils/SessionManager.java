package com.ivillas.utils;

import com.ivillas.model.UsuariDTO;

public class SessionManager {
    private static UsuariDTO usuarioLogueado;

    public static void setUsuario(UsuariDTO u) { usuarioLogueado = u; }
    public static UsuariDTO getUsuario() { return usuarioLogueado; }
    public static void logout() { usuarioLogueado = null; }
}