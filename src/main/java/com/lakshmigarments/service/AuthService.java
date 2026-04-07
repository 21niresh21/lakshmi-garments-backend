package com.lakshmigarments.service;

import com.lakshmigarments.dto.request.LoginRequest;
import com.lakshmigarments.dto.response.LoginResponse;

public interface AuthService {
	
	LoginResponse authenticateUser(LoginRequest loginRequest);

}
