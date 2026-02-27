package com.ivillas.service;

import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.prefs.Preferences;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivillas.model.SupermercatDTO;
import com.ivillas.network.HttpClientProvider;

/**
 * Clase Service per gestionar supermercats amb la api del servidor
 */
public class SupermercatServiceClient {
	private static final Preferences prefs = Preferences.userNodeForPackage(SupermercatServiceClient.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Metode per obtenir la llista dels supermercats
	 * @return
	 * @throws Exception
	 */
	public static List<SupermercatDTO> getAll() throws Exception {
		HttpClient client = HttpClientProvider.getClient();

		String url = HttpClientProvider.getBaseUrl() + "/supermercats";

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.build();

		// Resposta y Mapeig
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		List<SupermercatDTO> lista = mapper.readValue(response.body(), new TypeReference<List<SupermercatDTO>>() {});

		// Sincronitzem al el HD (Preferences)
		for (SupermercatDTO s : lista) {
			String nombreLimpio = s.getNom().toLowerCase().replace(" ", "").trim();
			// Llegim si el usuari el te activat o desactivat en aquesta maquina
			boolean estadoLocal = prefs.getBoolean("status_" + nombreLimpio, true);
			s.setActiu(estadoLocal);
		}

		return lista;
	}

	/**
	 * Metode Per guardar(Desde ConfigController) ---
	 * @param nom
	 * @param actiu
	 */
	public static void saveStatus(String nom, boolean actiu) {
		// Llimpiem el nom junt i minuscules ("Plus Fresc" -> "plusfresc")
		String nombreLimpio = nom.toLowerCase().replace(" ", "").trim();
		// guardem amb prefix status
		prefs.putBoolean("status_" + nombreLimpio, actiu);

		try { 
			prefs.flush(); 
			System.out.println("GUARDADO EN DISCO -> status_" + nombreLimpio + ": " + actiu);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metode Per Llegir(Desde CrearLlistaController) ---
	 * @param nombre
	 * @return
	 */
	public static boolean getLocalStatus(String nombre) {
		// Llimpiem el nom que ve de la API (igual que al guardar)
		String nombreLimpio = nombre.toLowerCase().replace(" ", "").trim();

		// busquem el mateix prefix "status_"
		// Si no existeix per defecte retornem true
		boolean resultado = prefs.getBoolean("status_" + nombreLimpio, true);

		return resultado;
	}
}
