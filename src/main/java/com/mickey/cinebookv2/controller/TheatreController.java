package com.mickey.cinebookv2.controller;

import com.mickey.cinebookv2.dto.CreateTheatreRequest;
import com.mickey.cinebookv2.dto.TheatreResponse;
import com.mickey.cinebookv2.service.TheatreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatre")
@RequiredArgsConstructor
public class TheatreController {
	private final TheatreService  theatreService;
	
	// Create a theatre
	@PostMapping
	public ResponseEntity<TheatreResponse> createTheatre(
			@Valid @RequestBody CreateTheatreRequest request) {
		
		TheatreResponse response = theatreService.createTheatre(request);
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(response);
	}
	
	// Get all theatres with pagination
	@GetMapping
	public ResponseEntity<Page<TheatreResponse>> getTheatres(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		if (page < 0 || size < 1 || size > 100) {
			return ResponseEntity.badRequest().build();
		}
		
		return ResponseEntity.ok(
				theatreService.getTheatres(page, size)
		                        );
	}
	
	// Search theatres by title
	@GetMapping("/search")
	public ResponseEntity<List<TheatreResponse>> searchTheatres(
			@RequestParam String title) {
		
		return ResponseEntity.ok(
				theatreService.getTheatre(title)
		                        );
	}
	
	// Update a theatre by title
	@PutMapping
	public ResponseEntity<TheatreResponse> updateTheatre(
			@RequestParam Long id,
			@Valid @RequestBody CreateTheatreRequest theatre) {
		
		return ResponseEntity.ok(
				theatreService.updateTheatre(id, theatre)
		                        );
	}
	
	// Delete a theatre by title
	@DeleteMapping
	public ResponseEntity<Void> deleteTheatre(
			@RequestParam String title) {
		
		theatreService.deleteTheatre(title);
		
		return ResponseEntity.noContent().build();
	}
}
