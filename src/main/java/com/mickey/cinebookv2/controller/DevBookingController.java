package com.mickey.cinebookv2.controller;

import com.mickey.cinebookv2.dto.*;
import com.mickey.cinebookv2.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev/bookings")
@RequiredArgsConstructor
@Profile("dev")
public class DevBookingController {
	
	private final BookingService bookingService;
	
	@Value("${cinebook.booking.dev-user-email}")
	private String devUserEmail;
	
	@PostMapping
	public ResponseEntity<BookingResponse> createBooking(
			@Valid @RequestBody CreateBookingRequest request
	                                                    ) {
		
		BookingResponse response =
				bookingService.createBooking(devUserEmail, request);
		
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}
}