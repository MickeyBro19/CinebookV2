package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.BookingResponse;
import com.mickey.cinebookv2.dto.CreateBookingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
	BookingResponse createBooking(Long userId, CreateBookingRequest request);
	
	Page<BookingResponse> getMyBookings(Long userId, Pageable pageable);
	
	BookingResponse getBookingByReference(
			String bookingReference,
			Long authenticatedUserId,
			Boolean isAdmin
	                                     );
}
