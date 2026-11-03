package com.mickey.cinebookv2.controller;

import com.mickey.cinebookv2.dto.CreateScreenRequest;
import com.mickey.cinebookv2.dto.ScreenResponse;
import com.mickey.cinebookv2.service.ScreenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
public class ScreenController {
	
	private final ScreenService screenService;
	
	@PostMapping
	public ResponseEntity<ScreenResponse> createScreen(
			@Valid @RequestBody CreateScreenRequest request) {
		
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(screenService.createScreen(request));
	}
	
	@GetMapping
	public ResponseEntity<Page<ScreenResponse>> getScreens(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int pageSize) {
		
		return ResponseEntity.ok(
				screenService.getScreens(page, pageSize)
		                        );
	}
	
	@GetMapping("/search")
	public ResponseEntity<List<ScreenResponse>> searchScreens(
			@RequestParam String name) {
		
		return ResponseEntity.ok(
				screenService.getScreen(name)
		                        );
	}
	
	
	@PutMapping("/{id}")
	public ResponseEntity<ScreenResponse> updateScreen(
			@PathVariable Long id,
			@Valid @RequestBody CreateScreenRequest request) {
		
		return ResponseEntity.ok(
				screenService.updateScreen(id, request)
		                        );
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteScreen(
			@PathVariable Long id) {
		
		screenService.deleteScreen(id);
		
		return ResponseEntity.noContent().build();
	}
}