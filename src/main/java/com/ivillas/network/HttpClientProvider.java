package com.ivillas.network;


import java.net.InetSocketAddress; // Importa la classe per gestionar adreces IP
import java.net.ProxySelector; // Importa la classe per seleccionar proxies
import java.net.http.HttpClient; // Importa la classe HttpClient per fer peticions HTTP

// Classe que proporciona una instància singleton de HttpClient
public class HttpClientProvider {

    private static HttpClient httpClient; // Instància estàtica de HttpClient
    private static String baseUrl = "http://192.168.1.250:8081/api"; // URL base per a les peticions
    
    // Variables per guardar l'estat del proxy
    private static boolean useProxy = false;
    private static String proxyHost = "";
    private static int proxyPort = 0;
    /*
	
 	HttpClient client = HttpClient.newBuilder()
             .proxy(ProxySelector.of(new InetSocketAddress("192.168.2.1", 3128))) 
             .build();
*/
    
    // Mètode per obtenir la URL base
    public static String getBaseUrl() {
        return baseUrl;
    }
    
    public static void setBaseUrl(String newIp, String port) {
        // Permet actualitzar la URL base amb una nova IP i port
        baseUrl = "http://" + newIp + ":" + port + "/api";
    }

    // Constructor privat per evitar la creació d'instàncies externes
    private HttpClientProvider() {}

    // Mètode per obtenir la instància de HttpClient
    public static synchronized HttpClient getClient() {
        // Comprova si ja s'ha creat la instància
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder().build(); // Crea una nova instància si no existeix
        }
        return httpClient; // Retorna la instància de HttpClient
    }

    // Mètode per configurar un proxy per al HttpClient
    public static synchronized void configureProxy(
            boolean use, // Indica si s'ha d'utilitzar un proxy
            String host, // Adreça del servidor proxy
            int port // Port del servidor proxy
    ) {
        useProxy = use;
        proxyHost = host;
        proxyPort = port;
    	
        HttpClient.Builder builder = HttpClient.newBuilder(); // Crea un nou constructor de HttpClient

        // Comprova si s'ha d'utilitzar un proxy i si l'adreça no és nul·la ni buida
        if (useProxy && host != null && !host.isBlank()) {
            builder.proxy(
                ProxySelector.of(
                    new InetSocketAddress(host, port) // Estableix el proxy amb l'adreça i el port
                )
            );
        }

        httpClient = builder.build(); // Construeix la nova instància de HttpClient amb la configuració de proxy
    }
    
    
    public static boolean isUseProxy() { return useProxy; }
    public static String getProxyHost() { return proxyHost; }
    public static int getProxyPort() { return proxyPort; }

    
}

