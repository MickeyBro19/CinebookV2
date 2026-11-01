package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.CreateTheatreRequest;
import com.mickey.cinebookv2.dto.TheatreResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TheatreService {
	TheatreResponse createTheatre(CreateTheatreRequest theatre);
	Page<TheatreResponse> getTheatres(int page, int pageSize);
	List<TheatreResponse> getTheatre(String title);
	TheatreResponse updateTheatre(String title,CreateTheatreRequest theatre);
	void deleteTheatre(String title);
}
