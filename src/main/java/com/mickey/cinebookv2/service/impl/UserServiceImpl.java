package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateUserRequest;
import com.mickey.cinebookv2.dto.UserResponse;
import com.mickey.cinebookv2.entity.User;
import com.mickey.cinebookv2.entity.enums.Role;
import com.mickey.cinebookv2.exception.BadRequestException;
import com.mickey.cinebookv2.exception.DuplicateResourceException;
import com.mickey.cinebookv2.repo.UserRepo;
import com.mickey.cinebookv2.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
	
	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	@Transactional
	public UserResponse registerUser(CreateUserRequest request) {
		
		if (request == null) {
			throw new BadRequestException(
					"Registration details are required"
			);
		}
		
		String fullName = request.fullName().trim();
		String email = normalizeEmail(request.email());
		
		if (fullName.isBlank()) {
			throw new BadRequestException(
					"Full name is required"
			);
		}
		
		if (email.isBlank()) {
			throw new BadRequestException(
					"Email is required"
			);
		}
		
		if (userRepo.existsByEmailIgnoreCase(email)) {
			throw new DuplicateResourceException(
					"An account with this email already exists"
			);
		}
		
		User user = User.builder()
				.fullName(fullName)
				.email(email)
				.passwordHash(passwordEncoder.encode(request.password()))
				.role(Role.USER)
				.enabled(true)
				.build();
		
		User savedUser = userRepo.save(user);
		
		return mapToResponse(savedUser);
	}
	
	private String normalizeEmail(String email) {
		
		if (email == null) {
			throw new BadRequestException(
					"Email is required"
			);
		}
		
		return email.trim().toLowerCase(Locale.ROOT);
	}
	
	private UserResponse mapToResponse(User user) {
		
		return new UserResponse(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getRole()
		);
	}
}
