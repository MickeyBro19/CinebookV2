package com.mickey.cinebookv2.auth.service;

import com.mickey.cinebookv2.auth.dto.CreateUserRequest;
import com.mickey.cinebookv2.auth.dto.UserResponse;

public interface UserService {
	
	UserResponse registerUser(CreateUserRequest request);
}
