package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.ShowSeat;
import com.mickey.cinebookv2.entity.enums.SeatInventoryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShowSeatRepo extends JpaRepository<ShowSeat, Long> {
	
	List<ShowSeat> findByShowId(Long showId);
	
	List<ShowSeat> findByShowIdAndStatus(Long showId, SeatInventoryStatus status);
	
	long countByShowIdAndStatus(Long showId, SeatInventoryStatus status);
	
	boolean existsByShowId(Long showId);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT ss
			FROM ShowSeat ss
			WHERE ss.id = :showSeatId
			""")
	Optional<ShowSeat> findByIdForUpdate(@Param("showSeatId") Long showSeatId);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT ss
			FROM ShowSeat ss
			WHERE ss.show.id = :showId
			  AND ss.seat.id IN :seatIds
			ORDER BY ss.seat.id
			""")
	List<ShowSeat> lockSeats(
			@Param("showId") Long showId,
			@Param("seatIds") Collection<Long> seatIds
	                        );
}
