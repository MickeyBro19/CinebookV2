package com.mickey.cinebookv2.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
		name = "refresh_tokens",
		indexes = @Index(
				name = "idx_refresh_token_user",
				columnList = "user_id"
		)
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(nullable = false, unique = true, length = 64)
	private String tokenHash;
	
	@Column(nullable = false)
	private Instant expiresAt;
	
	private Instant revokedAt;
	
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
}