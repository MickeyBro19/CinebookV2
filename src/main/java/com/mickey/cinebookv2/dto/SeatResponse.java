package com.mickey.cinebookv2.dto;

public record SeatResponse(
		Long id,
		Long screenId,
		String rowLabel,
		Integer seatNumber
) {
}
