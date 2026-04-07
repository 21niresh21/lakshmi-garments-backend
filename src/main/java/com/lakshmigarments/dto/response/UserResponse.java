package com.lakshmigarments.dto.response;

import lombok.Data;

@Data
public class UserResponse {
	
	private Long id;
	
	private String firstName;
	
	private String lastName;
	
	private String username;
	
	private String roleName;
	
	private Boolean isActive;
	
	private Long version;

}
