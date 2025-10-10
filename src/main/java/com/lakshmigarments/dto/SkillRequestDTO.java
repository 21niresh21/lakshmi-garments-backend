package com.lakshmigarments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillRequestDTO {
	
    @NotNull(message = "Name must not be null")
	@NotBlank(message = "Name must not be blank")
    @Size(max = 200, message = "Name must be less than 200 characters")
    private String name;

}
