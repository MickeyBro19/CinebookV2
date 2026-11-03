package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepo extends JpaRepository<Screen, Long> {
	boolean existsByTheatreIdAndScreenNameIgnoreCaseAndIdNot(Long theatre_id, String screenName, Long id);
	
	List<Screen> findByScreenNameContainingIgnoreCase(String screenName);
	
	boolean existsByTheatreIdAndScreenNameIgnoreCase(Long theatreId, String name);
	
	boolean existsByTheatreId(Long theatreId);
}
