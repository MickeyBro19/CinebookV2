package com.mickey.cinebookv2.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> validationErrors
) {
}
