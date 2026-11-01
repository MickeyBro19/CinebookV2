package com.mickey.cinebookv2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theatres")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class Theatre {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 160)
	private String name;
	@Column(nullable = false, length = 500)
	private String address;
	@Column(nullable = false, length = 100)
	private String city;
	
}
