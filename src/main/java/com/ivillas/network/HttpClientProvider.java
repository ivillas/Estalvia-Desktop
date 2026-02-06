package com.ivillas.network;


import java.net.InetSocketAddress; // Importa la classe per gestionar adreces IP
import java.net.ProxySelector; // Importa la classe per seleccionar proxies
import java.net.http.HttpClient; // Importa la classe HttpClient per fer peticions HTTP
import java.util.prefs.Preferences;

// Classe que proporciona una instància singleton de HttpClient
public class HttpClientProvider {

    private static HttpClient httpClient;
    private static final Preferences prefs = Preferences.userRoot().node("com/estalvia/config");

    // --- LA CLAVE: Leer del disco al arrancar ---
    private static String baseUrl = prefs.get("base_url", "http://estalvia.ddns.net:8081/api");
    private static boolean useProxy = prefs.getBoolean("proxy_enabled", false);
    private static String proxyHost = prefs.get("proxy_host", "");
    private static int proxyPort = prefs.getInt("proxy_port", 0);

    // ...

    public static void setBaseUrl(String newIp, String port) {
        baseUrl = "http://" + newIp + ":" + port + "/api";
        prefs.put("base_url", baseUrl); // Guardamos la IP para la próxima vez
        try { prefs.flush(); } catch (Exception e) {}
    }

    public static synchronized HttpClient getClient() {
        if (httpClient == null) {
            // Ahora useProxy, proxyHost y proxyPort ya tienen los valores del disco
            configureProxy(useProxy, proxyHost, proxyPort);
        }
        return httpClient;
    }

    public static synchronized void configureProxy(boolean use, String host, int port) {
        // Tu código de guardado está perfecto, el problema era solo la carga inicial
        useProxy = use;
        proxyHost = host;
        proxyPort = port;

        prefs.putBoolean("proxy_enabled", use);
        prefs.put("proxy_host", host != null ? host : "");
        prefs.putInt("proxy_port", port);
        try { prefs.flush(); } catch (Exception e) {}

        HttpClient.Builder builder = HttpClient.newBuilder();
        if (use && host != null && !host.isBlank()) {
            builder.proxy(ProxySelector.of(new InetSocketAddress(host, port)));
        }
        httpClient = builder.build();
    }
    
    public static boolean isUseProxy() { return useProxy; }
    public static String getProxyHost() { return proxyHost; }
    public static int getProxyPort() { return proxyPort; }

    public static String getBaseUrl() {
        return baseUrl;
    }
    
    
}

