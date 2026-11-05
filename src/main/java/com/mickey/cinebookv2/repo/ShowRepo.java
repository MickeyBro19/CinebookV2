package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.Show;
import com.mickey.cinebookv2.entity.enums.ShowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ShowRepo extends JpaRepository<Show, Long> {
	@Query("""
			SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
			FROM Show s
			WHERE s.screen.id = :screenId
			  AND s.status = :status
			  AND s.startTime < :end
			  AND s.endTime > :start
			  AND (:excludeId IS NULL OR s.id <> :excludeId)
			""")
	boolean existsOverlap(
			@Param("screenId") Long screenId,
			@Param("status") ShowStatus status,
			@Param("start") Instant start,
			@Param("end") Instant end,
			@Param("excludeId") Long excludeId
	                     );
	
	Page<Show> findByScreenId(Long screenId, Pageable pageable);
	
	Page<Show> findByMovieId(Long movieId, Pageable pageable);
	
	
}
