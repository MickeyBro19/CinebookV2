package com.mickey.cinebookv2.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateBookingRequest(
		
		@NotNull
		@Positive
		Long showId,
		
		@NotEmpty
		@Size(max = 20, message = "A booking cannot contain more than 20 seats")
		List<@NotNull @Positive Long> seatIds

) {}