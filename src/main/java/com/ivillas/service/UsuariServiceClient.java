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

/**
 * Clase Service per gestionar el compte d'usuari
 */
public class UsuariServiceClient {
	private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

	/**
	 * Metode per eliminar compte
	 * @param userId 
	 * @param mode (tot el compte o deixar les llistes publiques
	 * @return
	 * @throws IOException
	 */
	public static boolean eliminarCompte(Long userId, String mode) throws IOException {
		String urlString = HttpClientProvider.getBaseUrl() + "/usuarios/" + userId + "?modo=" + mode;

		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("DELETE");

		int code = conn.getResponseCode();
		return code == 200;
	}


	/**
	 * Metode per obtenir el total de llistes privades
	 * @return
	 * @throws Exception
	 */
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
