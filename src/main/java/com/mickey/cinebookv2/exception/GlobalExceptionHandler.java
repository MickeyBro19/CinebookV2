package com.mickey.cinebookv2.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	// 404 - Resource not found
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError>
	handleResourceNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request
	                      ) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		
		return ResponseEntity
				.status(status)
				.body(buildError(status, ex.getMessage(), request));
	}
	
	private ApiError buildError(
			HttpStatus status,
			String message,
			HttpServletRequest request
	                           ) {
		return new ApiError(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				Map.of()
		);
	}
	
	// 409 - Duplicate resource
	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiError>
	handleDuplicateResource(
			DuplicateResourceException ex,
			HttpServletRequest request
	                       ) {
		HttpStatus status = HttpStatus.CONFLICT;
		
		return ResponseEntity
				.status(status)
				.body(buildError(status, ex.getMessage(), request));
	}
	
	// 409 - Business state conflict
	@ExceptionHandler(ResourceConflictException.class)
	public ResponseEntity<ApiError>
	handleResourceConflict(
			ResourceConflictException ex,
			HttpServletRequest request
	                      ) {
		HttpStatus status = HttpStatus.CONFLICT;
		
		return ResponseEntity
				.status(status)
				.body(buildError(status, ex.getMessage(), request));
	}
	
	// 400 - Invalid request
	@ExceptionHandler({
			BadRequestException.class,
			IllegalArgumentException.class
	})
	public ResponseEntity<ApiError>
	handleBadRequest(
			RuntimeException ex,
			HttpServletRequest request
	                ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity
				.status(status)
				.body(buildError(status, ex.getMessage(), request));
	}
	
	// 400 - DTO validation errors
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError>
	handleValidationErrors(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	                      ) {
		Map<String, String> errors = new LinkedHashMap<>();
		
		ex.getBindingResult()
				.getFieldErrors()
				.forEach(error ->
						         errors.putIfAbsent(
								         error.getField(),
								         error.getDefaultMessage()
						                           )
				        );
		
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		ApiError response = new ApiError(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				"Request validation failed",
				request.getRequestURI(),
				errors
		);
		
		return ResponseEntity
				.status(status)
				.body(response);
	}
	
	// 400 - Invalid JSON request body
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError>
	handleInvalidJson(
			HttpMessageNotReadableException ex,
			HttpServletRequest request
	                 ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity
				.status(status)
				.body(buildError(
						status,
						"Invalid or malformed request body",
						request
				                ));
	}
	
	// 400 - Missing request parameter
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiError>
	handleMissingParameter(
			MissingServletRequestParameterException ex,
			HttpServletRequest request
	                      ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity
				.status(status)
				.body(buildError(
						status,
						"Required parameter is missing: " + ex.getParameterName(),
						request
				                ));
	}
	
	// 400 - Invalid parameter type
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError>
	handleTypeMismatch(
			MethodArgumentTypeMismatchException ex,
			HttpServletRequest request
	                  ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity
				.status(status)
				.body(buildError(
						status,
						"Invalid value for parameter: " + ex.getName(),
						request
				                ));
	}
	
	// 400 - Constraint validation errors
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError>
	handleConstraintViolation(
			ConstraintViolationException ex,
			HttpServletRequest request
	                         ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity
				.status(status)
				.body(buildError(
						status,
						"Request constraint validation failed",
						request
				                ));
	}
	
	// 409 - Database constraint violation
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError>
	handleDataIntegrityViolation(
			DataIntegrityViolationException ex,
			HttpServletRequest request
	                            ) {
		HttpStatus status = HttpStatus.CONFLICT;
		
		return ResponseEntity
				.status(status)
				.body(buildError(
						status,
						"The operation violates a data integrity constraint",
						request
				                ));
	}
	
	// 500 - Unexpected errors
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError>
	handleUnexpectedException(
			Exception ex,
			HttpServletRequest request
	                         ) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		
		return ResponseEntity
				.status(status)
				.body(buildError(
						status,
						"An unexpected internal server error occurred",
						request
				                ));
	}
	
}
