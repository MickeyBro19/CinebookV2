package com.mickey.cinebookv2.auth.service.impl;

import com.mickey.cinebookv2.auth.dto.LoginRequest;
import com.mickey.cinebookv2.auth.dto.RefreshResponse;
import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import com.mickey.cinebookv2.auth.entity.RefreshToken;
import com.mickey.cinebookv2.auth.entity.User;
import com.mickey.cinebookv2.auth.repo.RefreshTokenRepo;
import com.mickey.cinebookv2.auth.repo.UserRepo;
import com.mickey.cinebookv2.auth.service.AuthService;
import com.mickey.cinebookv2.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

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
	@Transactional
	public RefreshResponse login(LoginRequest request) {
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.email().trim().toLowerCase(Locale.ROOT),
						request.password()
				)
		                                                                  );
		
		CustomUserDetails principal =
				(CustomUserDetails) authentication.getPrincipal();
		
		if (principal == null) {
			throw new UsernameNotFoundException("Username not found");
		}
		User user = userRepo.findById(principal.getUserId())
				.orElseThrow(() ->
						             new IllegalStateException("Authenticated user no longer exists")
				            );
		
		Instant now = Instant.now();
		String rawRefreshToken = refreshTokenService.generateRawToken();
		
		RefreshToken refreshToken = RefreshToken.builder()
				.user(user)
				.tokenHash(hashToken(rawRefreshToken))
				.createdAt(now)
				.expiresAt(now.plusSeconds(refreshTokenTtlSeconds))
				.build();
		
		refreshTokenRepo.save(refreshToken);
		
		return new RefreshResponse(
				jwtService.createAccessToken(principal),
				"Bearer",
				jwtService.getAccessTokenTtlSeconds(),
				rawRefreshToken
		);
	}
	
	private String hashToken(String rawToken) {
		
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256")
					.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			
			return HexFormat.of().formatHex(hash);
			
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}
}