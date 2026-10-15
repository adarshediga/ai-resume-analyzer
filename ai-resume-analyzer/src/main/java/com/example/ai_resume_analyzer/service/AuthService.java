
package com.example.ai_resume_analyzer.service;

import com.example.ai_resume_analyzer.entity.AppUser;
import com.example.ai_resume_analyzer.model.RegisterRequest;
import com.example.ai_resume_analyzer.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.ai_resume_analyzer.model.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(
                    CONFLICT, "Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(
                    CONFLICT, "Email already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public AppUser login(LoginRequest request) {

        AppUser user = userRepository
                .findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BadCredentialsException(
                        "Invalid username or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BadCredentialsException(
                    "Invalid username or password");
        }

        return user;
    }

}
