package com.mickey.cinebookv2.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Locale;

@Entity
@Table(
		name = "users",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_users_email",
						columnNames = "email"
				)
		}
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, length = 120)
	private String fullName;
	
	@Column(nullable = false, length = 254)
	private String email;
	
	@Column(nullable = false)
	private String passwordHash;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private Role role = Role.USER;
	
	@Column(nullable = false)
	@Builder.Default
	private boolean enabled = true;
	
	@PrePersist
	@PreUpdate
	private void normalizeEmail() {
		if (email != null) {
			email = email.trim().toLowerCase(Locale.ROOT);
		}
	}
}
