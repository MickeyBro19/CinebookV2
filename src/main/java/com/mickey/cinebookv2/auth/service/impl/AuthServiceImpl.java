package com.mickey.cinebookv2.auth.service.impl;

import com.mickey.cinebookv2.auth.dto.AuthResponse;
import com.mickey.cinebookv2.auth.dto.LoginRequest;
import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import com.mickey.cinebookv2.auth.service.AuthService;
import com.mickey.cinebookv2.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	
	@Override
	public AuthResponse login(LoginRequest loginRequest) {
		String email = loginRequest.email().toLowerCase(Locale.ROOT).trim();
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						email,
						loginRequest.password()
				)
		                                                                  );
		
		CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
		if(principal == null) {
			throw new UsernameNotFoundException("Invalid email or password");
		}
		String token= jwtService.createAccessToken(principal);
		
		return new AuthResponse(
				token,
				"Bearer",
				jwtService.getAccessTokenTtlSeconds()
		);
	}
}
