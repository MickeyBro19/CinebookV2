package com.mickey.cinebookv2.entity;

import com.mickey.cinebookv2.entity.enums.SeatInventoryStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
		name = "show_seats",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_show_seat",
						columnNames = {"show_id", "seat_id"}
				)
		},
		indexes = {
				@Index(
						name = "idx_show_seat_show_status",
						columnList = "show_id,status"
				)
		}
)
public class ShowSeat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "show_id", nullable = false)
	private Show show;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SeatInventoryStatus status = SeatInventoryStatus.AVAILABLE;
	
	public ShowSeat(Show show, Seat seat) {
		this.show = show;
		this.seat = seat;
	}
}