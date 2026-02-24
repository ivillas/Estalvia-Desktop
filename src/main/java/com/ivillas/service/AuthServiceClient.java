package com.ivillas.service;

/**
 * Clase Service Client de inici de sessió
 */
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivillas.model.UsuariDTO;
import com.ivillas.network.HttpClientProvider;

public class AuthServiceClient {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Usem el Provier per si se cambie la IP en config, s'actualitzi
	 * @param endpoint
	 * @return
	 */
	private static String getUrl(String endpoint) {
		return HttpClientProvider.getBaseUrl().replace("/api", "/auth") + endpoint;
	}

	public static void login(String user, String pass, Consumer<UsuariDTO> onSuccess, Consumer<String> onError) {
		Map<String, String> dades = Map.of("username", user, "password", pass);

		enviarPeticio("/login", dades, responseBody -> {
			try {
				//parsear l'usuario
				UsuariDTO usuari = objectMapper.readValue(responseBody, UsuariDTO.class);

				// Demanar les estadistiques
				demanarEstadistiques(usuari, onSuccess, onError);

			} catch (Exception e) {
				onError.accept("Error en les dades d'usuari.");
			}
		}, onError);
	}

	/**
	 * Metode per demanar estadistiques d'un usuari
	 * @param usuari
	 * @param onSuccess
	 * @param onError
	 */
	private static void demanarEstadistiques(UsuariDTO usuari, Consumer<UsuariDTO> onSuccess, Consumer<String> onError) {
		String urlStats = HttpClientProvider.getBaseUrl() + "/listas/stats/" + usuari.getUserId();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(urlStats))
				.GET()
				.build();

		HttpClientProvider.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
		.thenAccept(res -> {
			try {
				if (res.statusCode() == 200) {
					// Parsear el JSON {"privades":2, "publiques":1}
					Map<String, Integer> stats = objectMapper.readValue(res.body(), new TypeReference<Map<String, Integer>>() {});

					// 4. Injectar els valors al DTO
					usuari.setnLlistesPrivades(stats.getOrDefault("privades", 0));
					usuari.setnLlistesPublices(stats.getOrDefault("publiques", 0));

					// Retornar l'usuari complert al controlador
					onSuccess.accept(usuari);
				} else {
					// si fallen les stats, retornem l'usuari
					onSuccess.accept(usuari);
				}
			} catch (Exception e) {
				onSuccess.accept(usuari); // Error de parseig, pero el login es OK
			}
		})
		.exceptionally(e -> {
			onSuccess.accept(usuari); // Sense red per stats, pero login OK
			return null;
		});
	}

	/**
	 * Metode per registrar un usuari
	 */
	public static void register(String user, String email, String pass, Runnable onSuccess, Consumer<String> onError) {
		Map<String, String> datos = Map.of("username", user, "email", email, "password", pass);
		enviarPeticio("/register", datos, responseBody -> onSuccess.run(), onError);
	}

	/**
	 * Metode per recuperar una contrasenya
	 * @param email
	 * @param onSuccess
	 * @param onError
	 */
	public static void processRecovery(String email, Runnable onSuccess, Consumer<String> onError) {
		Map<String, String> dades = Map.of("email", email);
		enviarPeticio("/forgot-password", dades, res -> onSuccess.run(), onError);
	}

	/**
	 * Metode per enviar la peticio 
	 * @param endpoint
	 * @param data
	 * @param onSuccess
	 * @param onError
	 */
	private static void enviarPeticio(String endpoint, Map<String, ?> data, Consumer<String> onSuccess, Consumer<String> onError) {

		try {
			String body = objectMapper.writeValueAsString(data);

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(getUrl(endpoint)))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(body))
					.build();

			// Fa anar el client del provier
			HttpClientProvider.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenAccept(response -> {
				int status = response.statusCode();
				if (status == 200) {
					onSuccess.accept(response.body());
				} else {
					// Traduccio de codis que retorna per comprensio de l'usuari
					String mensajeMsg;
					switch (status) {
					case 401:
						mensajeMsg = "Usuari o contrasenya incorrectes.";
						break;
					case 403:
						mensajeMsg = "No tens permís per accedir.";
						break;
					case 404:
						mensajeMsg = "El servidor no troba l'usuari.";
						break;
					case 500:
						mensajeMsg = "Error intern del servidor. Torna-ho a provar més tard.";
						break;
					default:
						mensajeMsg = "Error inesperat (Codi " + status + ")";
						break;
					}
					onError.accept(mensajeMsg);
				}
			})
			.exceptionally(e -> {
				onError.accept("No hay conexión con el servidor");
				return null;
			});

		} catch (Exception e) {
			onError.accept("Error al preparar la petición: " + e.getMessage());
		}
	}
}
