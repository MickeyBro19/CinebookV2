package com.mickey.cinebookv2.auth.config;

import com.mickey.cinebookv2.auth.entity.CustomUserDetails;
import com.mickey.cinebookv2.auth.service.CustomUserDetailsService;
import com.mickey.cinebookv2.auth.service.JwtService;
import com.mickey.cinebookv2.exception.ApiError;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;
	private final ObjectMapper objectMapper;
	
	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	                               ) throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String authToken = authHeader.substring(7).trim();
		
		try {
			Long userId = jwtService.extractUserId(authToken);
			CustomUserDetails principal = userDetailsService.loadByUserId(userId);
			if (!principal.isEnabled()) {
				SecurityContextHolder.clearContext();
				writeUnauthorizedResponse(request, response);
				return;
			}
			
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
			authentication.setDetails(
					new WebAuthenticationDetailsSource().buildDetails(request)
			                         );
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
			SecurityContextHolder.clearContext();
			writeUnauthorizedResponse(request, response);
			return;
			
		}
		filterChain.doFilter(request, response);
	}
	
	private void writeUnauthorizedResponse(
			HttpServletRequest request,
			HttpServletResponse response
	                                      ) throws IOException {
		
		SecurityContextHolder.clearContext();
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		
		objectMapper.writeValue(
				response.getOutputStream(),
				new ApiError(
						LocalDateTime.now(),
						401,
						"Unauthorized",
						"Invalid or expired access token",
						request.getRequestURI(),
						Map.of()
				)
		                       );
	}
}
