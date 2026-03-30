package com.ecommerce.auth_service.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecommerce.auth_service.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Get Authorization header
        String header = request.getHeader("Authorization");

        // If no token → continue without authentication
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token (remove "Bearer ")
        String token = header.substring(7);

        // Validate token
        if (jwtUtil.validateToken(token)) {

            // Extract email (identity)
            String email = jwtUtil.extractEmail(token);

            // Extract roles from token
            List<String> roles = jwtUtil.extractRoles(token);

            // Convert roles into Spring Security authorities
            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new) // Required format
                    .toList();

            // Create authentication object
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Continue request
        filterChain.doFilter(request, response);
    }
}