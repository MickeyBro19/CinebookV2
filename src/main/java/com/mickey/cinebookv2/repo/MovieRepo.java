package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepo extends JpaRepository<Movie, Long> {
	List<Movie> findByTitleContainingIgnoreCase(String title);
	
	Optional<Movie> findByTitleIgnoreCase(String title);
	
	boolean existsByTitleIgnoreCaseAndIdNot(
			String title,
			Long id
	                                       );
	
}
