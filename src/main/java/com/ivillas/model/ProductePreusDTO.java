package com.ivillas.model;

import java.math.BigDecimal;
import java.util.Map;

public class ProductePreusDTO {

    public Long producteId;
    public String marca;
    public String nombre;
    public String unidad;
    public String pack;
    private String descripcio;
    private String imatge;
    private String envase;
    private java.time.LocalDateTime lastUpdate;
    public Map<String, BigDecimal> precios;
        
	public Long getProducteId() {
		return producteId;
	}
	public void setProducteId(Long producteId) {
		this.producteId = producteId;
	}
	public String getMarca() {
		return marca;
	}
	public void setMarca(String marca) {
		this.marca = marca;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getUnidad() {
		return unidad;
	}
	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public Map<String, BigDecimal> getPrecios() {
		return precios;
	}
	public void setPrecios(Map<String, BigDecimal> precios) {
		this.precios = precios;
	}
    
	@Override
	public String toString() {
	    return nombre; // O el nombre del campo donde guardas el nombre del producto
	}
	public String getDescripcio() {
		return descripcio;
	}
	public void setDescripcio(String descripcio) {
		this.descripcio = descripcio;
	}
	public String getImatge() {
		return imatge;
	}
	public void setImatge(String imatge) {
		this.imatge = imatge;
	}
	public String getEnvase() {
		return envase;
	}
	public void setEnvase(String envase) {
		this.envase = envase;
	}
	public java.time.LocalDateTime getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(java.time.LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
    
    
    
}