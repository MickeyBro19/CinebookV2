package com.mickey.cinebookv2.controller;

import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import com.mickey.cinebookv2.dto.BookingResponse;
import com.mickey.cinebookv2.dto.CreateBookingRequest;

import com.mickey.cinebookv2.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
	
	private final BookingService bookingService;
	
	@PostMapping
	public ResponseEntity<BookingResponse> createBooking(
			@AuthenticationPrincipal CustomUserDetails principal,
			@Valid @RequestBody CreateBookingRequest request
	                                                    ) {
		BookingResponse response = bookingService.createBooking(
				principal.getUserId(),
				request
		                                                       );
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@GetMapping("/me")
	public ResponseEntity<Page<BookingResponse>> getMyBookings(
			@AuthenticationPrincipal CustomUserDetails principal,
			Pageable pageable
	                                                          ) {
		return ResponseEntity.ok(
				bookingService.getMyBookings(principal.getUserId(), pageable)
		                        );
	}
	
	@GetMapping("/{reference}")
	public ResponseEntity<BookingResponse> getBookingByReference(
			@PathVariable String reference,
			@AuthenticationPrincipal CustomUserDetails principal
	                                                            ) {
		boolean isAdmin = principal.getAuthorities().stream()
				.anyMatch(authority ->
						          authority.getAuthority().equals("ROLE_ADMIN")
				         );
		
		return ResponseEntity.ok(
				bookingService.getBookingByReference(
						reference,
						principal.getUserId(),
						isAdmin
				                                    )
		                        );
	}
}