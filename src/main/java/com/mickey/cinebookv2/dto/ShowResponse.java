package com.mickey.cinebookv2.dto;

import com.mickey.cinebookv2.entity.enums.ShowStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ShowResponse(
		Long id,
		Long movieId,
		Long screenId,
		Instant startTime,
		Instant endTime,
		BigDecimal ticketPrice,
		ShowStatus status
) {}