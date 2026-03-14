package com.ivillas.request;

/**
 * Classe Request per a la petició de restabliment de contrasenya.
 * S'utilitza per enviar la nova contrasenya associada al correu electrònic
 * de l'usuari un cop verificat el procés de recuperació.
 */
public class ResetRequest {
    // --- Atributs de la petició ---
    private String email;       // Correu electrònic de l'usuari que vol canviar la contrasenya
    private String newPassword; // La nova contrasenya que s'aplicarà al compte

    /**
     * Constructor per defecte necessari per a la serialització/deserialització JSON.
     */
    public ResetRequest() {}
    
    // --- Getters i Setters ---

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}