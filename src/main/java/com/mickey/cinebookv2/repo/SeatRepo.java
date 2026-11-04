package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepo extends JpaRepository<Seat,Long> {
	List<Seat> findByScreenId(Long screenId);
	
	boolean existsByScreenIdAndRowLabelIgnoreCaseAndSeatNumber(
			Long screenId,
			String rowLabel,
			Integer seatNumber
	                                                          );
	
	boolean existsByScreenId(Long screenId);
	
}
