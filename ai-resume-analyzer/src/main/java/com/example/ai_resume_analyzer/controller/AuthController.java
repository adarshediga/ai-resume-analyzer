
package com.example.ai_resume_analyzer.controller;

import com.example.ai_resume_analyzer.entity.AppUser;
import com.example.ai_resume_analyzer.model.RegisterRequest;
import com.example.ai_resume_analyzer.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.ai_resume_analyzer.model.LoginRequest;
import com.example.ai_resume_analyzer.security.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService,JwtService jwtService) {
        this.authService = authService;
        this.jwtService=jwtService;
    }




    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        AppUser user = authService.register(request);

        Map<String, Object> response = Map.of(
                "message", "User registered successfully",
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request) {

        AppUser user = authService.login(request);

        String token = jwtService.generateToken(user.getUsername());

        Map<String, Object> response = Map.of(
                "message", "Login successful",
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "token", token,
                "tokenType", "Bearer"
        );

        return ResponseEntity.ok(response);
    }


}
