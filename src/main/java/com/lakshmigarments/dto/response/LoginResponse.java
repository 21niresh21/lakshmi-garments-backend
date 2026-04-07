package com.lakshmigarments.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
	
	private String token;
	
	private String type;
	
	private String username;
	
	List<String> roles;
	
	private ZonedDateTime expiresAt;

}
