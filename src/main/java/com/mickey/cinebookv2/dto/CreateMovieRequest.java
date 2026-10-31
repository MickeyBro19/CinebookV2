package com.mickey.cinebookv2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateMovieRequest(
		@NotBlank @Size(max = 200) String title,
		@Size(max = 5000) String synopsis,
		@NotNull @Positive Integer durationMinutes,
		LocalDate releaseDate,
		@Size(max = 80) String language
) {}