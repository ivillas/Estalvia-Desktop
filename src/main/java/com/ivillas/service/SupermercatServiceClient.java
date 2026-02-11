package com.ivillas.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.prefs.Preferences;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivillas.model.SupermercatDTO;
import com.ivillas.network.HttpClientProvider;

public class SupermercatServiceClient {
    private static final Preferences prefs = Preferences.userNodeForPackage(SupermercatServiceClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<SupermercatDTO> getAll() throws Exception {
        // 1. Usamos el HttpClient que ya sabe si hay proxy o no (centralizado)
        HttpClient client = HttpClientProvider.getClient();
        
        // 2. Usamos la URL base centralizada
        String url = HttpClientProvider.getBaseUrl() + "/supermercats";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // 3. Respuesta y Mapeo
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SupermercatDTO> lista = mapper.readValue(response.body(), new TypeReference<List<SupermercatDTO>>() {});

        // 4. Sincronizamos con el HD (Preferences)
        for (SupermercatDTO s : lista) {
            String nombreLimpio = s.getNom().toLowerCase().replace(" ", "").trim();
            // Leemos si el usuario lo activó/desactivó en este PC
            boolean estadoLocal = prefs.getBoolean("status_" + nombreLimpio, true);
            s.setActiu(estadoLocal);
        }
        
        return lista;
    }
    
 // --- PARA GUARDAR (Desde ConfigController) ---
    public static void saveStatus(String nombre, boolean actiu) {
        // 1. Limpiamos el nombre: "Plus Fresc" -> "plusfresc"
        String nombreLimpio = nombre.toLowerCase().replace(" ", "").trim();
        // 2. Guardamos con el prefijo "status_"
        prefs.putBoolean("status_" + nombreLimpio, actiu);
        
        try { 
            prefs.flush(); 
            System.out.println("GUARDADO EN DISCO -> status_" + nombreLimpio + ": " + actiu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- PARA LEER (Desde CrearLlistaController) ---
    public static boolean getLocalStatus(String nombre) {
        // 1. Limpiamos el nombre que viene de la API (igual que arriba)
        String nombreLimpio = nombre.toLowerCase().replace(" ", "").trim();
        
        // 2. Buscamos con el MISMO prefijo "status_"
        // Si no existe en el disco, por defecto devolvemos 'true'
        boolean resultado = prefs.getBoolean("status_" + nombreLimpio, true);
        
        // DEBUG para que veas en consola si está encontrando la llave
        // System.out.println("LEYENDO DISCO -> status_" + nombreLimpio + ": " + resultado);
        
        return resultado;
    }
}
