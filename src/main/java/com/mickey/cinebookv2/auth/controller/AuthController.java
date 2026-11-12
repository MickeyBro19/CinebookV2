package com.mickey.cinebookv2.auth.controller;

import com.mickey.cinebookv2.auth.dto.*;
import com.mickey.cinebookv2.auth.service.AuthService;
import com.mickey.cinebookv2.auth.service.RefreshTokenService;
import com.mickey.cinebookv2.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final UserService userService;
	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;
	
	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(
			@Valid @RequestBody CreateUserRequest request
	                                            ) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(userService.registerUser(request));
	}
	
	@PostMapping("/login")
	public ResponseEntity<RefreshResponse> login(
			@Valid @RequestBody LoginRequest request
	                                            ) {
		return ResponseEntity.ok(authService.login(request));
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponse> refresh(
			@Valid @RequestBody RefreshRequest request
	                                              ) {
		return ResponseEntity.ok(
				refreshTokenService.refresh(request.refreshToken())
		                        );
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@Valid @RequestBody LogoutRequest request
	                                  ) {
		refreshTokenService.revoke(request.refreshToken());
		return ResponseEntity.noContent().build();
	}
}