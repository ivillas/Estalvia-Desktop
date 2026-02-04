package com.ivillas.service;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ivillas.model.LlistaDTO;
import com.ivillas.network.HttpClientProvider;
import com.ivillas.request.CrearLlistaRequest;

public class LlistaServiceClient {

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()); // Imprescindible para LocalDateTime

    
    // 1. Obtener Listas Públicas (para todos)
    public static List<LlistaDTO> getPublicas() throws Exception {
    	String url = HttpClientProvider.getBaseUrl() + "/listas/publiques";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = HttpClientProvider.getClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        
        // ESTO ES CLAVE: Mira qué imprime en la consola del IDE
        System.out.println("URL llamada: " + request.uri());
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Cuerpo recibido: " + response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error: " + response.statusCode());
        }

        // Si el cuerpo es HTML (empieza por <), esto fallará. 
        // El print de arriba te dirá por qué el servidor manda HTML.
        return mapper.readValue(response.body(), new TypeReference<List<LlistaDTO>>() {});
    }

    // 2. Obtener Listas de un Usuario (Privadas/Propias)
    public static List<LlistaDTO> getPorUsuario(Long userId) throws Exception {
        String url = HttpClientProvider.getBaseUrl() + "/listas?usuariId=" + userId;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        HttpResponse<String> response = HttpClientProvider.getClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        validarRespuesta(response);
        return mapper.readValue(response.body(), new TypeReference<List<LlistaDTO>>() {});
    }

    // 3. Obtener Estadísticas (para el panel)
    public static Map<String, Long> getStats(Long userId) throws Exception {
        String url = HttpClientProvider.getBaseUrl() + "/listas/stats/" + userId;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        HttpResponse<String> response = HttpClientProvider.getClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        validarRespuesta(response);
        return mapper.readValue(response.body(), new TypeReference<Map<String, Long>>() {});
    }


    public static void copiarAmiLista(LlistaDTO listaPublica, Long miUserId) throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("usuariId", miUserId);
        req.put("nombre", "Copia: " + listaPublica.getNombre());
        req.put("descripcion", listaPublica.getDescripcion());
        req.put("visibilidad", "PRIVADA");
        
        // Mapeo manual de items simplificado
        req.put("items", listaPublica.getItems()); 

        String body = mapper.writeValueAsString(req);
        String url = HttpClientProvider.getBaseUrl() + "/listas";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = HttpClientProvider.getClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        
        validarRespuesta(response);
    }
    
    public static Long crearLlista(CrearLlistaRequest req) throws Exception {
        String json = mapper.writeValueAsString(req);
        String url = HttpClientProvider.getBaseUrl() + "/listas";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClientProvider.getClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        
        validarRespuesta(response);
        return Long.parseLong(response.body());
    }
    // Método de utilidad para no repetir validaciones
    private static void validarRespuesta(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            System.err.println("Error Body: " + response.body());
            throw new RuntimeException("Error en servidor: " + response.statusCode());
        }
    }
}