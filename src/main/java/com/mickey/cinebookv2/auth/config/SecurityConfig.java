package com.mickey.cinebookv2.auth.config;

import com.mickey.cinebookv2.exception.ApiError;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer :: disable)
				.formLogin(AbstractHttpConfigurer :: disable)
				.httpBasic(AbstractHttpConfigurer :: disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						                   .authenticationEntryPoint((req, resp, e) -> {
							                   resp.setStatus(401);
							                   resp.setContentType(
									                   MediaType.APPLICATION_JSON_VALUE
							                                      );
							                   
							                   objectMapper.writeValue(resp.getOutputStream(), new ApiError(
									                   LocalDateTime.now(),
									                   401,
									                   "Unauthorized",
									                   "Authentication is required to access this resource",
									                   req.getRequestURI(),
									                   Map.of()
							                   ));
						                   })
						                   .accessDeniedHandler((req, resp, e) -> {
							                   resp.setStatus(403);
							                   resp.setContentType(
									                   MediaType.APPLICATION_JSON_VALUE
							                                      );
							                   
							                   objectMapper.writeValue(
									                   resp.getOutputStream(),
									                   new ApiError(
											                   LocalDateTime.now(),
											                   403,
											                   "Forbidden",
											                   "You do not have permission to access this resource",
											                   req.getRequestURI(),
											                   Map.of()
									                   )
							                                          );
						                   })
				                  )
				.authorizeHttpRequests(auth -> auth
						                       .requestMatchers(
								                       "/api/auth/**",
								                       "/api/health",
								                       "/swagger-ui/**",
								                       "/v3/api-docs/**"
						                                       ).permitAll()
						                       .requestMatchers(
								                       HttpMethod.GET,
								                       "/api/movie/**",
								                       "/api/shows/**"
						                                       ).permitAll()
						                       .requestMatchers("/api/admin/**"
						                                       ).hasRole("ADMIN")
						                       .requestMatchers(
								                       HttpMethod.POST,
								                       "/api/movie/**",
								                       "/api/theatre/**",
								                       "/api/screens/**",
								                       "/api/seats/**",
								                       "/api/shows/**"
						                                       ).hasRole("ADMIN")
						                       .requestMatchers(
								                       HttpMethod.PUT,
								                       "/api/movie/**",
								                       "/api/theatre/**",
								                       "/api/screens/**",
								                       "/api/seats/**",
								                       "/api/shows/**"
						                                       ).hasRole("ADMIN")
						                       .requestMatchers(
								                       HttpMethod.PATCH,
								                       "/api/shows/**"
						                                       ).hasRole("ADMIN")
						                       .requestMatchers(
								                       HttpMethod.DELETE,
								                       "/api/movie/**",
								                       "/api/theatre/**",
								                       "/api/screens/**",
								                       "/api/seats/**",
								                       "/api/shows/**"
						                                       ).hasRole("ADMIN")
						                       .anyRequest().authenticated()
				                      )
				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class
				                )
				.build();
	}
}
