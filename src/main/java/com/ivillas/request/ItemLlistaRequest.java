package com.ivillas.request;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Classe Request per als ítems d'una llista de la compra.
 * S'utilitza per enviar les dades d'un producte al servidor quan es crea o s'edita una llista.
 */
public class ItemLlistaRequest {
    // --- Atributs de la petició ---
	private Long productoId;                // Identificador únic del producte
	private BigDecimal cantidad;            // Quantitat del producte (amb decimals)
	private String unidad;                  // Unitat de mesura (kg, unitats, etc.)
	private Long supermercadoPreferidoId;   // ID del supermercat preferit per aquest ítem
	private String productoNombre;          // Nom del producte
	private Map<String, BigDecimal> precios; // Mapa amb la comparativa de preus per supermercat

	// --- Getters i Setters ---
    
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