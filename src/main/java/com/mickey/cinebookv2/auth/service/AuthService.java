package com.mickey.cinebookv2.auth.service;

import com.mickey.cinebookv2.auth.dto.LoginRequest;
import com.mickey.cinebookv2.auth.dto.RefreshResponse;

public interface AuthService {
	
	RefreshResponse login(LoginRequest request);
}