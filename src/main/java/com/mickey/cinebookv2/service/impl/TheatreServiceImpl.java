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
		String name= theatre.name().trim();
		if (theatreRepo.findByNameIgnoreCase(name).isPresent()) {
			throw new IllegalArgumentException("theatre already exists");
		}
		Theatre theatre1 = Theatre.builder()
				.name(name).address(theatre.address().trim()).city(theatre.city().trim()).build();
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
	public List<TheatreResponse> getTheatre(String Name) {
		if (Name == null || Name.isEmpty()) {
			throw new IllegalArgumentException("Theatre title is null or empty");
		}
		return theatreRepo.findByNameContainingIgnoreCase(Name).stream().map(this :: theatreMapper).toList();
	}
	
	@Override
	public TheatreResponse updateTheatre(Long id, CreateTheatreRequest theatre) {
		if(id == null) {
			throw new IllegalArgumentException("theatre id is null");
		}
		if(theatreRepo.existsByNameIgnoreCaseAndIdNot(theatre.name(), id)) {
			throw new IllegalArgumentException("theatre already exists");
		}
		Theatre theatre1 = theatreRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("theatre not found"));
		theatre1.setName(theatre.name().trim());
		theatre1.setAddress(theatre.address());
		theatre1.setCity(theatre.city());
		return theatreMapper(theatreRepo.save(theatre1));
	}
	
	@Override
	public void deleteTheatre(String Name) {
		if (Name == null || Name.trim().isEmpty()) {
			throw new IllegalArgumentException("title is null or empty");
		}
		Theatre theatre= theatreRepo.findByNameIgnoreCase(Name).orElseThrow(() -> new IllegalArgumentException("theatre not found"));
		theatreRepo.delete(theatre);
		
	}
}
