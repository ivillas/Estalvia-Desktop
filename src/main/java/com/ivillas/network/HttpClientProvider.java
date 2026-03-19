package com.ivillas.network;


import java.net.InetSocketAddress; 
import java.net.ProxySelector;
import java.net.http.HttpClient; 
import java.util.prefs.Preferences;

/**
 * Classe que proporciona una instància singleton de HttpClient
 */

public class HttpClientProvider {

    private static HttpClient httpClient;
    private static final Preferences prefs = Preferences.userRoot().node("com/estalvia/config");

    // llegeig al arrancar
    private static String baseUrl = prefs.get("base_url", "http://estalvia.ddns.net:8081/api");
    private static boolean useProxy = prefs.getBoolean("proxy_enabled", false);
    private static String proxyHost = prefs.get("proxy_host", "");
    private static int proxyPort = prefs.getInt("proxy_port", 0);

    

    /**
     * Metode per asignar la direcció i el port
     * @param newIp
     * @param port
     */
    public static void setBaseUrl(String newIp, String port) {
        baseUrl = "http://" + newIp + ":" + port + "/api";
        prefs.put("base_url", baseUrl); //Guardem per la proxima
        try { prefs.flush(); } catch (Exception e) {}
    }

    /**
     * Metode per gestionar el proxi 
     * @return
     */
    public static synchronized HttpClient getClient() {
        if (httpClient == null) {
            // Ara useProxy, proxyHost y proxyPort ja tenen valors en disc
            configureProxy(useProxy, proxyHost, proxyPort);
        }
        return httpClient;
    }

    /**
     * Metode per configurar el proxie
     * @param use
     * @param host
     * @param port
     */
    public static synchronized void configureProxy(boolean use, String host, int port) {
        // 1. Actualitzem les variables d'estat i guardem a Preferences
        useProxy = use;
        proxyHost = host;
        proxyPort = port;

        prefs.putBoolean("proxy_enabled", use);
        prefs.put("proxy_host", host != null ? host : "");
        prefs.putInt("proxy_port", port);
        try { prefs.flush(); } catch (Exception e) {}

        // 2. Preparem el Builder per al HttpClient (API)
        HttpClient.Builder builder = HttpClient.newBuilder();

        if (use && host != null && !host.isBlank()) {
            // --- CAS AMB PROXY ACTIVAT ---
            
            // Configura el proxy per al HttpClient de la API
            builder.proxy(ProxySelector.of(new InetSocketAddress(host, port)));

            // CONFIGURACIÓ GLOBAL: Això permet que JavaFX Image (i altres) funcionin
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", String.valueOf(port));
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", String.valueOf(port));
        } else {
            // --- CAS SENSE PROXY ---
            
            // Netegem les propietats del sistema per evitar errors de connexió
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
        }

        // 3. Creem el client una sola vegada amb la configuració aplicada
        httpClient = builder.build();
    }

    
    /**
     * MEtode per saver l'estat del proxi
     * @return (true o false) (si es f aanar o no)
     */ 
    public static boolean isUseProxy() { return useProxy; }
    
    /**
     * Metode per obtenir el host del proxy
     * @return
     */
    public static String getProxyHost() { return proxyHost; }

    /**
     * Metode per obtenir el port del proxy
     * @return
     */
    public static int getProxyPort() { return proxyPort; }


    /**
     * Metode per obtenir la url base del servidor
     * @return
     */
    public static String getBaseUrl() {
        return baseUrl;
    }
    
    
}

