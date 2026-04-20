package com.ecommerce.auth_service.security;

import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.repository.UserRepository;
import com.ecommerce.auth_service.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		String email = oAuth2User.getAttribute("email");

		User user = userRepository.findByEmail(email)
				.orElseGet(() -> userRepository.save(User.builder().email(email).password("") // no password
						.enabled(true).createdAt(Instant.now()).build()));

		String token = jwtUtil.generateToken(user);

		response.sendRedirect("http://localhost:3000?token=" + token);
	}
}