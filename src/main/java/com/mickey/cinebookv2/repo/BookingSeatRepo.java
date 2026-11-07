package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepo
		extends JpaRepository<BookingSeat, Long> {
	
	List<BookingSeat> findByBookingId(Long bookingId);
}