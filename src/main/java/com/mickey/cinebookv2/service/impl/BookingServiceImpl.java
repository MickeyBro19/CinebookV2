package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.*;
import com.mickey.cinebookv2.entity.*;
import com.mickey.cinebookv2.entity.enums.*;
import com.mickey.cinebookv2.exception.*;
import com.mickey.cinebookv2.repo.*;
import com.mickey.cinebookv2.service.BookingService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
	
	private final BookingRepo bookingRepo;
	private final BookingSeatRepo bookingSeatRepo;
	private final ShowRepo showRepo;
	private final ShowSeatRepo showSeatRepo;
	private final UserRepo userRepo;
	
	@Value("${cinebook.booking.cutoff:PT15M}")
	private Duration bookingCutoff;
	
	@Override
	@Transactional
	public BookingResponse createBooking(
			String userEmail,
			CreateBookingRequest request
	                                    ) {
		
		// Reject duplicate seat IDs.
		List<Long> requestedSeatIds = request.seatIds();
		
		Set<Long> distinctSeatIds = new HashSet<>(requestedSeatIds);
		
		if (distinctSeatIds.size() != requestedSeatIds.size()) {
			throw new BadRequestException(
					"Duplicate seat IDs are not allowed"
			);
		}
		
		// Load the trusted user identity.
		User user = userRepo.findByEmailIgnoreCase(userEmail)
				.filter(User::isEnabled)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Booking user was not found or is disabled"
				));
		
		// Load and validate the Show.
		Show show = showRepo.findById(request.showId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Show not found with id: " + request.showId()
				));
		
		if (show.getStatus() != ShowStatus.SCHEDULED) {
			throw new ResourceConflictException(
					"Bookings are allowed only for scheduled shows"
			);
		}
		
		// Enforce the booking cutoff.
		Instant bookingDeadline =
				show.getStartTime().minus(bookingCutoff);
		
		if (!Instant.now().isBefore(bookingDeadline)) {
			throw new ResourceConflictException(
					"The booking cutoff has passed for this show"
			);
		}
		
		// Lock inventory rows in a stable order.
		List<Long> sortedSeatIds = distinctSeatIds.stream()
				.sorted()
				.toList();
		
		List<ShowSeat> lockedShowSeats =
				showSeatRepo.lockSeats(show.getId(), sortedSeatIds);
		
		// Ensure every requested seat belongs to this Show.
		if (lockedShowSeats.size() != distinctSeatIds.size()) {
			throw new ResourceNotFoundException(
					"One or more selected seats do not belong to this show"
			);
		}
		
		// Ensure every requested seat is AVAILABLE.
		boolean unavailable = lockedShowSeats.stream()
				.anyMatch(showSeat ->
						          showSeat.getStatus() != SeatInventoryStatus.AVAILABLE
				         );
		
		if (unavailable) {
			throw new ResourceConflictException(
					"One or more selected seats are no longer available"
			);
		}
		
		// Calculate price from the database, never from the request.
		BigDecimal totalAmount = show.getTicketPrice()
				.multiply(BigDecimal.valueOf(lockedShowSeats.size()));
		
		// Create and persist the Booking.
		Booking booking = Booking.builder()
				.bookingReference("CBK-" + UUID.randomUUID())
				.user(user)
				.show(show)
				.status(BookingStatus.CONFIRMED)
				.totalAmount(totalAmount)
				.build();
		
		Booking savedBooking = bookingRepo.saveAndFlush(booking);
		
		// Create BookingSeat relationship records.
		List<BookingSeat> bookingSeats = lockedShowSeats.stream()
				.map(showSeat -> BookingSeat.builder()
						.booking(savedBooking)
						.seat(showSeat.getSeat())
						.build())
				.toList();
		
		bookingSeatRepo.saveAll(bookingSeats);
		
		// Mark the inventory as BOOKED.
		lockedShowSeats.forEach(showSeat ->
				                        showSeat.setStatus(SeatInventoryStatus.BOOKED)
		                       );
		
		showSeatRepo.saveAll(lockedShowSeats);
		
		// Flush before returning. Any failure rolls back the transaction.
		bookingSeatRepo.flush();
		showSeatRepo.flush();
		
		return toResponse(savedBooking, lockedShowSeats);
	}
	
	private BookingResponse toResponse(
			Booking booking,
			List<ShowSeat> showSeats
	                                  ) {
		
		List<BookedSeatResponse> seats = showSeats.stream()
				.map(showSeat -> new BookedSeatResponse(
						showSeat.getSeat().getId(),
						showSeat.getSeat().getRowLabel(),
						showSeat.getSeat().getSeatNumber()
				))
				.toList();
		
		return new BookingResponse(
				booking.getId(),
				booking.getBookingReference(),
				booking.getShow().getId(),
				booking.getStatus(),
				booking.getTotalAmount(),
				booking.getCreatedAt(),
				seats
		);
	}
}