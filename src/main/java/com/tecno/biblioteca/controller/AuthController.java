// AuthController.java

package com.tecno.biblioteca.controller;

import com.tecno.biblioteca.model.ResponseMessage;
import com.tecno.biblioteca.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Controller
@RequestMapping("/")
public class AuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public String redirectToLogin() {
        return "redirect:/login"; // Redirige a la vista de inicio de sesión
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Retorna la vista login.html
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro"; // Retorna la vista registro.html
    }

    @PostMapping("/api/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        try {
            // Mostrar en consola que se recibió el registro
            System.out.println("Nombre: " + usuario.getNombre());
            System.out.println("Apellido: " + usuario.getApellido());
            System.out.println("Email: " + usuario.getEmail());

            // Verificar si el usuario ya existe
            if (existeUsuario(usuario.getEmail())) {
                return ResponseEntity.status(409).body(new ResponseMessage(false, "El usuario ya existe. ¿Deseas restablecer la clave?"));
            }

            // Insertar el nuevo usuario
            insertarUsuario(usuario);

            // Generar y almacenar el token
            String token = generarToken();
            insertarToken(token, usuario.getEmail());

            // Respuesta de éxito
            return ResponseEntity.ok().body(new ResponseMessage(true, "Registro exitoso. Por favor, revisa tu correo para completar el registro."));
        } catch (Exception e) {
            e.printStackTrace(); // Muestra el error en la consola
            return ResponseEntity.status(500).body(new ResponseMessage(false, "Error al registrar usuario: " + e.getMessage()));
        }
    }

    private boolean existeUsuario(String email) {
        String query = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        return jdbcTemplate.queryForObject(query, new Object[]{email}, Integer.class) > 0;
    }

    private void insertarUsuario(Usuario usuario) {
        String query = "INSERT INTO usuarios (nombre, apellido, email, PASSWORD) VALUES (?, ?, ?, ?)";
        // Aquí deberías encriptar la contraseña antes de insertarla
        jdbcTemplate.update(query, usuario.getNombre(), usuario.getApellido(), usuario.getEmail(), "contraseña_encriptada");
    }

    private void insertarToken(String token, String email) {
        Timestamp fechaExpiracion = new Timestamp(new Date().getTime() + 86400000); // 24 horas
        String query = "INSERT INTO links_registro_usuarios (token, email, fecha_expiracion) VALUES (?, ?, ?)";
        jdbcTemplate.update(query, token, email, fechaExpiracion);
    }

    private String generarToken() {
        return UUID.randomUUID().toString();
    }
}
