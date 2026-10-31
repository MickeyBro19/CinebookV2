package com.mickey.cinebookv2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Movie {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 200)
	private String title;
	@Column(length = 5000)
	private String synopsis;
	@Column(nullable = false)
	private Integer durationMinutes;
	private LocalDate releaseDate;
	private String language;
	private String classification;
	private String posterUrl;
}
