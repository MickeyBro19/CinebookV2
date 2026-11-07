package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.BookingResponse;
import com.mickey.cinebookv2.dto.CreateBookingRequest;

public interface BookingService {
	
	BookingResponse createBooking(
			String userEmail,
			CreateBookingRequest request
	                             );
}