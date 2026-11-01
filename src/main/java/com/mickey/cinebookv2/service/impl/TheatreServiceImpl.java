package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateTheatreRequest;
import com.mickey.cinebookv2.dto.TheatreResponse;
import com.mickey.cinebookv2.entity.Theatre;
import com.mickey.cinebookv2.repo.TheatreRepo;
import com.mickey.cinebookv2.service.TheatreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TheatreServiceImpl implements TheatreService {
	private final TheatreRepo theatreRepo;
	
	@Override
	public TheatreResponse createTheatre(CreateTheatreRequest theatre) {
		if (theatreRepo.findByNameIgnoreCase(theatre.name()).isPresent()) {
			throw new IllegalArgumentException("theatre already exists");
		}
		Theatre theatre1 = Theatre.builder()
				.name(theatre.name()).address(theatre.address()).city(theatre.city()).build();
		return theatreMapper(theatreRepo.save(theatre1));
	}
	
	private TheatreResponse theatreMapper(Theatre theatre) {
		return new TheatreResponse(
				theatre.getId(),
				theatre.getName(),
				theatre.getAddress(),
				theatre.getCity()
		);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<TheatreResponse> getTheatres(int page, int pageSize) {
		return theatreRepo.findAll(PageRequest.of(page, pageSize)).map(this :: theatreMapper);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<TheatreResponse> getTheatre(String title) {
		if (title == null || title.isEmpty()) {
			throw new IllegalArgumentException("Theatre title is null or empty");
		}
		return theatreRepo.findByNameContainingIgnoreCase(title).stream().map(this :: theatreMapper).toList();
	}
	
	@Override
	public TheatreResponse updateTheatre(String title, CreateTheatreRequest theatre) {
		if (title == null || title.isEmpty()) {
			throw new IllegalArgumentException("Theatre title is null or empty");
		}
		Theatre theatre1 = theatreRepo.findByNameIgnoreCase(title).orElseThrow(() -> new IllegalArgumentException("theatre not found"));
		theatre1.setName(theatre.name());
		theatre1.setAddress(theatre.address());
		theatre1.setCity(theatre.city());
		return theatreMapper(theatreRepo.save(theatre1));
	}
	
	@Override
	public void deleteTheatre(String title) {
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("title is null or empty");
		}
		Theatre theatre= theatreRepo.findByNameIgnoreCase(title).orElseThrow(() -> new IllegalArgumentException("theatre not found"));
		theatreRepo.delete(theatre);
		
	}
}
