package com.mickey.cinebookv2.auth.dto;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresInSeconds
) {}