package com.ivillas.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ivillas.network.HttpClientProvider;

public class UsuariServiceClient {
	 private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static boolean eliminarCuenta(Long userId, String modo) throws IOException {
        // Accedemos a la URL a través de tu proveedor centralizado
        String urlString = HttpClientProvider.getBaseUrl() + "/usuarios/" + userId + "?modo=" + modo;
        
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        
        // Opcional: Si tu Provider tiene un método para configurar headers (como el JSON), úsalo
        // HttpClientProvider.setCommonHeaders(conn);

        int code = conn.getResponseCode();
        return code == 200;
    }
    
    
    // Nou: Obtenir el total de llistes privades
    public static int getTotalUsuaris() throws Exception {
        String url = HttpClientProvider.getBaseUrl() + "/usuarios/usuaris/count";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = HttpClientProvider.getClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), Integer.class);
    }
}
