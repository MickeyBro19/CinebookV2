package com.mickey.cinebookv2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_seats",
		uniqueConstraints = @UniqueConstraint(name = "uk_booking_seat", columnNames = {"booking_id", "seat_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;
}
