package com.mickey.cinebookv2.controller;

import com.mickey.cinebookv2.dto.CreateShowRequest;
import com.mickey.cinebookv2.dto.ShowResponse;
import com.mickey.cinebookv2.exception.BadRequestException;
import com.mickey.cinebookv2.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {
	
	private final ShowService showService;
	
	@PostMapping
	public ResponseEntity<ShowResponse> createShow(
			@Valid @RequestBody CreateShowRequest request
	                                              ) {
		
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(showService.createShow(request));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ShowResponse> getShowById(
			@PathVariable Long id
	                                               ) {
		
		return ResponseEntity.ok(showService.getShowById(id));
	}
	
	@GetMapping("/screen/{screenId}")
	public ResponseEntity<Page<ShowResponse>> getShowsByScreen(
			@PathVariable Long screenId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		if (page < 0 || size < 1 || size > 100) {
			throw new BadRequestException(
					"Invalid pagination parameters. Page must be non-negative and size must be between 1 and 100"
			);
		}
		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.by("startTime").descending()
		                                  );
		
		
		return ResponseEntity.ok(
				showService.getShowsByScreen(screenId, pageable)
		                        );
	}
	
	@GetMapping("/movie/{movieId}")
	public ResponseEntity<Page<ShowResponse>> getShowsByMovie(
			@PathVariable Long movieId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		if (page < 0 || size < 1 || size > 100) {
			throw new BadRequestException(
					"Invalid pagination parameters. Page must be non-negative and size must be between 1 and 100"
			);
		}
		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.by("startTime").descending()
		                                  );
		
		
		return ResponseEntity.ok(
				showService.getShowsByMovie(movieId, pageable)
		                        );
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ShowResponse> updateShow(
			@PathVariable Long id,
			@Valid @RequestBody CreateShowRequest request
	                                              ) {
		
		return ResponseEntity.ok(
				showService.updateShow(id, request)
		                        );
	}
	
	@PatchMapping("/{id}/cancel")
	public ResponseEntity<ShowResponse> cancelShow(
			@PathVariable Long id
	                                              ) {
		
		return ResponseEntity.ok(
				showService.cancelShow(id)
		                        );
	}
}