package com.lakshmigarments.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeRequestDTO {
	
	@NotNull(message = "Name cannot be null")
	@NotBlank(message = "Name cannot be blank")
	@Size(max = 200, message = "Name must be at most 200 characters")
	private String name;
	
	private List<Long> skills;

}
