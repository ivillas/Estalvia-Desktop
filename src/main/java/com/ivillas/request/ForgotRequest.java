package com.ivillas.request;


/**
 * Classe Request per a la petició de recuperació de contrasenya.
 * S'utilitza per enviar el correu electrònic de l'usuari al servidor
 * i iniciar el procés de restabliment.
 */
public class ForgotRequest {
    // --- Atributs de la petició ---
	private String email; // Correu electrònic associat al compte que es vol recuperar

    /**
     * Constructor per defecte necessari per a la serialització/deserialització JSON.
     */
	public ForgotRequest() {}

	// --- Getters & Setters ---

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
}