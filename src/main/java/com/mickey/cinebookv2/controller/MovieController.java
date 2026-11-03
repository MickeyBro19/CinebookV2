package com.mickey.cinebookv2.controller;

import com.mickey.cinebookv2.dto.CreateMovieRequest;
import com.mickey.cinebookv2.dto.MovieResponse;
import com.mickey.cinebookv2.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie")
@RequiredArgsConstructor
public class MovieController {
	private final MovieService movieService;
	
	// Create a movie
	@PostMapping
	public ResponseEntity<MovieResponse> createMovie(
			@Valid @RequestBody CreateMovieRequest request) {
		
		MovieResponse response = movieService.createMovie(request);
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(response);
	}
	
	// Get all movies with pagination
	@GetMapping
	public ResponseEntity<Page<MovieResponse>> getMovies(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		if (page < 0 || size < 1 || size > 100) {
			return ResponseEntity.badRequest().build();
		}
		
		return ResponseEntity.ok(
				movieService.getMovies(page, size)
		                        );
	}
	
	// Search movies by title
	@GetMapping("/search")
	public ResponseEntity<List<MovieResponse>> searchMovies(
			@RequestParam String title) {
		
		return ResponseEntity.ok(
				movieService.getMovie(title)
		                        );
	}
	
	// Update a movie by title
	@PutMapping("/{id}")
	public ResponseEntity<MovieResponse> updateMovie(
			@PathVariable Long id,
			@Valid @RequestBody CreateMovieRequest movie) {
		
		return ResponseEntity.ok(
				movieService.updateMovie(id, movie)
		                        );
	}
	
	// Delete a movie by title
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMovie(
			@PathVariable Long id) {
		
		movieService.deleteMovie(id);
		
		return ResponseEntity.noContent().build();
	}
}
