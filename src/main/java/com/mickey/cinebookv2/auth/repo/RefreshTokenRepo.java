package com.mickey.cinebookv2.auth.repo;

import com.mickey.cinebookv2.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
            SELECT token
            FROM RefreshToken token
            WHERE token.tokenHash = :tokenHash
            """)
	Optional<RefreshToken> findByTokenHashForUpdate(
			@Param("tokenHash") String tokenHash
	                                               );
}