package com.mickey.cinebookv2.entity;

import com.mickey.cinebookv2.entity.enums.ShowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
		name = "shows",
		indexes = {
				@Index(
						name = "idx_show_screen_start",
						columnList = "screen_id,start_time"
				)
		}
)
public class Show {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "movie_id", nullable = false)
	private Movie movie;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "screen_id", nullable = false)
	private Screen screen;
	
	@Column(name = "start_time", nullable = false)
	private Instant startTime;
	
	@Column(name = "end_time", nullable = false)
	private Instant endTime;
	
	@Column(
			name = "ticket_price",
			nullable = false,
			precision = 10,
			scale = 2
	)
	private BigDecimal ticketPrice;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ShowStatus status = ShowStatus.SCHEDULED;
}