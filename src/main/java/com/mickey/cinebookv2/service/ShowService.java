package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.CreateShowRequest;
import com.mickey.cinebookv2.dto.ShowResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShowService {
	
	ShowResponse createShow(CreateShowRequest request);
	
	ShowResponse getShowById(Long id);
	
	Page<ShowResponse> getShowsByScreen(Long screenId, Pageable pageable);
	
	Page<ShowResponse> getShowsByMovie(Long movieId, Pageable pageable);
	
	ShowResponse updateShow(Long id, CreateShowRequest request);
	
	ShowResponse cancelShow(Long id);
}