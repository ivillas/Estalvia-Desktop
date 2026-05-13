package com.ivillas.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ivillas.controller.MainController;
import com.ivillas.model.UsuariDTO;
import com.ivillas.request.CrearLlistaRequest;
import com.ivillas.service.ProducteServiceClient;

import javafx.application.Platform;

/**
 * Classe que gestiona les dades globals de la sessió de l'aplicació.
 * Actua com un magatzem central (Singleton) per a l'usuari loguejat, 
 * la llista temporal de la compra i els productes preferits.
 */
public class SessionManager {
	private static UsuariDTO usuariLoguejat;                   // Dades de l'usuari que ha iniciat sessió
	private static CrearLlistaRequest llistaTemporal = new CrearLlistaRequest(); // Llista de treball actual
	private static MainController mainController;              // Referència al controlador principal de la UI
	private static Set<Long> idsFavorits = new HashSet<>();    // Conjunt d'IDs de productes marcats com a preferits
	
	private static String ultimaBusqueda;                      // Emmagatzema el darrer text cercat per a persistència entre vistes

    /**
     * Defineix el text de l'última cerca realitzada.
     */
	public static void setultimaBusqueda(String q) { 
		
		ultimaBusqueda = q; 
		
	}
	
    /**
     * Obté el text de l'última cerca realitzada.
     */
	public static String getultimaBusqueda() { 
		
		return ultimaBusqueda; 
		
	}

	/**
	 * Mètode per obtenir la llista temporal que hi ha en la app.
	 * S'utilitza per crearLlista, comparar, afegir productes/llistes...
	 * @return Llista actual de treball (CrearLlistaRequest)
	 */
	public static CrearLlistaRequest getLlistaTemporal() {
		if (llistaTemporal.getItems() == null) {
			llistaTemporal.setItems(new ArrayList<>());
		}
		return llistaTemporal;
	}

	/**
	 * Mètode per obtenir els ids dels productes favorits de l'usuari.
	 * @return Un Set amb els identificadors dels productes.
	 */
	public static Set<Long> getIdsFavorits() {
		return idsFavorits;
	}

	/**
	 * Mètode per carregar els ids dels productes favorits d'un usuari a la llista de ids.
	 * Primer llimpiarem la llista per evitar duplicats o dades residuals.
	 */
	public static void carregarFavorits() {
		if (isLoggedIn()) {
			// Fil secundari per no bloquejar la interfície mentre es consulta l'API
			new Thread(() -> {
				try {
					List<Long> ids = ProducteServiceClient.getIdsFavorits(usuariLoguejat.getUserId());
					idsFavorits.clear();
					idsFavorits.addAll(ids);

					// Actualització de la interfície al fil principal de JavaFX
					Platform.runLater(() -> {
						// Si el MainController està carregat, intentem refrescar la vista actual (per actualitzar cors, etc.)
						if (mainController != null) {
							mainController.refrescarVistaActualSiEsPerfil();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	/**
	 * Mètode per comprovar si un producte és favorit (si està en la llista de IDs).
	 * @param productoId Identificador del producte a comprovar.
	 * @return true si és favorit, false en cas contrari.
	 */
	public static boolean esFavorit(Long productoId) {
		return idsFavorits.contains(productoId);
	}

	/**
	 * Mètode per resetejar la llista temporal de memòria (per exemple, després de desar-la).
	 */
	public static void resetListaTemporal() {
		llistaTemporal = new CrearLlistaRequest();
		llistaTemporal.setItems(new ArrayList<>());
	}

	/**
	 * Mètode per afegir l'usuari a l'objecte de sessió i carregar els seus favorits.
	 */
	public static void setUsuari(UsuariDTO u) { 
		usuariLoguejat = u; 
		if (u != null) {
			carregarFavorits(); 
		}
	}

	/**
	 * Mètode per obtenir l'usuari actual loguejat.
	 * @return L'objecte UsuariDTO de la sessió.
	 */
	public static UsuariDTO getUsuario() { return usuariLoguejat; }


	/**
	 * Mètode per comprovar si l'usuari ha iniciat sessió.
	 * @return true si hi ha un usuari a la sessió, false si és nul.
	 */
	public static boolean isLoggedIn() {
		return usuariLoguejat != null;
	}

	/**
	 * Mètode per buidar l'usuari i la llista de IDs de productes favorits (tancament de sessió).
	 */
	public static void logout() {
		usuariLoguejat = null;
		idsFavorits.clear();
	}


	/**
	 * Mètode per demanar el MainController des de qualsevol punt de l'aplicació.
	 * @return La referència al controlador principal.
	 */
	public static MainController getMainController() {
		return mainController;
	}

	/**
	 * Mètode per assignar el MainController a la sessió.
	 * @param controller El controlador principal de la vista.
	 */
	public static void setMainController(MainController controller) {
		mainController = controller;
	}

	/**
	 * Mètode per assignar una nova llista temporal a la sessió (Edició).
	 * @param novaLlista El nou borrador amb els productes carregats.
	 */
	public static void setLlistaTemporal(CrearLlistaRequest novaLlista) {
		if (novaLlista == null) {
			llistaTemporal = new CrearLlistaRequest();
			llistaTemporal.setItems(new ArrayList<>());
		} else {
			llistaTemporal = novaLlista;
			// Ens assegurem que la llista d'ítems no sigui nul·la internament
			if (llistaTemporal.getItems() == null) {
				llistaTemporal.setItems(new ArrayList<>());
			}
		}
	}
	
}