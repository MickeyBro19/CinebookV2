package com.mickey.cinebookv2.config;

import com.mickey.cinebookv2.entity.User;
import com.mickey.cinebookv2.repo.UserRepo;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevUserInitializer implements ApplicationRunner {
	
	private final UserRepo userRepo;
	
	@Value("${cinebook.booking.dev-user-email}")
	private String devUserEmail;
	
	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		
		if (userRepo.existsByEmailIgnoreCase(devUserEmail)) {
			return;
		}
		
		User developmentUser = User.builder()
				.name("Cinebook Development User")
				.email(devUserEmail)
				.enabled(true)
				.build();
		
		userRepo.save(developmentUser);
	}
}