package com.mickey.cinebookv2.controller;

import com.mickey.cinebookv2.dto.CreateSeat;
import com.mickey.cinebookv2.dto.SeatResponse;
import com.mickey.cinebookv2.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {
	private final SeatService seatService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SeatResponse createSeat(
			@Valid @RequestBody CreateSeat request) {
		
		return seatService.createSeat(request);
	}
	
	@GetMapping("/{id}")
	public SeatResponse getSeatById(@PathVariable Long id) {
		
		return seatService.getSeatById(id);
	}
	
	@GetMapping("/screen/{screenId}")
	public List<SeatResponse> getSeatsByScreen(
			@PathVariable Long screenId) {
		
		return seatService.getSeatsByScreen(screenId);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSeat(@PathVariable Long id) {
		
		seatService.deleteSeat(id);
	}
}
