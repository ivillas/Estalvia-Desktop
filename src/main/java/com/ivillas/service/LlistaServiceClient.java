package com.ivillas.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ivillas.model.LlistaDTO;
import com.ivillas.network.HttpClientProvider;
import com.ivillas.request.CrearLlistaRequest;

/**
 * Clase Service Client de la Llista
 */
public class LlistaServiceClient {

	private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()); // Imprescindible para LocalDateTime

	/**
	 * Metode per Obtenir les llistes publiques(per a tots)
	 * @return
	 * @throws Exception
	 */
	public static List<LlistaDTO> getPubliques() throws Exception {
		String url = HttpClientProvider.getBaseUrl() + "/listas/publiques";
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Error: " + response.statusCode());
		}
		return mapper.readValue(response.body(), new TypeReference<List<LlistaDTO>>() {});
	}

	/**
	 * Metode per obtenir el total de les llistes privades
	 * @return
	 * @throws Exception
	 */
	public static int getTotalPrivades() throws Exception {
		String url = HttpClientProvider.getBaseUrl() + "/listas/privades/stats";

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		return mapper.readValue(response.body(), Integer.class);
	}

	/**
	 * Metode per Obtenir les llistes d'un usuari (Privades/Propies)
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public static List<LlistaDTO> getPerUsuari(Long userId) throws Exception {
		String url = HttpClientProvider.getBaseUrl() + "/listas?usuariId=" + userId;
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		validarResposta(response);
		return mapper.readValue(response.body(), new TypeReference<List<LlistaDTO>>() {});
	}

	/**
	 * Metode Obtenir estadistiques, per el perfil d'usuari
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Long> getStats(Long userId) throws Exception {
		String url = HttpClientProvider.getBaseUrl() + "/listas/stats/" + userId;
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		validarResposta(response);
		return mapper.readValue(response.body(), new TypeReference<Map<String, Long>>() {});
	}

	/**
	 * Metode per copiar tots els productes de una llista (publica o privada) a la llista temporal
	 * @param llistaPublica
	 * @param miUserId
	 * @throws Exception
	 */
	public static void copiarALaMevaLlista(LlistaDTO llistaPublica, Long miUserId) throws Exception {

		Map<String, Object> req = new HashMap<>();
		req.put("usuariId", miUserId);
		req.put("nom", "Copia: " + llistaPublica.getNombre());
		req.put("descripcio", llistaPublica.getDescripcion());
		req.put("visibilitat", "PRIVADA");

		// Mapeig manual dels items simplificat
		req.put("items", llistaPublica.getItems());

		String body = mapper.writeValueAsString(req);
		String url = HttpClientProvider.getBaseUrl() + "/listas";

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		validarResposta(response);
	}

	/**
	 * Metode per crear una llista
	 * @param req
	 * @return
	 * @throws Exception
	 */
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

		validarResposta(response);
		return Long.parseLong(response.body());
	}

	/**
	 *  Metode per no repetir validacions
	 * @param response
	 */
	private static void validarResposta(HttpResponse<String> response) {
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			System.err.println("Error Body: " + response.body());
			throw new RuntimeException("Error en servidor: " + response.statusCode());
		}
	}

	/**
	 * MEtode per eliminar una llista especifica
	 * @param llistaId
	 * @param usuariId
	 * @throws Exception
	 */
	public static void eliminarLlista(Long llistaId, Long usuariId) throws Exception {
		String url = HttpClientProvider.getBaseUrl() + "/listas/" + llistaId + "/" + usuariId;
		System.out.println("URL FINAL: " + url);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.DELETE() 
				.build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		// Reutilitzem el metode de validaó
		validarResposta(response);
	}
	
	/**
	 * Mètode per actualitzar una llista existent.
	 * @param llistaId Identificador de la llista a modificar.
	 * @param req Objecte amb les noves dades i ítems.
	 * @throws Exception Si el servidor respon amb error.
	 */
	public static void actualitzarLlista(Long llistaId, CrearLlistaRequest req) throws Exception {
		String json = mapper.writeValueAsString(req);
		// El endpoint estructurado sigue el estándar REST: /listas/{id}
		String url = HttpClientProvider.getBaseUrl() + "/listas/" + llistaId;

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(json)) // Petición PUT para actualizar
				.build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		// Reutilizamos tu método de validación ya existente
		validarResposta(response);
	}
}