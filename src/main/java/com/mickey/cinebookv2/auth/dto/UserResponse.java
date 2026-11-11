package com.mickey.cinebookv2.auth.dto;

import com.mickey.cinebookv2.auth.entity.enums.Role;

public record UserResponse(
		Long id,
		String fullName,
		String email,
		Role role
) {}
