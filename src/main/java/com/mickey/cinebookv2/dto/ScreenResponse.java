package com.mickey.cinebookv2.dto;

public record ScreenResponse(
		Long screenId,
		String screenName,
		Long theatreId,
		String theatreName
) {
}
