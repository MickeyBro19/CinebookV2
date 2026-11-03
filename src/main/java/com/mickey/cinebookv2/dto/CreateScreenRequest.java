package com.mickey.cinebookv2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateScreenRequest(
		@NotNull @Positive Long theatreId,
		@NotBlank @Size(max = 80) String screenName
) {
}
