package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.auth.entity.User;
import com.mickey.cinebookv2.auth.repo.UserRepo;
import com.mickey.cinebookv2.dto.BookingResponse;
import com.mickey.cinebookv2.dto.CreateBookingRequest;
import com.mickey.cinebookv2.entity.*;
import com.mickey.cinebookv2.entity.enums.BookingStatus;
import com.mickey.cinebookv2.entity.enums.SeatInventoryStatus;
import com.mickey.cinebookv2.entity.enums.ShowStatus;
import com.mickey.cinebookv2.exception.BadRequestException;
import com.mickey.cinebookv2.exception.ResourceConflictException;
import com.mickey.cinebookv2.exception.ResourceNotFoundException;
import com.mickey.cinebookv2.repo.*;
import com.mickey.cinebookv2.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
	private final BookingRepo bookingRepo;
	private final BookingSeatRepo bookingSeatRepo;
	private final ShowRepo showRepo;
	private final ShowSeatRepo showSeatRepo;
	private final UserRepo userRepo;
	
	@Override
	@Transactional
	public BookingResponse createBooking(User authenticatedUser, CreateBookingRequest request) {
		
		//  Validate the trusted user reference.
		if (authenticatedUser == null || authenticatedUser.getId() == null) {
			throw new BadRequestException("An authenticated user is required to create a booking");
		}
		
		//  Load the managed User entity.
		User user = userRepo.findById(authenticatedUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		
		//  Validate the request.
		if (request == null || request.showId() == null || request.seatIds() == null || request.seatIds().isEmpty()) {
			throw new BadRequestException("A show and at least one seat must be provided");
		}
		
		//  Reject null and duplicate seat IDs.
		List<Long> requestedSeatIds = request.seatIds();
		Set<Long> distinctSeatIds = new LinkedHashSet<>(requestedSeatIds);
		if (distinctSeatIds.contains(null) || distinctSeatIds.size() != requestedSeatIds.size()) {
			throw new BadRequestException("Seat IDs must be non-null and must not contain duplicates");
		}
		
		//  Lock the Show row.
		Show show = showRepo.findByIdForUpdate(request.showId())
				.orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.showId()));
		
		//  Validate the Show state and booking cutoff: booking closes at the Show start time.
		if (show.getStatus() != ShowStatus.SCHEDULED) {
			throw new ResourceConflictException("Bookings are allowed only for scheduled shows");
		}
		if (!show.getStartTime().isAfter(Instant.now())) {
			throw new ResourceConflictException("Booking is closed because the show has started");
		}
		
		//  Lock the requested ShowSeat inventory rows.
		List<Long> seatIdsToLock = new ArrayList<>(distinctSeatIds);
		List<ShowSeat> lockedInventory = showSeatRepo.lockSeats(show.getId(), seatIdsToLock);
		
		//  Verify every requested seat belongs to this Show.
		if (lockedInventory.size() != distinctSeatIds.size()) {
			throw new ResourceNotFoundException("One or more requested seats do not belong to this show");
		}
		
		//  Reject unavailable seats.
		if (lockedInventory.stream().anyMatch(ss -> ss.getStatus() != SeatInventoryStatus.AVAILABLE)) {
			throw new ResourceConflictException("One or more selected seats have already been booked");
		}
		
		//  Calculate the total from the stored ticket price.
		BigDecimal totalAmount = show.getTicketPrice().multiply(BigDecimal.valueOf(lockedInventory.size()));
		
		//  Create and persist the Booking.
		Booking booking = Booking.builder()
				.bookingReference("CB-" + UUID.randomUUID())
				.user(user)
				.show(show)
				.status(BookingStatus.CONFIRMED)
				.totalAmount(totalAmount)
				.createdAt(Instant.now())
				.build();
		Booking savedBooking = bookingRepo.saveAndFlush(booking);
		
		//  Create BookingSeat records.
		List<BookingSeat> bookingSeats = lockedInventory.stream()
				.map(ss -> BookingSeat.builder().booking(savedBooking).seat(ss.getSeat()).build())
				.toList();
		bookingSeatRepo.saveAllAndFlush(bookingSeats);
		
		//  Update the managed ShowSeat inventory rows.
		lockedInventory.forEach(ss -> ss.setStatus(SeatInventoryStatus.BOOKED));
		
		// Force the inventory update before returning.
		showSeatRepo.flush();
		
		List<Long> bookedSeatIds = lockedInventory.stream().map(ss -> ss.getSeat().getId()).toList();
		return new BookingResponse(savedBooking.getId(), savedBooking.getBookingReference(), show.getId(),
		                           savedBooking.getStatus(), savedBooking.getTotalAmount(), savedBooking.getCreatedAt(), bookedSeatIds);
	}
	
	
}
