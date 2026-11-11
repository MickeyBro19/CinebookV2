package com.mickey.cinebookv2.auth.dto;

import com.mickey.cinebookv2.auth.entity.Role;

public record UserResponse(
		Long id,
		String fullName,
		String email,
		Role role
) {}
