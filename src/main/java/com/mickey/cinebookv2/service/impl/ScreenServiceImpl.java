package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateScreenRequest;
import com.mickey.cinebookv2.dto.ScreenResponse;
import com.mickey.cinebookv2.entity.Screen;
import com.mickey.cinebookv2.entity.Theatre;
import com.mickey.cinebookv2.repo.ScreenRepo;
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
	
	@Override
	public ScreenResponse createScreen(CreateScreenRequest screen) {
		Theatre theatre = theatreRepo.findById(screen.theatreId()).orElseThrow(() -> new IllegalArgumentException("theatre not found"));
		String screenName = screen.screenName().trim();
		if (screenName.isEmpty()) {
			throw new IllegalArgumentException("screen name is empty");
		}
		if (screenRepo.existsByTheatreIdAndScreenNameIgnoreCase(theatre.getId(), screenName)) {
			throw new IllegalArgumentException("screen name already exists");
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
			throw new IllegalArgumentException("Invalid pagination parameters");
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
		if (name==null || name.trim().isEmpty() ) {
			throw new IllegalArgumentException("Name is empty");
		}
		
		return screenRepo.findByScreenNameContainingIgnoreCase(name.trim()).stream().map(this :: screenMapper).toList();
	}
	
	@Override
	public ScreenResponse updateScreen(Long id, CreateScreenRequest screen) {
		Screen existingScreen=screenRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("screen not found"));
		Theatre theatre=theatreRepo.findById(screen.theatreId()).orElseThrow(() -> new IllegalArgumentException("theatre not found"));
		String screenName = screen.screenName().trim();
		if (screenName.isEmpty()) {
			throw new IllegalArgumentException("screen name is empty");
		}
		boolean duplicate=screenRepo.existsByTheatreIdAndScreenNameIgnoreCaseAndIdNot(theatre.getId(), screenName,id);
		if (duplicate) {
			throw new IllegalArgumentException("screen name already exists");
		}
		existingScreen.setScreenName(screenName);
		existingScreen.setTheatre(theatre);
		
		
		return screenMapper(screenRepo.save(existingScreen));
	}
	
	@Override
	public void deleteScreen(Long screenId) {
		Screen screen = screenRepo.findById(screenId)
				.orElseThrow(() ->
						             new IllegalArgumentException("Screen not found"));
		
		screenRepo.delete(screen);
	}
}
