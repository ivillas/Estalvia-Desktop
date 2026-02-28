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
 * Clase que gestiona les dades de la app
 */
public class SessionManager {
	private static UsuariDTO usuariLoguejat;
	private static CrearLlistaRequest llistaTemporal = new CrearLlistaRequest();
	private static MainController mainController;
	private static Set<Long> idsFavorits = new HashSet<>();
	
	private static String ultimaBusqueda;

	public static void setultimaBusqueda(String q) { 
		
		ultimaBusqueda = q; 
		
	}
	
	
	public static String getultimaBusqueda() { 
		
		return ultimaBusqueda; 
		
	}

	/**
	 * Metode per obtenir la llista temporal que hi ha en la app
	 * per crearLlista, comparar, afegir productes/llistes...
	 * @return Llista
	 */
	public static CrearLlistaRequest getLlistaTemporal() {
		if (llistaTemporal.getItems() == null) {
			llistaTemporal.setItems(new ArrayList<>());
		}
		return llistaTemporal;
	}

	/**
	 * Metode per obtenir els ids dels productes favorits de l'usuari
	 * @return
	 */
	public static Set<Long> getIdsFavorits() {
		return idsFavorits;
	}

	/**
	 * Metode per carregar els ids dels productes favorits d'un usuari a la llista de ids
	 * Primer llimpiarem la llista  
	 */
	public static void carregarFavorits() {
		if (isLoggedIn()) {
			new Thread(() -> {
				try {
					List<Long> ids = ProducteServiceClient.getIdsFavorits(usuariLoguejat.getUserId());
					idsFavorits.clear();
					idsFavorits.addAll(ids);


					Platform.runLater(() -> {
						// Si el MainController está carregat, intentem refrescar la vista actual
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
	 * Metode per comprovar si un producte es favorit (si esta en la llista)
	 * @param productoId
	 * @return (true o false)
	 */
	public static boolean esFavorit(Long productoId) {
		return idsFavorits.contains(productoId);
	}

	/**
	 * Metode per resetejar la llista temporar de memoria
	 */
	public static void resetListaTemporal() {
		llistaTemporal = new CrearLlistaRequest();
		llistaTemporal.setItems(new ArrayList<>());
	}

	/**
	 * Metode mer afegir l'usuari a l'objecte
	 */
	public static void setUsuari(UsuariDTO u) { 
		usuariLoguejat = u; 
		if (u != null) {
			carregarFavorits(); 
		}
	}

	/**
	 * Metode per obtenir lusuari actual
	 * @return usuari(objecte)
	 */
	public static UsuariDTO getUsuario() { return usuariLoguejat; }


	/**
	 * Metode per comprobar si esta logejat
	 * @return (true o false)
	 */
	public static boolean isLoggedIn() {
		return usuariLoguejat != null;
	}

	/**
	 * Metode per buidar l'usuari y la llista de ids de productes favorits
	 */
	public static void logout() {
		usuariLoguejat = null;
		idsFavorits.clear();
	}


	/**
	 * Metode mer demanat el MAinController
	 * @return mainController
	 */
	public static MainController getMainController() {
		return mainController;
	}

	/**
	 * Metode per afegiel Maincontroller
	 * @param controller
	 */
	public static void setMainController(MainController controller) {
		mainController = controller;
	}

}