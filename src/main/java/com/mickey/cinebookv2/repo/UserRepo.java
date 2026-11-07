package com.mickey.cinebookv2.repo;

import com.mickey.cinebookv2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
	
	Optional<User> findByEmailIgnoreCase(String email);
	
	boolean existsByEmailIgnoreCase(String email);
}