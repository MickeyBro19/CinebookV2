package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateSeat;
import com.mickey.cinebookv2.dto.SeatResponse;
import com.mickey.cinebookv2.entity.Screen;
import com.mickey.cinebookv2.entity.Seat;
import com.mickey.cinebookv2.exception.DuplicateResourceException;
import com.mickey.cinebookv2.exception.ResourceNotFoundException;
import com.mickey.cinebookv2.repo.ScreenRepo;
import com.mickey.cinebookv2.repo.SeatRepo;
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
	private final SeatRepo seatRepository;
	private final ScreenRepo screenRepository;
	
	@Override
	@Transactional
	public SeatResponse createSeat(CreateSeat request) {
		
		Screen screen = screenRepository.findById(request.screenId())
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Screen not found with ID: " + request.screenId()
						             ));
		
		String rowLabel = request.rowLabel()
				.trim()
				.toUpperCase(Locale.ROOT);
		
		if (seatRepository.existsByScreenIdAndRowLabelIgnoreCaseAndSeatNumber(
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
		
		Seat savedSeat = seatRepository.save(seat);
		
		return mapToResponse(savedSeat);
	}
	
	@Override
	public SeatResponse getSeatById(Long id) {
		
		Seat seat = seatRepository.findById(id)
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Seat not found with ID: " + id
						             ));
		
		return mapToResponse(seat);
	}
	
	@Override
	public List<SeatResponse> getSeatsByScreen(Long screenId) {
		
		if (!screenRepository.existsById(screenId)) {
			throw new ResourceNotFoundException(
					"Screen not found with ID: " + screenId
			);
		}
		
		return seatRepository.findByScreenId(screenId)
				.stream()
				.map(this :: mapToResponse)
				.toList();
	}
	
	@Override
	@Transactional
	public void deleteSeat(Long id) {
		
		Seat seat = seatRepository.findById(id)
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Seat not found with ID: " + id
						             ));
		
		seatRepository.delete(seat);
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
