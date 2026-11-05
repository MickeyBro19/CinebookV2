package com.mickey.cinebookv2.controller;

import com.mickey.cinebookv2.dto.CreateShowRequest;
import com.mickey.cinebookv2.dto.ShowResponse;
import com.mickey.cinebookv2.service.ShowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
			@PageableDefault(size = 10) Pageable pageable
	                                                          ) {
		
		return ResponseEntity.ok(
				showService.getShowsByScreen(screenId, pageable)
		                        );
	}
	
	@GetMapping("/movie/{movieId}")
	public ResponseEntity<Page<ShowResponse>> getShowsByMovie(
			@PathVariable Long movieId,
			@PageableDefault(size = 10) Pageable pageable
	                                                         ) {
		
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