package com.ivillas.model;

import java.math.BigDecimal;

public class ItemLlistaDTO {

    private Long itemId;
    private Long productoId;
    private String nombreProducto;
    private BigDecimal cantidad;
    private String unidad;
    private boolean marcada;
    private Long supermercadoPreferidoId;
	
    
    public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public Long getProductoId() {
		return productoId;
	}
	public void setProductoId(Long productoId) {
		this.productoId = productoId;
	}
	public String getNombreProducto() {
		return nombreProducto;
	}
	public void setNombreProducto(String nombreProducto) {
		this.nombreProducto = nombreProducto;
	}
	public BigDecimal getCantidad() {
		return cantidad;
	}
	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}
	public String getUnidad() {
		return unidad;
	}
	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}
	public boolean isMarcada() {
		return marcada;
	}
	public void setMarcada(boolean marcada) {
		this.marcada = marcada;
	}
	public Long getSupermercadoPreferidoId() {
		return supermercadoPreferidoId;
	}
	public void setSupermercadoPreferidoId(Long supermercadoPreferidoId) {
		this.supermercadoPreferidoId = supermercadoPreferidoId;
	}

    // getters & setters
    
    
}