package com.mickey.cinebookv2.auth.repo;

import com.mickey.cinebookv2.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
	
	Optional<User> findByEmailIgnoreCase(String email);
	
	boolean existsByEmailIgnoreCase(String email);
}
