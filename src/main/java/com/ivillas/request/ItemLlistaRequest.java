package com.ivillas.request;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Clase request dels Item Llista
 */
public class ItemLlistaRequest {
	private Long productoId;
	private BigDecimal cantidad;
	private String unidad;
	private Long supermercadoPreferidoId;
	private String productoNombre;
	private Map<String, BigDecimal> precios;

	// Getters y Setters
	public Long getProductoId() { return productoId; }
	public void setProductoId(Long productoId) { this.productoId = productoId; }
	public BigDecimal getCantidad() { return cantidad; }
	public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
	public String getUnidad() { return unidad; }
	public void setUnidad(String unidad) { this.unidad = unidad; }
	public Long getSupermercadoPreferidoId() { return supermercadoPreferidoId; }
	public void setSupermercadoPreferidoId(Long supermercadoPreferidoId) { this.supermercadoPreferidoId = supermercadoPreferidoId; }
	public String getProductoNombre() { return productoNombre; }
	public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
	public Map<String, BigDecimal> getPrecios() { return precios; }
	public void setPrecios(Map<String, BigDecimal> map) { this.precios = map; }

}