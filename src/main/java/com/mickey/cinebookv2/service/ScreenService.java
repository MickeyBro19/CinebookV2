package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.CreateScreenRequest;
import com.mickey.cinebookv2.dto.ScreenResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ScreenService {
	ScreenResponse createScreen(CreateScreenRequest screen);
	
	Page<ScreenResponse> getScreens(int page, int pageSize);
	
	List<ScreenResponse> getScreen(String name);
	
	ScreenResponse updateScreen(Long id, CreateScreenRequest screen);
	
	void deleteScreen(Long id);
}
