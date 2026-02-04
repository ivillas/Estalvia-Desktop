package com.ivillas.service;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivillas.model.UsuariDTO;
import com.ivillas.network.HttpClientProvider;

public class AuthServiceClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Usamos el Provider para que si cambias la IP en Config, esto se actualice
    private static String getUrl(String endpoint) {
        return HttpClientProvider.getBaseUrl().replace("/api", "/auth") + endpoint;
    }

    public static void login(String user, String pass, Consumer<UsuariDTO> onSuccess, Consumer<String> onError) {
        Map<String, String> datos = Map.of("username", user, "password", pass);
        
        enviarPeticion("/login", datos, responseBody -> {
            try {
                // 1. Parsear el usuario
                UsuariDTO usuario = objectMapper.readValue(responseBody, UsuariDTO.class);
                
                // 2. Pedir las estadísticas inmediatamente
                pedirEstadisticas(usuario, onSuccess, onError);
                
            } catch (Exception e) {
                onError.accept("Error en les dades d'usuari.");
            }
        }, onError);
    }

    private static void pedirEstadisticas(UsuariDTO usuario, Consumer<UsuariDTO> onSuccess, Consumer<String> onError) {
        // Construimos la URL: api/listas/stats/{id}
        String urlStats = HttpClientProvider.getBaseUrl() + "/listas/stats/" + usuario.getUserId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlStats))
                .GET()
                .build();

        HttpClientProvider.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(res -> {
                try {
                    if (res.statusCode() == 200) {
                        // 3. Parsear el JSON {"privades":2, "publiques":1}
                        Map<String, Integer> stats = objectMapper.readValue(res.body(), new TypeReference<Map<String, Integer>>() {});
                        
                        // 4. Inyectar los valores en el DTO
                        usuario.setnLlistesPrivades(stats.getOrDefault("privades", 0));
                        usuario.setnLlistesPublices(stats.getOrDefault("publiques", 0));
                        
                        // 5. Devolver el usuario completo al controlador
                        onSuccess.accept(usuario);
                    } else {
                        // Si fallan las stats, devolvemos al menos el usuario
                        onSuccess.accept(usuario);
                    }
                } catch (Exception e) {
                    onSuccess.accept(usuario); // Error de parseo, pero el login fue OK
                }
            })
            .exceptionally(e -> {
                onSuccess.accept(usuario); // Sin red para stats, pero login OK
                return null;
            });
    }
    
    public static void register(String user, String email, String pass, Runnable onSuccess, Consumer<String> onError) {
        Map<String, String> datos = Map.of("username", user, "email", email, "password", pass);
        enviarPeticion("/register", datos, responseBody -> onSuccess.run(), onError);
    }

    public static void processRecovery(String email, Runnable onSuccess, Consumer<String> onError) {
        Map<String, String> datos = Map.of("email", email);
        enviarPeticion("/forgot-password", datos, res -> onSuccess.run(), onError);
    }

    private static void enviarPeticion(String endpoint, Map<String, ?> data, Consumer<String> onSuccess, Consumer<String> onError) {
        
        try {
            String body = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getUrl(endpoint)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // USA EL CLIENTE DEL PROVIDER (IMPORTANTE PARA EL PROXY)
            HttpClientProvider.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                int status = response.statusCode();
                if (status == 200) {
                    onSuccess.accept(response.body());
                } else {
                    // TRADUCCIÓN DE CÓDIGOS
                    String mensajeMsg;
                    switch (status) {
                        case 401:
                            mensajeMsg = "Usuari o contrasenya incorrectes.";
                            break;
                        case 403:
                            mensajeMsg = "No tens permís per accedir.";
                            break;
                        case 404:
                            mensajeMsg = "El servidor no troba l'usuari.";
                            break;
                        case 500:
                            mensajeMsg = "Error intern del servidor. Torna-ho a provar més tard.";
                            break;
                        default:
                            mensajeMsg = "Error inesperat (Codi " + status + ")";
                            break;
                    }
                    onError.accept(mensajeMsg);
                }
            })
                .exceptionally(e -> {
                    onError.accept("No hay conexión con el servidor");
                    return null;
                });

        } catch (Exception e) {
            onError.accept("Error al preparar la petición: " + e.getMessage());
        }
    }
}

	    
	    /*
	
	
		    // Este es el método de verificación que usa la librería BCrypt
	    private boolean verificarPassword(String passwordPlana, String hashDeLaBD) {
	        try {
	            return BCrypt.checkpw(passwordPlana, hashDeLaBD);
	        } catch (Exception e) {
	            return false;
	        }
	    }  
	
	    private void enviarEmail(String destinatario, String codigo) {
	        String remitente = "tu_correo@gmail.com";
	        String password = "tu_password_de_aplicacion"; // No es la normal de Gmail

	        Properties props = new Properties();
	        props.put("mail.smtp.host", "smtp.gmail.com");
	        props.put("mail.smtp.port", "587");
	        props.put("mail.smtp.auth", "true");
	        props.put("mail.smtp.starttls.enable", "true");

	        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                // Importante: PasswordAuthentication también debe ser de jakarta.mail
	                return new PasswordAuthentication(remitente, appPassword);
	            }
	        });

	        try {
	            Message message = new MimeMessage(session);
	            message.setFrom(new InternetAddress(remitente));
	            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
	            message.setSubject("Codi de Recuperació - Estalvia!");
	            message.setText("El teu codi de seguretat es: " + codigo);

	            Transport.send(message);
	            System.out.println("Email eniat exitosament.");
	        } catch (MessagingException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    private String hashPassword(String passwordPlana) {
	        // El método hashpw genera el hash. 
	        // gensalt() determina la complejidad (por defecto es 10)
	        return BCrypt.hashpw(passwordPlana, BCrypt.gensalt());
	    }
	    */
