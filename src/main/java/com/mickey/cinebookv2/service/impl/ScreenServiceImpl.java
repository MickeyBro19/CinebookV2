package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateScreenRequest;
import com.mickey.cinebookv2.dto.ScreenResponse;
import com.mickey.cinebookv2.entity.Screen;
import com.mickey.cinebookv2.entity.Theatre;
import com.mickey.cinebookv2.exception.BadRequestException;
import com.mickey.cinebookv2.exception.DuplicateResourceException;
import com.mickey.cinebookv2.exception.ResourceConflictException;
import com.mickey.cinebookv2.exception.ResourceNotFoundException;
import com.mickey.cinebookv2.repo.ScreenRepo;
import com.mickey.cinebookv2.repo.SeatRepo;
import com.mickey.cinebookv2.repo.TheatreRepo;
import com.mickey.cinebookv2.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ScreenServiceImpl implements ScreenService {
	private final ScreenRepo screenRepo;
	private final TheatreRepo theatreRepo;
	private final SeatRepo seatRepo;
	
	@Override
	public ScreenResponse createScreen(CreateScreenRequest screen) {
		Theatre theatre = theatreRepo.findById(screen.theatreId())
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Theatre not found with ID: " + screen.theatreId()
						             )
				            );
		String screenName = screen.screenName().trim();
		if (screenName.isEmpty()) {
			throw new BadRequestException("Screen name cannot be empty");
		}
		if (screenRepo.existsByTheatreIdAndScreenNameIgnoreCase(
				theatre.getId(), screenName)) {
			
			throw new DuplicateResourceException(
					"Screen already exists with name: " + screenName
			);
		}
		Screen newScreen = Screen.builder()
				.theatre(theatre)
				.screenName(screenName)
				.build();
		return screenMapper(screenRepo.save(newScreen));
	}
	
	private ScreenResponse screenMapper(Screen screen) {
		return new ScreenResponse(
				screen.getId(),
				screen.getScreenName(),
				screen.getTheatre().getId(),
				screen.getTheatre().getName()
		);
	}
	
	@Transactional(readOnly = true)
	@Override
	public Page<ScreenResponse> getScreens(int page, int pageSize) {
		if (page < 0 || pageSize < 1 || pageSize > 100) {
			throw new BadRequestException(
					"Invalid pagination parameters. Page must be non-negative and size must be between 1 and 100"
			);
		}
		
		Pageable pageable = PageRequest.of(
				page,
				pageSize,
				Sort.by("id").descending()
		                                  );
		return screenRepo.findAll(pageable).map(this :: screenMapper);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ScreenResponse> getScreen(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new BadRequestException("Screen name cannot be empty");
		}
		
		return screenRepo.findByScreenNameContainingIgnoreCase(name.trim()).stream().map(this :: screenMapper).toList();
	}
	
	@Override
	public ScreenResponse updateScreen(Long id, CreateScreenRequest screen) {
		Screen existingScreen = screenRepo.findById(id)
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Screen not found with ID: " + id
						             )
				            );
		
		Theatre theatre = theatreRepo.findById(screen.theatreId())
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Theatre not found with ID: " + screen.theatreId()
						             )
				            );
		String screenName = screen.screenName().trim();
		if (screenName.isEmpty()) {
			throw new BadRequestException("Screen name cannot be empty");
		}
		boolean duplicate =
				screenRepo.existsByTheatreIdAndScreenNameIgnoreCaseAndIdNot(
						theatre.getId(),
						screenName,
						id
				                                                           );
		
		if (duplicate) {
			throw new DuplicateResourceException(
					"Screen already exists with name: " + screenName
			);
		}
		existingScreen.setScreenName(screenName);
		existingScreen.setTheatre(theatre);
		
		
		return screenMapper(screenRepo.save(existingScreen));
	}
	
	@Override
	public void deleteScreen(Long screenId) {
		
		Screen screen = screenRepo.findById(screenId)
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Screen not found with ID: " + screenId
						             )
				            );
		
		if (seatRepo.existsByScreenId(screenId)) {
			throw new ResourceConflictException(
					"Cannot delete screen containing seats"
			);
		}
		
		screenRepo.delete(screen);
	}
}
