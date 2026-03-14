package com.ivillas.request;

/**
 * Classe Request per a la petició de registre de nou usuari.
 * S'utilitza per enviar les dades necessàries per crear un compte al servidor.
 */
public class RegisterRequest {
    // --- Atributs de la petició ---
    private String username; // Nom d'usuari o àlies escollit
    private String email;    // Adreça de correu electrònic
    private String password; // Contrasenya per al nou compte

    /**
     * Constructor per defecte necessari per a la serialització/deserialització JSON.
     */
    public RegisterRequest() {}
    
    // --- Getters i Setters ---
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}