package com.mickey.cinebookv2.auth.service.impl;

import com.mickey.cinebookv2.auth.dto.AuthResponse;
import com.mickey.cinebookv2.auth.dto.LoginRequest;
import com.mickey.cinebookv2.auth.dto.RefreshResponse;
import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import com.mickey.cinebookv2.auth.entity.RefreshToken;
import com.mickey.cinebookv2.auth.entity.User;
import com.mickey.cinebookv2.auth.repo.RefreshTokenRepo;
import com.mickey.cinebookv2.auth.repo.UserRepo;
import com.mickey.cinebookv2.auth.service.AuthService;
import com.mickey.cinebookv2.auth.service.JwtService;
import com.mickey.cinebookv2.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenServiceImpl refreshTokenService;
	private final RefreshTokenRepo refreshTokenRepo;
	private final UserRepo userRepo;
	
	@Value("${app.security.jwt.refresh-token-ttl-seconds}")
	private long refreshTokenTtlSeconds;
	
	@Override
	public AuthResponse login(LoginRequest request) {
		
		CustomUserDetails principal = authenticate(request);
		
		return new AuthResponse(
				jwtService.createAccessToken(principal),
				"Bearer",
				jwtService.getAccessTokenTtlSeconds()
		);
	}
	
	@Override
	@Transactional
	public RefreshResponse loginWithRefreshToken(LoginRequest request) {
		
		CustomUserDetails principal = authenticate(request);
		
		String accessToken = jwtService.createAccessToken(principal);
		String rawRefreshToken = refreshTokenService.generateRawToken();
		User user=userRepo.findByEmailIgnoreCase(request.email()).orElseThrow(()->new ResourceNotFoundException("User doesn't exist"));
		
		RefreshToken refreshToken = RefreshToken.builder()
				.user(user)
				.tokenHash(hashToken(rawRefreshToken))
				.createdAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(refreshTokenTtlSeconds))
				.build();
		
		refreshTokenRepo.save(refreshToken);
		
		return new RefreshResponse(
				accessToken,
				"Bearer",
				jwtService.getAccessTokenTtlSeconds(),
				rawRefreshToken
		);
	}
	
	private String hashToken(String rawToken) {
		
		try {
			byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
					.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			
			return java.util.HexFormat.of().formatHex(hash);
			
		}
		catch (java.security.NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}
	
	private CustomUserDetails authenticate(LoginRequest request) {
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.email().trim().toLowerCase(java.util.Locale.ROOT),
						request.password()
				)
		                                                                  );
		
		return (CustomUserDetails) authentication.getPrincipal();
	}
}