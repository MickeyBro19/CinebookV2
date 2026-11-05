package com.mickey.cinebookv2.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	// 404 - Resource not found
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleResourceNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request
	                                                      ) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		
		return ResponseEntity.status(status)
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
	public ResponseEntity<ApiError> handleDuplicateResource(
			DuplicateResourceException ex,
			HttpServletRequest request
	                                                       ) {
		HttpStatus status = HttpStatus.CONFLICT;
		
		return ResponseEntity.status(status)
				.body(buildError(status, ex.getMessage(), request));
	}
	
	// 409 - Business state conflict
	@ExceptionHandler(ResourceConflictException.class)
	public ResponseEntity<ApiError> handleResourceConflict(
			ResourceConflictException ex,
			HttpServletRequest request
	                                                      ) {
		HttpStatus status = HttpStatus.CONFLICT;
		
		return ResponseEntity.status(status)
				.body(buildError(status, ex.getMessage(), request));
	}
	
	// 400 - Invalid request
	@ExceptionHandler({
			BadRequestException.class,
			IllegalArgumentException.class
	})
	public ResponseEntity<ApiError> handleBadRequest(
			RuntimeException ex,
			HttpServletRequest request
	                                                ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity.status(status)
				.body(buildError(status, ex.getMessage(), request));
	}
	
	// 400 - DTO validation errors
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidationErrors(
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
		
		return ResponseEntity.status(status).body(response);
	}
	
	// 400 - Invalid JSON
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleInvalidJson(
			HttpMessageNotReadableException ex,
			HttpServletRequest request
	                                                 ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity.status(status)
				.body(buildError(
						status,
						"Invalid or malformed request body",
						request
				                ));
	}
	
	// 400 - Missing request parameter
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiError> handleMissingParameter(
			MissingServletRequestParameterException ex,
			HttpServletRequest request
	                                                      ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity.status(status)
				.body(buildError(
						status,
						"Required parameter is missing: " + ex.getParameterName(),
						request
				                ));
	}
	
	// 400 - Invalid parameter type
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(
			MethodArgumentTypeMismatchException ex,
			HttpServletRequest request
	                                                  ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity.status(status)
				.body(buildError(
						status,
						"Invalid value for parameter: " + ex.getName(),
						request
				                ));
	}
	
	// 400 - Constraint validation errors
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraintViolation(
			ConstraintViolationException ex,
			HttpServletRequest request
	                                                         ) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity.status(status)
				.body(buildError(
						status,
						"Request constraint validation failed",
						request
				                ));
	}
	
	// 405 - HTTP method not supported
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiError> handleMethodNotSupported(
			HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request
	                                                        ) {
		HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
		
		return ResponseEntity.status(status)
				.body(buildError(
						status,
						"HTTP method is not supported for this endpoint",
						request
				                ));
	}
	
	// 415 - Unsupported content type
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ApiError> handleUnsupportedMediaType(
			HttpMediaTypeNotSupportedException ex,
			HttpServletRequest request
	                                                          ) {
		HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
		
		return ResponseEntity.status(status)
				.body(buildError(
						status,
						"Unsupported request content type",
						request
				                ));
	}
	
	// 409 - Database integrity violation
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolation(
			DataIntegrityViolationException ex,
			HttpServletRequest request
	                                                            ) {
		log.warn(
				"Data integrity violation at {}: {}",
				request.getRequestURI(),
				ex.getMostSpecificCause().getMessage()
		        );
		
		HttpStatus status = HttpStatus.CONFLICT;
		
		return ResponseEntity.status(status)
				.body(buildError(
						status,
						"The operation violates a data integrity constraint",
						request
				                ));
	}
	
	// 500 - Unexpected errors
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpectedException(
			Exception ex,
			HttpServletRequest request
	                                                         ) {
		log.error(
				"Unhandled exception at {}",
				request.getRequestURI(),
				ex
		         );
		
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		
		return ResponseEntity.status(status)
				.body(buildError(
						status,
						"An unexpected internal server error occurred",
						request
				                ));
	}
}
