package com.ivillas.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.ivillas.network.HttpClientProvider;

public class UsuariServiceClient {

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
}
