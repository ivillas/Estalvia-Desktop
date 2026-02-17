package com.ivillas.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ivillas.model.ProductePreusDTO;
import com.ivillas.network.HttpClientProvider;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ProducteServiceClient {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // Obtenir tots els productes de la base de dades
    public static List<ProductePreusDTO> getProductos() throws Exception {
        String url = HttpClientProvider.getBaseUrl() + "/productos/con-precios";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = HttpClientProvider.getClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), new TypeReference<List<ProductePreusDTO>>() {});
    }

    public static LocalDateTime ultimaDataProductes(List<ProductePreusDTO> productos) {
        if (productos == null || productos.isEmpty()) return null;
        return productos.stream()
                .map(ProductePreusDTO::getLastUpdate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
    
    
    //  Obtenir els ids dels favorits de l'usuari
    public static List<Long> getIdsFavoritos(Long userId) throws Exception {
        String url = HttpClientProvider.getBaseUrl() + "/favoritos/ids/" + userId;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = HttpClientProvider.getClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), new TypeReference<List<Long>>() {});
    }

    // afegir o eliminar favorit
    public static boolean gestionarFavoritoAPI(Long userId, Long prodId, boolean esAñadir) throws Exception {
        String url = HttpClientProvider.getBaseUrl() + "/favoritos/" + userId + "/" + prodId;
        
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        
        if (esAñadir) builder.POST(HttpRequest.BodyPublishers.noBody());
        else builder.DELETE();

        HttpResponse<String> response = HttpClientProvider.getClient()
            .send(builder.build(), HttpResponse.BodyHandlers.ofString());

        return response.statusCode() == 200;
    }
}