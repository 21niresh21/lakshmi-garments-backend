package com.lakshmigarments.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponseDTO {
	
	private Long id;
	
	private String name;
	
	private List<SkillResponseDTO> skills;

}
