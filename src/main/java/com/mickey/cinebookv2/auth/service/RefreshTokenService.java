package com.mickey.cinebookv2.auth.service;

import com.mickey.cinebookv2.auth.dto.RefreshResponse;

public interface RefreshTokenService {
	
	RefreshResponse refresh(String rawRefreshToken);
	
	void revoke(String rawRefreshToken);
	
	String generateRawToken();
}