package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.Screen;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScreenRepo extends JpaRepository<Screen, Long> {
	boolean existsByTheatreIdAndScreenNameIgnoreCaseAndIdNot(Long theatre_id, String screenName, Long id);
	
	List<Screen> findByScreenNameContainingIgnoreCase(String screenName);
	
	boolean existsByTheatreIdAndScreenNameIgnoreCase(Long theatreId, String name);
	
	boolean existsByTheatreId(Long theatreId);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Screen s WHERE s.id = :screenId")
	Optional<Screen> findByIdForUpdate(
			@Param("screenId") Long screenId
	                                  );
}
