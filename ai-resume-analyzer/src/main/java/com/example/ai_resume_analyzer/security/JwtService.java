
package com.example.ai_resume_analyzer.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "change-this-secret-key-to-a-long-random-secret-key-123456"
                    .getBytes(StandardCharsets.UTF_8)
    );

    public String generateToken(String username) {

        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + (1000L * 60 * 60);

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            return extractUsername(token).equals(username);
        } catch (Exception exception) {
            return false;
        }
    }
}
