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

/**
 * Clase Service per gestionar productes amb la api del servidor
 */
public class ProducteServiceClient {

	private static final ObjectMapper mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule());

	/**
	 *  Obtenir tots els productes de la base de dades
	 * @return
	 * @throws Exception
	 */
	public static List<ProductePreusDTO> getProductes() throws Exception {
		String url = HttpClientProvider.getBaseUrl() + "/productos/con-precios";

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		return mapper.readValue(response.body(), new TypeReference<List<ProductePreusDTO>>() {});
	}

	/**
	 * Metode per obtenir la data mes nova dels productes
	 * Per fer servir com ultima actualització 
	 * @param productos
	 * @return
	 */
	public static LocalDateTime ultimaDataProductes(List<ProductePreusDTO> productos) {
		if (productos == null || productos.isEmpty()) return null;
		return productos.stream()
				.map(ProductePreusDTO::getLastUpdate)
				.filter(Objects::nonNull)
				.max(LocalDateTime::compareTo)
				.orElse(null);
	}


	/**
	 * Metode per Obtenir els ids dels favorits de l'usuari
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public static List<Long> getIdsFavorits(Long userId) throws Exception {
		String url = HttpClientProvider.getBaseUrl() + "/favoritos/ids/" + userId;

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.build();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		return mapper.readValue(response.body(), new TypeReference<List<Long>>() {});
	}

	/**
	 * Metode per afegir o eliminar favorit
	 * @param userId
	 * @param prodId
	 * @param esAfegir
	 * @return
	 * @throws Exception
	 */
	public static boolean gestionarFavoritAPI(Long userId, Long prodId, boolean esAfegir) throws Exception {
		String url = HttpClientProvider.getBaseUrl() + "/favoritos/" + userId + "/" + prodId;

		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));

		if (esAfegir) builder.POST(HttpRequest.BodyPublishers.noBody());
		else builder.DELETE();

		HttpResponse<String> response = HttpClientProvider.getClient()
				.send(builder.build(), HttpResponse.BodyHandlers.ofString());

		return response.statusCode() == 200;
	}
}