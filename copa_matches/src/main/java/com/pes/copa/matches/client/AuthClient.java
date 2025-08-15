/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pes.copa.matches.client;

/**
 *
 * @author sangr
 */
import com.pes.copa.matches.dto.external.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component
public class AuthClient {
    
//    @Value("${services.auth.url:http://localhost:8082}")
//    private String authServiceUrl;
//    
//    private final RestTemplate restTemplate;
//    
//    public AuthClient(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//    
//    /**
//     * Obtiene información básica de un usuario
//     */
//    public UserDTO getUser(Long userId) {
//        try {
//            String url = authServiceUrl + "/api/v1/users/" + userId;
//            return restTemplate.getForObject(url, UserDTO.class);
//        } catch (RestClientException e) {
//            throw new RuntimeException("Failed to get user info: " + e.getMessage());
//        }
//    }
}