package com.mickey.cinebookv2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateSeat(
		@NotNull @Positive Long screenId,
		@NotBlank @Size(max = 8) String rowLabel,
		@NotNull @Positive Integer seatNumber
) {
}
