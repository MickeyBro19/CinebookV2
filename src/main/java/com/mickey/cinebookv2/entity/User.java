package com.mickey.cinebookv2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
		name = "users",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_user_email",
				columnNames = "email"
		)
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, length = 120)
	private String name;
	
	@Column(nullable = false, length = 254)
	private String email;
	
	@Column(nullable = false)
	@Builder.Default
	private boolean enabled = true;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
	
	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
		
		if (email != null) {
			email = email.trim().toLowerCase();
		}
	}
}