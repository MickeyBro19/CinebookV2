package com.mickey.cinebookv2.auth.dto;

public record RefreshResponse(
		String accessToken,
		String tokenType,
		long expiresInSeconds,
		String refreshToken
) {}