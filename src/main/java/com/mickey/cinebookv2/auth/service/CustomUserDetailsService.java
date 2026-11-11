package com.mickey.cinebookv2.auth.service;

import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import com.mickey.cinebookv2.auth.entity.User;
import com.mickey.cinebookv2.auth.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepo userRepo;
	
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		String normalizeEmail = email.trim().toLowerCase(Locale.ROOT);
		
		User user = userRepo.findByEmailIgnoreCase(normalizeEmail).orElseThrow(() -> new UsernameNotFoundException("Invalid email or password " + normalizeEmail));
		
		return new CustomUserDetails(user);
	}
	
	@Transactional(readOnly = true)
	public CustomUserDetails loadByUserId(Long userId) {
		User user = userRepo.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return new CustomUserDetails(user);
	}
}
