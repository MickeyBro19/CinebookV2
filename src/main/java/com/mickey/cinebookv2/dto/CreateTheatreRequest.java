package com.mickey.cinebookv2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTheatreRequest(
		@NotBlank @Size(max = 160) String name,
		@NotBlank @Size(max = 500) String address,
		@NotBlank @Size(max = 100) String city
) {}