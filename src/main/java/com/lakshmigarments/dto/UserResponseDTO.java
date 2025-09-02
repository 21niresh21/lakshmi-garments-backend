package com.lakshmigarments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
	
	private Long id;

	private String name;

	private Boolean isActive;

	private String roleName;

}
