package com.mickey.cinebookv2.service;

import com.mickey.cinebookv2.dto.CreateUserRequest;
import com.mickey.cinebookv2.dto.UserResponse;

public interface UserService {
	
	UserResponse registerUser(CreateUserRequest request);
}
