package com.todo.smarttodo.controller;

import com.todo.smarttodo.dto.LoginRequestDTO;
import com.todo.smarttodo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // Login endpoint
    @PostMapping
    public Map<String, String> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(),
                            dto.getPassword()
                    )
            );

            // Generate JWT
            String token = jwtUtil.generateToken(dto.getEmail());

            // Log request and token
            System.out.println("Login attempt for: " + dto.getEmail());
            System.out.println("Generated token: " + token);

            // Send token to frontend
            return Map.of("token", token);

        } catch (AuthenticationException e) {
            System.out.println("Authentication failed for: " + dto.getEmail());
            throw e; // This will return 401 to frontend
        }
    }
}
