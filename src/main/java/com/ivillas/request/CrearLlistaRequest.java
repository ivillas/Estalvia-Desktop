package com.ivillas.request;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Classe Request per a la creació d'una nova llista de la compra.
 * S'utilitza per empaquetar i enviar tota la informació de la llista al servidor.
 */
public class CrearLlistaRequest {
    // --- Atributs de la petició ---
	private Long listaId; 					 // Identificador de la llista (opcional, pot ser null per a noves llistes)                    
	private Long usuariId;                   // Identificador de l'usuari que crea la llista
	private String nombre;                   // Nom de la llista de la compra
	private String descripcion;              // Descripció opcional del contingut de la llista
	private String visibilidad;              // Estat de visibilitat: "PUBLICA" o "PRIVADA"
	private List<ItemLlistaRequest> items;   // Llista de productes que componen la petició

    // --- Constructor ---
    public CrearLlistaRequest() {
        this.items = new ArrayList<>();
    }
	
	// --- Getters i Setters ---

	public Long getUsuariId() { return usuariId; }
	public void setUsuariId(Long usuariId) { this.usuariId = usuariId; }

	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }

	public String getDescripcion() { return descripcion; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

	public String getVisibilidad() { return visibilidad; }
	public void setVisibilidad(String visibilidad) { this.visibilidad = visibilidad; }
	 
	public Long getListaId() { return listaId; }
    public void setListaId(Long listaId) { this.listaId = listaId; }

	public List<ItemLlistaRequest> getItems() { 
		if (this.items == null) {
			this.items = new ArrayList<>(); 
		}
		return this.items; 
	}
	public void setItems(List<ItemLlistaRequest> items) { this.items = items; }

}
