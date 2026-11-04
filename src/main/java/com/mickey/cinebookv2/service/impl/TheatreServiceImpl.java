package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateTheatreRequest;
import com.mickey.cinebookv2.dto.TheatreResponse;
import com.mickey.cinebookv2.entity.Theatre;
import com.mickey.cinebookv2.exception.BadRequestException;
import com.mickey.cinebookv2.exception.DuplicateResourceException;
import com.mickey.cinebookv2.exception.ResourceConflictException;
import com.mickey.cinebookv2.exception.ResourceNotFoundException;
import com.mickey.cinebookv2.repo.ScreenRepo;
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
	private final ScreenRepo screenRepo;
	
	@Override
	public TheatreResponse createTheatre(CreateTheatreRequest theatre) {
		String name = theatre.name().trim();
		if (theatreRepo.findByNameIgnoreCase(name).isPresent()) {
			throw new DuplicateResourceException(
					"Theatre already exists with name: " + name
			);
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
	public List<TheatreResponse> getTheatre(String name) {
		if (name == null || name.isBlank()) {
			throw new BadRequestException(
					"Theatre name cannot be empty"
			);
		}
		
		return theatreRepo.findByNameContainingIgnoreCase(name.trim())
				.stream()
				.map(this :: theatreMapper)
				.toList();
	}
	
	@Override
	public TheatreResponse updateTheatre(Long id, CreateTheatreRequest theatre) {
		if (id == null) {
			throw new BadRequestException("Theatre ID cannot be null");
		}
		if (theatreRepo.existsByNameIgnoreCaseAndIdNot(
				theatre.name().trim(), id)) {
			
			throw new DuplicateResourceException(
					"Theatre already exists with name: " + theatre.name()
			);
		}
		
		Theatre theatre1 = theatreRepo.findById(id)
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Theatre not found with ID: " + id
						             )
				            );
		theatre1.setName(theatre.name().trim());
		theatre1.setAddress(theatre.address().trim());
		theatre1.setCity(theatre.city().trim());
		return theatreMapper(theatreRepo.save(theatre1));
	}
	
	@Override
	public void deleteTheatre(Long id) {
		if (id == null) {
			throw new BadRequestException("Theatre ID cannot be null");
		}
		Theatre theatre = theatreRepo.findById(id)
				.orElseThrow(() ->
						             new ResourceNotFoundException(
								             "Theatre not found with ID: " + id
						             )
				            );
		
		if (screenRepo.existsByTheatreId(id)) {
			throw new ResourceConflictException(
					"Cannot delete theatre containing screens"
			);
		}
		
		theatreRepo.delete(theatre);
	}
}
