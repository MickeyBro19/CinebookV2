package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.CreateMovieRequest;
import com.mickey.cinebookv2.dto.MovieResponse;
import com.mickey.cinebookv2.entity.Movie;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovieService {
	MovieResponse createMovie(CreateMovieRequest movie);
	Page<MovieResponse> getMovies(int page, int pageSize);
	List<MovieResponse> getMovie(String title);
	MovieResponse updateMovie(String title,CreateMovieRequest movie);
	void deleteMovie(String title);
}
