package com.mickey.cinebookv2.auth.controller;

import com.mickey.cinebookv2.auth.dto.AuthResponse;
import com.mickey.cinebookv2.auth.dto.CreateUserRequest;
import com.mickey.cinebookv2.auth.dto.LoginRequest;
import com.mickey.cinebookv2.auth.dto.UserResponse;
import com.mickey.cinebookv2.auth.service.AuthService;
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
	
	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(
			@Valid @RequestBody CreateUserRequest request
	                                            ) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(userService.registerUser(request));
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@Valid @RequestBody LoginRequest request
	                                         ) {
		return ResponseEntity.ok(authService.login(request));
	}
}