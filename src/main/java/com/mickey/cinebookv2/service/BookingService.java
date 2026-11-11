package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.BookingResponse;
import com.mickey.cinebookv2.dto.CreateBookingRequest;
import com.mickey.cinebookv2.auth.entity.User;

public interface BookingService {
    BookingResponse createBooking(User authenticatedUser, CreateBookingRequest request);
}
