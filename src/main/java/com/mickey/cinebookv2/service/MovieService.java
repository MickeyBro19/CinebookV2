package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.CreateMovieRequest;
import com.mickey.cinebookv2.dto.MovieResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovieService {
	MovieResponse createMovie(CreateMovieRequest movie);
	Page<MovieResponse> getMovies(int page, int pageSize);
	List<MovieResponse> getMovie(String title);
	MovieResponse updateMovie(Long id,CreateMovieRequest movie);
	void deleteMovie(Long id);
}
