package com.lakshmigarments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
	
	@NotBlank(message = "First name is required")
	@Size(min = 3, message = "First name size should be 3 at minimum")
	private String firstName;

	private String lastName;
	
	@NotBlank(message = "Username is required")
	@Size(min = 3, message = "Username size should be 3 at minimum")
	private String username;
	
	@NotBlank(message = "Password is required")
	private String password;
	
	@NotBlank(message = "Role name is required")
	private String roleName;
	
	private Boolean isActive;

}
