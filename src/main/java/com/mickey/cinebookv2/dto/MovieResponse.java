package com.mickey.cinebookv2.dto;

import java.time.LocalDate;

public record MovieResponse(
		Long id,
		String title,
		String synopsis,
		Integer durationMinutes,
		LocalDate releaseDate,
		String language,
		String classification,
		String posterUrl
) {}
