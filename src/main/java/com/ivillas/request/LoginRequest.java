package com.ivillas.request;

/**
 * Classe Request per a la petició d'inici de sessió (Login).
 * S'utilitza per enviar les credencials de l'usuari al servidor
 * per a la seva autenticació.
 */
public class LoginRequest {
    // --- Atributs de la petició ---
	private String username; // Nom d'usuari o àlies per a l'accés
	private String password; // Contrasenya de l'usuari

    /**
     * Constructor per defecte necessari per a la serialització/deserialització JSON.
     */
	public LoginRequest() {}

	// --- Getters i Setters ---

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
}