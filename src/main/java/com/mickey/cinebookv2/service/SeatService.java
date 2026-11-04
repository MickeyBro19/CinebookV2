package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.CreateSeat;
import com.mickey.cinebookv2.dto.SeatResponse;

import java.util.List;

public interface SeatService {
	SeatResponse createSeat(CreateSeat request);
	
	SeatResponse getSeatById(Long id);
	
	List<SeatResponse> getSeatsByScreen(Long screenId);
	
	void deleteSeat(Long id);
}
