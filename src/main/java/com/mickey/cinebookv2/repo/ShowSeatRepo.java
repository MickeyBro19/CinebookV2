package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.ShowSeat;
import com.mickey.cinebookv2.entity.enums.SeatInventoryStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShowSeatRepo
		extends JpaRepository<ShowSeat, Long> {
	
	// Retrieve all inventory records for a Show.
	List<ShowSeat> findByShowId(Long showId);
	
	// Retrieve inventory records by status.
	List<ShowSeat> findByShowIdAndStatus(
			Long showId,
			SeatInventoryStatus status
	                                    );
	
	// Count available seats for a Show.
	long countByShowIdAndStatus(
			Long showId,
			SeatInventoryStatus status
	                           );
	
	// Check whether a Show has inventory records.
	boolean existsByShowId(Long showId);
	
	// Retrieve a particular ShowSeat with a database write lock.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			    SELECT ss
			    FROM ShowSeat ss
			    WHERE ss.id = :showSeatId
			""")
	Optional<ShowSeat> findByIdForUpdate(
			@Param("showSeatId") Long showSeatId
	                                    );
	
	// Retrieve inventory for multiple physical seats with write locks.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			    SELECT ss
			    FROM ShowSeat ss
			    WHERE ss.show.id = :showId
			      AND ss.seat.id IN :seatIds
			    ORDER BY ss.id
			""")
	List<ShowSeat> findByShowIdAndSeatIdsForUpdate(
			@Param("showId") Long showId,
			@Param("seatIds") List<Long> seatIds
	                                              );
}