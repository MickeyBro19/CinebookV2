package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.auth.entity.User;
import com.mickey.cinebookv2.auth.repo.UserRepo;
import com.mickey.cinebookv2.dto.BookingResponse;
import com.mickey.cinebookv2.dto.CreateBookingRequest;
import com.mickey.cinebookv2.entity.Booking;
import com.mickey.cinebookv2.entity.Seat;
import com.mickey.cinebookv2.entity.Show;
import com.mickey.cinebookv2.entity.ShowSeat;
import com.mickey.cinebookv2.entity.enums.SeatInventoryStatus;
import com.mickey.cinebookv2.entity.enums.ShowStatus;
import com.mickey.cinebookv2.exception.BadRequestException;
import com.mickey.cinebookv2.exception.ResourceConflictException;
import com.mickey.cinebookv2.exception.ResourceNotFoundException;
import com.mickey.cinebookv2.repo.BookingRepo;
import com.mickey.cinebookv2.repo.BookingSeatRepo;
import com.mickey.cinebookv2.repo.ShowRepo;
import com.mickey.cinebookv2.repo.ShowSeatRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
	
	@Mock
	private BookingRepo bookingRepo;
	
	@Mock
	private BookingSeatRepo bookingSeatRepo;
	
	@Mock
	private ShowRepo showRepo;
	
	@Mock
	private ShowSeatRepo showSeatRepo;
	
	@Mock
	private UserRepo userRepo;
	
	@InjectMocks
	private BookingServiceImpl bookingService;
	
	private User user;
	private Show show;
	private Seat seat;
	private ShowSeat showSeat;
	
	@BeforeEach
	void setUp() {
		user = User.builder()
				.id(1L)
				.fullName("Test User")
				.email("test@example.com")
				.passwordHash("encoded-password")
				.build();
		
		show = Show.builder()
				.id(5L)
				.ticketPrice(new BigDecimal("250.00"))
				.startTime(Instant.now().plusSeconds(3600))
				.endTime(Instant.now().plusSeconds(7200))
				.status(ShowStatus.SCHEDULED)
				.build();
		
		seat = Seat.builder()
				.id(10L)
				.rowLabel("A")
				.seatNumber(1)
				.build();
		
		showSeat = new ShowSeat(show, seat);
		showSeat.setId(20L);
		showSeat.setStatus(SeatInventoryStatus.AVAILABLE);
	}
	
	@Test
	void createBooking_shouldBookAvailableSeat() {
		stubUserAndShow();
		stubAvailableInventory();
		stubBookingPersistence();
		
		BookingResponse response = bookingService.createBooking(
				1L,
				new CreateBookingRequest(5L, List.of(10L))
		                                                       );
		
		assertThat(response).isNotNull();
		assertThat(response.showId()).isEqualTo(5L);
		assertThat(response.totalAmount())
				.isEqualByComparingTo("250.00");
		assertThat(response.seatIds()).containsExactly(10L);
		
		assertThat(showSeat.getStatus())
				.isEqualTo(SeatInventoryStatus.BOOKED);
		
		verify(bookingRepo).saveAndFlush(any(Booking.class));
		verify(bookingSeatRepo).saveAllAndFlush(anyList());
		verify(showSeatRepo).flush();
	}
	
	private void stubUserAndShow() {
		when(userRepo.findById(1L)).thenReturn(Optional.of(user));
		when(showRepo.findByIdForUpdate(5L)).thenReturn(Optional.of(show));
	}
	
	private void stubAvailableInventory() {
		when(showSeatRepo.lockSeats(5L, List.of(10L)))
				.thenReturn(List.of(showSeat));
	}
	
	private void stubBookingPersistence() {
		when(bookingRepo.saveAndFlush(any(Booking.class)))
				.thenAnswer(invocation -> {
					Booking booking = invocation.getArgument(0);
					booking.setId(100L);
					return booking;
				});
		
		when(bookingSeatRepo.saveAllAndFlush(anyList()))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}
	
	@Test
	void createBooking_shouldRejectAlreadyBookedSeat() {
		stubUserAndShow();
		
		showSeat.setStatus(SeatInventoryStatus.BOOKED);
		stubAvailableInventory();
		
		assertThatThrownBy(() -> bookingService.createBooking(
				1L,
				new CreateBookingRequest(5L, List.of(10L))
		                                                     )).isInstanceOf(ResourceConflictException.class);
		
		verify(bookingRepo, never()).saveAndFlush(any());
		verify(bookingSeatRepo, never()).saveAllAndFlush(anyList());
	}
	
	@Test
	void createBooking_shouldRejectSeatFromAnotherShow() {
		stubUserAndShow();
		
		when(showSeatRepo.lockSeats(5L, List.of(99L)))
				.thenReturn(List.of());
		
		assertThatThrownBy(() -> bookingService.createBooking(
				1L,
				new CreateBookingRequest(5L, List.of(99L))
		                                                     )).isInstanceOf(ResourceNotFoundException.class);
		
		verify(bookingRepo, never()).saveAndFlush(any());
	}
	
	@Test
	void createBooking_shouldRejectDuplicateSeatIds() {
		when(userRepo.findById(1L)).thenReturn(Optional.of(user));
		
		assertThatThrownBy(() -> bookingService.createBooking(
				1L,
				new CreateBookingRequest(5L, List.of(10L, 10L))
		                                                     )).isInstanceOf(BadRequestException.class);
		
		verify(userRepo).findById(1L);
		verifyNoInteractions(showRepo, showSeatRepo, bookingRepo,
		                     bookingSeatRepo);
	}

	
	@Test
	void createBooking_shouldCalculateTotalFromStoredTicketPrice() {
		stubUserAndShow();
		
		Seat secondSeat = Seat.builder()
				.id(11L)
				.rowLabel("A")
				.seatNumber(2)
				.build();
		
		ShowSeat secondShowSeat = new ShowSeat(show, secondSeat);
		
		when(showSeatRepo.lockSeats(5L, List.of(10L, 11L)))
				.thenReturn(List.of(showSeat, secondShowSeat));
		
		stubBookingPersistence();
		
		BookingResponse response = bookingService.createBooking(
				1L,
				new CreateBookingRequest(5L, List.of(10L, 11L))
		                                                       );
		
		assertThat(response.totalAmount())
				.isEqualByComparingTo("500.00");
		
		verify(bookingRepo).saveAndFlush(argThat(booking ->
				                                         booking.getTotalAmount()
						                                         .compareTo(new BigDecimal("500.00")) == 0
		                                        ));
	}
	
	@Test
	void createBooking_shouldNotChangeInventoryBeforeBookingSeatsPersist() {
		stubUserAndShow();
		stubAvailableInventory();
		
		when(bookingRepo.saveAndFlush(any(Booking.class)))
				.thenAnswer(invocation -> {
					Booking booking = invocation.getArgument(0);
					booking.setId(100L);
					return booking;
				});
		
		when(bookingSeatRepo.saveAllAndFlush(anyList()))
				.thenThrow(new DataIntegrityViolationException(
						"Simulated persistence failure"
				));
		
		assertThatThrownBy(() -> bookingService.createBooking(
				1L,
				new CreateBookingRequest(5L, List.of(10L))
		                                                     )).isInstanceOf(DataIntegrityViolationException.class);
		
		assertThat(showSeat.getStatus())
				.isEqualTo(SeatInventoryStatus.AVAILABLE);
		
		verify(showSeatRepo, never()).flush();
	}
	
	@Test
	void createBooking_shouldRejectUnknownUser() {
		when(userRepo.findById(1L)).thenReturn(Optional.empty());
		
		assertThatThrownBy(() -> bookingService.createBooking(
				1L,
				new CreateBookingRequest(5L, List.of(10L))
		                                                     )).isInstanceOf(ResourceNotFoundException.class);
		
		verifyNoInteractions(showRepo, showSeatRepo, bookingRepo);
	}
	
	@Test
	void createBooking_shouldRejectUnknownShow() {
		when(userRepo.findById(1L)).thenReturn(Optional.of(user));
		when(showRepo.findByIdForUpdate(5L)).thenReturn(Optional.empty());
		
		assertThatThrownBy(() -> bookingService.createBooking(
				1L,
				new CreateBookingRequest(5L, List.of(10L))
		                                                     )).isInstanceOf(ResourceNotFoundException.class);
		
		verifyNoInteractions(showSeatRepo, bookingRepo);
	}
}
