package com.mickey.cinebookv2.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateShowRequest(
		
		@NotNull
		@Positive
		Long movieId,
		
		@NotNull
		@Positive
		Long screenId,
		
		@NotNull
		Instant startTime,
		
		@NotNull
		Instant endTime,
		
		@NotNull
		@DecimalMin("0.00")
		@Digits(integer = 8, fraction = 2)
		BigDecimal ticketPrice

) {}