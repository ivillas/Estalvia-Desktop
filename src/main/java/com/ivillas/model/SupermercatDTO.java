package com.ivillas.model;


/**
 * Clase DTO de Supermercats 
 */

public class SupermercatDTO {
	private Long supermercatId;
	private String nom;
	private String web;
	private boolean actiu;

	public SupermercatDTO() {
	}

	// Getters & Setters 
	public SupermercatDTO(Long supermercatId, String nom, String web, boolean actiu) {
		this.supermercatId = supermercatId;
		this.nom = nom;
		this.web = web;
		this.actiu = actiu;
	}

	public Long getSupermercatId() {
		return supermercatId;
	}

	public void setSupermercatId(Long supermercatId) {
		this.supermercatId = supermercatId;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public boolean isActiu() {
		return actiu;
	}

	public void setActiu(boolean actiu) {
		this.actiu = actiu;
	}

}
