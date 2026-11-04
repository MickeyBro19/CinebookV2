package com.mickey.cinebookv2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="seats",uniqueConstraints = @UniqueConstraint(
		name = "uk_seat_screen_position",
		columnNames = {"screen_id","row_label","seat_number"}
))
public class Seat {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="screen_id", nullable=false)
	private Screen screen;
	@Column(name="row_label", nullable=false, length=8)
	private String rowLabel;
	@Column(name="seat_number", nullable=false)
	private Integer seatNumber;
	
}
