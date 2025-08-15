package com.pes.copa.auth.controller;

import com.pes.copa.auth.entity.User;
import com.pes.copa.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Registro de usuario
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User registeredUser = authService.registerUser(user);
            
            // No retornamos la contraseña
            registeredUser.setPassword(null);
            
            response.put("success", true);
            response.put("message", "Usuario registrado exitosamente");
            response.put("user", registeredUser);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            log.error("Error al registrar usuario: {}", e.getMessage());
            
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Login de usuario
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();
        
        String usernameOrEmail = loginData.get("usernameOrEmail");
        String password = loginData.get("password");
        
        if (usernameOrEmail == null || password == null) {
            response.put("success", false);
            response.put("message", "Usuario/email y contraseña son requeridos");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        Optional<User> userOpt = authService.loginUser(usernameOrEmail, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null); // No retornamos la contraseña
            
            response.put("success", true);
            response.put("message", "Inicio de sesión exitoso");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Credenciales inválidas");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<User> userOpt = authService.findById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null);
            
            response.put("success", true);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Verificar disponibilidad de username
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Object>> checkUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        boolean available = authService.isUsernameAvailable(username);
        
        response.put("available", available);
        response.put("message", available ? "Username disponible" : "Username no disponible");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verificar disponibilidad de email
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmail(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        boolean available = authService.isEmailAvailable(email);
        
        response.put("available", available);
        response.put("message", available ? "Email disponible" : "Email no disponible");
        
        return ResponseEntity.ok(response);
    }
}