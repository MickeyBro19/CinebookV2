package com.mickey.cinebookv2.auth.service.impl;

import com.mickey.cinebookv2.auth.dto.RefreshResponse;
import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import com.mickey.cinebookv2.auth.entity.RefreshToken;
import com.mickey.cinebookv2.auth.entity.User;
import com.mickey.cinebookv2.auth.repo.RefreshTokenRepo;
import com.mickey.cinebookv2.auth.service.CustomUserDetailsService;
import com.mickey.cinebookv2.auth.service.JwtService;
import com.mickey.cinebookv2.auth.service.RefreshTokenService;
import com.mickey.cinebookv2.exception.BadRequestException;
import com.mickey.cinebookv2.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
	
	private final RefreshTokenRepo refreshTokenRepo;
	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;
	private final SecureRandom secureRandom = new SecureRandom();
	@Value("${app.security.jwt.refresh-token-ttl-seconds}")
	private long refreshTokenTtlSeconds;
	
	@Override
	@Transactional
	public RefreshResponse refresh(String rawRefreshToken) {
		
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new BadRequestException("Refresh token is required");
		}
		
		String hash = hashToken(rawRefreshToken);
		
		RefreshToken current = refreshTokenRepo.findByTokenHashForUpdate(hash)
				.orElseThrow(() ->
						             new ResourceConflictException("Refresh token is invalid")
				            );
		
		Instant now = Instant.now();
		
		if (current.getRevokedAt() != null || !current.getExpiresAt().isAfter(now)) {
			throw new ResourceConflictException("Refresh token is expired or revoked");
		}
		
		User user = current.getUser();
		
		if (!user.isEnabled()) {
			current.setRevokedAt(now);
			throw new ResourceConflictException("Refresh token is no longer valid");
		}
		
		// Revoke the old token before creating its replacement.
		current.setRevokedAt(now);
		
		CustomUserDetails principal =
				userDetailsService.loadByUserId(user.getId());
		
		String accessToken = jwtService.createAccessToken(principal);
		String newRawToken = generateRawToken();
		
		RefreshToken replacement = RefreshToken.builder()
				.user(user)
				.tokenHash(hashToken(newRawToken))
				.createdAt(now)
				.expiresAt(now.plusSeconds(refreshTokenTtlSeconds))
				.build();
		
		refreshTokenRepo.save(replacement);
		
		return new RefreshResponse(
				accessToken,
				"Bearer",
				jwtService.getAccessTokenTtlSeconds(),
				newRawToken
		);
	}
	
	@Override
	@Transactional
	public void revoke(String rawRefreshToken) {
		
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new BadRequestException("Refresh token is required");
		}
		
		refreshTokenRepo.findByTokenHashForUpdate(hashToken(rawRefreshToken))
				.ifPresent(token -> {
					if (token.getRevokedAt() == null) {
						token.setRevokedAt(Instant.now());
					}
				});
	}
	
	private String hashToken(String rawToken) {
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			
			byte[] hash = digest.digest(
					rawToken.getBytes(StandardCharsets.UTF_8)
			                           );
			
			return HexFormat.of().formatHex(hash);
			
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}
	
	@Override
	public String generateRawToken() {
		
		byte[] bytes = new byte[64];
		secureRandom.nextBytes(bytes);
		
		return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(bytes);
	}
}