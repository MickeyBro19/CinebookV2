package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateShowRequest;
import com.mickey.cinebookv2.dto.ShowResponse;
import com.mickey.cinebookv2.entity.Movie;
import com.mickey.cinebookv2.entity.Screen;
import com.mickey.cinebookv2.entity.Show;
import com.mickey.cinebookv2.entity.enums.ShowStatus;
import com.mickey.cinebookv2.exception.BadRequestException;
import com.mickey.cinebookv2.exception.ResourceConflictException;
import com.mickey.cinebookv2.exception.ResourceNotFoundException;
import com.mickey.cinebookv2.repo.MovieRepo;
import com.mickey.cinebookv2.repo.ScreenRepo;
import com.mickey.cinebookv2.repo.ShowRepo;
import com.mickey.cinebookv2.service.ShowService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowServiceImpl implements ShowService {
	
	private final ShowRepo showRepo;
	private final MovieRepo movieRepo;
	private final ScreenRepo screenRepo;
	
	@Override
	@Transactional
	public ShowResponse createShow(CreateShowRequest request) {
		
		validateTimeInterval(request.startTime(), request.endTime());
		
		// Lock the screen before checking for overlapping shows.
		Screen screen = getLockedScreen(request.screenId());
		
		Movie movie = movieRepo.findById(request.movieId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Movie not found with id: " + request.movieId()
				));
		
		validateNoOverlap(
				screen.getId(),
				request.startTime(),
				request.endTime(),
				null
		                 );
		
		Show show = Show.builder()
				.movie(movie)
				.screen(screen)
				.startTime(request.startTime())
				.endTime(request.endTime())
				.ticketPrice(request.ticketPrice())
				.status(ShowStatus.SCHEDULED)
				.build();
		
		return mapToResponse(showRepo.save(show));
	}
	
	@Override
	public ShowResponse getShowById(Long id) {
		
		Show show = getShow(id);
		
		return mapToResponse(show);
	}
	
	@Override
	public Page<ShowResponse> getShowsByScreen(
			Long screenId,
			Pageable pageable
	                                          ) {
		
		if (!screenRepo.existsById(screenId)) {
			throw new ResourceNotFoundException(
					"Screen not found with id: " + screenId
			);
		}
		
		return showRepo.findByScreenId(screenId, pageable)
				.map(this :: mapToResponse);
	}
	
	@Override
	public Page<ShowResponse> getShowsByMovie(
			Long movieId,
			Pageable pageable
	                                         ) {
		
		if (!movieRepo.existsById(movieId)) {
			throw new ResourceNotFoundException(
					"Movie not found with id: " + movieId
			);
		}
		
		return showRepo.findByMovieId(movieId, pageable)
				.map(this :: mapToResponse);
	}
	
	@Override
	@Transactional
	public ShowResponse updateShow(Long id, CreateShowRequest request) {
		
		validateTimeInterval(request.startTime(), request.endTime());
		
		Show show = getShow(id);
		
		if (show.getStatus() != ShowStatus.SCHEDULED) {
			throw new ResourceConflictException(
					"Only scheduled shows can be updated"
			);
		}
		
		Long oldScreenId = show.getScreen().getId();
		Long newScreenId = request.screenId();
		
		List<Long> screenIds = oldScreenId.equals(newScreenId)
				? List.of(oldScreenId)
				: Stream.of(oldScreenId, newScreenId)
				.sorted()
				.toList();
		
		Screen targetScreen = null;
		
		for (Long screenId : screenIds) {
			Screen lockedScreen = getLockedScreen(screenId);
			
			if (screenId.equals(newScreenId)) {
				targetScreen = lockedScreen;
			}
		}
		
		Movie movie = movieRepo.findById(request.movieId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Movie not found with id: " + request.movieId()
				));
		
		assert targetScreen != null;
		validateNoOverlap(
				targetScreen.getId(),
				request.startTime(),
				request.endTime(),
				show.getId()
		                 );
		
		show.setMovie(movie);
		show.setScreen(targetScreen);
		show.setStartTime(request.startTime());
		show.setEndTime(request.endTime());
		show.setTicketPrice(request.ticketPrice());
		
		return mapToResponse(showRepo.save(show));
	}
	
	@Override
	@Transactional
	public ShowResponse cancelShow(Long id) {
		
		Show show = getShow(id);
		
		if (show.getStatus() != ShowStatus.SCHEDULED) {
			throw new ResourceConflictException(
					"Only scheduled shows can be cancelled"
			);
		}
		
		show.setStatus(ShowStatus.CANCELLED);
		
		return mapToResponse(showRepo.save(show));
	}
	
	private Show getShow(Long id) {
		
		return showRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Show not found with id: " + id
				));
	}
	
	private void validateTimeInterval(Instant startTime, Instant endTime) {
		
		if (!startTime.isBefore(endTime)) {
			throw new BadRequestException(
					"Show start time must be before end time"
			);
		}
	}
	
	private Screen getLockedScreen(Long screenId) {
		
		return screenRepo.findByIdForUpdate(screenId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Screen not found with id: " + screenId
				));
	}
	
	private void validateNoOverlap(
			Long screenId,
			Instant startTime,
			Instant endTime,
			Long excludeId
	                              ) {
		
		boolean overlap = showRepo.existsOverlap(
				screenId,
				ShowStatus.SCHEDULED,
				startTime,
				endTime,
				excludeId
		                                        );
		
		if (overlap) {
			throw new ResourceConflictException(
					"This screen already has a scheduled show during the requested time"
			);
		}
	}
	
	private ShowResponse mapToResponse(Show show) {
		
		return new ShowResponse(
				show.getId(),
				show.getMovie().getId(),
				show.getScreen().getId(),
				show.getStartTime(),
				show.getEndTime(),
				show.getTicketPrice(),
				show.getStatus()
		);
	}
}