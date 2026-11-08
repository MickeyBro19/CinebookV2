package com.mickey.cinebookv2.dto;

import com.mickey.cinebookv2.entity.enums.Role;

public record UserResponse(
		Long id,
		String fullName,
		String email,
		Role role
) {}
