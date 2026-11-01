package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TheatreRepo extends JpaRepository<Theatre,Long> {
	List<Theatre> findByNameContainingIgnoreCase(String title);
	Optional<Theatre> findByNameIgnoreCase(String title);
}
