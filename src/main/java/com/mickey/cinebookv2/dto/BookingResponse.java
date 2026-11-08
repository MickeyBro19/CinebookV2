package com.mickey.cinebookv2.dto;

import com.mickey.cinebookv2.entity.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
		Long id,
		String bookingReference,
		Long showId,
		BookingStatus status,
		BigDecimal totalAmount,
		Instant createdAt,
		List<Long> seatIds
) {}
