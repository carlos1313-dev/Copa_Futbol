package com.pes.copa.auth.service;

import com.pes.copa.auth.entity.User;
import com.pes.copa.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    
    
    
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Registra un nuevo usuario
     */
    public User registerUser(User user) {
        log.info("Intentando registrar usuario: {}", user.getUsername());
        
        // Validar que el usuario no exista
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // TODO: Encriptar contraseña (por ahora guardamos en texto plano)
        // En producción usar BCryptPasswordEncoder
        
        User savedUser = userRepository.save(user);
        log.info("Usuario registrado exitosamente: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    /**
     * Autentica un usuario
     */
    public Optional<User> loginUser(String usernameOrEmail, String password) {
        log.info("Intentando autenticar usuario: {}", usernameOrEmail);
        
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // TODO: Comparar contraseña encriptada
            // En producción usar BCryptPasswordEncoder.matches()
            if (user.getPassword().equals(password) && user.getIsActive()) {
                log.info("Usuario autenticado exitosamente: {}", user.getUsername());
                return Optional.of(user);
            } else {
                log.warn("Credenciales inválidas para: {}", usernameOrEmail);
            }
        } else {
            log.warn("Usuario no encontrado: {}", usernameOrEmail);
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca usuario por ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Busca usuario por username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Verifica si un username está disponible
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    /**
     * Verifica si un email está disponible
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}