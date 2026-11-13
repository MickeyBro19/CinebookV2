
package com.mickey.cinebookv2.security;

import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import com.mickey.cinebookv2.auth.entity.User;
import com.mickey.cinebookv2.auth.entity.enums.Role;
import com.mickey.cinebookv2.dto.CreateMovieRequest;
import com.mickey.cinebookv2.dto.MovieResponse;
import com.mickey.cinebookv2.service.BookingService;
import com.mickey.cinebookv2.service.MovieService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private BookingService bookingService;
	
	@MockitoBean
	private MovieService movieService;
	
	@Test
	void anonymousUser_shouldReceiveJson401ForProtectedBookingEndpoint()
			throws Exception {
		
		mockMvc.perform(get("/api/bookings/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(
						MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Unauthorized"))
				.andExpect(jsonPath("$.path").value("/api/bookings/me"));
		
		verifyNoInteractions(bookingService);
	}
	
	@Test
	void authenticatedUser_shouldAccessOwnBookingHistory()
			throws Exception {
		
		CustomUserDetails principal = principal(7L, Role.USER);
		
		when(bookingService.getMyBookings(eq(7L), any()))
				.thenReturn(Page.empty(PageRequest.of(0, 10)));
		
		mockMvc.perform(get("/api/bookings/me")
				                .with(authentication(auth(principal))))
				.andExpect(status().isOk());
		
		verify(bookingService).getMyBookings(eq(7L), any());
	}
	
	@Test
	void userRole_shouldBeForbiddenFromCreatingMovie()
			throws Exception {
		
		mockMvc.perform(post("/api/movie")
				                .with(authentication(auth(
						                principal(7L, Role.USER))))
				                .contentType(MediaType.APPLICATION_JSON)
				                .content("""
                                {
                                  "title": "Test Movie",
                                  "synopsis": "Test synopsis",
                                  "durationMinutes": 120,
                                  "releaseDate": "2026-01-01",
                                  "language": "English"
                                }
                                """))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(
						MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.error").value("Forbidden"));
		
		verifyNoInteractions(movieService);
	}
	
	@Test
	void adminRole_shouldBeAllowedToCreateMovie()
			throws Exception {
		
		LocalDate releaseDate = LocalDate.of(2026, 1, 1);
		
		MovieResponse response = new MovieResponse(
				1L,
				"Test Movie",
				"Test synopsis",
				120,
				releaseDate,
				"English",
				null,
				null
		);
		
		when(movieService.createMovie(any(CreateMovieRequest.class)))
				.thenReturn(response);
		
		mockMvc.perform(post("/api/movie")
				                .with(authentication(auth(
						                principal(99L, Role.ADMIN))))
				                .contentType(MediaType.APPLICATION_JSON)
				                .content("""
                                {
                                  "title": "Test Movie",
                                  "synopsis": "Test synopsis",
                                  "durationMinutes": 120,
                                  "releaseDate": "2026-01-01",
                                  "language": "English"
                                }
                                """))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.title").value("Test Movie"));
		
		verify(movieService).createMovie(
				any(CreateMovieRequest.class));
	}
	
	private CustomUserDetails principal(Long userId, Role role) {
		
		User user = User.builder()
				.id(userId)
				.fullName("Security Test User")
				.email("security-test-" + userId + "@example.com")
				.passwordHash("not-used")
				.role(role)
				.enabled(true)
				.build();
		
		return new CustomUserDetails(user);
	}
	
	private UsernamePasswordAuthenticationToken auth(
			CustomUserDetails principal) {
		
		return UsernamePasswordAuthenticationToken.authenticated(
				principal,
				null,
				principal.getAuthorities()
		                                                        );
	}
}
