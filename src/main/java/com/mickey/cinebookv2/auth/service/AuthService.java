package com.mickey.cinebookv2.auth.service;

import com.mickey.cinebookv2.auth.dto.AuthResponse;
import com.mickey.cinebookv2.auth.dto.LoginRequest;

public interface AuthService {
	AuthResponse login(LoginRequest loginRequest);
}
