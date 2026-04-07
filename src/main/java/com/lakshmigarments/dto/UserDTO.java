package com.lakshmigarments.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
	
//	@NotNull(message = "User name cannot be null")
	private String name;
	
//	@NotNull(message = "Password cannot be null")
	private String password;
	
	private Boolean isActive;
	
//	@NotNull(message = "Role name cannot be null")
	private String roleName;

}
