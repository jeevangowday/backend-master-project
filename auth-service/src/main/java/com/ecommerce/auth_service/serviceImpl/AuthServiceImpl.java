package com.ecommerce.auth_service.serviceImpl;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.auth_service.dto.AuthResponse;
import com.ecommerce.auth_service.dto.LoginRequest;
import com.ecommerce.auth_service.dto.RegisterRequest;
import com.ecommerce.auth_service.entity.RefreshToken;
import com.ecommerce.auth_service.entity.Role;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.exception.InvalidCredentialsException;
import com.ecommerce.auth_service.exception.UserAlreadyExistsException;
import com.ecommerce.auth_service.exception.UserNotFoundException;
import com.ecommerce.auth_service.repository.RefreshTokenRepository;
import com.ecommerce.auth_service.repository.RoleRepository;
import com.ecommerce.auth_service.repository.UserRepository;
import com.ecommerce.auth_service.service.AuthService;
import com.ecommerce.auth_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Override
	public void register(RegisterRequest request) {
		// 1. Check if user exists
		userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
			throw new UserAlreadyExistsException("User already exists");
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
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new InvalidCredentialsException("Invalid credentials");
		}

		String token = jwtUtil.generateToken(user);
		String refreshTokenValue = UUID.randomUUID().toString();
		RefreshToken refreshToken = RefreshToken.builder().token(refreshTokenValue).user(user)
				.expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
				.build();

		refreshTokenRepository.save(refreshToken);

		return AuthResponse.builder().accessToken(token).refreshToken(refreshTokenValue).build();
	}

	@Override
	public AuthResponse refresh(String refreshTokenValue) {

		RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
				.orElseThrow(() -> new RuntimeException("Invalid refresh token"));

		if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
			throw new RuntimeException("Refresh token expired");
		}

		String newAccessToken = jwtUtil.generateToken(refreshToken.getUser());

		return AuthResponse.builder().accessToken(newAccessToken).refreshToken(refreshTokenValue).build();
	}
}