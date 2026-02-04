package com.ivillas.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivillas.model.ProductePreusDTO;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ProducteServiceClient {

    private static final String URL =
        "http://estalvia.ddns.net:8081/api/productos/con-precios";

    public static List<ProductePreusDTO> getProductos() throws Exception {

       HttpClient client = HttpClient.newHttpClient();
       /*
    	
    	HttpClient client = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("192.168.2.1", 3128))) 
                .build();
  */

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .GET()
            .build();

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(
            response.body(),
            new TypeReference<List<ProductePreusDTO>>() {}
        );
    }
}