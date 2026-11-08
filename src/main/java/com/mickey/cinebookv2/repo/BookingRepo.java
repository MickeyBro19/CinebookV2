package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);
}
