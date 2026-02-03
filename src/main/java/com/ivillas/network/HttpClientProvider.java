package com.ivillas.network;


import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

public class HttpClientProvider {

    private static HttpClient httpClient;

    private HttpClientProvider() {}

    public static synchronized HttpClient getClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder().build();
        }
        return httpClient;
    }

    public static synchronized void configureProxy(
            boolean useProxy,
            String host,
            int port
    ) {
        HttpClient.Builder builder = HttpClient.newBuilder();

        if (useProxy && host != null && !host.isBlank()) {
            builder.proxy(
                ProxySelector.of(
                    new InetSocketAddress(host, port)
                )
            );
        }

        httpClient = builder.build();
    }
}
