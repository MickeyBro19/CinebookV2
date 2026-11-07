package com.mickey.cinebookv2.dto;

public record BookedSeatResponse(
		Long seatId,
		String rowLabel,
		Integer seatNumber
) {}