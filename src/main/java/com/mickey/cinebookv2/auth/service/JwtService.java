package com.mickey.cinebookv2.auth.service;

import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
	public static final String TOKEN_TYPE = "access";
	private final SecretKey signingKey;
	private final String issuer;
	@Getter
	private final Long accessTokenTtlSeconds;
	
	public JwtService(
			@Value("${app.security.jwt.secret}") String secretKey,
			@Value("${app.security.jwt.issuer}") String issuer,
			@Value("${app.security.jwt.access-token-ttl-seconds}") Long accessTokenTtlSeconds
	                 ) {
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(secretKey);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("JWT_SECRET must be a valid base64 encoded string");
		}
		if (keyBytes.length != 32) {
			throw new IllegalArgumentException("JWT_SECRET key length must be 32 bytes.");
		}
		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
		this.issuer = issuer;
		this.accessTokenTtlSeconds = accessTokenTtlSeconds;
	}
	
	public String createAccessToken(CustomUserDetails principal) {
		Instant now = Instant.now();
		Instant expiry = Instant.now().plusSeconds(accessTokenTtlSeconds);
		
		return Jwts.builder()
				.issuer(issuer)
				.subject(String.valueOf(principal.getUserId()))
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.claim("token_type", TOKEN_TYPE)
				.signWith(signingKey)
				.compact();
	}
	
	public Long extractUserId(String token) {
		
		Claims claims = Jwts.parser()
				.verifyWith(signingKey)
				.requireIssuer(issuer)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		
		String tokenType = claims.get("token_type", String.class);
		
		if (!TOKEN_TYPE.equals(tokenType)) {
			throw new JwtException("Unexpected token type");
		}
		
		try {
			return Long.valueOf(claims.getSubject());
		} catch (NumberFormatException ex) {
			throw new JwtException("Invalid token subject", ex);
		}
	}
	
}
