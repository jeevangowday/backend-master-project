package com.ecommerce.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecommerce.auth_service.security.JwtAuthenticationFilter;
import com.ecommerce.auth_service.security.OAuth2SuccessHandler;
import com.ecommerce.auth_service.security.RateLimitingFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final RateLimitingFilter rateLimitingFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				// Disable CSRF
				.csrf(csrf -> csrf.disable())

				// Stateless session (JWT)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// Authorization
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/auth/**", "/oauth2/**").permitAll().anyRequest().authenticated())

				// OAuth2 login
				.oauth2Login(oauth -> oauth.successHandler(oAuth2SuccessHandler))

				// ✅ FIRST → Rate Limiting
				.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)

				// ✅ SECOND → JWT Filter
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
