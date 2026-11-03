package com.mickey.cinebookv2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "screens",uniqueConstraints =@UniqueConstraint (
		name="uk_screen_theatre_name",
		columnNames = {"theatre_id","screen_name"}
		))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Screen {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "theatre_id",nullable = false)
	private Theatre theatre;
	@Column(nullable = false,name = "screen_name",length = 80)
	private String screenName;
}
