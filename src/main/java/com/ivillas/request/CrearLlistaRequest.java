package com.ivillas.request;

import java.util.List;

/**
 * Clase Request de CrearLlista
 */

public class CrearLlistaRequest {
	private Long usuariId;
	private String nombre;
	private String descripcion;
	private String visibilidad;
	private List<ItemLlistaRequest> items;

	// Getters y Setters
	public Long getUsuariId() { return usuariId; }
	public void setUsuariId(Long usuariId) { this.usuariId = usuariId; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public String getDescripcion() { return descripcion; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	public String getVisibilidad() { return visibilidad; }
	public void setVisibilidad(String visibilidad) { this.visibilidad = visibilidad; }
	public List<ItemLlistaRequest> getItems() { return items; }
	public void setItems(List<ItemLlistaRequest> items) { this.items = items; }
}
