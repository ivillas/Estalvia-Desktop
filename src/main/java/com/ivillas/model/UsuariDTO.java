package com.ivillas.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuariDTO {
    private Long userId;
    private String username;
    private String email;
    private String dataCreacio; 
    private int nLlistesPrivades;
    private int nLlistesPublices;

    public UsuariDTO() {}

    // Getters y Setters (TODOS sin la palabra static)
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; } // Quitamos static
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; } // Quitamos static
    public void setEmail(String email) { this.email = email; }

    public String getDataCreacio() { return dataCreacio; } // Quitamos static
    public void setDataCreacio(String dataCreacio) { this.dataCreacio = dataCreacio; }

	public int getnLlistesPrivades() {
		return nLlistesPrivades;
	}

	public void setnLlistesPrivades(int nLlistesPrivades) {
		this.nLlistesPrivades = nLlistesPrivades;
	}

	public int getnLlistesPublices() {
		return nLlistesPublices;
	}

	public void setnLlistesPublices(int nLlistesPublices) {
		this.nLlistesPublices = nLlistesPublices;
	}
    
    
    
    
}