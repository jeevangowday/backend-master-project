package com.ecommerce.auth_service.service;

import java.time.Instant;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.auth_service.dto.AuthResponse;
import com.ecommerce.auth_service.dto.LoginRequest;
import com.ecommerce.auth_service.dto.RegisterRequest;
import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.repository.RoleRepository;
import com.ecommerce.auth_service.repository.UserRepository;
import com.ecommerce.auth_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Override
	public void register(RegisterRequest request) {

		// 1. Check if user exists
		userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
			throw new RuntimeException("User already exists");
		});

		// 2. Get default role
		Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Role not found"));

		// 3. Create user
		User user = User.builder().email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
				.enabled(true).createdAt(Instant.now()).roles(Set.of(role)).build();

		// 4. Save user
		userRepository.save(user);
	}
	
	@Override
	public AuthResponse login(LoginRequest request) {

	    User user = userRepository.findByEmail(request.getEmail())
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new RuntimeException("Invalid credentials");
	    }

	    String token = jwtUtil.generateToken(user);

	    return AuthResponse.builder()
	            .accessToken(token)
	            .build();
	}

}