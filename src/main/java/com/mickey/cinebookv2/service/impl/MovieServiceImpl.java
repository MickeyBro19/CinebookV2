package com.mickey.cinebookv2.service.impl;

import com.mickey.cinebookv2.dto.CreateMovieRequest;
import com.mickey.cinebookv2.dto.MovieResponse;
import com.mickey.cinebookv2.entity.Movie;
import com.mickey.cinebookv2.repo.MovieRepo;
import com.mickey.cinebookv2.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieServiceImpl implements MovieService {
	private final MovieRepo movieRepo;
	
	@Override
	public MovieResponse createMovie(CreateMovieRequest movie) {
		String title=movie.title().trim();
		if(movieRepo.findByTitleIgnoreCase(title).isPresent()) {
			throw new IllegalArgumentException("Title already exists");
		}
		Movie newMovie = Movie.builder()
				.title(title)
				.synopsis(movie.synopsis())
				.durationMinutes(movie.durationMinutes())
				.releaseDate(movie.releaseDate())
				.language(movie.language())
				.build();
		
		return movieMapper(movieRepo.save(newMovie));
	}
	
	private MovieResponse movieMapper(Movie movie) {
		return new MovieResponse(
				movie.getId(),
				movie.getTitle(),
				movie.getSynopsis(),
				movie.getDurationMinutes(),
				movie.getReleaseDate(),
				movie.getLanguage(),
				movie.getClassification(),
				movie.getPosterUrl()
		);
	}
	
	@Transactional(readOnly = true)
	@Override
	public Page<MovieResponse> getMovies(int page, int pageSize) {
		return movieRepo.findAll(PageRequest.of(page, pageSize)).map(this :: movieMapper);
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<MovieResponse> getMovie(String title) {
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("Movie title is null or empty");
		}
		return movieRepo.findByTitleContainingIgnoreCase(title.trim()).stream().map(this::movieMapper).toList();
	}
	
	@Override
	public MovieResponse updateMovie(Long id, CreateMovieRequest movie) {
		if(id==null) {
			throw new IllegalArgumentException("Movie Id is null");
		}
		if(movieRepo.existsByTitleIgnoreCaseAndIdNot(movie.title().trim(), id)) {
			throw new IllegalArgumentException("Title already exists");
		}
		Movie uMovie = movieRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Movie not found"));
		uMovie.setTitle(movie.title().trim());
		uMovie.setSynopsis(movie.synopsis());
		uMovie.setDurationMinutes(movie.durationMinutes());
		uMovie.setReleaseDate(movie.releaseDate());
		uMovie.setLanguage(movie.language());
		
		return movieMapper(movieRepo.save(uMovie));
	}
	
	@Override
	public void deleteMovie(Long id) {
		Movie movie = movieRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Movie not found"));
		movieRepo.delete(movie);
		
	}
}
