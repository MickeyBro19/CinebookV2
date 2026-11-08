package com.mickey.cinebookv2.service.impl;

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
        // This User must come from trusted application code, later from the authenticated principal.
        if (authenticatedUser == null || authenticatedUser.getId() == null) {
            throw new BadRequestException("An authenticated user is required to create a booking");
        }
        User user = userRepo.findById(authenticatedUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request == null || request.showId() == null || request.seatIds() == null || request.seatIds().isEmpty()) {
            throw new BadRequestException("A show and at least one seat must be provided");
        }

        List<Long> requestedSeatIds = request.seatIds();
        Set<Long> distinctSeatIds = new LinkedHashSet<>(requestedSeatIds);
        if (distinctSeatIds.contains(null) || distinctSeatIds.size() != requestedSeatIds.size()) {
            throw new BadRequestException("Seat IDs must be non-null and must not contain duplicates");
        }

        Show show = showRepo.findByIdForUpdate(request.showId())
            .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.showId()));

        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new ResourceConflictException("Bookings are allowed only for scheduled shows");
        }
        // Booking cutoff: booking closes at the Show start time.
        if (!show.getStartTime().isAfter(Instant.now())) {
            throw new ResourceConflictException("Booking is closed because the show has started");
        }

        List<Long> seatIdsToLock = new ArrayList<>(distinctSeatIds);
        List<ShowSeat> lockedInventory = showSeatRepo.lockSeats(show.getId(), seatIdsToLock);
        if (lockedInventory.size() != distinctSeatIds.size()) {
            throw new ResourceNotFoundException("One or more requested seats do not belong to this show");
        }
        if (lockedInventory.stream().anyMatch(ss -> ss.getStatus() != SeatInventoryStatus.AVAILABLE)) {
            throw new ResourceConflictException("One or more selected seats have already been booked");
        }

        BigDecimal totalAmount = show.getTicketPrice().multiply(BigDecimal.valueOf(lockedInventory.size()));
        Booking booking = Booking.builder()
            .bookingReference("CB-" + UUID.randomUUID())
            .user(user)
            .show(show)
            .status(BookingStatus.CONFIRMED)
            .totalAmount(totalAmount)
            .createdAt(Instant.now())
            .build();
        Booking savedBooking = bookingRepo.saveAndFlush(booking);

        List<BookingSeat> bookingSeats = lockedInventory.stream()
            .map(ss -> BookingSeat.builder().booking(savedBooking).seat(ss.getSeat()).build())
            .toList();
        bookingSeatRepo.saveAllAndFlush(bookingSeats);

        // These rows are managed entities loaded under PESSIMISTIC_WRITE; dirty checking updates them.
        lockedInventory.forEach(ss -> ss.setStatus(SeatInventoryStatus.BOOKED));
        showSeatRepo.flush();

        List<Long> bookedSeatIds = lockedInventory.stream().map(ss -> ss.getSeat().getId()).toList();
        return new BookingResponse(savedBooking.getId(), savedBooking.getBookingReference(), show.getId(),
            savedBooking.getStatus(), savedBooking.getTotalAmount(), savedBooking.getCreatedAt(), bookedSeatIds);
    }
}
