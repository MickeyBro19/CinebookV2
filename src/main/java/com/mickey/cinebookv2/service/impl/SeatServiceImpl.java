
package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateSeat;
import com.mickey.cinebookv2.dto.SeatResponse;
import com.mickey.cinebookv2.entity.Screen;
import com.mickey.cinebookv2.entity.Seat;
import com.mickey.cinebookv2.exception.DuplicateResourceException;
import com.mickey.cinebookv2.exception.ResourceConflictException;
import com.mickey.cinebookv2.exception.ResourceNotFoundException;
import com.mickey.cinebookv2.repo.ScreenRepo;
import com.mickey.cinebookv2.repo.SeatRepo;
import com.mickey.cinebookv2.repo.ShowRepo;
import com.mickey.cinebookv2.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {
	
	private final SeatRepo seatRepo;
	private final ScreenRepo screenRepo;
	private final ShowRepo showRepo;
	
	@Override
	public SeatResponse createSeat(CreateSeat request) {
		
		// The fix is to make seat creation and deletion acquire the same Screen row lock used by show creation
		Screen screen = screenRepo.findByIdForUpdate(request.screenId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Screen not found with ID: " + request.screenId()
				));
		
		if (showRepo.existsByScreenId(screen.getId())) {
			throw new ResourceConflictException(
					"Cannot add seats to a screen that already has shows"
			);
		}
		
		String rowLabel = request.rowLabel()
				.trim()
				.toUpperCase(Locale.ROOT);
		
		if (seatRepo.existsByScreenIdAndRowLabelIgnoreCaseAndSeatNumber(
				screen.getId(), rowLabel, request.seatNumber())) {
			
			throw new DuplicateResourceException(
					"Seat already exists at row " + rowLabel
							+ " and seat number " + request.seatNumber()
			);
		}
		
		Seat seat = Seat.builder()
				.screen(screen)
				.rowLabel(rowLabel)
				.seatNumber(request.seatNumber())
				.build();
		
		Seat savedSeat = seatRepo.save(seat);
		
		return mapToResponse(savedSeat);
	}
	
	@Override
	public SeatResponse getSeatById(Long id) {
		
		Seat seat = seatRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Seat not found with ID: " + id
				));
		
		return mapToResponse(seat);
	}
	
	@Override
	public List<SeatResponse> getSeatsByScreen(Long screenId) {
		
		if (!screenRepo.existsById(screenId)) {
			throw new ResourceNotFoundException(
					"Screen not found with ID: " + screenId
			);
		}
		
		return seatRepo.findByScreenId(screenId)
				.stream()
				.map(this::mapToResponse)
				.toList();
	}
	
	@Override
	public void deleteSeat(Long id) {
		
		// First identify which screen owns this seat.
		Seat existingSeat = seatRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Seat not found with ID: " + id
				));
		
		Long screenId = existingSeat.getScreen().getId();
		
		// Acquire the same screen lock used by show creation.
		screenRepo.findByIdForUpdate(screenId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Screen not found with ID: " + screenId
				));
		
		// Re-fetch after acquiring the lock.
		Seat seat = seatRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Seat not found with ID: " + id
				));
		
		if (showRepo.existsByScreenId(screenId)) {
			throw new ResourceConflictException(
					"Cannot delete seats from a screen that already has shows"
			);
		}
		
		seatRepo.delete(seat);
	}
	
	private SeatResponse mapToResponse(Seat seat) {
		
		return new SeatResponse(
				seat.getId(),
				seat.getScreen().getId(),
				seat.getRowLabel(),
				seat.getSeatNumber()
		);
	}
}
