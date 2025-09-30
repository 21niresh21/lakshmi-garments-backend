package com.lakshmigarments.dto;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeUpdateDTO {
	
	@Size(max = 200, message = "Name must be at most 200 characters")
    private String name;

    private List<Long> skills;

}
