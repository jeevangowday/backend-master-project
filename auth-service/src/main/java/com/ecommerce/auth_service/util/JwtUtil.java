package com.ecommerce.auth_service.util;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 🔐 Generate JWT token
    public String generateToken(User user) {

        return Jwts.builder()
                // Subject = unique identifier (email here)
                .setSubject(user.getEmail())

                // Custom claims → extra data inside token
                // Why roles?
                // Needed for authorization (RBAC)
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName) // Extract role names
                        .toList())

                // Token creation time
                .setIssuedAt(new Date())

                // Expiration time (1 hour)
                // Prevents infinite token usage (security)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))

                // Sign token using secret key
                // Ensures token integrity (cannot be tampered)
                .signWith(key)

                // Build final token string
                .compact();
    }

    // 📤 Extract email (subject)
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 📤 Extract expiration time
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // 📤 Extract roles from token
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    // 🔍 Validate token
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // If parsing fails → invalid token
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false; // Token invalid / tampered
        }
    }

    // ⏰ Check expiration
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 🧠 Central parser method
    private Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key) // Verify signature using same key
                .build()
                .parseClaimsJws(token) // Parse token
                .getBody(); // Extract payload (claims)
    }
}